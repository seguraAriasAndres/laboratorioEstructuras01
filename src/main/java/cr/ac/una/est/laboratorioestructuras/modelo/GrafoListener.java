package cr.ac.una.est.laboratorioestructuras.modelo;

/**
 * Oyente de cambios del grafo. La vista se suscribe aqui para redibujarse.
 */
@FunctionalInterface
public interface GrafoListener {

    void grafoCambiado(Grafo grafo, TipoCambio tipo);
}
