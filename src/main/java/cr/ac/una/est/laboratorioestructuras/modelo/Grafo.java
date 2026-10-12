package cr.ac.una.est.laboratorioestructuras.modelo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Grafo dirigido o no dirigido, con posiciones en el plano.
 * Toda modificacion notifica a los oyentes con el {@link TipoCambio} correspondiente.
 */
public class Grafo {

    private boolean dirigido;
    private final List<Nodo> nodos = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();
    private final List<GrafoListener> listeners = new ArrayList<>();
    private int siguienteNodoId;
    private int siguienteAristaId;

    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
    }

    public boolean isDirigido() {
        return dirigido;
    }

    public List<Nodo> getNodos() {
        return Collections.unmodifiableList(nodos);
    }

    public List<Arista> getAristas() {
        return Collections.unmodifiableList(aristas);
    }

    public Optional<Nodo> buscarNodo(int id) {
        for (Nodo nodo : nodos) {
            if (nodo.getId() == id) {
                return Optional.of(nodo);
            }
        }
        return Optional.empty();
    }

    public Nodo agregarNodo(String etiqueta, double x, double y) {
        Nodo nodo = new Nodo(siguienteNodoId++, etiqueta, x, y);
        nodos.add(nodo);
        notificar(TipoCambio.TOPOLOGIA);
        return nodo;
    }

    public Arista conectar(int origenId, int destinoId, double peso) {
        Nodo origen = buscarNodo(origenId)
                .orElseThrow(() -> nodoInexistente(origenId));
        Nodo destino = buscarNodo(destinoId)
                .orElseThrow(() -> nodoInexistente(destinoId));
        Arista arista = new Arista(siguienteAristaId++, origen, destino, peso);
        aristas.add(arista);
        notificar(TipoCambio.TOPOLOGIA);
        return arista;
    }

    public void eliminarNodo(int id) {
        Nodo nodo = buscarNodo(id).orElse(null);
        if (nodo == null) {
            return;
        }
        nodos.remove(nodo);
        aristas.removeIf(a -> a.getOrigen() == nodo || a.getDestino() == nodo);
        notificar(TipoCambio.TOPOLOGIA);
    }

    public void eliminarArista(int id) {
        if (aristas.removeIf(a -> a.getId() == id)) {
            notificar(TipoCambio.TOPOLOGIA);
        }
    }

    public void moverNodo(int id, double x, double y) {
        Nodo nodo = buscarNodo(id).orElse(null);
        if (nodo == null) {
            return;
        }
        nodo.setPosicion(x, y);
        notificar(TipoCambio.POSICION);
    }

    /** Mueve varios nodos de una sola vez y emite una unica notificacion. */
    public void moverNodos(Map<Integer, Posicion> posiciones) {
        boolean cambio = false;
        for (Map.Entry<Integer, Posicion> entry : posiciones.entrySet()) {
            Nodo nodo = buscarNodo(entry.getKey()).orElse(null);
            if (nodo != null) {
                nodo.setPosicion(entry.getValue().x(), entry.getValue().y());
                cambio = true;
            }
        }
        if (cambio) {
            notificar(TipoCambio.POSICION);
        }
    }

    /**
     * Sustituye el contenido del grafo (usado por el conversor de
     * representaciones) y notifica un unico cambio de topologia.
     */
    public void reemplazarContenido(boolean dirigido, List<Nodo> nuevosNodos,
                                    List<Arista> nuevasAristas) {
        reemplazar(dirigido, nuevosNodos, nuevasAristas);
        notificar(TipoCambio.TOPOLOGIA);
    }

    public void addListener(GrafoListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GrafoListener listener) {
        listeners.remove(listener);
    }

    private void reemplazar(boolean dirigido, List<Nodo> nuevosNodos,
                            List<Arista> nuevasAristas) {
        this.dirigido = dirigido;
        nodos.clear();
        nodos.addAll(nuevosNodos);
        aristas.clear();
        aristas.addAll(nuevasAristas);
        siguienteNodoId = siguienteId(nodos, Nodo::getId);
        siguienteAristaId = siguienteId(aristas, Arista::getId);
    }

    private void notificar(TipoCambio tipo) {
        for (GrafoListener listener : List.copyOf(listeners)) {
            listener.grafoCambiado(this, tipo);
        }
    }

    private static IllegalArgumentException nodoInexistente(int id) {
        return new IllegalArgumentException("No existe el nodo " + id);
    }

    private static <T> int siguienteId(Collection<T> elementos,
                                       java.util.function.ToIntFunction<T> id) {
        int maximo = -1;
        for (T elemento : elementos) {
            maximo = Math.max(maximo, id.applyAsInt(elemento));
        }
        return maximo + 1;
    }
}
