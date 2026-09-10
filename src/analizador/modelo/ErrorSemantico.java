package analizador.modelo;

/**
 * ErrorSemantico
 * ------------------------------------------------------------
 * Representa un error detectado por el AnalizadorSemantico.
 * Misma estructura que ErrorSintactico (linea + mensaje) para
 * mantener consistencia con el resto del proyecto.
 *
 * NOTA: si ya tienes una clase ErrorSintactico con esta misma forma,
 * puedes renombrar esta clase o reusar la misma con otro nombre.
 * Aqui se deja separada para no mezclar errores de distintas fases.
 * ------------------------------------------------------------
 */
public class ErrorSemantico {

    private int linea;
    private String mensaje;

    public ErrorSemantico(int linea, String mensaje) {
        this.linea = linea;
        this.mensaje = mensaje;
    }

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
