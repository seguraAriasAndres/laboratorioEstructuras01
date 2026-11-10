package cr.ac.una.est.laboratorioestructuras.servicio;

import cr.ac.una.est.laboratorioestructuras.modelo.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServiciosTest {
    private final ConversorRepresentaciones conversor = new ConversorRepresentaciones();

    @Test void conversionConservaIdsDiscontinuosBuclesYPesos() {
        Grafo g = new Grafo(true);
        Nodo a = g.agregarNodo("A", 50, 60);
        Nodo eliminado = g.agregarNodo("X", 0, 0);
        Nodo b = g.agregarNodo("B", 200, 100);
        g.eliminarNodo(eliminado.getId());
        g.conectar(a.getId(), b.getId(), -2.5);
        g.conectar(b.getId(), a.getId(), 4);
        g.conectar(b.getId(), b.getId(), 3);
        MatrizAdyacencia m = conversor.aMatriz(g);
        assertEquals(List.of(0, 2), m.getIdsNodos());
        assertEquals(-2.5, m.getValor(0, 1));
        assertEquals(4, m.getValor(1, 0));
        assertEquals(3, m.getValor(1, 1));
        Grafo copia = conversor.desdeMatriz(m);
        assertEquals(conversor.aListaAdyacencia(g), conversor.aListaAdyacencia(copia));
        assertEquals("B", copia.buscarNodo(2).orElseThrow().getEtiqueta());
        assertEquals(3, copia.agregarNodo("C", 0, 0).getId());
    }

    @Test void conversionNoDirigidaNoDuplicaAristasYNodosAislados() {
        Map<Integer, Map<Integer, Double>> lista = new LinkedHashMap<>();
        lista.put(7, Map.of(12, 2.0));
        lista.put(12, Map.of(7, 2.0, 12, -3.0));
        lista.put(20, Map.of());
        Grafo g = conversor.desdeLista(lista, false);
        assertEquals(3, g.getNodos().size());
        assertEquals(2, g.getAristas().size());
        assertEquals(lista, conversor.aListaAdyacencia(conversor.desdeMatriz(conversor.aMatriz(g))));
        assertThrows(UnsupportedOperationException.class,
                () -> conversor.aListaAdyacencia(g).get(7).put(20, 1.0));
    }

    @Test void listaRechazaPesosInvalidosYConflictosInclusoCero() {
        assertThrows(IllegalArgumentException.class,
                () -> conversor.desdeLista(Map.of(0, Map.of(1, Double.NaN)), true));
        assertThrows(IllegalArgumentException.class,
                () -> conversor.desdeLista(Map.of(0, Map.of(1, 0.0), 1, Map.of(0, 2.0)), false));
        assertThrows(IllegalArgumentException.class,
                () -> conversor.desdeLista(Map.of(0, Map.of(1, 2.0), 1, Map.of(0, 3.0)), false));
        Grafo unilateral = conversor.desdeLista(Map.of(0, Map.of(1, 2.0)), false);
        assertEquals(2.0, conversor.aListaAdyacencia(unilateral).get(1).get(0));
    }

    @Test void aplicarMatrizConservaInstanciaPosicionesYNotificaUnaVez() {
        Grafo g = new Grafo(false);
        g.agregarNodo("A", 100, 150);
        List<TipoCambio> eventos = new ArrayList<>();
        g.addListener((grafo, tipo) -> eventos.add(tipo));
        MatrizAdyacencia m = new MatrizAdyacencia(true, List.of(0, 8), List.of("Renombrado", "Nuevo"),
                new double[][]{{0, 7}, {0, 0}});
        assertEquals(Set.of(8), conversor.aplicarMatriz(m, g));
        assertTrue(g.isDirigido());
        assertEquals(100, g.buscarNodo(0).orElseThrow().getX());
        assertEquals(150, g.buscarNodo(0).orElseThrow().getY());
        assertEquals("Renombrado", g.buscarNodo(0).orElseThrow().getEtiqueta());
        assertEquals(List.of(TipoCambio.TOPOLOGIA), eventos);
    }

    @Test void servicioPosicionaNuevosYConservaExistentes() {
        Grafo g = new Grafo(false);
        GrafoService s = new GrafoService(g, conversor, new DistribuidorCircular(40));
        s.crearNuevo(false, 1, 600, 400);
        s.moverNodo(0, 75, 90);
        s.importarLista(Map.of(0, Map.of(9, 2.0)), true, 600, 400);
        assertSame(g, s.getGrafo());
        assertEquals(75, g.buscarNodo(0).orElseThrow().getX());
        assertEquals(300, g.buscarNodo(9).orElseThrow().getX());
        assertEquals(200, g.buscarNodo(9).orElseThrow().getY());
        assertThrows(IllegalArgumentException.class, () -> s.crearNuevo(true, 4, Double.NaN, 400));
        assertEquals(2, g.getNodos().size());
    }

    @Test void recorridosRespetanDireccionOrdenCiclosYComponentes() {
        Grafo g = new Grafo(true);
        g.reiniciar(true, 6);
        g.conectar(0, 1, 1); g.conectar(0, 2, 1); g.conectar(1, 3, 1);
        g.conectar(3, 2, 1); g.conectar(2, 4, 1); g.conectar(4, 0, 1); g.conectar(4, 4, 1);
        RecorridoService r = new RecorridoService();
        assertEquals(List.of(0, 1, 2, 3, 4), r.bfs(g, 0));
        assertEquals(List.of(0, 1, 3, 2, 4), r.dfs(g, 0));
        assertEquals(List.of(5), r.bfs(g, 5));
        assertThrows(IllegalArgumentException.class, () -> r.dfs(g, 99));
        Grafo noDirigido = conversor.desdeLista(Map.of(7, Map.of(2, 1.0)), false);
        assertEquals(List.of(2, 7), r.bfs(noDirigido, 2));
    }

    @Test void distribucionSoportaVacioCentroYEspacioReducido() {
        DistribuidorCircular d = new DistribuidorCircular(40);
        assertTrue(d.calcular(List.of(), 10, 10).isEmpty());
        assertEquals(new Posicion(300, 200), d.calcular(List.of(9), 600, 400).get(9));
        Map<Integer, Posicion> p = d.calcular(List.of(7, 2, 9), 600, 400);
        assertEquals(List.of(7, 2, 9), new ArrayList<>(p.keySet()));
        assertEquals(40, p.get(7).y(), 1e-8);
        assertTrue(d.calcular(List.of(1, 2), 10, 10).values().stream().allMatch(v -> v.equals(new Posicion(5, 5))));
        assertThrows(IllegalArgumentException.class, () -> d.calcular(List.of(1, 1), 100, 100));
    }
}
