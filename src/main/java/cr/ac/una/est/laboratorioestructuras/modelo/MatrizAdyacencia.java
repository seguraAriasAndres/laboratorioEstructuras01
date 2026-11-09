package cr.ac.una.est.laboratorioestructuras.modelo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representación matricial inmutable de un grafo.
 */
public final class MatrizAdyacencia {

    private final boolean dirigido;
    private final List<Integer> idsNodos;
    private final List<String> etiquetas;
    private final double[][] valores;

    // Construye una matriz y conserva copias defensivas de sus datos.
    // Entrada: dirección, IDs, etiquetas y valores; salida: matriz validada.
    public MatrizAdyacencia(
            boolean dirigido,
            List<Integer> idsNodos,
            List<String> etiquetas,
            double[][] valores) {
        this.dirigido = dirigido;
        this.idsNodos = List.copyOf(idsNodos);
        this.etiquetas = List.copyOf(etiquetas);
        this.valores = copiar(valores);
        validar();
    }

    // Indica si la matriz representa un grafo dirigido.
    // Entrada: ninguna; salida: true si la matriz es dirigida.
    public boolean isDirigido() {
        return dirigido;
    }

    // Devuelve la cantidad de filas y columnas de la matriz.
    // Entrada: ninguna; salida: tamaño de la matriz.
    public int size() {
        return idsNodos.size();
    }

    // Devuelve los IDs en el mismo orden que las filas y columnas.
    // Entrada: ninguna; salida: lista inmutable de IDs de nodos.
    public List<Integer> getIdsNodos() {
        return idsNodos;
    }

    // Devuelve la etiqueta asociada a un índice de fila o columna.
    // Entrada: índice; salida: etiqueta del nodo correspondiente.
    public String getEtiqueta(int indice) {
        return etiquetas.get(indice);
    }

    // Devuelve el peso ubicado en una celda.
    // Entrada: fila y columna; salida: valor de la celda.
    public double getValor(int fila, int columna) {
        return valores[fila][columna];
    }

    // Busca la posición matricial correspondiente a un ID real de nodo.
    // Entrada: ID de nodo; salida: índice o -1 si el ID no está en la matriz.
    public int indiceDe(int idNodo) {
        return idsNodos.indexOf(idNodo);
    }

    // Crea una matriz nueva con el valor actualizado y refleja el cambio si es no dirigida.
    // Entrada: fila, columna y peso; salida: nueva matriz sin modificar esta instancia.
    public MatrizAdyacencia conValor(int fila, int columna, double peso) {
        double[][] nuevosValores = copiar(valores);
        nuevosValores[fila][columna] = peso;

        if (!dirigido) {
            nuevosValores[columna][fila] = peso;
        }

        return new MatrizAdyacencia(
                dirigido,
                idsNodos,
                etiquetas,
                nuevosValores);
    }

    // Comprueba que IDs, etiquetas y dimensiones de la matriz sean consistentes.
    // Entrada: ninguna; salida: no devuelve valor o lanza excepción si hay datos inválidos.
    private void validar() {
        int cantidad = idsNodos.size();

        if (etiquetas.size() != cantidad) {
            throw new IllegalArgumentException(
                    "IDs y etiquetas no coinciden en tamaño");
        }

        Set<Integer> idsUnicos = new HashSet<>();
        for (Integer idNodo : idsNodos) {
            if (!idsUnicos.add(idNodo)) {
                throw new IllegalArgumentException(
                        "ID de nodo duplicado en la matriz: " + idNodo);
            }
        }

        if (valores.length != cantidad) {
            throw new IllegalArgumentException(
                    "La matriz no es de tamaño " + cantidad + "x" + cantidad);
        }

        for (double[] fila : valores) {
            if (fila.length != cantidad) {
                throw new IllegalArgumentException(
                        "La matriz no es de tamaño " + cantidad + "x" + cantidad);
            }
        }
    }

    // Copia cada fila para impedir que cambios externos alteren la matriz.
    // Entrada: matriz original; salida: copia profunda de sus filas.
    private static double[][] copiar(double[][] original) {
        if (original == null) {
            throw new IllegalArgumentException(
                    "La matriz de valores no puede ser null");
        }

        double[][] copia = new double[original.length][];
        for (int fila = 0; fila < original.length; fila++) {
            if (original[fila] == null) {
                throw new IllegalArgumentException(
                        "Una fila de la matriz no puede ser null");
            }
            copia[fila] = original[fila].clone();
        }

        return copia;
    }
}
