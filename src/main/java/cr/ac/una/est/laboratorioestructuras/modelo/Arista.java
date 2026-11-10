package cr.ac.una.est.laboratorioestructuras.modelo;

//Esta es la conexión entre dos nodos y el peso que tiene.
public class Arista {

    private final int id;
    private final Nodo origen;
    private final Nodo destino;
    private double peso;

    //Guardamos el ID, los dos nodos y el peso de la conexión.
    public Arista(int id, Nodo origen, Nodo destino, double peso) {
        if (id < 0 || id == Integer.MAX_VALUE || origen == null || destino == null) {
            throw new IllegalArgumentException("La arista requiere un ID válido y ambos extremos");
        }
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        setPeso(peso);
    }

    //Nos da el ID de esta arista.
    public int getId() {
        return id;
    }

    //Nos dice de qué nodo sale la conexión.
    public Nodo getOrigen() {
        return origen;
    }

    //Nos dice a qué nodo llega la conexión.
    public Nodo getDestino() {
        return destino;
    }

    //Nos da el peso que tiene la arista.
    public double getPeso() {
        return peso;
    }

    //Cambiamos el peso, pero primero revisamos que sea válido.
    void setPeso(double peso) {
        if (!Double.isFinite(peso) || peso == 0.0) {
            throw new IllegalArgumentException("El peso debe ser finito y distinto de cero");
        }
        this.peso = peso;
    }

    //Armamos un texto con los datos de la arista para poder revisarla.
    @Override
    public String toString() {
        return "Arista{" + id + " " + origen.getId()
                + " -> " + destino.getId() + " w=" + peso + "}";
    }
}
