package cr.ac.una.est.laboratorioestructuras.modelo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Grafo dirigido o no dirigido, con posiciones en el plano.
 */
public class Grafo {

    private boolean dirigido;
    private final List<Nodo> nodos = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();
    private final List<GrafoListener> listeners = new ArrayList<>();
    private int siguienteNodoId;
    private int siguienteAristaId;

    // Inicializa un grafo vacío con el tipo de dirección indicado.
    // Entrada: dirigido; salida: instancia vacía del grafo.
    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
    }

    // Indica si el grafo es dirigido.
    // Entrada: ninguna; salida: true si el grafo es dirigido.
    public boolean isDirigido() {
        return dirigido;
    }

    // Devuelve la vista no modificable de los nodos del grafo.
    // Entrada: ninguna; salida: lista de nodos en orden de inserción.
    public List<Nodo> getNodos() {
        return Collections.unmodifiableList(nodos);
    }

    // Devuelve la vista no modificable de las aristas del grafo.
    // Entrada: ninguna; salida: lista de aristas en orden de inserción.
    public List<Arista> getAristas() {
        return Collections.unmodifiableList(aristas);
    }

    // Busca un nodo por su identificador.
    // Entrada: id del nodo; salida: Optional con el nodo o vacío si no existe.
    public Optional<Nodo> buscarNodo(int id) {
        for (Nodo nodo : nodos) {
            if (nodo.getId() == id) {
                return Optional.of(nodo);
            }
        }
        return Optional.empty();
    }

    // Agrega un nodo con el siguiente identificador disponible.
    // Entrada: etiqueta y coordenadas; salida: nodo agregado.
    public Nodo agregarNodo(String etiqueta, double x, double y) {
        Nodo nodo = new Nodo(siguienteNodoId++, etiqueta, x, y);
        nodos.add(nodo);
        notificar(TipoCambio.TOPOLOGIA);
        return nodo;
    }

    // Conecta dos nodos existentes mediante una arista con peso.
    // Entrada: IDs de origen y destino, y peso; salida: arista agregada.
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

    // Elimina un nodo y todas las aristas conectadas a él.
    // Entrada: id del nodo; salida: no devuelve valor y notifica si hubo cambios.
    public void eliminarNodo(int id) {
        Nodo nodo = buscarNodo(id).orElse(null);
        if (nodo == null) {
            return;
        }

        nodos.remove(nodo);
        aristas.removeIf(arista ->
                arista.getOrigen() == nodo || arista.getDestino() == nodo);
        notificar(TipoCambio.TOPOLOGIA);
    }

    // Elimina una arista por su identificador.
    // Entrada: id de la arista; salida: no devuelve valor y notifica si la eliminó.
    public void eliminarArista(int id) {
        if (aristas.removeIf(arista -> arista.getId() == id)) {
            notificar(TipoCambio.TOPOLOGIA);
        }
    }

    // Cambia la posición de un nodo existente.
    // Entrada: id del nodo y coordenadas nuevas; salida: no devuelve valor.
    public void moverNodo(int id, double x, double y) {
        Nodo nodo = buscarNodo(id).orElse(null);
        if (nodo == null) {
            return;
        }

        nodo.setPosicion(x, y);
        notificar(TipoCambio.POSICION);
    }

    // Cambia varias posiciones y emite una sola notificación.
    // Entrada: mapa de IDs a posiciones; salida: no devuelve valor.
    public void moverNodos(Map<Integer, Posicion> posiciones) {
        boolean huboCambios = false;

        for (Map.Entry<Integer, Posicion> entrada : posiciones.entrySet()) {
            Nodo nodo = buscarNodo(entrada.getKey()).orElse(null);
            if (nodo != null) {
                Posicion posicion = entrada.getValue();
                nodo.setPosicion(posicion.x(), posicion.y());
                huboCambios = true;
            }
        }

        if (huboCambios) {
            notificar(TipoCambio.POSICION);
        }
    }

    // Reinicia el grafo y crea la cantidad indicada de nodos aislados.
    // Entrada: dirección y cantidad de nodos; salida: no devuelve valor.
    public void reiniciar(boolean dirigido, int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de nodos no puede ser negativa");
        }

        List<Nodo> nuevosNodos = new ArrayList<>();
        for (int id = 0; id < cantidad; id++) {
            nuevosNodos.add(new Nodo(id, "N" + (id + 1), 0, 0));
        }

        reemplazarContenido(dirigido, nuevosNodos, List.of());
    }

    // Valida y reemplaza el contenido del grafo con una sola notificación.
    // Entrada: dirección, nodos y aristas nuevas; salida: no devuelve valor.
    public void reemplazarContenido(
            boolean dirigido,
            List<Nodo> nuevosNodos,
            List<Arista> nuevasAristas) {
        validarContenido(nuevosNodos, nuevasAristas, dirigido);
        reemplazar(dirigido, nuevosNodos, nuevasAristas);
        notificar(TipoCambio.TOPOLOGIA);
    }

    // Registra un oyente para recibir cambios del grafo.
    // Entrada: listener que recibirá las notificaciones; salida: no devuelve valor.
    public void addListener(GrafoListener listener) {
        listeners.add(listener);
    }

    // Retira un oyente registrado.
    // Entrada: listener que se desea retirar; salida: no devuelve valor.
    public void removeListener(GrafoListener listener) {
        listeners.remove(listener);
    }

    // Comprueba IDs únicos y que cada arista conecte nodos contenidos en la lista.
    // Entrada: nodos, aristas y dirección; salida: no devuelve valor o lanza excepción.
    private void validarContenido(
            List<Nodo> nuevosNodos,
            List<Arista> nuevasAristas,
            boolean dirigido) {
        if (nuevosNodos == null || nuevasAristas == null) {
            throw new IllegalArgumentException(
                    "Las listas de nodos y aristas no pueden ser null");
        }

        Map<Integer, Nodo> nodosPorId = new HashMap<>();
        for (Nodo nodo : nuevosNodos) {
            if (nodo == null) {
                throw new IllegalArgumentException(
                        "La lista de nodos no puede contener null");
            }
            if (nodosPorId.putIfAbsent(nodo.getId(), nodo) != null) {
                throw new IllegalArgumentException(
                        "ID de nodo duplicado: " + nodo.getId());
            }
        }

        Set<Integer> idsAristas = new HashSet<>();
        for (Arista arista : nuevasAristas) {
            if (arista == null) {
                throw new IllegalArgumentException(
                        "La lista de aristas no puede contener null");
            }
            if (!idsAristas.add(arista.getId())) {
                throw new IllegalArgumentException(
                        "ID de arista duplicado: " + arista.getId());
            }

            Nodo origen = arista.getOrigen();
            Nodo destino = arista.getDestino();
            if (origen == null || destino == null) {
                throw new IllegalArgumentException(
                        "Cada arista debe tener origen y destino");
            }
            if (nodosPorId.get(origen.getId()) != origen) {
                throw new IllegalArgumentException(
                        "El origen de la arista no pertenece a los nodos nuevos");
            }
            if (nodosPorId.get(destino.getId()) != destino) {
                throw new IllegalArgumentException(
                        "El destino de la arista no pertenece a los nodos nuevos");
            }
        }
    }

    // Sustituye las listas actuales y calcula los siguientes IDs disponibles.
    // Entrada: dirección, nodos y aristas validados; salida: no devuelve valor.
    private void reemplazar(
            boolean dirigido,
            List<Nodo> nuevosNodos,
            List<Arista> nuevasAristas) {
        this.dirigido = dirigido;

        nodos.clear();
        nodos.addAll(nuevosNodos);

        aristas.clear();
        aristas.addAll(nuevasAristas);

        siguienteNodoId = siguienteId(nodos, Nodo::getId);
        siguienteAristaId = siguienteId(aristas, Arista::getId);
    }

    // Notifica a una copia de los oyentes para tolerar cambios durante la notificación.
    // Entrada: tipo de cambio; salida: no devuelve valor.
    private void notificar(TipoCambio tipo) {
        for (GrafoListener listener : List.copyOf(listeners)) {
            listener.grafoCambiado(this, tipo);
        }
    }

    // Crea la excepción usada cuando un nodo solicitado no existe.
    // Entrada: ID inexistente; salida: excepción descriptiva.
    private static IllegalArgumentException nodoInexistente(int id) {
        return new IllegalArgumentException("No existe el nodo " + id);
    }

    // Calcula el siguiente ID a partir del máximo ID de una colección.
    // Entrada: elementos y función para obtener sus IDs; salida: máximo ID más uno.
    private static <T> int siguienteId(
            Collection<T> elementos,
            java.util.function.ToIntFunction<T> obtenerId) {
        int maximo = -1;
        for (T elemento : elementos) {
            maximo = Math.max(maximo, obtenerId.applyAsInt(elemento));
        }
        return maximo + 1;
    }
}
