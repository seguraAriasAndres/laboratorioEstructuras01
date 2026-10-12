package cr.ac.una.est.laboratorioestructuras.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrafoTest {

    @Test
    void agregaConectaYElimina() {
        Grafo grafo = new Grafo(false);
        Nodo a = grafo.agregarNodo("A", 0, 0);
        Nodo b = grafo.agregarNodo("B", 10, 0);
        Arista arista = grafo.conectar(a.getId(), b.getId(), 2.5);

        assertEquals(2, grafo.getNodos().size());
        assertEquals(1, grafo.getAristas().size());
        assertEquals(2.5, arista.getPeso());
        assertTrue(grafo.buscarNodo(a.getId()).isPresent());

        grafo.eliminarNodo(a.getId());
        assertTrue(grafo.buscarNodo(a.getId()).isEmpty());
        assertTrue(grafo.getAristas().isEmpty(), "las aristas del nodo eliminado deben irse");
    }

    @Test
    void notificaSegunElTipoDeCambio() {
        Grafo grafo = new Grafo(true);
        List<TipoCambio> recibidos = new ArrayList<>();
        grafo.addListener((g, tipo) -> recibidos.add(tipo));

        Nodo a = grafo.agregarNodo("A", 0, 0);
        grafo.moverNodo(a.getId(), 5, 5);
        grafo.moverNodos(Map.of(a.getId(), new Posicion(7, 7)));

        assertEquals(List.of(TipoCambio.TOPOLOGIA, TipoCambio.POSICION,
                TipoCambio.POSICION), recibidos);
        assertEquals(7, grafo.buscarNodo(a.getId()).get().getX());
    }

    @Test
    void conectarAUnNodoInexistenteFalla() {
        Grafo grafo = new Grafo(true);
        Nodo a = grafo.agregarNodo("A", 0, 0);
        assertThrows(IllegalArgumentException.class,
                () -> grafo.conectar(a.getId(), 99, 1));
    }

    @Test
    void matrizEsInmutableYValidaSuForma() {
        MatrizAdyacencia m = new MatrizAdyacencia(true,
                List.of(0, 1), List.of("A", "B"),
                new double[][]{{0, 1}, {0, 0}});

        MatrizAdyacencia m2 = m.conValor(0, 1, 3);

        assertEquals(1, m.getValor(0, 1), "la original no cambia");
        assertEquals(3, m2.getValor(0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new MatrizAdyacencia(true, List.of(0, 1), List.of("A"),
                        new double[][]{{0, 1}}));
    }
}
