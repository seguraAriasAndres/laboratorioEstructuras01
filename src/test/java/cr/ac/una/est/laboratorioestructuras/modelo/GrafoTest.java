package cr.ac.una.est.laboratorioestructuras.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

class GrafoTest {

    @Test
    void reconectarActualizaPesoSinPerderIdentidadNiDuplicar() {
        Grafo g = new Grafo(false);
        g.reiniciar(false, 2);
        Arista original = g.conectar(0, 1, 2);
        assertSame(original, g.conectar(1, 0, -3));
        assertEquals(1, g.getAristas().size());
        assertEquals(-3, original.getPeso());
        g.reiniciar(true, 2);
        g.conectar(0, 1, 1);
        g.conectar(1, 0, 2);
        assertEquals(2, g.getAristas().size());
    }

    @Test
    void datosInvalidosNoModificanElGrafoNiConsumenIds() {
        Grafo g = new Grafo(true);
        assertThrows(IllegalArgumentException.class, () -> g.agregarNodo(null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> g.agregarNodo("X", Double.NaN, 0));
        assertEquals(0, g.agregarNodo("A", 0, 0).getId());
        assertThrows(IllegalArgumentException.class, () -> g.conectar(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> g.conectar(0, 0, Double.POSITIVE_INFINITY));
        assertEquals(0, g.conectar(0, 0, 1).getId());
        assertThrows(IllegalArgumentException.class, () -> g.moverNodo(0, 2, Double.NaN));
        assertEquals(0, g.buscarNodo(0).orElseThrow().getX());
    }

    @Test
    void reemplazoAceptaVistasPropiasYRechazaAristasParalelasSinCambiarEstado() {
        Grafo g = new Grafo(false);
        Nodo a = g.agregarNodo("A", 1, 2);
        Nodo b = g.agregarNodo("B", 3, 4);
        Arista e = g.conectar(0, 1, 2);
        g.reemplazarContenido(false, g.getNodos(), g.getAristas());
        assertEquals(2, g.getNodos().size());
        assertEquals(1, g.getAristas().size());
        assertThrows(IllegalArgumentException.class, () -> g.reemplazarContenido(false,
                List.of(a, b), List.of(e, new Arista(2, b, a, 7))));
        assertEquals(2, g.getAristas().getFirst().getPeso());
    }

    @Test
    void matrizRechazaNoFinitosYAsimetriaYCopiaSusDatos() {
        double[][] valores = {{0, 2}, {2, 0}};
        MatrizAdyacencia m = new MatrizAdyacencia(false, List.of(4, 8), List.of("A", "B"), valores);
        valores[0][1] = 99;
        assertEquals(2, m.getValor(0, 1));
        MatrizAdyacencia editada = m.conValor(0, 1, -5);
        assertEquals(-5, editada.getValor(1, 0));
        assertEquals(2, m.getValor(1, 0));
        assertThrows(IllegalArgumentException.class, () -> m.conValor(0, 0, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new MatrizAdyacencia(false,
                List.of(0, 1), List.of("A", "B"), new double[][]{{0, 1}, {0, 0}}));
    }

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
