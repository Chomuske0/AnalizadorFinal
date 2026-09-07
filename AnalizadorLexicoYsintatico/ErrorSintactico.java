/**
 * Clase ErrorSintactico
 * Representa un error detectado por el analizador sintactico.
 * Contiene: numero de linea y mensaje descriptivo del error.
 * Cumple con el requisito de tener getters y setters.
 */
public class ErrorSintactico {

    private int linea;
    private String mensaje;

    public ErrorSintactico() {
    }

    public ErrorSintactico(int linea, String mensaje) {
        this.linea = linea;
        this.mensaje = mensaje;
    }

    // ---------- GETTERS Y SETTERS ----------

    public int getLinea() {
        return linea;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "Linea " + linea + ": " + mensaje;
    }
}
