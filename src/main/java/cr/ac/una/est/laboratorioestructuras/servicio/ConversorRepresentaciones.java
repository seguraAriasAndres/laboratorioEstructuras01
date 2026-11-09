package cr.ac.una.est.laboratorioestructuras.servicio;

import cr.ac.una.est.laboratorioestructuras.modelo.Arista;
import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.modelo.MatrizAdyacencia;
import cr.ac.una.est.laboratorioestructuras.modelo.Nodo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Convierte grafos entre matrices y listas de adyacencia.
 */
public class ConversorRepresentaciones {

    // Convierte un grafo a una matriz conservando el orden y los IDs de sus nodos.
    // Entrada: grafo; salida: matriz de adyacencia inmutable.
    public MatrizAdyacencia aMatriz(Grafo grafo) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser null");
        }

        List<Nodo> nodos = grafo.getNodos();
        List<Integer> idsNodos = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        Map<Integer, Integer> indicesPorId = new HashMap<>();
        double[][] valores = new double[nodos.size()][nodos.size()];

        for (int indice = 0; indice < nodos.size(); indice++) {
            Nodo nodo = nodos.get(indice);
            if (indicesPorId.putIfAbsent(nodo.getId(), indice) != null) {
                throw new IllegalStateException(
                        "El grafo contiene IDs de nodo duplicados");
            }
            idsNodos.add(nodo.getId());
            etiquetas.add(nodo.getEtiqueta());
        }

        for (Arista arista : grafo.getAristas()) {
            Integer indiceOrigen = indicesPorId.get(arista.getOrigen().getId());
            Integer indiceDestino = indicesPorId.get(arista.getDestino().getId());

            if (indiceOrigen == null || indiceDestino == null) {
                throw new IllegalStateException(
                        "Una arista referencia un nodo que no está en el grafo");
            }

            valores[indiceOrigen][indiceDestino] = arista.getPeso();
            if (!grafo.isDirigido()) {
                valores[indiceDestino][indiceOrigen] = arista.getPeso();
            }
        }

        return new MatrizAdyacencia(
                grafo.isDirigido(),
                idsNodos,
                etiquetas,
                valores);
    }

    // Crea un grafo nuevo a partir de una matriz de adyacencia.
    // Entrada: matriz; salida: grafo con nodos y aristas representados en ella.
    public Grafo desdeMatriz(MatrizAdyacencia matriz) {
        if (matriz == null) {
            throw new IllegalArgumentException("La matriz no puede ser null");
        }

        Grafo grafo = new Grafo(matriz.isDirigido());
        List<Nodo> nodos = prepararNodos(matriz, grafo);
        List<Arista> aristas = prepararAristas(matriz, nodos);
        grafo.reemplazarContenido(matriz.isDirigido(), nodos, aristas);
        return grafo;
    }

    // Aplica una matriz a un grafo y devuelve los IDs de los nodos recién agregados.
    // Entrada: matriz y grafo destino; salida: conjunto inmutable de IDs nuevos.
    public Set<Integer> aplicarMatriz(
            MatrizAdyacencia matriz,
            Grafo destino) {
        if (matriz == null || destino == null) {
            throw new IllegalArgumentException(
                    "La matriz y el grafo destino no pueden ser null");
        }

        Set<Integer> idsExistentes = new HashSet<>();
        for (Nodo nodo : destino.getNodos()) {
            idsExistentes.add(nodo.getId());
        }

        List<Nodo> nodos = prepararNodos(matriz, destino);
        List<Arista> aristas = prepararAristas(matriz, nodos);
        Set<Integer> idsNuevos = new LinkedHashSet<>();

        for (Integer id : matriz.getIdsNodos()) {
            if (!idsExistentes.contains(id)) {
                idsNuevos.add(id);
            }
        }

        destino.reemplazarContenido(matriz.isDirigido(), nodos, aristas);
        return Set.copyOf(idsNuevos);
    }

    // Convierte un grafo en una lista de adyacencia con una fila por nodo.
    // Entrada: grafo; salida: mapa inmutable de nodos y vecinos con sus pesos.
    public Map<Integer, Map<Integer, Double>> aListaAdyacencia(Grafo grafo) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser null");
        }

        Map<Integer, Map<Integer, Double>> lista = new LinkedHashMap<>();
        for (Nodo nodo : grafo.getNodos()) {
            lista.put(nodo.getId(), new LinkedHashMap<>());
        }

        for (Arista arista : grafo.getAristas()) {
            int idOrigen = arista.getOrigen().getId();
            int idDestino = arista.getDestino().getId();
            Map<Integer, Double> vecinosOrigen = lista.get(idOrigen);
            Map<Integer, Double> vecinosDestino = lista.get(idDestino);

            if (vecinosOrigen == null || vecinosDestino == null) {
                throw new IllegalStateException(
                        "Una arista referencia un nodo que no está en el grafo");
            }

            vecinosOrigen.put(idDestino, arista.getPeso());
            if (!grafo.isDirigido()) {
                vecinosDestino.put(idOrigen, arista.getPeso());
            }
        }

        Map<Integer, Map<Integer, Double>> resultado = new LinkedHashMap<>();
        for (Map.Entry<Integer, Map<Integer, Double>> entrada : lista.entrySet()) {
            resultado.put(
                    entrada.getKey(),
                    Collections.unmodifiableMap(
                            new LinkedHashMap<>(entrada.getValue())));
        }

        return Collections.unmodifiableMap(resultado);
    }

    // Crea un grafo desde una lista de adyacencia y conserva los IDs indicados.
    // Entrada: lista de adyacencia y dirección; salida: grafo convertido.
    public Grafo desdeLista(
            Map<Integer, Map<Integer, Double>> lista,
            boolean dirigido) {
        if (lista == null) {
            throw new IllegalArgumentException(
                    "La lista de adyacencia no puede ser null");
        }

        Set<Integer> idsOrdenados = new LinkedHashSet<>();
        for (Map.Entry<Integer, Map<Integer, Double>> entrada : lista.entrySet()) {
            Integer idOrigen = entrada.getKey();
            Map<Integer, Double> vecinos = entrada.getValue();

            if (idOrigen == null || vecinos == null) {
                throw new IllegalArgumentException(
                        "La lista no puede contener IDs ni filas null");
            }

            idsOrdenados.add(idOrigen);
            for (Map.Entry<Integer, Double> vecino : vecinos.entrySet()) {
                if (vecino.getKey() == null || vecino.getValue() == null) {
                    throw new IllegalArgumentException(
                            "Los vecinos y sus pesos no pueden ser null");
                }
                idsOrdenados.add(vecino.getKey());
            }
        }

        Map<Integer, Nodo> nodosPorId = new LinkedHashMap<>();
        List<Nodo> nodos = new ArrayList<>();
        for (Integer id : idsOrdenados) {
            Nodo nodo = new Nodo(id, "N" + id, 0, 0);
            nodos.add(nodo);
            nodosPorId.put(id, nodo);
        }

        List<Arista> aristas = new ArrayList<>();
        Set<Long> paresNoDirigidos = new HashSet<>();
        int siguienteAristaId = 0;

        for (Map.Entry<Integer, Map<Integer, Double>> entrada : lista.entrySet()) {
            int idOrigen = entrada.getKey();

            for (Map.Entry<Integer, Double> vecino : entrada.getValue().entrySet()) {
                int idDestino = vecino.getKey();
                double peso = vecino.getValue();

                if (peso == 0.0) {
                    continue;
                }

                if (!dirigido) {
                    Map<Integer, Double> filaReversa = lista.get(idDestino);
                    if (filaReversa != null
                            && filaReversa.containsKey(idOrigen)
                            && Double.compare(
                                    filaReversa.get(idOrigen),
                                    peso) != 0) {
                        throw new IllegalArgumentException(
                                "Los pesos de una arista no dirigida deben coincidir");
                    }

                    int menorId = Math.min(idOrigen, idDestino);
                    int mayorId = Math.max(idOrigen, idDestino);
                    long clavePar = (((long) menorId) << 32)
                            | (mayorId & 0xffffffffL);

                    if (!paresNoDirigidos.add(clavePar)) {
                        continue;
                    }
                }

                aristas.add(new Arista(
                        siguienteAristaId++,
                        nodosPorId.get(idOrigen),
                        nodosPorId.get(idDestino),
                        peso));
            }
        }

        Grafo grafo = new Grafo(dirigido);
        grafo.reemplazarContenido(dirigido, nodos, aristas);
        return grafo;
    }

    // Prepara nodos con las etiquetas de la matriz y conserva posiciones existentes.
    // Entrada: matriz y grafo destino; salida: lista de nodos en orden matricial.
    private List<Nodo> prepararNodos(
            MatrizAdyacencia matriz,
            Grafo destino) {
        if (matriz == null || destino == null) {
            throw new IllegalArgumentException(
                    "La matriz y el grafo destino no pueden ser null");
        }

        Map<Integer, Nodo> existentesPorId = new HashMap<>();
        for (Nodo nodo : destino.getNodos()) {
            existentesPorId.put(nodo.getId(), nodo);
        }

        List<Nodo> nodos = new ArrayList<>();
        for (int indice = 0; indice < matriz.size(); indice++) {
            int id = matriz.getIdsNodos().get(indice);
            Nodo existente = existentesPorId.get(id);
            double x = existente == null ? 0 : existente.getX();
            double y = existente == null ? 0 : existente.getY();

            nodos.add(new Nodo(
                    id,
                    matriz.getEtiqueta(indice),
                    x,
                    y));
        }

        return nodos;
    }

    // Prepara aristas desde los valores distintos de cero de la matriz.
    // Entrada: matriz y nodos correspondientes; salida: lista de aristas.
    private List<Arista> prepararAristas(
            MatrizAdyacencia matriz,
            List<Nodo> nodos) {
        if (matriz == null || nodos == null) {
            throw new IllegalArgumentException(
                    "La matriz y la lista de nodos no pueden ser null");
        }
        if (nodos.size() != matriz.size()) {
            throw new IllegalArgumentException(
                    "La cantidad de nodos no coincide con el tamaño de la matriz");
        }

        Map<Integer, Nodo> nodosPorId = new HashMap<>();
        for (Nodo nodo : nodos) {
            if (nodosPorId.putIfAbsent(nodo.getId(), nodo) != null) {
                throw new IllegalArgumentException(
                        "La lista de nodos contiene IDs duplicados");
            }
        }

        List<Integer> idsNodos = matriz.getIdsNodos();
        List<Arista> aristas = new ArrayList<>();
        int siguienteAristaId = 0;

        for (int fila = 0; fila < matriz.size(); fila++) {
            int primeraColumna = matriz.isDirigido() ? 0 : fila;

            for (int columna = primeraColumna;
                    columna < matriz.size();
                    columna++) {
                double peso = matriz.getValor(fila, columna);

                if (!matriz.isDirigido() && fila != columna) {
                    double pesoReverso = matriz.getValor(columna, fila);
                    boolean ambosCero = peso == 0.0 && pesoReverso == 0.0;

                    if (!ambosCero && Double.compare(peso, pesoReverso) != 0) {
                        throw new IllegalArgumentException(
                                "La matriz de un grafo no dirigido debe ser simétrica");
                    }
                }

                if (peso == 0.0) {
                    continue;
                }

                Nodo origen = nodosPorId.get(idsNodos.get(fila));
                Nodo destino = nodosPorId.get(idsNodos.get(columna));
                if (origen == null || destino == null) {
                    throw new IllegalArgumentException(
                            "Falta un nodo requerido por la matriz");
                }

                aristas.add(new Arista(
                        siguienteAristaId++,
                        origen,
                        destino,
                        peso));
            }
        }

        return aristas;
    }
}
