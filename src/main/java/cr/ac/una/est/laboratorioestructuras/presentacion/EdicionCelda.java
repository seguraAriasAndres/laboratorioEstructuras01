package cr.ac.una.est.laboratorioestructuras.presentacion;

/**
 * Describe una edición solicitada para una celda de la matriz.
 */
public record EdicionCelda(int fila, int columna, double valor) {

    // Valida los datos de una edición de celda.
    // Entrada: fila, columna y valor; salida: registro validado o excepción.
    public EdicionCelda {
        if (fila < 0 || columna < 0) {
            throw new IllegalArgumentException(
                    "La fila y la columna no pueden ser negativas");
        }
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(
                    "El valor debe ser un número finito");
        }
    }
}
