package analizador.analisis;

import analizador.modelo.ErrorSintactico;
import analizador.modelo.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * AnalizadorSintactico
 * ------------------------------------------------------------
 * Analizador sintactico para codigo ensamblador del procesador 8086.
 * Trabaja SOBRE LA SALIDA del analizador lexico (lista de Token),
 * no sobre la cadena de caracteres.
 *
 * Reglas de diseno:
 *  - Estructura ESTATICA (igual que AnalizadorLexico).
 *  - El control de flujo para reconocer cada tipo de instruccion o
 *    directiva se hace EXCLUSIVAMENTE con switch-case.
 *  - No se usan expresiones regulares ni split().
 *
 * Gramatica soportada (resumen):
 *
 *  linea        -> etiqueta? (instruccion | declaracion | directiva)
 *  etiqueta     -> IDENTIFICADOR ':'
 *  declaracion  -> IDENTIFICADOR DIRECTIVA operandos?      (VAR DB 5  / DATOS SEGMENT / P1 PROC)
 *  instruccion  -> MNEMONICO_8086 operandos?
 *  directiva    -> DIRECTIVA operandos?                    (END, ORG, ASSUME, ...)
 *  operandos    -> operando (',' operando)*
 *  operando     -> REGISTRO | NUMERO | IDENTIFICADOR | CADENA | '?'
 *                | '[' (REGISTRO|NUMERO|IDENTIFICADOR|'+'|'-')+ ']'
 * ------------------------------------------------------------
 */
public class AnalizadorSintactico {

    private static List<ErrorSintactico> errores;

    private AnalizadorSintactico() {
    }

    /**
     * Analiza la lista de tokens generada por AnalizadorLexico.
     * Devuelve true si el programa es sintacticamente correcto,
     * false si se encontraron errores (consultar getErrores()).
     */
    public static boolean analizar(List<Token> listaTokens) {
        errores = new ArrayList<ErrorSintactico>();

        List<Token> grupoActual = new ArrayList<Token>();
        int lineaGrupo = -1;

        for (int i = 0; i < listaTokens.size(); i++) {
            Token t = listaTokens.get(i);
            String tipo = t.getToken();

            switch (tipo) {
                case "COMENTARIO":
                    // los comentarios no forman parte de la sintaxis, se ignoran
                    continue;
                case "CADENA_NO_CERRADA":
                    agregarError(t.getLinea(), "Cadena de texto sin cerrar: " + t.getLexema());
                    continue;
                case "DESCONOCIDO":
                    agregarError(t.getLinea(), "Simbolo o caracter no reconocido: '" + t.getLexema() + "'");
                    continue;
                default:
                    break;
            }

            if (lineaGrupo == -1) {
                lineaGrupo = t.getLinea();
            }
            if (t.getLinea() != lineaGrupo) {
                procesarLinea(grupoActual, lineaGrupo);
                grupoActual = new ArrayList<Token>();
                lineaGrupo = t.getLinea();
            }
            grupoActual.add(t);
        }

        if (!grupoActual.isEmpty()) {
            procesarLinea(grupoActual, lineaGrupo);
        }

        return errores.isEmpty();
    }

    // ------------------------------------------------------------
    // ANALISIS DE UNA LINEA LOGICA
    // ------------------------------------------------------------

    private static void procesarLinea(List<Token> lineaTokens, int numeroLinea) {
        if (lineaTokens.isEmpty()) {
            return;
        }

        Token primero = lineaTokens.get(0);

        switch (primero.getToken()) {

            case "IDENTIFICADOR":
                if (lineaTokens.size() > 1 && lineaTokens.get(1).getToken().equals("SEPARADOR_DOSPUNTOS")) {
                    // etiqueta:  IDENTIFICADOR ':' resto_opcional
                    List<Token> resto = new ArrayList<Token>(lineaTokens.subList(2, lineaTokens.size()));
                    if (!resto.isEmpty()) {
                        procesarLinea(resto, numeroLinea);
                    }
                } else if (lineaTokens.size() > 1 && lineaTokens.get(1).getToken().equals("DIRECTIVA")) {
                    // declaracion:  NOMBRE  DIRECTIVA  operandos?
                    parsearDeclaracion(lineaTokens, numeroLinea);
                } else {
                    agregarError(numeroLinea, "Se esperaba ':' o una directiva despues de '"
                            + primero.getLexema() + "'");
                }
                break;

            case "MNEMONICO_8086":
                parsearInstruccion(lineaTokens, numeroLinea);
                break;

            case "DIRECTIVA":
                parsearDirectivaGeneral(lineaTokens, numeroLinea);
                break;

            default:
                agregarError(numeroLinea, "La linea no puede iniciar con '" + primero.getLexema()
                        + "' (" + primero.getToken() + ")");
                break;
        }
    }

    // ------------------------------------------------------------
    // INSTRUCCIONES:  MNEMONICO operando1 , operando2
    // ------------------------------------------------------------

    private static void parsearInstruccion(List<Token> lineaTokens, int numeroLinea) {
        Token mnemonico = lineaTokens.get(0);
        String nombre = mnemonico.getLexema().toUpperCase();
        List<Token> resto = new ArrayList<Token>(lineaTokens.subList(1, lineaTokens.size()));

        List<List<Token>> operandos = separarPorComas(resto, numeroLinea);
        int cantidad = operandos.size();

        int categoria = obtenerCategoriaMnemonico(nombre);

        switch (categoria) {
            case 0: // no admite operandos
                if (cantidad != 0) {
                    agregarError(numeroLinea, "La instruccion '" + nombre
                            + "' no admite operandos, se encontraron " + cantidad);
                }
                break;
            case 1: // exactamente un operando
                if (cantidad != 1) {
                    agregarError(numeroLinea, "La instruccion '" + nombre
                            + "' requiere exactamente 1 operando, se encontraron " + cantidad);
                }
                break;
            case 2: // exactamente dos operandos
                if (cantidad != 2) {
                    agregarError(numeroLinea, "La instruccion '" + nombre
                            + "' requiere exactamente 2 operandos, se encontraron " + cantidad);
                }
                break;
            case 3: // uno o dos operandos (IN / OUT)
                if (cantidad < 1 || cantidad > 2) {
                    agregarError(numeroLinea, "La instruccion '" + nombre
                            + "' requiere 1 o 2 operandos, se encontraron " + cantidad);
                }
                break;
            default:
                agregarError(numeroLinea, "Mnemonico no reconocido por el analizador sintactico: '"
                        + nombre + "'");
                break;
        }
    }

    private static int obtenerCategoriaMnemonico(String nombre) {
        switch (nombre) {
            case "NOP": case "RET": case "HLT": case "CLC": case "STC":
            case "STI": case "CLI":
                return 0;

            case "PUSH": case "POP": case "INC": case "DEC": case "INT":
            case "CALL": case "JMP": case "JZ": case "JNZ": case "JE":
            case "JNE": case "JG": case "JL": case "JGE": case "JLE":
            case "JA": case "JB": case "LOOP": case "NOT": case "NEG":
            case "MUL": case "DIV": case "IMUL": case "IDIV":
                return 1;

            case "MOV": case "ADD": case "SUB": case "CMP": case "AND":
            case "OR": case "XOR": case "TEST": case "LEA": case "XCHG":
            case "SHL": case "SHR": case "ROL": case "ROR":
                return 2;

            case "IN": case "OUT":
                return 3;

            default:
                return -1;
        }
    }

    // ------------------------------------------------------------
    // DECLARACIONES CON NOMBRE:  NOMBRE  DIRECTIVA  operandos?
    // (segmentos, procedimientos, variables, constantes)
    // ------------------------------------------------------------

    private static void parsearDeclaracion(List<Token> lineaTokens, int numeroLinea) {
        Token directiva = lineaTokens.get(1);
        String dir = directiva.getLexema().toUpperCase();
        List<Token> resto = new ArrayList<Token>(lineaTokens.subList(2, lineaTokens.size()));

        switch (dir) {
            case "SEGMENT":
            case "ENDS":
            case "PROC":
            case "ENDP":
                // pueden llevar como maximo una palabra adicional (ej: P1 PROC FAR)
                if (resto.size() > 1) {
                    agregarError(numeroLinea, "Elementos sobrantes despues de '" + dir + "'");
                }
                break;

            case "EQU":
                if (resto.size() != 1) {
                    agregarError(numeroLinea, "'EQU' requiere exactamente un valor asignado");
                }
                break;

            case "DB": case "DW": case "DD": case "DQ":
                if (resto.isEmpty()) {
                    agregarError(numeroLinea, "'" + dir + "' requiere al menos un valor");
                } else {
                    separarPorComas(resto, numeroLinea);
                }
                break;

            default:
                // otras directivas admitidas de forma flexible (MODEL, PUBLIC, etc.)
                break;
        }
    }

    // ------------------------------------------------------------
    // DIRECTIVAS SIN NOMBRE PREVIO:  END, ORG, ASSUME, ...
    // ------------------------------------------------------------

    private static void parsearDirectivaGeneral(List<Token> lineaTokens, int numeroLinea) {
        Token directiva = lineaTokens.get(0);
        String dir = directiva.getLexema().toUpperCase();
        List<Token> resto = new ArrayList<Token>(lineaTokens.subList(1, lineaTokens.size()));

        switch (dir) {
            case "END":
                if (resto.size() > 1) {
                    agregarError(numeroLinea, "'END' admite unicamente una etiqueta de inicio opcional");
                } else if (resto.size() == 1 && !resto.get(0).getToken().equals("IDENTIFICADOR")) {
                    agregarError(numeroLinea, "'END' debe ir seguido de una etiqueta valida");
                }
                break;

            case "ORG":
                if (resto.size() != 1 || !esOperandoNumerico(resto.get(0))) {
                    agregarError(numeroLinea, "'ORG' requiere un unico valor numerico");
                }
                break;

            case "ASSUME":
                validarAssume(resto, numeroLinea);
                break;

            default:
                // MODEL, STACK, DATA, CODE, TITLE, INCLUDE, PUBLIC, EXTRN, PTR, OFFSET...
                break;
        }
    }

    private static void validarAssume(List<Token> resto, int numeroLinea) {
        if (resto.isEmpty()) {
            agregarError(numeroLinea, "'ASSUME' requiere al menos una asignacion de segmento");
            return;
        }
        List<List<Token>> grupos = agruparPorComas(resto);
        for (int i = 0; i < grupos.size(); i++) {
            List<Token> g = grupos.get(i);
            boolean formatoValido = g.size() == 3
                    && g.get(0).getToken().equals("REGISTRO")
                    && g.get(1).getToken().equals("SEPARADOR_DOSPUNTOS")
                    && g.get(2).getToken().equals("IDENTIFICADOR");
            if (!formatoValido) {
                agregarError(numeroLinea, "Formato invalido en 'ASSUME', se esperaba REGISTRO:NOMBRE");
            }
        }
    }

    // ------------------------------------------------------------
    // OPERANDOS
    // ------------------------------------------------------------

    private static List<List<Token>> separarPorComas(List<Token> lista, int numeroLinea) {
        List<List<Token>> validos = new ArrayList<List<Token>>();
        if (lista.isEmpty()) {
            return validos;
        }

        List<List<Token>> grupos = agruparPorComas(lista);
        for (int i = 0; i < grupos.size(); i++) {
            List<Token> g = grupos.get(i);
            if (g.isEmpty()) {
                agregarError(numeroLinea, "Operando vacio (coma de mas, o coma al inicio/final)");
            } else {
                validarOperando(g, numeroLinea);
                validos.add(g);
            }
        }
        return validos;
    }

    private static List<List<Token>> agruparPorComas(List<Token> lista) {
        List<List<Token>> grupos = new ArrayList<List<Token>>();
        List<Token> actual = new ArrayList<Token>();

        for (int i = 0; i < lista.size(); i++) {
            Token t = lista.get(i);
            if (t.getToken().equals("SEPARADOR_COMA")) {
                grupos.add(actual);
                actual = new ArrayList<Token>();
            } else {
                actual.add(t);
            }
        }
        grupos.add(actual);
        return grupos;
    }

    private static void validarOperando(List<Token> operando, int numeroLinea) {
        Token primero = operando.get(0);
        String tipo = primero.getToken();

        switch (tipo) {
            case "REGISTRO":
            case "NUMERO_DECIMAL":
            case "NUMERO_HEXADECIMAL":
            case "NUMERO_BINARIO":
            case "IDENTIFICADOR":
            case "CADENA":
            case "SIMBOLO_INTERROGACION":
                if (operando.size() > 1) {
                    agregarError(numeroLinea, "Operando mal formado cerca de '" + primero.getLexema() + "'");
                }
                break;

            case "CORCHETE_APERTURA":
                Token ultimo = operando.get(operando.size() - 1);
                if (!ultimo.getToken().equals("CORCHETE_CIERRE")) {
                    agregarError(numeroLinea, "Falta ']' para cerrar la referencia de memoria");
                } else {
                    for (int i = 1; i < operando.size() - 1; i++) {
                        String t = operando.get(i).getToken();
                        boolean valido = t.equals("REGISTRO") || t.equals("NUMERO_DECIMAL")
                                || t.equals("NUMERO_HEXADECIMAL") || t.equals("IDENTIFICADOR")
                                || t.equals("OPERADOR_SUMA") || t.equals("OPERADOR_RESTA");
                        if (!valido) {
                            agregarError(numeroLinea, "Contenido invalido dentro de '[ ]': '"
                                    + operando.get(i).getLexema() + "'");
                        }
                    }
                }
                break;

            case "OPERADOR_RESTA":
                // numero negativo, ej: -1
                boolean esNegativoValido = operando.size() == 2 && esOperandoNumerico(operando.get(1));
                if (!esNegativoValido) {
                    agregarError(numeroLinea, "Operando invalido cerca de '" + primero.getLexema() + "'");
                }
                break;

            default:
                agregarError(numeroLinea, "Tipo de operando no valido: '" + primero.getLexema() + "'");
                break;
        }
    }
    
    public static void main(String[] args) {
        String codigoFuente =
                "DATOS SEGMENT\n" +
                "  VAR1 DB 5\n" +
                "DATOS ENDS\n" +
                "\n" +
                "CODIGO SEGMENT\n" +
                "INICIO:\n" +
                "  MOV AX, DATOS\n" +
                "  MOV DS, AX\n" +
                "  MOV BX, VAR1\n" +
                "  ADD AX, BX\n" +
                "  HLT\n" +
                "CODIGO ENDS\n" +
                "END INICIO\n";
 
        List<Token> tokens = AnalizadorLexico.analizar(codigoFuente);
        boolean esValido = AnalizadorSintactico.analizar(tokens);
 
        if (esValido) {
            System.out.println("El codigo es sintacticamente VALIDO.");
        } else {
            System.out.println("Se encontraron errores:");
            for (ErrorSintactico error : AnalizadorSintactico.getErrores()) {
                System.out.println("  Linea " + error.getLinea() + ": " + error.getMensaje());
            }
        }
    }

    private static boolean esOperandoNumerico(Token t) {
        String tipo = t.getToken();
        return tipo.equals("NUMERO_DECIMAL") || tipo.equals("NUMERO_HEXADECIMAL")
                || tipo.equals("NUMERO_BINARIO");
    }

    private static void agregarError(int linea, String mensaje) {
        errores.add(new ErrorSintactico(linea, mensaje));
    }

    // ---------- GETTERS Y SETTERS ----------

    public static List<ErrorSintactico> getErrores() {
        return errores;
    }

    public static void setErrores(List<ErrorSintactico> nuevaLista) {
        errores = nuevaLista;
    }
}
