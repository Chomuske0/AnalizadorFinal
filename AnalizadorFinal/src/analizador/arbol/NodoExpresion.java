package analizador.arbol;

import java.util.ArrayList;
import java.util.List;

/**
 * NodoExpresion
 * ------------------------------------------------------------
 * Representa un nodo dentro del arbol de expresion del programa.
 * Cada nodo tiene un tipo, un valor (lexema), el numero de linea
 * y una lista de hijos.
 * ------------------------------------------------------------
 */
public class NodoExpresion {

    private String tipo;
    private String valor;
    private int numLinea;
    private List<NodoExpresion> hijos;

    public NodoExpresion(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.numLinea = 0;
        this.hijos = new ArrayList<NodoExpresion>();
    }

    public NodoExpresion(String tipo, String valor, int numLinea) {
        this.tipo = tipo;
        this.valor = valor;
        this.numLinea = numLinea;
        this.hijos = new ArrayList<NodoExpresion>();
    }

    public void agregarHijo(NodoExpresion hijo) {
        hijos.add(hijo);
    }

    // ---------- GETTERS Y SETTERS ----------

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public int getNumLinea() {
        return numLinea;
    }

    public void setNumLinea(int numLinea) {
        this.numLinea = numLinea;
    }

    public List<NodoExpresion> getHijos() {
        return hijos;
    }

    public void setHijos(List<NodoExpresion> hijos) {
        this.hijos = hijos;
    }

    @Override
    public String toString() {
        return valor + " (" + tipo + ")";
    }
}
