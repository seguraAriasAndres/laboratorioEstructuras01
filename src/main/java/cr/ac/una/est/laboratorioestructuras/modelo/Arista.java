package cr.ac.una.est.laboratorioestructuras.modelo;

/**
 * Arista con peso entre dos nodos. En un grafo no dirigido el par
 * (origen, destino) es indistinto pero se conserva el orden de creacion.
 */
public class Arista {

    private final int id;
    private final Nodo origen;
    private final Nodo destino;
    private double peso;

    public Arista(int id, Nodo origen, Nodo destino, double peso) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    public int getId() {
        return id;
    }

    public Nodo getOrigen() {
        return origen;
    }

    public Nodo getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    @Override
    public String toString() {
        return "Arista{" + id + " " + origen.getId() + " -> " + destino.getId() + " w=" + peso + "}";
    }
}
