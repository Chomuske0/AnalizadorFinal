package analizador.arbol;

import analizador.modelo.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * ArbolExpresion
 * ------------------------------------------------------------
 * Construye el arbol de expresion completo del programa 8086.
 * Trabaja sobre la lista de tokens generada por AnalizadorLexico.
 *
 * Estructura del arbol:
 *   PROGRAMA
 *   ├── Linea N: INSTRUCCION / DECLARACION / DIRECTIVA / ETIQUETA
 *   │   ├── operando 1
 *   │   ├── operando 2
 *   │   └── ...
 *   └── ...
 * ------------------------------------------------------------
 */
public class ArbolExpresion {

    private ArbolExpresion() {
    }

    // ------------------------------------------------------------
    // CONSTRUCCION DEL ARBOL COMPLETO
    // ------------------------------------------------------------

    public static NodoExpresion construir(List<Token> listaTokens) {
        NodoExpresion raiz = new NodoExpresion("PROGRAMA", "PROGRAMA");

        List<List<Token>> lineas = agruparPorLinea(listaTokens);

        for (int i = 0; i < lineas.size(); i++) {
            NodoExpresion nodoLinea = construirLinea(lineas.get(i));
            if (nodoLinea != null) {
                raiz.agregarHijo(nodoLinea);
            }
        }

        return raiz;
    }

    // ------------------------------------------------------------
    // CONSTRUCCION DE CADA LINEA
    // ------------------------------------------------------------

    private static NodoExpresion construirLinea(List<Token> linea) {
        if (linea.isEmpty()) {
            return null;
        }

        Token primero = linea.get(0);
        int numLinea = primero.getLinea();

        switch (primero.getToken()) {

            case "MNEMONICO_8086":
                return construirInstruccion(linea, numLinea);

            case "IDENTIFICADOR":
                if (linea.size() > 1 && linea.get(1).getToken().equals("SEPARADOR_DOSPUNTOS")) {
                    return construirEtiqueta(linea, numLinea);
                } else if (linea.size() > 1 && linea.get(1).getToken().equals("DIRECTIVA")) {
                    return construirDeclaracion(linea, numLinea);
                }
                return null;

            case "DIRECTIVA":
                return construirDirectivaGeneral(linea, numLinea);

            default:
                return null;
        }
    }

    // ------------------------------------------------------------
    // INSTRUCCION: MNEMONICO operando1, operando2
    // ------------------------------------------------------------

    private static NodoExpresion construirInstruccion(List<Token> linea, int numLinea) {
        Token mnemonico = linea.get(0);
        NodoExpresion nodo = new NodoExpresion("INSTRUCCION", mnemonico.getLexema(), numLinea);

        List<Token> resto = new ArrayList<Token>(linea.subList(1, linea.size()));
        List<List<Token>> operandos = agruparPorComas(resto);

        for (int i = 0; i < operandos.size(); i++) {
            NodoExpresion nodoOp = construirOperando(operandos.get(i));
            if (nodoOp != null) {
                nodo.agregarHijo(nodoOp);
            }
        }

        return nodo;
    }

    // ------------------------------------------------------------
    // DECLARACION: NOMBRE DIRECTIVA valor(es)
    // Ej: VAR1 DW 10  /  DATOS SEGMENT  /  P1 PROC
    // ------------------------------------------------------------

    private static NodoExpresion construirDeclaracion(List<Token> linea, int numLinea) {
        Token nombre = linea.get(0);
        Token directiva = linea.get(1);

        NodoExpresion nodo = new NodoExpresion("DECLARACION", directiva.getLexema(), numLinea);
        nodo.agregarHijo(new NodoExpresion("IDENTIFICADOR", nombre.getLexema()));

        List<Token> resto = new ArrayList<Token>(linea.subList(2, linea.size()));
        List<List<Token>> valores = agruparPorComas(resto);

        for (int i = 0; i < valores.size(); i++) {
            NodoExpresion nodoVal = construirOperando(valores.get(i));
            if (nodoVal != null) {
                nodo.agregarHijo(nodoVal);
            }
        }

        return nodo;
    }

    // ------------------------------------------------------------
    // DIRECTIVA GENERAL: END, ORG, ASSUME
    // ------------------------------------------------------------

    private static NodoExpresion construirDirectivaGeneral(List<Token> linea, int numLinea) {
        Token directiva = linea.get(0);
        NodoExpresion nodo = new NodoExpresion("DIRECTIVA", directiva.getLexema(), numLinea);

        List<Token> resto = new ArrayList<Token>(linea.subList(1, linea.size()));
        for (int i = 0; i < resto.size(); i++) {
            Token t = resto.get(i);
            if (!t.getToken().equals("SEPARADOR_COMA")) {
                nodo.agregarHijo(new NodoExpresion(mapearTipo(t.getToken()), t.getLexema()));
            }
        }

        return nodo;
    }

    // ------------------------------------------------------------
    // ETIQUETA: NOMBRE ':'
    // ------------------------------------------------------------

    private static NodoExpresion construirEtiqueta(List<Token> linea, int numLinea) {
        Token nombre = linea.get(0);
        NodoExpresion nodo = new NodoExpresion("ETIQUETA", nombre.getLexema(), numLinea);

        // si hay instruccion despues de la etiqueta en la misma linea
        if (linea.size() > 2) {
            List<Token> resto = new ArrayList<Token>(linea.subList(2, linea.size()));
            NodoExpresion nodoResto = construirLinea(resto);
            if (nodoResto != null) {
                nodo.agregarHijo(nodoResto);
            }
        }

        return nodo;
    }

    // ------------------------------------------------------------
    // OPERANDOS
    // ------------------------------------------------------------

    private static NodoExpresion construirOperando(List<Token> operando) {
        if (operando.isEmpty()) {
            return null;
        }

        // Referencia de memoria: [ BX + 10 ]
        if (operando.get(0).getToken().equals("CORCHETE_APERTURA")) {
            NodoExpresion nodoMem = new NodoExpresion("MEMORIA", "[...]");
            // contenido dentro de los corchetes (sin [ y ])
            if (operando.size() > 2) {
                List<Token> dentro = new ArrayList<Token>(
                        operando.subList(1, operando.size() - 1));
                NodoExpresion expr = construirExpresionAritmetica(dentro);
                if (expr != null) {
                    nodoMem.agregarHijo(expr);
                }
            }
            return nodoMem;
        }

        // Token simple
        if (operando.size() == 1) {
            Token t = operando.get(0);
            return new NodoExpresion(mapearTipo(t.getToken()), t.getLexema());
        }

        // Expresion aritmetica compuesta
        return construirExpresionAritmetica(operando);
    }

    private static NodoExpresion construirExpresionAritmetica(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }
        if (tokens.size() == 1) {
            Token t = tokens.get(0);
            return new NodoExpresion(mapearTipo(t.getToken()), t.getLexema());
        }

        // Buscar operador + o -
        for (int i = 0; i < tokens.size(); i++) {
            String tipo = tokens.get(i).getToken();
            if (tipo.equals("OPERADOR_SUMA") || tipo.equals("OPERADOR_RESTA")) {
                NodoExpresion nodoOp = new NodoExpresion(
                        "OPERADOR", tokens.get(i).getLexema());
                List<Token> izq = new ArrayList<Token>(tokens.subList(0, i));
                List<Token> der = new ArrayList<Token>(tokens.subList(i + 1, tokens.size()));
                NodoExpresion nodoIzq = construirExpresionAritmetica(izq);
                NodoExpresion nodoDer = construirExpresionAritmetica(der);
                if (nodoIzq != null) nodoOp.agregarHijo(nodoIzq);
                if (nodoDer != null) nodoOp.agregarHijo(nodoDer);
                return nodoOp;
            }
        }

        // Sin operador, retorna el primero
        Token t = tokens.get(0);
        return new NodoExpresion(mapearTipo(t.getToken()), t.getLexema());
    }

    // ------------------------------------------------------------
    // CONVERSION A TEXTO CON CARACTERES DE ARBOL
    // ------------------------------------------------------------

    public static String arbolATexto(NodoExpresion raiz) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRAMA\n");

        List<NodoExpresion> hijos = raiz.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            boolean esUltimo = (i == hijos.size() - 1);
            NodoExpresion hijo = hijos.get(i);
            String prefijo = esUltimo ? "└── " : "├── ";
            String prefijoHijo = esUltimo ? "    " : "│   ";

            sb.append(prefijo)
              .append("[Linea ").append(hijo.getNumLinea()).append("] ")
              .append(hijo.getValor())
              .append(" (").append(hijo.getTipo()).append(")\n");

            List<NodoExpresion> subHijos = hijo.getHijos();
            for (int j = 0; j < subHijos.size(); j++) {
                boolean esUltimoSub = (j == subHijos.size() - 1);
                imprimirNodo(sb, subHijos.get(j), prefijoHijo, esUltimoSub);
            }
        }

        return sb.toString();
    }

    private static void imprimirNodo(StringBuilder sb, NodoExpresion nodo,
                                     String prefijo, boolean esUltimo) {
        String conector = esUltimo ? "└── " : "├── ";
        String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");

        sb.append(prefijo).append(conector)
          .append(nodo.getValor())
          .append(" (").append(nodo.getTipo()).append(")\n");

        List<NodoExpresion> hijos = nodo.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            boolean esUltimoHijo = (i == hijos.size() - 1);
            imprimirNodo(sb, hijos.get(i), nuevoPrefijo, esUltimoHijo);
        }
    }

    // ------------------------------------------------------------
    // UTILIDADES
    // ------------------------------------------------------------

    private static List<List<Token>> agruparPorLinea(List<Token> tokens) {
        List<List<Token>> lineas = new ArrayList<List<Token>>();
        List<Token> actual = new ArrayList<Token>();
        int lineaActual = -1;

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String tipo = t.getToken();

            // ignorar comentarios y tokens de error
            if (tipo.equals("COMENTARIO") || tipo.equals("DESCONOCIDO")
                    || tipo.equals("CADENA_NO_CERRADA")) {
                continue;
            }

            if (lineaActual == -1) {
                lineaActual = t.getLinea();
            }

            if (t.getLinea() != lineaActual) {
                if (!actual.isEmpty()) {
                    lineas.add(actual);
                }
                actual = new ArrayList<Token>();
                lineaActual = t.getLinea();
            }
            actual.add(t);
        }

        if (!actual.isEmpty()) {
            lineas.add(actual);
        }

        return lineas;
    }

    private static List<List<Token>> agruparPorComas(List<Token> lista) {
        List<List<Token>> grupos = new ArrayList<List<Token>>();
        List<Token> actual = new ArrayList<Token>();

        for (int i = 0; i < lista.size(); i++) {
            Token t = lista.get(i);
            if (t.getToken().equals("SEPARADOR_COMA")) {
                if (!actual.isEmpty()) {
                    grupos.add(actual);
                }
                actual = new ArrayList<Token>();
            } else {
                actual.add(t);
            }
        }
        if (!actual.isEmpty()) {
            grupos.add(actual);
        }

        return grupos;
    }

    private static String mapearTipo(String tipoToken) {
        switch (tipoToken) {
            case "MNEMONICO_8086":         return "INSTRUCCION";
            case "NUMERO_DECIMAL":         return "NUMERO DEC";
            case "NUMERO_HEXADECIMAL":     return "NUMERO HEX";
            case "NUMERO_BINARIO":         return "NUMERO BIN";
            case "OPERADOR_SUMA":          return "OPERADOR";
            case "OPERADOR_RESTA":         return "OPERADOR";
            case "OPERADOR_MULTIPLICACION":return "OPERADOR";
            case "SEPARADOR_DOSPUNTOS":    return "DOS PUNTOS";
            case "SEPARADOR_COMA":         return "COMA";
            case "SIMBOLO_INTERROGACION":  return "VALOR INDEFINIDO";
            default:                       return tipoToken;
        }
    }
}
