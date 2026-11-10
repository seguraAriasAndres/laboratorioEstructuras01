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

//Acá llevamos los nodos y las conexiones, con o sin dirección.
public class Grafo {

    private boolean dirigido;
    private final List<Nodo> nodos = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();
    private final List<GrafoListener> listeners = new ArrayList<>();
    private int siguienteNodoId;
    private int siguienteAristaId;

    //Arrancamos con un grafo vacío y elegimos si lleva dirección.
    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
    }

    //Nos dice si las conexiones tienen dirección.
    public boolean isDirigido() {
        return dirigido;
    }

    //Dejamos ver los nodos, pero sin que puedan editar la lista desde afuera.
    public List<Nodo> getNodos() {
        return Collections.unmodifiableList(nodos);
    }

    //Dejamos ver las aristas, pero sin que puedan editar la lista desde afuera.
    public List<Arista> getAristas() {
        return Collections.unmodifiableList(aristas);
    }

    //Buscamos el nodo por su ID; puede que no exista.
    public Optional<Nodo> buscarNodo(int id) {
        for (Nodo nodo : nodos) {
            if (nodo.getId() == id) {
                return Optional.of(nodo);
            }
        }
        return Optional.empty();
    }

    //Creamos un nodo usando el siguiente ID libre.
    public Nodo agregarNodo(String etiqueta, double x, double y) {
        Nodo nodo = new Nodo(siguienteNodoId, etiqueta, x, y);
        siguienteNodoId++;
        nodos.add(nodo);
        notificar(TipoCambio.TOPOLOGIA);
        return nodo;
    }

    //Unimos dos nodos que ya estén en el grafo.
    public Arista conectar(int origenId, int destinoId, double peso) {
        Nodo origen = buscarNodo(origenId)
                .orElseThrow(() -> nodoInexistente(origenId));
        Nodo destino = buscarNodo(destinoId)
                .orElseThrow(() -> nodoInexistente(destinoId));

        //Como la matriz guarda un peso por par, si ya estaban conectados solo cambiamos ese peso.
        for (Arista existente : aristas) {
            if ((existente.getOrigen() == origen && existente.getDestino() == destino)
                    || (!dirigido && existente.getOrigen() == destino
                    && existente.getDestino() == origen)) {
                existente.setPeso(peso);
                notificar(TipoCambio.TOPOLOGIA);
                return existente;
            }
        }
        Arista arista = new Arista(siguienteAristaId, origen, destino, peso);
        siguienteAristaId++;
        aristas.add(arista);
        notificar(TipoCambio.TOPOLOGIA);
        return arista;
    }

    //Borramos el nodo y también las conexiones que lo usaban.
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

    //Buscamos la arista por su ID y la borramos.
    public void eliminarArista(int id) {
        if (aristas.removeIf(arista -> arista.getId() == id)) {
            notificar(TipoCambio.TOPOLOGIA);
        }
    }

    //Movemos el nodo si todavía está en el grafo.
    public void moverNodo(int id, double x, double y) {
        Nodo nodo = buscarNodo(id).orElse(null);
        if (nodo == null) {
            return;
        }

        nodo.setPosicion(x, y);
        notificar(TipoCambio.POSICION);
    }

    //Movemos varios nodos y avisamos una sola vez al terminar.
    public void moverNodos(Map<Integer, Posicion> posiciones) {
        if (posiciones == null || posiciones.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw new IllegalArgumentException("Las posiciones e IDs son obligatorios");
        }
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

    //Empezamos de cero con la cantidad de nodos que nos pidieron.
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

    //Revisamos los datos antes de cambiar todo el grafo y avisamos al terminar.
    public void reemplazarContenido(
            boolean dirigido,
            List<Nodo> nuevosNodos,
            List<Arista> nuevasAristas) {
        validarContenido(nuevosNodos, nuevasAristas, dirigido);
        reemplazar(dirigido, new ArrayList<>(nuevosNodos), new ArrayList<>(nuevasAristas));
        notificar(TipoCambio.TOPOLOGIA);
    }

    //Agregamos a quien quiera enterarse de los cambios del grafo.
    public void addListener(GrafoListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("El listener es obligatorio");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    //Quitamos a quien ya no necesite recibir los avisos.
    public void removeListener(GrafoListener listener) {
        listeners.remove(listener);
    }

    //Revisamos que no se repitan IDs y que las aristas usen nodos de esta lista.
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
        Set<String> pares = new HashSet<>();
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
            int a = origen.getId();
            int b = destino.getId();
            String par = dirigido ? a + ":" + b : Math.min(a, b) + ":" + Math.max(a, b);
            if (!pares.add(par)) {
                throw new IllegalArgumentException("Hay más de una arista para el mismo par de nodos");
            }
        }
    }

    //Cambiamos las listas y dejamos listos los siguientes IDs.
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

    //Usamos una copia por si alguien se sale de los avisos mientras estamos avisando.
    private void notificar(TipoCambio tipo) {
        for (GrafoListener listener : List.copyOf(listeners)) {
            listener.grafoCambiado(this, tipo);
        }
    }

    //Preparamos el error para cuando nos pidan un nodo que no existe.
    private static IllegalArgumentException nodoInexistente(int id) {
        return new IllegalArgumentException("No existe el nodo " + id);
    }

    //Buscamos el ID más grande y usamos el que le sigue.
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
