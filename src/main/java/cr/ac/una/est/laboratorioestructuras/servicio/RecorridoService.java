package cr.ac.una.est.laboratorioestructuras.servicio;

import cr.ac.una.est.laboratorioestructuras.modelo.Arista;
import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.modelo.Nodo;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ejecuta recorridos BFS y DFS sobre un grafo.
 */
public class RecorridoService {

    // Recorre el grafo en anchura desde un nodo inicial.
    // Entrada: grafo e ID inicial; salida: IDs visitados en orden BFS.
    public List<Integer> bfs(Grafo grafo, int inicioId) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser null");
        }

        Map<Integer, List<Integer>> vecindad = construirVecindad(grafo);
        if (!vecindad.containsKey(inicioId)) {
            throw new IllegalArgumentException(
                    "No existe el nodo inicial " + inicioId);
        }

        List<Integer> orden = new ArrayList<>();
        Set<Integer> visitados = new LinkedHashSet<>();
        Deque<Integer> pendientes = new ArrayDeque<>();

        visitados.add(inicioId);
        pendientes.addLast(inicioId);

        while (!pendientes.isEmpty()) {
            int actual = pendientes.removeFirst();
            orden.add(actual);

            for (Integer vecino : vecindad.get(actual)) {
                if (visitados.add(vecino)) {
                    pendientes.addLast(vecino);
                }
            }
        }

        return List.copyOf(orden);
    }

    // Recorre el grafo en profundidad desde un nodo inicial.
    // Entrada: grafo e ID inicial; salida: IDs visitados en orden DFS.
    public List<Integer> dfs(Grafo grafo, int inicioId) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser null");
        }

        Map<Integer, List<Integer>> vecindad = construirVecindad(grafo);
        if (!vecindad.containsKey(inicioId)) {
            throw new IllegalArgumentException(
                    "No existe el nodo inicial " + inicioId);
        }

        List<Integer> orden = new ArrayList<>();
        Set<Integer> visitados = new LinkedHashSet<>();
        Deque<Integer> pendientes = new ArrayDeque<>();
        pendientes.push(inicioId);

        while (!pendientes.isEmpty()) {
            int actual = pendientes.pop();
            if (!visitados.add(actual)) {
                continue;
            }

            orden.add(actual);
            List<Integer> vecinos = vecindad.get(actual);

            for (int indice = vecinos.size() - 1; indice >= 0; indice--) {
                Integer vecino = vecinos.get(indice);
                if (!visitados.contains(vecino)) {
                    pendientes.push(vecino);
                }
            }
        }

        return List.copyOf(orden);
    }

    // Construye vecinos según la dirección y el orden de las aristas del grafo.
    // Entrada: grafo; salida: mapa inmutable de IDs a listas de vecinos únicos.
    public Map<Integer, List<Integer>> construirVecindad(Grafo grafo) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser null");
        }

        Map<Integer, Set<Integer>> vecinosPorId = new LinkedHashMap<>();
        for (Nodo nodo : grafo.getNodos()) {
            vecinosPorId.put(nodo.getId(), new LinkedHashSet<>());
        }

        for (Arista arista : grafo.getAristas()) {
            int idOrigen = arista.getOrigen().getId();
            int idDestino = arista.getDestino().getId();
            Set<Integer> vecinosOrigen = vecinosPorId.get(idOrigen);
            Set<Integer> vecinosDestino = vecinosPorId.get(idDestino);

            if (vecinosOrigen == null || vecinosDestino == null) {
                throw new IllegalStateException(
                        "Una arista referencia un nodo que no está en el grafo");
            }

            vecinosOrigen.add(idDestino);
            if (!grafo.isDirigido()) {
                vecinosDestino.add(idOrigen);
            }
        }

        Map<Integer, List<Integer>> resultado = new LinkedHashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entrada
                : vecinosPorId.entrySet()) {
            resultado.put(
                    entrada.getKey(),
                    List.copyOf(entrada.getValue()));
        }

        return Collections.unmodifiableMap(resultado);
    }
}
