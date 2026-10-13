package analizador.codigo_intermedio;

/**
 * Cuadruplo
 * ------------------------------------------------------------
 * Una instruccion de codigo intermedio de tres direcciones:
 * (operador, arg1, arg2, resultado)
 * ------------------------------------------------------------
 */
public class Cuadruplo {

    private int numero;
    private int linea;
    private String operador;
    private String arg1;
    private String arg2;
    private String resultado;
    private String codigo;

    public Cuadruplo(int linea, String operador, String arg1,
                     String arg2, String resultado, String codigo) {
        this.linea = linea;
        this.operador = operador;
        this.arg1 = arg1 == null ? "_" : arg1;
        this.arg2 = arg2 == null ? "_" : arg2;
        this.resultado = resultado == null ? "_" : resultado;
        this.codigo = codigo;
    }

    // ---------- GETTERS Y SETTERS ----------

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getLinea() {
        return linea;
    }

    public String getOperador() {
        return operador;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResultado() {
        return resultado;
    }

    /** Forma legible de tres direcciones, ej: AX = AX + BX */
    public String getCodigo() {
        return codigo;
    }

    @Override
    public String toString() {
        return numero + ": (" + operador + ", " + arg1 + ", "
                + arg2 + ", " + resultado + ")";
    }
}
