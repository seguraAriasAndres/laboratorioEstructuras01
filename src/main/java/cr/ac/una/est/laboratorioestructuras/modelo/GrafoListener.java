package cr.ac.una.est.laboratorioestructuras.modelo;

//La vista usa esto para enterarse de los cambios y volver a dibujar el grafo.
@FunctionalInterface
public interface GrafoListener {

    void grafoCambiado(Grafo grafo, TipoCambio tipo);
}
