package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.*;
import cr.ac.una.est.laboratorioestructuras.servicio.*;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class InterfazTest {
    @BeforeAll static void iniciarJavaFx() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Requiere escritorio para JavaFX y Draw");
        CountDownLatch listo = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); listo.countDown(); });
        assertTrue(listo.await(20, TimeUnit.SECONDS));
    }

    @AfterAll static void cerrarJavaFx() { Platform.exit(); }

    @Test void editarMatrizSinPerderCamposYRechazarTextoInvalido() throws Exception {
        enFx(() -> {
            try (Vista v = new Vista()) {
                v.c.crearNuevo(false, 2);
                TextField celda = v.celdas().get(1);
                celda.setText("2.5");
                celda.fireEvent(new ActionEvent());
                assertEquals(2.5, v.s.exportarMatriz().getValor(0, 1));
                assertEquals("2.5", v.celdas().get(2).getText());
                assertSame(celda, v.celdas().get(1));
                celda.setText("NaN");
                celda.fireEvent(new ActionEvent());
                assertEquals("2.5", celda.getText());
                assertEquals(2.5, v.s.exportarMatriz().getValor(0, 1));
                celda.setText("0");
                celda.fireEvent(new ActionEvent());
                assertTrue(v.s.getGrafo().getAristas().isEmpty());
                v.c.crearNuevo(false, 1);
                celda.setText("7");
                celda.fireEvent(new ActionEvent()); //Probamos qué pasa si llega un evento de una celda que ya quitamos.
                assertTrue(v.s.getGrafo().getAristas().isEmpty());
            }
        });
    }

    @Test void dibujoActualizaMatrizYConservaNodoDuranteArrastre() throws Exception {
        enFx(() -> {
            try (Vista v = new Vista()) {
                v.c.setModoMatriz(false);
                v.s.agregarNodo("A", 150, 150);
                v.s.agregarNodo("B", 350, 200);
                Group nodo = v.nodo(0);
                v.c.setHerramienta(HerramientaEdicion.CONECTAR);
                v.evento(MouseEvent.MOUSE_CLICKED, nodo, 150, 150);
                v.evento(MouseEvent.MOUSE_CLICKED, v.nodo(1), 350, 200);
                assertEquals(1, v.s.exportarMatriz().getValor(0, 1));
                v.c.setHerramienta(HerramientaEdicion.SELECCIONAR);
                v.evento(MouseEvent.MOUSE_PRESSED, nodo, 150, 150);
                v.evento(MouseEvent.MOUSE_DRAGGED, nodo, 220, 240);
                v.evento(MouseEvent.MOUSE_DRAGGED, nodo, 260, 250);
                v.evento(MouseEvent.MOUSE_RELEASED, nodo, 260, 250);
                assertSame(nodo, v.nodo(0));
                assertEquals(260, v.s.getGrafo().buscarNodo(0).orElseThrow().getX(), 0.01);
                assertEquals(250, v.s.getGrafo().buscarNodo(0).orElseThrow().getY(), 0.01);
                v.c.setHerramienta(HerramientaEdicion.ELIMINAR);
                v.evento(MouseEvent.MOUSE_CLICKED, v.nodo(1), 350, 200);
                assertEquals(1, v.s.getGrafo().getNodos().size());
                assertTrue(v.s.getGrafo().getAristas().isEmpty());
            }
        });
    }

    @Test void reiniciarCancelaConexionPendienteYRecorridoSeLimpia() throws Exception {
        enFx(() -> {
            try (Vista v = new Vista()) {
                v.c.crearNuevo(true, 3);
                v.c.setModoMatriz(false);
                v.c.setHerramienta(HerramientaEdicion.CONECTAR);
                v.evento(MouseEvent.MOUSE_CLICKED, v.nodo(2), 0, 0);
                v.c.crearNuevo(true, 2);
                v.evento(MouseEvent.MOUSE_CLICKED, v.nodo(0), 0, 0);
                assertTrue(v.s.getGrafo().getAristas().isEmpty());
                v.evento(MouseEvent.MOUSE_CLICKED, v.nodo(1), 0, 0);
                v.c.ejecutarBfs(0);
                TextArea resultado = (TextArea) v.r.getChildren().get(1);
                assertTrue(resultado.getText().contains("N1 (#0) → N2 (#1)"));
                v.s.moverNodo(0, 200, 180);
                assertFalse(resultado.getText().isEmpty());
                v.s.eliminarArista(v.s.getGrafo().getAristas().getFirst().getId());
                assertTrue(resultado.getText().isEmpty());
            }
        });
    }

    @Test void drawPintaPesosBuclesReciprocasSuperposicionesYResaltado() throws Exception {
        enFx(() -> {
            try (Vista v = new Vista()) {
                v.s.agregarNodo("A", 180, 160);
                v.s.agregarNodo("B", 420, 260);
                v.c.cargarMatriz(new MatrizAdyacencia(true, List.of(0, 1), List.of("A", "B"),
                        new double[][]{{3, 2.5}, {-4, 0}}));
                ImageView imagen = (ImageView) v.d.getChildren().getFirst();
                javafx.scene.paint.Color color = imagen.getImage().getPixelReader().getColor(180, 170);
                assertTrue(color.getBlue() > color.getRed(), "Draw debe pintar el relleno celeste");
                v.c.ejecutarDfs(0);
                color = imagen.getImage().getPixelReader().getColor(180, 170);
                assertTrue(color.getRed() > 0.9 && color.getBlue() < 0.1, "El recorrido se pinta dorado");
                v.s.moverNodo(1, 180, 160); //Ponemos los dos nodos en el mismo lugar para revisar que el dibujo no falle.
                assertNotNull(imagen.getImage());
                v.s.moverNodo(1, 420, 260);
                guardarCaptura(v.stage.getScene(), "target/validacion-grafo.png");
            }
        });
    }

    @Test void aplicacionPrincipalArrancaYCierra() throws Exception {
        enFx(() -> {
            AplicacionPrincipal app = new AplicacionPrincipal();
            Stage stage = new Stage();
            try {
                app.start(stage);
                assertTrue(stage.isShowing());
                assertEquals("Laboratorio de grafos", stage.getTitle());
                List<ToggleButton> botones = stage.getScene().getRoot().lookupAll(".toggle-button").stream()
                        .map(ToggleButton.class::cast).toList();
                ToggleButton modo = botones.stream().filter(b -> b.getText().equals("Dibujar → Matriz"))
                        .findFirst().orElseThrow();
                modo.fire();
                assertTrue(modo.isSelected());
                ToggleButton crear = botones.stream().filter(b -> b.getText().equals("Crear nodo"))
                        .findFirst().orElseThrow();
                assertFalse(crear.isDisabled());
                crear.fire();
                assertTrue(crear.isSelected());
                crear.fire();
                assertTrue(crear.isSelected(), "La herramienta activa no se puede deseleccionar");
            } finally {
                app.stop();
                stage.close();
            }
        });
    }

    private static void enFx(Runnable accion) throws Exception {
        FutureTask<Void> tarea = new FutureTask<>(accion, null);
        Platform.runLater(tarea);
        tarea.get(40, TimeUnit.SECONDS);
    }

    private static void guardarCaptura(Scene escena, String archivo) {
        try {
            WritableImage imagen = escena.snapshot(null);
            int ancho = (int) imagen.getWidth(), alto = (int) imagen.getHeight();
            int[] pixeles = new int[ancho * alto];
            imagen.getPixelReader().getPixels(0, 0, ancho, alto, PixelFormat.getIntArgbInstance(), pixeles, 0, ancho);
            BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            salida.setRGB(0, 0, ancho, alto, pixeles, 0, ancho);
            Files.createDirectories(Path.of(archivo).getParent());
            ImageIO.write(salida, "png", Path.of(archivo).toFile());
        } catch (Exception e) { throw new AssertionError(e); }
    }

    private static class Vista implements AutoCloseable {
        final GrafoService s = new GrafoService(new Grafo(false), new ConversorRepresentaciones(), new DistribuidorCircular(60));
        final PanelDibujo d = new PanelDibujo();
        final PanelMatriz m = new PanelMatriz();
        final PanelRecorrido r = new PanelRecorrido();
        final ControladorGrafo c = new ControladorGrafo(s, new RecorridoService(), d, m, r);
        final Stage stage = new Stage();
        Vista() {
            BorderPane raiz = new BorderPane(d);
            raiz.setRight(new VBox(m, r));
            stage.setScene(new Scene(raiz, 1050, 600));
            stage.show();
            raiz.applyCss(); raiz.layout();
            c.inicializar();
            c.inicializar(); //Lo llamamos otra vez para comprobar que no se conecte todo dos veces.
        }
        List<TextField> celdas() {
            GridPane tabla = (GridPane) ((VBox) m.getContent()).getChildren().get(1);
            return tabla.getChildren().stream().filter(TextField.class::isInstance).map(TextField.class::cast).toList();
        }
        Group nodo(int id) {
            return (Group) d.getChildren().stream().filter(n -> d.idNodoDe(n).orElse(-1) == id).findFirst().orElseThrow();
        }
        void evento(EventType<MouseEvent> tipo, Node destino, double x, double y) {
            javafx.geometry.Point2D escena = d.localToScene(x, y);
            MouseEvent evento = new MouseEvent(tipo, escena.getX(), escena.getY(), 0, 0,
                    MouseButton.PRIMARY, 1, false, false, false, false,
                    tipo != MouseEvent.MOUSE_RELEASED, false, false, false, false, true,
                    new PickResult(destino, escena.getX(), escena.getY()));
            Event.fireEvent(destino, evento);
        }
        @Override public void close() { c.dispose(); stage.close(); }
    }
}
