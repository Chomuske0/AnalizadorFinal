package analizador.analisis;

import analizador.modelo.Token;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorLexico {

    // ---------- ESTADOS DEL AUTOMATA
    private static final int ESTADO_INICIO = 0;
    private static final int ESTADO_IDENTIFICADOR = 1;
    private static final int ESTADO_NUMERO = 2;
    private static final int ESTADO_COMENTARIO = 3;
    private static final int ESTADO_CADENA = 4;

    // ---------- ESTRUCTURA ESTATICA 
    private static List<Token> listaTokens;
    private static char[] entrada;
    private static int posicion;
    private static int longitud;
    private static int lineaActual;

    // ---------- PALABRAS RESERVADAS DEL 8086 
    private static final String[] REGISTROS = {
        "AX", "BX", "CX", "DX", "AH", "AL", "BH", "BL", "CH", "CL",
        "DH", "DL", "SI", "DI", "BP", "SP", "CS", "DS", "ES", "SS", "IP"
    };

    private static final String[] MNEMONICOS = {
        "MOV", "ADD", "SUB", "MUL", "DIV", "IMUL", "IDIV", "JMP", "JZ",
        "JNZ", "JE", "JNE", "JG", "JL", "JGE", "JLE", "JA", "JB", "CALL",
        "RET", "PUSH", "POP", "INC", "DEC", "CMP", "AND", "OR", "XOR",
        "NOT", "NOP", "INT", "LEA", "LOOP", "IN", "OUT", "SHL", "SHR",
        "NEG", "ROL", "ROR", "STC", "CLC", "STI", "CLI", "HLT", "XCHG",
        "TEST"
    };

    private static final String[] DIRECTIVAS = {
        "ORG", "END", "DB", "DW", "DD", "DQ", "EQU", "SEGMENT", "ENDS",
        "PROC", "ENDP", "ASSUME", "MODEL", "STACK", "DATA", "CODE",
        "PUBLIC", "EXTRN", "INCLUDE", "TITLE", "PTR", "OFFSET"
    };

    private AnalizadorLexico() {
    }

    public static List<Token> analizar(String cadena) {
        // Inicializacion de la estructura estatica
        listaTokens = new ArrayList<Token>();
        entrada = cadena.toCharArray();
        longitud = entrada.length;
        posicion = 0;
        lineaActual = 1;

        int estado = ESTADO_INICIO;
        StringBuilder buffer = new StringBuilder();
        char comillaActual = ' ';

        while (posicion < longitud) {
            char c = entrada[posicion];

            switch (estado) {

                case ESTADO_INICIO:
                    if (c == '\n') {
                        lineaActual++;
                        posicion++;
                    } else if (c == ' ' || c == '\t' || c == '\r') {
                        posicion++;
                    } else if (esLetra(c) || c == '_') {
                        buffer.setLength(0);
                        buffer.append(c);
                        posicion++;
                        estado = ESTADO_IDENTIFICADOR;
                    } else if (esDigito(c)) {
                        buffer.setLength(0);
                        buffer.append(c);
                        posicion++;
                        estado = ESTADO_NUMERO;
                    } else if (c == ';') {
                        buffer.setLength(0);
                        buffer.append(c);
                        posicion++;
                        estado = ESTADO_COMENTARIO;
                    } else if (c == '"' || c == '\'') {
                        comillaActual = c;
                        buffer.setLength(0);
                        buffer.append(c);
                        posicion++;
                        estado = ESTADO_CADENA;
                    } else {

                        switch (c) {
                            case '+':
                                agregarToken("OPERADOR_SUMA", "+");
                                break;
                            case '-':
                                agregarToken("OPERADOR_RESTA", "-");
                                break;
                            case '*':
                                agregarToken("OPERADOR_MULTIPLICACION", "*");
                                break;
                            case ',':
                                agregarToken("SEPARADOR_COMA", ",");
                                break;
                            case ':':
                                agregarToken("SEPARADOR_DOSPUNTOS", ":");
                                break;
                            case '[':
                                agregarToken("CORCHETE_APERTURA", "[");
                                break;
                            case ']':
                                agregarToken("CORCHETE_CIERRE", "]");
                                break;
                            case '=':
                                agregarToken("OPERADOR_IGUAL", "=");
                                break;
                            case '?':
                                agregarToken("SIMBOLO_INTERROGACION", "?");
                                break;
                            default:
                                agregarToken("DESCONOCIDO", String.valueOf(c));
                                break;
                        }
                        posicion++;
                    }
                    break;

                // ----------------------------------------------------
                case ESTADO_IDENTIFICADOR:
                    if (esLetra(c) || esDigito(c) || c == '_') {
                        buffer.append(c);
                        posicion++;
                    } else {
                        clasificarIdentificador(buffer.toString());
                        estado = ESTADO_INICIO;
                    }
                    break;

                // ----------------------------------------------------
                case ESTADO_NUMERO:
                    if (esDigito(c) || esLetraHex(c) || c == 'h' || c == 'H') {
                        buffer.append(c);
                        posicion++;
                    } else {
                        clasificarNumero(buffer.toString());
                        estado = ESTADO_INICIO;
                    }
                    break;

                // ----------------------------------------------------
                case ESTADO_COMENTARIO:
                    if (c == '\n') {
                        agregarToken("COMENTARIO", buffer.toString());
                        estado = ESTADO_INICIO;
                        // el salto de linea se procesa en ESTADO_INICIO
                    } else {
                        buffer.append(c);
                        posicion++;
                    }
                    break;

                // ----------------------------------------------------
                case ESTADO_CADENA:
                    buffer.append(c);
                    posicion++;
                    if (c == comillaActual) {
                        agregarToken("CADENA", buffer.toString());
                        estado = ESTADO_INICIO;
                    }
                    break;

                default:
                    posicion++;
                    break;
            }
        }

        // Al terminar la cadena puede quedar un lexema pendiente por cerrar
        switch (estado) {
            case ESTADO_IDENTIFICADOR:
                clasificarIdentificador(buffer.toString());
                break;
            case ESTADO_NUMERO:
                clasificarNumero(buffer.toString());
                break;
            case ESTADO_COMENTARIO:
                agregarToken("COMENTARIO", buffer.toString());
                break;
            case ESTADO_CADENA:
                agregarToken("CADENA_NO_CERRADA", buffer.toString());
                break;
            default:
                break;
        }

        return listaTokens;
    }

    // ------------------------------------------------------------
    // METODOS DE CLASIFICACION 
    // ------------------------------------------------------------
    private static void clasificarIdentificador(String lexema) {
        if (perteneceA(lexema, REGISTROS)) {
            agregarToken("REGISTRO", lexema);
        } else if (perteneceA(lexema, MNEMONICOS)) {
            agregarToken("MNEMONICO_8086", lexema);
        } else if (perteneceA(lexema, DIRECTIVAS)) {
            agregarToken("DIRECTIVA", lexema);
        } else {
            agregarToken("IDENTIFICADOR", lexema);
        }
    }

    private static void clasificarNumero(String lexema) {
        int len = lexema.length();
        char ultimo = lexema.charAt(len - 1);

        switch (ultimo) {
            case 'h':
            case 'H':
                if (esHexValido(lexema.substring(0, len - 1))) {
                    agregarToken("NUMERO_HEXADECIMAL", lexema);
                } else {
                    agregarToken("DESCONOCIDO", lexema);
                }
                break;
            case 'b':
            case 'B':
                if (esBinarioValido(lexema.substring(0, len - 1))) {
                    agregarToken("NUMERO_BINARIO", lexema);
                } else if (esDecimalValido(lexema)) {

                    agregarToken("DESCONOCIDO", lexema);
                } else {
                    agregarToken("DESCONOCIDO", lexema);
                }
                break;
            default:
                if (esDecimalValido(lexema)) {
                    agregarToken("NUMERO_DECIMAL", lexema);
                } else {
                    agregarToken("DESCONOCIDO", lexema);
                }
                break;
        }
    }

    //validaciones caracter por caracter
    private static boolean perteneceA(String lexema, String[] lista) {
        for (int i = 0; i < lista.length; i++) {
            if (lista[i].equalsIgnoreCase(lexema)) {
                return true;
            }
        }
        return false;
    }

    private static boolean esLetra(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static boolean esDigito(char c) {
        return (c >= '0' && c <= '9');
    }

    private static boolean esLetraHex(char c) {
        return (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private static boolean esHexValido(String texto) {
        if (texto.length() == 0) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (!(esDigito(c) || esLetraHex(c))) {
                return false;
            }
        }
        return true;
    }

    private static boolean esBinarioValido(String texto) {
        if (texto.length() == 0) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c != '0' && c != '1') {
                return false;
            }
        }
        return true;
    }

    private static boolean esDecimalValido(String texto) {
        if (texto.length() == 0) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            if (!esDigito(texto.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static void agregarToken(String tipo, String lexema) {
        listaTokens.add(new Token(lineaActual, tipo, lexema));
    }

    // ---------- GETTERS Y SETTERS 
    public static List<Token> getListaTokens() {
        return listaTokens;
    }

    public static void setListaTokens(List<Token> nuevaLista) {
        listaTokens = nuevaLista;
    }

    public static int getLineaActual() {
        return lineaActual;
    }

    public static void setLineaActual(int nuevaLinea) {
        lineaActual = nuevaLinea;
    }
}
