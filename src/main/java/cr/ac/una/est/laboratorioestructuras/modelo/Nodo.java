package cr.ac.una.est.laboratorioestructuras.modelo;

/**
 * Vertice del grafo: identificador unico, etiqueta y posicion en el plano.
 */
public class Nodo {

    private final int id;
    private final String etiqueta;
    private double x;
    private double y;

    public Nodo(int id, String etiqueta, double x, double y) {
        this.id = id;
        this.etiqueta = etiqueta;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosicion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Nodo{" + id + " '" + etiqueta + "' (" + x + ", " + y + ")}";
    }
}
