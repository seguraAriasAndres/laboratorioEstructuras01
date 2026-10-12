package cr.ac.una.est.laboratorioestructuras.modelo;

/**
 * Motivo por el cual un {@link Grafo} notifico a sus oyentes.
 */
public enum TipoCambio {
    /** Cambio en nodos o aristas (alta, baja, reconexion). */
    TOPOLOGIA,
    /** Cambio solo de coordenadas de nodos. */
    POSICION
}
