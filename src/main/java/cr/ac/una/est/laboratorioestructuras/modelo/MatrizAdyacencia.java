package cr.ac.una.est.laboratorioestructuras.modelo;

import java.util.List;

/**
 * Representacion matricial inmutable de un grafo.
 * Las filas/columnas se indexan por posicion (0..size()-1) y {@link #getIds()}
 * da el nodo real que representa cada indice.
 */
public final class MatrizAdyacencia {

    private final boolean dirigido;
    private final List<Integer> idsNodos;
    private final List<String> etiquetas;
    private final double[][] valores;

    public MatrizAdyacencia(boolean dirigido, List<Integer> idsNodos,
                            List<String> etiquetas, double[][] valores) {
        this.dirigido = dirigido;
        this.idsNodos = List.copyOf(idsNodos);
        this.etiquetas = List.copyOf(etiquetas);
        this.valores = copiar(valores);
        validar();
    }

    public boolean isDirigido() {
        return dirigido;
    }

    public int size() {
        return idsNodos.size();
    }

    public List<Integer> getIds() {
        return idsNodos;
    }

    public String getEtiqueta(int indice) {
        return etiquetas.get(indice);
    }

    public double getValor(int fila, int columna) {
        return valores[fila][columna];
    }

    /** Devuelve una nueva matriz con un solo valor sobrescrito (la original no cambia). */
    public MatrizAdyacencia conValor(int fila, int columna, double peso) {
        double[][] nuevos = copiar(valores);
        nuevos[fila][columna] = peso;
        if (!dirigido) {
            nuevos[columna][fila] = peso;
        }
        return new MatrizAdyacencia(dirigido, idsNodos, etiquetas, nuevos);
    }

    public void validar() {
        int n = idsNodos.size();
        if (etiquetas.size() != n) {
            throw new IllegalArgumentException("ids y etiquetas no coinciden en tamano");
        }
        if (valores.length != n) {
            throw new IllegalArgumentException("matriz no es " + n + "x" + n);
        }
        for (double[] fila : valores) {
            if (fila.length != n) {
                throw new IllegalArgumentException("matriz no es " + n + "x" + n);
            }
        }
    }

    private static double[][] copiar(double[][] original) {
        double[][] copia = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            copia[i] = original[i].clone();
        }
        return copia;
    }
}
