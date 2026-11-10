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

//Acá juntamos las operaciones que la ventana le pide al grafo.
public class GrafoService {

    private final Grafo grafo;
    private final ConversorRepresentaciones conversor;
    private final DistribuidorCircular distribuidor;

    //Guardamos el grafo y las clases que nos ayudan a convertirlo y acomodarlo.
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

    //Nos da el grafo con el que estamos trabajando.
    public Grafo getGrafo() {
        return grafo;
    }

    //Creamos un grafo desde cero y acomodamos sus nodos.
    public void crearNuevo(
            boolean dirigido,
            int cantidad,
            double ancho,
            double alto) {
        validarDimensiones(ancho, alto);
        grafo.reiniciar(dirigido, cantidad);
        redistribuir(ancho, alto);
    }

    //Agregamos un nodo con su nombre y posición.
    public Nodo agregarNodo(String etiqueta, double x, double y) {
        return grafo.agregarNodo(etiqueta, x, y);
    }

    //Unimos los dos nodos con el peso que nos pasaron.
    public Arista conectar(int origenId, int destinoId, double peso) {
        return grafo.conectar(origenId, destinoId, peso);
    }

    //Borramos el nodo junto con sus conexiones.
    public void eliminarNodo(int id) {
        grafo.eliminarNodo(id);
    }

    //Borramos la arista usando su ID.
    public void eliminarArista(int id) {
        grafo.eliminarArista(id);
    }

    //Cambiamos de lugar el nodo.
    public void moverNodo(int id, double x, double y) {
        grafo.moverNodo(id, x, y);
    }

    //Sacamos la matriz del grafo actual.
    public MatrizAdyacencia exportarMatriz() {
        return conversor.aMatriz(grafo);
    }

    //Cargamos la matriz y buscamos un lugar para los nodos nuevos.
    public void aplicarMatriz(
            MatrizAdyacencia matriz,
            double ancho,
            double alto) {
        validarDimensiones(ancho, alto);
        Set<Integer> idsNuevos = conversor.aplicarMatriz(matriz, grafo);
        posicionarNuevos(idsNuevos, ancho, alto);
    }

    //Sacamos la lista de vecinos y pesos del grafo.
    public Map<Integer, Map<Integer, Double>> exportarLista() {
        return conversor.aListaAdyacencia(grafo);
    }

    //Cargamos una lista de adyacencia y acomodamos los nodos nuevos.
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

    //Acomodamos todos los nodos en un círculo.
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

    //Acomodamos solo los nodos nuevos, siguiendo el orden que tienen en el grafo.
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

    //Revisamos que el tamaño del área sea válido.
    private void validarDimensiones(double ancho, double alto) {
        if (!Double.isFinite(ancho) || !Double.isFinite(alto)
                || ancho < 0 || alto < 0) {
            throw new IllegalArgumentException(
                    "El ancho y el alto deben ser finitos y no negativos");
        }
    }
}
