package cr.ac.una.est.laboratorioestructuras.modelo;

//Guardamos las coordenadas x e y juntas, sin poder cambiarlas después.
public record Posicion(double x, double y) {
    public Posicion {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Las coordenadas deben ser finitas");
        }
    }
}
