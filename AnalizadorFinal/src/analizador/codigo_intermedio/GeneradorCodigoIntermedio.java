package analizador.codigo_intermedio;

import analizador.arbol.NodoExpresion;

import java.util.ArrayList;
import java.util.List;

/**
 * GeneradorCodigoIntermedio
 * ------------------------------------------------------------
 * Recorre el arbol de expresion (AST) del programa 8086 y genera
 * codigo intermedio de tres direcciones en forma de cuadruplos.
 *
 * Ejemplos:
 *   MOV AX, 10      ->  (=, 10, _, AX)         AX = 10
 *   ADD AX, BX      ->  (+, AX, BX, AX)        AX = AX + BX
 *   CMP AX, 5       ->  (cmp, AX, 5, _)        cmp AX, 5
 *   JE  FIN         ->  (if==, AX, 5, FIN)     if AX == 5 goto FIN
 *   JMP INICIO      ->  (goto, _, _, INICIO)   goto INICIO
 *
 * Simplificaciones: MUL/DIV usan AX (y DX para el residuo) y los
 * saltos condicionales se expresan con los operandos del ultimo
 * CMP/TEST o con el resultado de la ultima operacion aritmetica.
 * ------------------------------------------------------------
 */
public class GeneradorCodigoIntermedio {

    private List<Cuadruplo> cuadruplos;
    private int contadorTemporales;

    // operandos con los que se evaluan los saltos condicionales
    private String cmpArg1;
    private String cmpArg2;

    private GeneradorCodigoIntermedio() {
        cuadruplos = new ArrayList<Cuadruplo>();
        contadorTemporales = 0;
        cmpArg1 = null;
        cmpArg2 = null;
    }

    // ------------------------------------------------------------
    // PUNTO DE ENTRADA
    // ------------------------------------------------------------

    public static List<Cuadruplo> generar(NodoExpresion raiz) {
        GeneradorCodigoIntermedio gen = new GeneradorCodigoIntermedio();
        if (raiz != null) {
            for (NodoExpresion hijo : raiz.getHijos()) {
                gen.procesarNodo(hijo);
            }
        }
        for (int i = 0; i < gen.cuadruplos.size(); i++) {
            gen.cuadruplos.get(i).setNumero(i + 1);
        }
        return gen.cuadruplos;
    }

    /** Todo el codigo intermedio como texto de tres direcciones. */
    public static String aTexto(List<Cuadruplo> lista) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            Cuadruplo c = lista.get(i);
            if (c.getOperador().equals("label")) {
                sb.append(c.getResultado()).append(":\n");
            } else {
                sb.append("    ").append(c.getCodigo()).append("\n");
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------
    // RECORRIDO
    // ------------------------------------------------------------

    private void procesarNodo(NodoExpresion nodo) {
        String tipo = nodo.getTipo();
        int linea = nodo.getNumLinea();

        if (tipo.equals("ETIQUETA")) {
            emitir(linea, "label", null, null, nodo.getValor(), nodo.getValor() + ":");
            for (NodoExpresion h : nodo.getHijos()) {
                procesarNodo(h);
            }
        } else if (tipo.equals("INSTRUCCION")) {
            procesarInstruccion(nodo);
        } else if (tipo.equals("DECLARACION")) {
            procesarDeclaracion(nodo);
        } else if (tipo.equals("DIRECTIVA")) {
            if (nodo.getValor().equalsIgnoreCase("END")) {
                emitir(linea, "end", null, null, null, "end");
            }
        }
    }

    // ------------------------------------------------------------
    // DECLARACIONES: VAR DW 10 / P1 PROC / P1 ENDP
    // ------------------------------------------------------------

    private void procesarDeclaracion(NodoExpresion nodo) {
        String dir = nodo.getValor().toUpperCase();
        int linea = nodo.getNumLinea();
        List<NodoExpresion> hijos = nodo.getHijos();
        if (hijos.isEmpty()) {
            return;
        }
        String nombre = hijos.get(0).getValor();

        if (dir.equals("PROC")) {
            emitir(linea, "proc", null, null, nombre, "proc " + nombre);
        } else if (dir.equals("ENDP")) {
            emitir(linea, "endp", null, null, nombre, "endp " + nombre);
        } else if (dir.equals("DB") || dir.equals("DW") || dir.equals("DD")
                || dir.equals("DQ") || dir.equals("EQU")) {
            StringBuilder valores = new StringBuilder();
            for (int i = 1; i < hijos.size(); i++) {
                if (i > 1) {
                    valores.append(", ");
                }
                valores.append(expr(hijos.get(i)));
            }
            String v = valores.length() == 0 ? "?" : valores.toString();
            emitir(linea, dir.toLowerCase(), v, null, nombre,
                    nombre + " " + dir.toLowerCase() + " " + v);
        }
        // SEGMENT / ENDS no generan codigo intermedio
    }

    // ------------------------------------------------------------
    // INSTRUCCIONES
    // ------------------------------------------------------------

    private void procesarInstruccion(NodoExpresion nodo) {
        String m = nodo.getValor().toUpperCase();
        int linea = nodo.getNumLinea();
        List<NodoExpresion> ops = nodo.getHijos();
        String a = ops.size() > 0 ? expr(ops.get(0)) : null;
        String b = ops.size() > 1 ? expr(ops.get(1)) : null;

        if (m.equals("MOV")) {
            emitir(linea, "=", b, null, a, a + " = " + b);
        } else if (m.equals("LEA")) {
            emitir(linea, "lea", b, null, a, a + " = &" + b);
        } else if (m.equals("ADD")) {
            binaria(linea, "+", a, b);
        } else if (m.equals("SUB")) {
            binaria(linea, "-", a, b);
        } else if (m.equals("AND")) {
            binaria(linea, "&", a, b);
        } else if (m.equals("OR")) {
            binaria(linea, "|", a, b);
        } else if (m.equals("XOR")) {
            binaria(linea, "^", a, b);
        } else if (m.equals("SHL")) {
            binaria(linea, "<<", a, b);
        } else if (m.equals("SHR")) {
            binaria(linea, ">>", a, b);
        } else if (m.equals("ROL")) {
            binaria(linea, "rol", a, b);
        } else if (m.equals("ROR")) {
            binaria(linea, "ror", a, b);
        } else if (m.equals("INC")) {
            binaria(linea, "+", a, "1");
        } else if (m.equals("DEC")) {
            binaria(linea, "-", a, "1");
        } else if (m.equals("NOT")) {
            emitir(linea, "~", a, null, a, a + " = ~" + a);
        } else if (m.equals("NEG")) {
            emitir(linea, "neg", a, null, a, a + " = -" + a);
            establecerBanderas(a);
        } else if (m.equals("MUL") || m.equals("IMUL")) {
            emitir(linea, "*", "AX", a, "AX", "AX = AX * " + a);
        } else if (m.equals("DIV") || m.equals("IDIV")) {
            emitir(linea, "%", "AX", a, "DX", "DX = AX % " + a);
            emitir(linea, "/", "AX", a, "AX", "AX = AX / " + a);
        } else if (m.equals("CMP")) {
            cmpArg1 = a;
            cmpArg2 = b;
            emitir(linea, "cmp", a, b, null, "cmp " + a + ", " + b);
        } else if (m.equals("TEST")) {
            String t = nuevoTemporal();
            emitir(linea, "&", a, b, t, t + " = " + a + " & " + b);
            cmpArg1 = t;
            cmpArg2 = "0";
        } else if (m.equals("JMP")) {
            emitir(linea, "goto", null, null, a, "goto " + a);
        } else if (esSaltoCondicional(m)) {
            saltoCondicional(linea, m, a);
        } else if (m.equals("LOOP")) {
            emitir(linea, "-", "CX", "1", "CX", "CX = CX - 1");
            emitir(linea, "if!=", "CX", "0", a, "if CX != 0 goto " + a);
        } else if (m.equals("CALL")) {
            emitir(linea, "call", a, null, null, "call " + a);
        } else if (m.equals("RET")) {
            emitir(linea, "ret", null, null, null, "ret");
        } else if (m.equals("PUSH")) {
            emitir(linea, "push", a, null, null, "push " + a);
        } else if (m.equals("POP")) {
            emitir(linea, "pop", null, null, a, "pop " + a);
        } else if (m.equals("XCHG")) {
            String t = nuevoTemporal();
            emitir(linea, "=", a, null, t, t + " = " + a);
            emitir(linea, "=", b, null, a, a + " = " + b);
            emitir(linea, "=", t, null, b, b + " = " + t);
        } else if (m.equals("INT")) {
            emitir(linea, "int", a, null, null, "int " + a);
        } else if (m.equals("IN")) {
            emitir(linea, "in", b, null, a, a + " = in(" + b + ")");
        } else if (m.equals("OUT")) {
            emitir(linea, "out", b, null, a, "out(" + a + ") = " + b);
        } else if (m.equals("HLT")) {
            emitir(linea, "halt", null, null, null, "halt");
        } else if (m.equals("NOP")) {
            emitir(linea, "nop", null, null, null, "nop");
        } else if (m.equals("STC") || m.equals("CLC")
                || m.equals("STI") || m.equals("CLI")) {
            emitir(linea, m.toLowerCase(), null, null, null, m.toLowerCase());
        }
    }

    // ------------------------------------------------------------
    // AUXILIARES
    // ------------------------------------------------------------

    /** d = d OP s */
    private void binaria(int linea, String op, String d, String s) {
        emitir(linea, op, d, s, d, d + " = " + d + " " + op + " " + s);
        establecerBanderas(d);
    }

    /** Despues de una operacion aritmetica/logica, los saltos comparan el resultado con 0. */
    private void establecerBanderas(String destino) {
        cmpArg1 = destino;
        cmpArg2 = "0";
    }

    private boolean esSaltoCondicional(String m) {
        return m.equals("JZ") || m.equals("JE") || m.equals("JNZ")
                || m.equals("JNE") || m.equals("JG") || m.equals("JL")
                || m.equals("JGE") || m.equals("JLE") || m.equals("JA")
                || m.equals("JB");
    }

    private void saltoCondicional(int linea, String m, String etiqueta) {
        String rel;
        if (m.equals("JZ") || m.equals("JE")) {
            rel = "==";
        } else if (m.equals("JNZ") || m.equals("JNE")) {
            rel = "!=";
        } else if (m.equals("JG") || m.equals("JA")) {
            rel = ">";
        } else if (m.equals("JL") || m.equals("JB")) {
            rel = "<";
        } else if (m.equals("JGE")) {
            rel = ">=";
        } else {
            rel = "<=";
        }

        if (cmpArg1 == null) {
            // sin comparacion previa conocida: se usa el registro de banderas
            emitir(linea, "if" + rel, "FLAGS", "0", etiqueta,
                    "if FLAGS " + rel + " 0 goto " + etiqueta);
        } else {
            emitir(linea, "if" + rel, cmpArg1, cmpArg2, etiqueta,
                    "if " + cmpArg1 + " " + rel + " " + cmpArg2
                            + " goto " + etiqueta);
        }
    }

    private String nuevoTemporal() {
        contadorTemporales++;
        return "T" + contadorTemporales;
    }

    private void emitir(int linea, String op, String a1, String a2,
                        String res, String codigo) {
        cuadruplos.add(new Cuadruplo(linea, op, a1, a2, res, codigo));
    }

    /** Convierte un operando del arbol a texto. */
    private String expr(NodoExpresion n) {
        if (n == null) {
            return "";
        }
        if (n.getTipo().equals("MEMORIA")) {
            StringBuilder sb = new StringBuilder("[");
            for (NodoExpresion h : n.getHijos()) {
                sb.append(expr(h));
            }
            return sb.append("]").toString();
        }
        if (n.getTipo().equals("OPERADOR") && n.getHijos().size() == 2) {
            return expr(n.getHijos().get(0)) + n.getValor()
                    + expr(n.getHijos().get(1));
        }
        return n.getValor();
    }
}
