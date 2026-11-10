package cr.ac.una.est.laboratorioestructuras.modelo;

//Acá guardamos los datos de un nodo y dónde está dibujado.
public class Nodo {

    private final int id;
    private final String etiqueta;
    private double x;
    private double y;

    //Creamos el nodo con su ID, nombre y posición.
    public Nodo(int id, String etiqueta, double x, double y) {
        if (id < 0 || id == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El ID del nodo debe estar entre 0 y 2147483646");
        }
        if (etiqueta == null || etiqueta.isBlank()) {
            throw new IllegalArgumentException("La etiqueta del nodo es obligatoria");
        }
        this.id = id;
        this.etiqueta = etiqueta;
        setPosicion(x, y);
    }

    //Nos da el ID del nodo.
    public int getId() {
        return id;
    }

    //Nos da el nombre que se muestra en el dibujo.
    public String getEtiqueta() {
        return etiqueta;
    }

    //Nos da la posición horizontal del nodo.
    public double getX() {
        return x;
    }

    //Nos da la posición vertical del nodo.
    public double getY() {
        return y;
    }

    //Movemos el nodo a estas coordenadas.
    void setPosicion(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Las coordenadas deben ser finitas");
        }
        this.x = x;
        this.y = y;
    }

    //Armamos un texto con los datos del nodo para poder revisarlo.
    @Override
    public String toString() {
        return "Nodo{" + id + " '" + etiqueta + "' (" + x + ", " + y + ")}";
    }
}
