package analizador.analisis;

import analizador.modelo.ErrorSemantico;
import analizador.modelo.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalizadorSemantico
 * ------------------------------------------------------------
 * Analizador semantico para codigo ensamblador del procesador 8086.
 * Se ejecuta DESPUES del AnalizadorSintactico y trabaja sobre la
 * misma lista de Token que este recibe. Se asume que la estructura
 * de cada linea ya es sintacticamente valida (no vuelve a validar
 * forma, solo significado).
 *
 * Que valida:
 *  1) Identificadores usados sin haber sido declarados.
 *  2) Declaraciones duplicadas (mismo nombre declarado 2+ veces),
 *     salvo la reapertura de un mismo SEGMENT (valido en 8086 real).
 *  3) Bloques SEGMENT/ENDS y PROC/ENDP balanceados y con nombres
 *     coincidentes entre apertura y cierre.
 *  4) Compatibilidad de tamanos en instrucciones de 2 operandos
 *     (BYTE vs WORD) entre registros y variables.
 *  5) Que los saltos (JMP, CALL, Jxx, LOOP) apunten a una etiqueta
 *     o procedimiento realmente declarado.
 *  6) Que ASSUME referencie segmentos realmente declarados.
 *
 * Reglas de diseno:
 *  - Estructura ESTATICA, igual que AnalizadorLexico/Sintactico.
 *  - El reconocimiento de casos se hace con switch-case.
 * ------------------------------------------------------------
 */
public class AnalizadorSemantico {

    // tabla de simbolos: clave = nombre en MAYUSCULAS
    private static Map<String, String> tipoSimbolo;     // SEGMENTO | PROCEDIMIENTO | ETIQUETA | VARIABLE | CONSTANTE
    private static Map<String, String> tamanoSimbolo;   // BYTE | WORD | DWORD | QWORD | N_A
    private static Map<String, Integer> lineaDeclaracion;

    private static List<ErrorSemantico> errores;

    private AnalizadorSemantico() {
    }

    /**
     * Analiza la lista de tokens (la misma que recibe AnalizadorSintactico).
     * Devuelve true si no se encontraron errores semanticos.
     * Se recomienda llamarlo solo si AnalizadorSintactico.analizar() ya
     * devolvio true, para evitar arrastrar errores de una fase anterior.
     */
    public static boolean analizar(List<Token> listaTokens) {
        errores = new ArrayList<ErrorSemantico>();
        tipoSimbolo = new HashMap<String, String>();
        tamanoSimbolo = new HashMap<String, String>();
        lineaDeclaracion = new HashMap<String, Integer>();

        List<List<Token>> lineas = agruparLineas(listaTokens);

        // Fase 1: construir la tabla de simbolos
        for (int i = 0; i < lineas.size(); i++) {
            registrarSimbolos(lineas.get(i));
        }

        // Fase 2: validar apertura/cierre de bloques SEGMENT/ENDS y PROC/ENDP
        validarBloques(lineas);

        // Fase 3: validar el uso de los simbolos (referencias, tamanos, saltos, ASSUME)
        for (int i = 0; i < lineas.size(); i++) {
            validarUsos(lineas.get(i));
        }

        return errores.isEmpty();
    }

    // ------------------------------------------------------------
    // AGRUPAMIENTO EN LINEAS LOGICAS (igual criterio que el sintactico)
    // ------------------------------------------------------------

    private static List<List<Token>> agruparLineas(List<Token> listaTokens) {
        List<List<Token>> lineas = new ArrayList<List<Token>>();
        List<Token> grupoActual = new ArrayList<Token>();
        int lineaGrupo = -1;

        for (int i = 0; i < listaTokens.size(); i++) {
            Token t = listaTokens.get(i);
            String tipo = t.getToken();

            switch (tipo) {
                case "COMENTARIO":
                case "CADENA_NO_CERRADA":
                case "DESCONOCIDO":
                    continue;
                default:
                    break;
            }

            if (lineaGrupo == -1) {
                lineaGrupo = t.getLinea();
            }
            if (t.getLinea() != lineaGrupo) {
                lineas.add(grupoActual);
                grupoActual = new ArrayList<Token>();
                lineaGrupo = t.getLinea();
            }
            grupoActual.add(t);
        }
        if (!grupoActual.isEmpty()) {
            lineas.add(grupoActual);
        }
        return lineas;
    }

    // ------------------------------------------------------------
    // FASE 1: TABLA DE SIMBOLOS
    // ------------------------------------------------------------

    private static void registrarSimbolos(List<Token> linea) {
        if (linea.isEmpty()) {
            return;
        }
        Token primero = linea.get(0);

        switch (primero.getToken()) {
            case "IDENTIFICADOR":
                if (linea.size() > 1 && linea.get(1).getToken().equals("SEPARADOR_DOSPUNTOS")) {
                    // etiqueta: NOMBRE ':'
                    declarar(primero.getLexema(), "ETIQUETA", "N_A", primero.getLinea());
                    List<Token> resto = new ArrayList<Token>(linea.subList(2, linea.size()));
                    if (!resto.isEmpty()) {
                        registrarSimbolos(resto);
                    }
                } else if (linea.size() > 1 && linea.get(1).getToken().equals("DIRECTIVA")) {
                    registrarDeclaracion(primero, linea.get(1), linea.get(1).getLinea());
                }
                break;
            default:
                // MNEMONICO_8086 y DIRECTIVA sueltas (END/ORG/ASSUME) no declaran simbolos
                break;
        }
    }

    private static void registrarDeclaracion(Token nombreTok, Token directivaTok, int numeroLinea) {
        String nombre = nombreTok.getLexema();
        String dir = directivaTok.getLexema().toUpperCase();

        switch (dir) {
            case "SEGMENT":
                declarar(nombre, "SEGMENTO", "N_A", numeroLinea);
                break;
            case "PROC":
                declarar(nombre, "PROCEDIMIENTO", "N_A", numeroLinea);
                break;
            case "EQU":
                declarar(nombre, "CONSTANTE", "N_A", numeroLinea);
                break;
            case "DB":
                declarar(nombre, "VARIABLE", "BYTE", numeroLinea);
                break;
            case "DW":
                declarar(nombre, "VARIABLE", "WORD", numeroLinea);
                break;
            case "DD":
                declarar(nombre, "VARIABLE", "DWORD", numeroLinea);
                break;
            case "DQ":
                declarar(nombre, "VARIABLE", "QWORD", numeroLinea);
                break;
            case "ENDS":
            case "ENDP":
                // cierres de bloque, no se registran como simbolo nuevo
                // (su validacion de nombre/balance ocurre en validarBloques)
                break;
            default:
                // MODEL, PUBLIC, STACK, etc. -> no se registran como simbolo
                break;
        }
    }

    private static void declarar(String nombre, String tipo, String tamano, int numeroLinea) {
        String clave = nombre.toUpperCase();

        if (tipoSimbolo.containsKey(clave)) {
            String tipoPrevio = tipoSimbolo.get(clave);
            // un SEGMENT puede reabrirse varias veces, eso es valido en 8086 real
            if (tipo.equals("SEGMENTO") && tipoPrevio.equals("SEGMENTO")) {
                return;
            }
            agregarError(numeroLinea, "El identificador '" + nombre
                    + "' ya habia sido declarado en la linea " + lineaDeclaracion.get(clave));
            return;
        }

        tipoSimbolo.put(clave, tipo);
        tamanoSimbolo.put(clave, tamano);
        lineaDeclaracion.put(clave, numeroLinea);
    }

    // ------------------------------------------------------------
    // FASE 2: BALANCE DE BLOQUES SEGMENT/ENDS Y PROC/ENDP
    // ------------------------------------------------------------

    private static void validarBloques(List<List<Token>> lineas) {
        Deque<String[]> pila = new ArrayDeque<String[]>(); // {tipoBloque, nombre, lineaApertura}

        for (int i = 0; i < lineas.size(); i++) {
            List<Token> linea = lineas.get(i);
            if (linea.size() < 2) {
                continue;
            }
            Token primero = linea.get(0);
            Token segundo = linea.get(1);

            if (!primero.getToken().equals("IDENTIFICADOR") || !segundo.getToken().equals("DIRECTIVA")) {
                continue;
            }

            String nombre = primero.getLexema();
            String dir = segundo.getLexema().toUpperCase();
            int numeroLinea = segundo.getLinea();

            switch (dir) {
                case "SEGMENT":
                    pila.push(new String[]{"SEGMENT", nombre, String.valueOf(numeroLinea)});
                    break;
                case "PROC":
                    pila.push(new String[]{"PROC", nombre, String.valueOf(numeroLinea)});
                    break;
                case "ENDS":
                    cerrarBloque(pila, "SEGMENT", nombre, numeroLinea);
                    break;
                case "ENDP":
                    cerrarBloque(pila, "PROC", nombre, numeroLinea);
                    break;
                default:
                    break;
            }
        }

        while (!pila.isEmpty()) {
            String[] bloque = pila.pop();
            agregarError(Integer.parseInt(bloque[2]), "El bloque '" + bloque[1]
                    + "' (" + bloque[0] + ") nunca fue cerrado");
        }
    }

    private static void cerrarBloque(Deque<String[]> pila, String tipoEsperado, String nombreCierre, int numeroLinea) {
        if (pila.isEmpty()) {
            agregarError(numeroLinea, "Se encontro un cierre '" + (tipoEsperado.equals("SEGMENT") ? "ENDS" : "ENDP")
                    + "' para '" + nombreCierre + "' sin apertura correspondiente");
            return;
        }
        String[] tope = pila.peek();
        if (!tope[0].equals(tipoEsperado)) {
            agregarError(numeroLinea, "Se esperaba cerrar '" + tope[0] + " " + tope[1]
                    + "' pero se encontro cierre de '" + nombreCierre + "'");
            return;
        }
        pila.pop();
        if (!tope[1].equalsIgnoreCase(nombreCierre)) {
            agregarError(numeroLinea, "El bloque abierto como '" + tope[1]
                    + "' se cierra con un nombre distinto: '" + nombreCierre + "'");
        }
    }

    // ------------------------------------------------------------
    // FASE 3: VALIDACION DE USOS (referencias, tamanos, saltos, ASSUME)
    // ------------------------------------------------------------

    private static void validarUsos(List<Token> linea) {
        if (linea.isEmpty()) {
            return;
        }
        Token primero = linea.get(0);

        switch (primero.getToken()) {
            case "IDENTIFICADOR":
                if (linea.size() > 1 && linea.get(1).getToken().equals("SEPARADOR_DOSPUNTOS")) {
                    List<Token> resto = new ArrayList<Token>(linea.subList(2, linea.size()));
                    if (!resto.isEmpty()) {
                        validarUsos(resto);
                    }
                } else if (linea.size() > 1 && linea.get(1).getToken().equals("DIRECTIVA")) {
                    validarValoresDeclaracion(linea);
                }
                break;

            case "MNEMONICO_8086":
                validarInstruccion(linea);
                break;

            case "DIRECTIVA":
                validarDirectivaGeneral(linea);
                break;

            default:
                break;
        }
    }

    private static void validarValoresDeclaracion(List<Token> linea) {
        String dir = linea.get(1).getLexema().toUpperCase();
        List<Token> resto = new ArrayList<Token>(linea.subList(2, linea.size()));

        switch (dir) {
            case "DB":
            case "DW":
            case "DD":
            case "DQ":
            case "EQU":
                validarIdentificadoresReferenciados(resto);
                break;
            default:
                break;
        }
    }

    private static void validarDirectivaGeneral(List<Token> linea) {
        Token directiva = linea.get(0);
        String dir = directiva.getLexema().toUpperCase();
        List<Token> resto = new ArrayList<Token>(linea.subList(1, linea.size()));

        switch (dir) {
            case "ASSUME":
                validarAssume(resto);
                break;
            case "END":
                if (resto.size() == 1 && resto.get(0).getToken().equals("IDENTIFICADOR")) {
                    validarEtiquetaOProcedimiento(resto.get(0));
                }
                break;
            default:
                break;
        }
    }

    private static void validarAssume(List<Token> resto) {
        List<Token> actual = new ArrayList<Token>();
        for (int i = 0; i <= resto.size(); i++) {
            boolean finGrupo = (i == resto.size()) || resto.get(i).getToken().equals("SEPARADOR_COMA");
            if (finGrupo) {
                if (actual.size() == 3 && actual.get(2).getToken().equals("IDENTIFICADOR")) {
                    Token nombreSegmento = actual.get(2);
                    String clave = nombreSegmento.getLexema().toUpperCase();
                    if (!tipoSimbolo.containsKey(clave)) {
                        agregarError(nombreSegmento.getLinea(), "El segmento '" + nombreSegmento.getLexema()
                                + "' referenciado en ASSUME no ha sido declarado");
                    } else if (!tipoSimbolo.get(clave).equals("SEGMENTO")) {
                        agregarError(nombreSegmento.getLinea(), "'" + nombreSegmento.getLexema()
                                + "' se usa en ASSUME pero no es un segmento, es " + tipoSimbolo.get(clave));
                    }
                }
                actual = new ArrayList<Token>();
            } else {
                actual.add(resto.get(i));
            }
        }
    }

    private static void validarEtiquetaOProcedimiento(Token idTok) {
        String clave = idTok.getLexema().toUpperCase();
        if (!tipoSimbolo.containsKey(clave)) {
            agregarError(idTok.getLinea(), "'" + idTok.getLexema() + "' no ha sido declarado como etiqueta o procedimiento");
            return;
        }
        String tipo = tipoSimbolo.get(clave);
        if (!tipo.equals("ETIQUETA") && !tipo.equals("PROCEDIMIENTO")) {
            agregarError(idTok.getLinea(), "'" + idTok.getLexema()
                    + "' debe ser una etiqueta o procedimiento, pero es " + tipo);
        }
    }

    private static void validarIdentificadoresReferenciados(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getToken().equals("IDENTIFICADOR")) {
                String clave = t.getLexema().toUpperCase();
                if (!tipoSimbolo.containsKey(clave)) {
                    agregarError(t.getLinea(), "El identificador '" + t.getLexema()
                            + "' no ha sido declarado");
                }
            }
        }
    }

    // ------------------------------------------------------------
    // INSTRUCCIONES: uso de simbolos + tamanos + saltos
    // ------------------------------------------------------------

    private static void validarInstruccion(List<Token> linea) {
        Token mnemonicoTok = linea.get(0);
        String mnemonico = mnemonicoTok.getLexema().toUpperCase();
        List<Token> resto = new ArrayList<Token>(linea.subList(1, linea.size()));
        List<List<Token>> operandos = agruparPorComas(resto);

        switch (categoriaSemantica(mnemonico)) {
            case "SALTO":
                if (operandos.size() == 1 && operandos.get(0).size() == 1
                        && operandos.get(0).get(0).getToken().equals("IDENTIFICADOR")) {
                    validarEtiquetaOProcedimiento(operandos.get(0).get(0));
                } else {
                    validarIdentificadoresReferenciados(resto);
                }
                break;

            case "DOS_OPERANDOS_TAMANO":
                for (int i = 0; i < operandos.size(); i++) {
                    validarIdentificadoresReferenciados(operandos.get(i));
                }
                if (operandos.size() == 2) {
                    validarCompatibilidadTamanos(mnemonico, operandos.get(0), operandos.get(1), mnemonicoTok.getLinea());
                }
                break;

            default:
                // resto de instrucciones: solo se valida que los identificadores existan
                validarIdentificadoresReferenciados(resto);
                break;
        }
    }

    private static String categoriaSemantica(String mnemonico) {
        switch (mnemonico) {
            case "JMP": case "CALL": case "JZ": case "JNZ": case "JE": case "JNE":
            case "JG": case "JL": case "JGE": case "JLE": case "JA": case "JB": case "LOOP":
                return "SALTO";

            case "MOV": case "ADD": case "SUB": case "CMP": case "AND":
            case "OR": case "XOR": case "TEST": case "XCHG":
                return "DOS_OPERANDOS_TAMANO";

            default:
                return "GENERAL";
        }
    }

    private static void validarCompatibilidadTamanos(String mnemonico, List<Token> op1, List<Token> op2, int numeroLinea) {
        String tamano1 = tamanoDeOperando(op1);
        String tamano2 = tamanoDeOperando(op2);

        // si alguno es indeterminado (memoria compleja, numero inmediato, cadena) no se compara
        if (tamano1 == null || tamano2 == null || tamano1.equals("N_A") || tamano2.equals("N_A")) {
            return;
        }
        if (!tamano1.equals(tamano2)) {
            agregarError(numeroLinea, "En '" + mnemonico + "' los operandos tienen tamanos incompatibles: "
                    + tamano1 + " y " + tamano2);
        }
    }

    /**
     * Devuelve BYTE, WORD, DWORD, QWORD, N_A (numero/cadena inmediatos,
     * indeterminado para memoria compleja) o null si no se pudo determinar
     * porque el operando referencia algo no declarado (ya reportado antes).
     */
    private static String tamanoDeOperando(List<Token> operando) {
        if (operando.size() != 1) {
            // referencias de memoria [ ... ] u operandos compuestos: no se evalua tamano
            return "N_A";
        }
        Token t = operando.get(0);
        switch (t.getToken()) {
            case "REGISTRO":
                return tamanoRegistro(t.getLexema());
            case "IDENTIFICADOR":
                String clave = t.getLexema().toUpperCase();
                if (!tipoSimbolo.containsKey(clave)) {
                    return null; // ya se reporto como no declarado
                }
                String tam = tamanoSimbolo.get(clave);
                return (tam == null) ? "N_A" : tam;
            case "NUMERO_DECIMAL":
            case "NUMERO_HEXADECIMAL":
            case "NUMERO_BINARIO":
            case "CADENA":
            case "SIMBOLO_INTERROGACION":
                return "N_A"; // valores inmediatos, se ajustan al operando destino
            default:
                return "N_A";
        }
    }

    private static String tamanoRegistro(String nombreRegistro) {
        switch (nombreRegistro.toUpperCase()) {
            case "AL": case "AH": case "BL": case "BH":
            case "CL": case "CH": case "DL": case "DH":
                return "BYTE";

            case "AX": case "BX": case "CX": case "DX":
            case "SI": case "DI": case "BP": case "SP":
            case "CS": case "DS": case "ES": case "SS": case "IP":
                return "WORD";

            default:
                return "N_A";
        }
    }

    // ------------------------------------------------------------
    // UTILIDADES
    // ------------------------------------------------------------

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

    private static void agregarError(int linea, String mensaje) {
        errores.add(new ErrorSemantico(linea, mensaje));
    }

    // ---------- GETTERS Y SETTERS ----------

    public static List<ErrorSemantico> getErrores() {
        return errores;
    }

    public static void setErrores(List<ErrorSemantico> nuevaLista) {
        errores = nuevaLista;
    }

    public static Map<String, String> getTipoSimbolo() {
        return tipoSimbolo;
    }

    public static Map<String, String> getTamanoSimbolo() {
        return tamanoSimbolo;
    }
}
