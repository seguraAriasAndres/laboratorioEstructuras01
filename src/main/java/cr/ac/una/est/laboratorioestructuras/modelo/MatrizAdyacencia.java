package cr.ac.una.est.laboratorioestructuras.modelo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Esta es la matriz del grafo; una vez creada, sus datos no se cambian.
public final class MatrizAdyacencia {

    private final boolean dirigido;
    private final List<Integer> idsNodos;
    private final List<String> etiquetas;
    private final double[][] valores;

    //Copiamos los datos para que nadie pueda cambiar la matriz desde afuera.
    public MatrizAdyacencia(
            boolean dirigido,
            List<Integer> idsNodos,
            List<String> etiquetas,
            double[][] valores) {
        this.dirigido = dirigido;
        if (idsNodos == null || etiquetas == null
                || idsNodos.stream().anyMatch(id -> id == null || id < 0 || id == Integer.MAX_VALUE)
                || etiquetas.stream().anyMatch(e -> e == null || e.isBlank())) {
            throw new IllegalArgumentException("Los IDs y las etiquetas deben ser válidos");
        }
        this.idsNodos = List.copyOf(idsNodos);
        this.etiquetas = List.copyOf(etiquetas);
        this.valores = copiar(valores);
        validar();
    }

    //Nos dice si las conexiones tienen dirección.
    public boolean isDirigido() {
        return dirigido;
    }

    //Nos da la cantidad de filas y columnas.
    public int size() {
        return idsNodos.size();
    }

    //Los IDs van en el mismo orden que las filas y columnas.
    public List<Integer> getIdsNodos() {
        return idsNodos;
    }

    //Buscamos el nombre del nodo que está en esta posición de la matriz.
    public String getEtiqueta(int indice) {
        return etiquetas.get(indice);
    }

    //Leemos el peso que hay en esta celda.
    public double getValor(int fila, int columna) {
        return valores[fila][columna];
    }

    //Buscamos en qué fila está el nodo; si no aparece, devolvemos -1.
    public int indiceDe(int idNodo) {
        return idsNodos.indexOf(idNodo);
    }

    //Hacemos otra matriz con el cambio y lo reflejamos si el grafo no es dirigido.
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

    //Revisamos que los IDs, los nombres y los valores de la matriz tengan sentido.
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
            for (double valor : fila) {
                if (!Double.isFinite(valor)) {
                    throw new IllegalArgumentException("Los pesos de la matriz deben ser finitos");
                }
            }
        }
        if (!dirigido) {
            for (int i = 0; i < cantidad; i++) {
                for (int j = i + 1; j < cantidad; j++) {
                    if (valores[i][j] != valores[j][i]) {
                        throw new IllegalArgumentException("La matriz no dirigida debe ser simétrica");
                    }
                }
            }
        }
    }

    //Copiamos las filas para que la matriz original quede aparte.
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
