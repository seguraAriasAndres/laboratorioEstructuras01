package cr.ac.una.est.laboratorioestructuras.modelo;

/**
 * Vértice del grafo con identificador, etiqueta y posición.
 */
public class Nodo {

    private final int id;
    private final String etiqueta;
    private double x;
    private double y;

    // Crea un nodo con su identidad, etiqueta y coordenadas iniciales.
    // Entrada: ID, etiqueta y coordenadas; salida: nodo inicializado.
    public Nodo(int id, String etiqueta, double x, double y) {
        this.id = id;
        this.etiqueta = etiqueta;
        this.x = x;
        this.y = y;
    }

    // Devuelve el identificador del nodo.
    // Entrada: ninguna; salida: ID del nodo.
    public int getId() {
        return id;
    }

    // Devuelve la etiqueta del nodo.
    // Entrada: ninguna; salida: etiqueta del nodo.
    public String getEtiqueta() {
        return etiqueta;
    }

    // Devuelve la coordenada horizontal del nodo.
    // Entrada: ninguna; salida: coordenada x.
    public double getX() {
        return x;
    }

    // Devuelve la coordenada vertical del nodo.
    // Entrada: ninguna; salida: coordenada y.
    public double getY() {
        return y;
    }

    // Actualiza ambas coordenadas del nodo.
    // Entrada: coordenadas nuevas; salida: no devuelve valor.
    void setPosicion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Genera una representación textual del nodo.
    // Entrada: ninguna; salida: texto con ID, etiqueta y coordenadas.
    @Override
    public String toString() {
        return "Nodo{" + id + " '" + etiqueta + "' (" + x + ", " + y + ")}";
    }
}
