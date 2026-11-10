package cr.ac.una.est.laboratorioestructuras.presentacion;

//Guardamos qué celda cambió y el valor que le pusieron.
public record EdicionCelda(int fila, int columna, double valor) {

    //Revisamos que la fila, la columna y el número sean válidos.
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
