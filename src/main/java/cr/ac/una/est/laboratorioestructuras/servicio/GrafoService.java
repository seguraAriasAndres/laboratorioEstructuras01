package cr.ac.una.est.laboratorioestructuras.servicio;

import cr.ac.una.est.laboratorioestructuras.modelo.Arista;
import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.modelo.MatrizAdyacencia;
import cr.ac.una.est.laboratorioestructuras.modelo.Nodo;
import cr.ac.una.est.laboratorioestructuras.modelo.Posicion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Coordina operaciones de aplicación sobre el grafo.
 */
public class GrafoService {

    private final Grafo grafo;
    private final ConversorRepresentaciones conversor;
    private final DistribuidorCircular distribuidor;

    // Crea el servicio con el grafo y sus colaboradores requeridos.
    // Entrada: grafo, conversor y distribuidor; salida: servicio inicializado.
    public GrafoService(
            Grafo grafo,
            ConversorRepresentaciones conversor,
            DistribuidorCircular distribuidor) {
        if (grafo == null || conversor == null || distribuidor == null) {
            throw new IllegalArgumentException(
                    "El grafo, conversor y distribuidor son obligatorios");
        }

        this.grafo = grafo;
        this.conversor = conversor;
        this.distribuidor = distribuidor;
    }

    // Devuelve el grafo administrado por el servicio.
    // Entrada: ninguna; salida: instancia del grafo.
    public Grafo getGrafo() {
        return grafo;
    }

    // Reinicia el grafo, crea nodos y los distribuye en el área indicada.
    // Entrada: dirección, cantidad y dimensiones; salida: no devuelve valor.
    public void crearNuevo(
            boolean dirigido,
            int cantidad,
            double ancho,
            double alto) {
        validarDimensiones(ancho, alto);
        grafo.reiniciar(dirigido, cantidad);
        redistribuir(ancho, alto);
    }

    // Agrega un nodo con la etiqueta y posición indicadas.
    // Entrada: etiqueta y coordenadas; salida: nodo agregado.
    public Nodo agregarNodo(String etiqueta, double x, double y) {
        return grafo.agregarNodo(etiqueta, x, y);
    }

    // Conecta dos nodos existentes.
    // Entrada: IDs de origen y destino, y peso; salida: arista agregada.
    public Arista conectar(int origenId, int destinoId, double peso) {
        return grafo.conectar(origenId, destinoId, peso);
    }

    // Elimina un nodo y sus aristas incidentes.
    // Entrada: ID del nodo; salida: no devuelve valor.
    public void eliminarNodo(int id) {
        grafo.eliminarNodo(id);
    }

    // Elimina una arista por su ID.
    // Entrada: ID de la arista; salida: no devuelve valor.
    public void eliminarArista(int id) {
        grafo.eliminarArista(id);
    }

    // Cambia la posición de un nodo existente.
    // Entrada: ID y coordenadas nuevas; salida: no devuelve valor.
    public void moverNodo(int id, double x, double y) {
        grafo.moverNodo(id, x, y);
    }

    // Exporta el grafo a una matriz de adyacencia.
    // Entrada: ninguna; salida: matriz que representa el grafo actual.
    public MatrizAdyacencia exportarMatriz() {
        return conversor.aMatriz(grafo);
    }

    // Aplica una matriz y distribuye los nodos que sean nuevos.
    // Entrada: matriz y dimensiones del área; salida: no devuelve valor.
    public void aplicarMatriz(
            MatrizAdyacencia matriz,
            double ancho,
            double alto) {
        validarDimensiones(ancho, alto);
        Set<Integer> idsNuevos = conversor.aplicarMatriz(matriz, grafo);
        posicionarNuevos(idsNuevos, ancho, alto);
    }

    // Exporta el grafo como mapa de listas de adyacencia.
    // Entrada: ninguna; salida: mapa de vecinos y pesos.
    public Map<Integer, Map<Integer, Double>> exportarLista() {
        return conversor.aListaAdyacencia(grafo);
    }

    // Importa una lista de adyacencia y posiciona sus nodos nuevos.
    // Entrada: lista, dirección y dimensiones; salida: no devuelve valor.
    public void importarLista(
            Map<Integer, Map<Integer, Double>> lista,
            boolean dirigido,
            double ancho,
            double alto) {
        validarDimensiones(ancho, alto);
        Grafo importado = conversor.desdeLista(lista, dirigido);
        MatrizAdyacencia matriz = conversor.aMatriz(importado);
        aplicarMatriz(matriz, ancho, alto);
    }

    // Distribuye todos los nodos actuales en forma circular.
    // Entrada: dimensiones del área; salida: no devuelve valor.
    public void redistribuir(double ancho, double alto) {
        validarDimensiones(ancho, alto);

        List<Integer> ids = new ArrayList<>();
        for (Nodo nodo : grafo.getNodos()) {
            ids.add(nodo.getId());
        }

        Map<Integer, Posicion> posiciones =
                distribuidor.calcular(ids, ancho, alto);
        grafo.moverNodos(posiciones);
    }

    // Distribuye los nodos indicados respetando el orden del grafo.
    // Entrada: IDs nuevos y dimensiones del área; salida: no devuelve valor.
    public void posicionarNuevos(
            Set<Integer> ids,
            double ancho,
            double alto) {
        validarDimensiones(ancho, alto);
        if (ids == null) {
            throw new IllegalArgumentException(
                    "El conjunto de IDs no puede ser null");
        }

        List<Integer> idsEnOrden = new ArrayList<>();
        for (Nodo nodo : grafo.getNodos()) {
            if (ids.contains(nodo.getId())) {
                idsEnOrden.add(nodo.getId());
            }
        }

        Map<Integer, Posicion> posiciones =
                distribuidor.calcular(idsEnOrden, ancho, alto);
        grafo.moverNodos(posiciones);
    }

    // Comprueba que las dimensiones sean finitas y no negativas.
    // Entrada: ancho y alto; salida: no devuelve valor o lanza excepción.
    private void validarDimensiones(double ancho, double alto) {
        if (!Double.isFinite(ancho) || !Double.isFinite(alto)
                || ancho < 0 || alto < 0) {
            throw new IllegalArgumentException(
                    "El ancho y el alto deben ser finitos y no negativos");
        }
    }
}
