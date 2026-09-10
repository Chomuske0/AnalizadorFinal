package analizador.modelo;

public class Token {

    private int linea;
    private String token;
    private String lexema;

    public Token() {
    }

    public Token(int linea, String token, String lexema) {
        this.linea = linea;
        this.token = token;
        this.lexema = lexema;
    }

    // ---------- GETTERS Y SETTERS ----------
    public int getLinea() {
        return linea;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    @Override
    public String toString() {
        return "Linea " + linea + " | Token: " + token + " | Lexema: " + lexema;
    }
}
