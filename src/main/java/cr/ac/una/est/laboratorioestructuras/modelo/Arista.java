package cr.ac.una.est.laboratorioestructuras.modelo;

/**
 * Arista con peso entre dos nodos.
 */
public class Arista {

    private final int id;
    private final Nodo origen;
    private final Nodo destino;
    private double peso;

    // Crea una arista con ID, extremos y peso.
    // Entrada: ID, nodo de origen, nodo de destino y peso; salida: arista inicializada.
    public Arista(int id, Nodo origen, Nodo destino, double peso) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    // Devuelve el identificador de la arista.
    // Entrada: ninguna; salida: ID de la arista.
    public int getId() {
        return id;
    }

    // Devuelve el nodo de origen.
    // Entrada: ninguna; salida: nodo de origen.
    public Nodo getOrigen() {
        return origen;
    }

    // Devuelve el nodo de destino.
    // Entrada: ninguna; salida: nodo de destino.
    public Nodo getDestino() {
        return destino;
    }

    // Devuelve el peso de la arista.
    // Entrada: ninguna; salida: peso actual.
    public double getPeso() {
        return peso;
    }

    // Actualiza el peso de la arista.
    // Entrada: peso nuevo; salida: no devuelve valor.
    void setPeso(double peso) {
        this.peso = peso;
    }

    // Genera una representación textual de la arista.
    // Entrada: ninguna; salida: texto con extremos e ID.
    @Override
    public String toString() {
        return "Arista{" + id + " " + origen.getId()
                + " -> " + destino.getId() + " w=" + peso + "}";
    }
}
