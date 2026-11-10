package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.Arista;
import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.modelo.Nodo;
import edu.princeton.cs.algs4.Draw;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

//Draw se encarga de pintar y JavaFX nos ayuda a detectar los clics.
public class PanelDibujo extends Pane {
    private static final double RADIO_NODO = 22;
    private final Map<Integer, Group> nodosVisuales = new HashMap<>();
    private final Map<Integer, Group> aristasVisuales = new HashMap<>();
    private final Set<Integer> nodosResaltados = new HashSet<>();
    private final ImageView imagen = new ImageView();
    private Grafo grafoActual;
    private Draw draw;
    private int anchoDraw, altoDraw;
    private boolean cerrado;

    public PanelDibujo() {
        setMinSize(320, 240);
        setPrefSize(720, 560);
        setStyle("-fx-background-color: white; -fx-border-color: #b7c2cc;");
        imagen.setMouseTransparent(true);
        imagen.setViewOrder(2);
        getChildren().add(imagen);
        Rectangle limite = new Rectangle();
        limite.widthProperty().bind(widthProperty());
        limite.heightProperty().bind(heightProperty());
        setClip(limite);
        widthProperty().addListener((o, anterior, actual) -> redibujar());
        heightProperty().addListener((o, anterior, actual) -> redibujar());
    }

    public void reconciliar(Grafo grafo) {
        if (grafo == null) throw new IllegalArgumentException("El grafo no puede ser null");
        grafoActual = grafo;
        Set<Integer> ids = new HashSet<>();
        grafo.getNodos().forEach(n -> ids.add(n.getId()));
        nodosVisuales.keySet().removeIf(id -> !ids.contains(id));
        nodosResaltados.retainAll(ids);
        aristasVisuales.clear();
        List<javafx.scene.Node> hijos = new ArrayList<>();
        hijos.add(imagen);
        for (Arista arista : grafo.getAristas()) {
            Group zona = crearAristaVisual(arista, grafo.isDirigido());
            zona.setViewOrder(1);
            aristasVisuales.put(arista.getId(), zona);
            hijos.add(zona);
        }
        for (Nodo nodo : grafo.getNodos()) {
            //Reutilizamos el mismo objeto para que el arrastre siga funcionando al redibujar.
            Group zona = nodosVisuales.computeIfAbsent(nodo.getId(), id -> crearNodoVisual(nodo));
            zona.setLayoutX(nodo.getX());
            zona.setLayoutY(nodo.getY());
            hijos.add(zona);
        }
        getChildren().removeIf(hijo -> !hijos.contains(hijo));
        for (javafx.scene.Node hijo : hijos) {
            if (hijo.getParent() != this) getChildren().add(hijo);
        }
        redibujar();
    }

    public void resaltar(List<Integer> orden) {
        if (orden == null) throw new IllegalArgumentException("El recorrido no puede ser null");
        nodosResaltados.clear();
        nodosResaltados.addAll(orden);
        redibujar();
    }

    public void limpiarResaltado() {
        if (!nodosResaltados.isEmpty()) {
            nodosResaltados.clear();
            redibujar();
        }
    }

    public OptionalInt idNodoDe(javafx.scene.Node objeto) {
        for (javafx.scene.Node actual = objeto; actual != null; actual = actual.getParent()) {
            if (actual.getUserData() instanceof Integer id && nodosVisuales.get(id) == actual)
                return OptionalInt.of(id);
        }
        return OptionalInt.empty();
    }

    public OptionalInt idAristaDe(javafx.scene.Node objeto) {
        for (javafx.scene.Node actual = objeto; actual != null; actual = actual.getParent()) {
            if (actual.getUserData() instanceof AristaReferencia ref && aristasVisuales.get(ref.id()) == actual)
                return OptionalInt.of(ref.id());
        }
        return OptionalInt.empty();
    }

    public Point2D posicionLocal(MouseEvent evento) {
        if (evento == null) throw new IllegalArgumentException("El evento no puede ser null");
        return sceneToLocal(evento.getSceneX(), evento.getSceneY());
    }

    public void instalarManejador(EventHandler<MouseEvent> manejador) {
        if (manejador == null) throw new IllegalArgumentException("El manejador no puede ser null");
        addEventHandler(MouseEvent.MOUSE_PRESSED, manejador);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, manejador);
        addEventHandler(MouseEvent.MOUSE_RELEASED, manejador);
        addEventHandler(MouseEvent.MOUSE_CLICKED, manejador);
    }

    public void retirarManejador(EventHandler<MouseEvent> manejador) {
        if (manejador == null) return;
        removeEventHandler(MouseEvent.MOUSE_PRESSED, manejador);
        removeEventHandler(MouseEvent.MOUSE_DRAGGED, manejador);
        removeEventHandler(MouseEvent.MOUSE_RELEASED, manejador);
        removeEventHandler(MouseEvent.MOUSE_CLICKED, manejador);
    }

    protected Group crearNodoVisual(Nodo nodo) {
        Group grupo = new Group(new Circle(RADIO_NODO, Color.TRANSPARENT));
        grupo.setUserData(nodo.getId());
        return grupo;
    }

    protected Group crearAristaVisual(Arista arista, boolean dirigido) {
        Trazo trazo = geometria(arista, dirigido);
        Polyline zona = new Polyline();
        for (Point2D p : trazo.puntos()) zona.getPoints().addAll(p.getX(), p.getY());
        zona.setFill(null);
        zona.setStroke(Color.TRANSPARENT);
        zona.setStrokeWidth(12);
        Text peso = new Text(trazo.etiqueta().getX(), trazo.etiqueta().getY(), trazo.peso());
        peso.setOpacity(0);
        Group grupo = new Group(zona, peso);
        grupo.setPickOnBounds(false);
        grupo.setUserData(new AristaReferencia(arista.getId()));
        return grupo;
    }

    //Usamos la misma forma para dibujar una arista y para detectar los clics sobre ella.
    private Trazo geometria(Arista arista, boolean dirigido) {
        Point2D a = new Point2D(arista.getOrigen().getX(), arista.getOrigen().getY());
        Point2D b = new Point2D(arista.getDestino().getX(), arista.getDestino().getY());
        List<Point2D> puntos = new ArrayList<>();
        if (a.distance(b) < 1) {
            //Si dos nodos están en el mismo lugar, dibujamos una vuelta para no dividir entre cero.
            for (int i = 0; i <= 48; i++) {
                double angulo = Math.PI / 4 + 2 * Math.PI * i / 48;
                puntos.add(a.add(18 * Math.cos(angulo), -38 + 18 * Math.sin(angulo)));
            }
            return new Trazo(List.copyOf(puntos), a.add(30, -48), formato(arista.getPeso()), dirigido);
        }
        Point2D normal = new Point2D(-(b.getY() - a.getY()), b.getX() - a.getX()).normalize();
        boolean reciproca = dirigido && grafoActual != null && grafoActual.getAristas().stream()
                .anyMatch(e -> e.getOrigen() == arista.getDestino() && e.getDestino() == arista.getOrigen());
        Point2D control = a.midpoint(b).add(normal.multiply(reciproca ? 55 : 0));
        Point2D inicio = a.add(control.subtract(a).normalize().multiply(RADIO_NODO));
        Point2D fin = b.add(control.subtract(b).normalize().multiply(RADIO_NODO));
        for (int i = 0; i <= 32; i++) {
            double t = i / 32.0;
            puntos.add(inicio.multiply((1-t)*(1-t)).add(control.multiply(2*t*(1-t))).add(fin.multiply(t*t)));
        }
        return new Trazo(List.copyOf(puntos), puntos.get(16).add(normal.multiply(10)), formato(arista.getPeso()), dirigido);
    }

    private void redibujar() {
        if (grafoActual == null || cerrado) return;
        int ancho = Math.max(1, (int) Math.ceil(getWidth() > 0 ? getWidth() : getPrefWidth()));
        int alto = Math.max(1, (int) Math.ceil(getHeight() > 0 ? getHeight() : getPrefHeight()));
        //Le pasamos copias de los datos a Swing para que no toque el modelo ni los controles de JavaFX.
        List<Vertice> vertices = grafoActual.getNodos().stream()
                .map(n -> new Vertice(n.getX(), n.getY(), n.getEtiqueta(), nodosResaltados.contains(n.getId()))).toList();
        List<Trazo> trazos = grafoActual.getAristas().stream().map(a -> geometria(a, grafoActual.isDirigido())).toList();
        AtomicReference<BufferedImage> resultado = new AtomicReference<>();
        enSwing(() -> resultado.set(pintar(ancho, alto, vertices, trazos)));
        BufferedImage raster = resultado.get();
        WritableImage salida = new WritableImage(ancho, alto);
        int[] pixeles = raster.getRGB(0, 0, ancho, alto, null, 0, ancho);
        salida.getPixelWriter().setPixels(0, 0, ancho, alto, PixelFormat.getIntArgbInstance(), pixeles, 0, ancho);
        imagen.setImage(salida);
    }

    private BufferedImage pintar(int ancho, int alto, List<Vertice> vertices, List<Trazo> trazos) {
        if (draw == null) {
            draw = new Draw();
            draw.setVisible(false);
            draw.enableDoubleBuffering();
        }
        if (ancho != anchoDraw || alto != altoDraw) {
            draw.setCanvasSize(ancho, alto);
            anchoDraw = ancho;
            altoDraw = alto;
        }
        draw.setXscale(0, ancho);
        draw.setYscale(0, alto);
        draw.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        draw.setPenRadius(0.003);
        draw.clear(java.awt.Color.WHITE);
        draw.setPenColor(java.awt.Color.DARK_GRAY);
        for (Trazo trazo : trazos) {
            List<Point2D> puntos = trazo.puntos();
            for (int i = 1; i < puntos.size(); i++) {
                Point2D a = puntos.get(i-1), b = puntos.get(i);
                draw.line(a.getX(), alto-a.getY(), b.getX(), alto-b.getY());
            }
            if (trazo.dirigido()) {
                Point2D punta = puntos.getLast();
                Point2D direccion = punta.subtract(puntos.get(puntos.size()-2)).normalize();
                Point2D base = punta.subtract(direccion.multiply(11));
                Point2D normal = new Point2D(-direccion.getY(), direccion.getX()).multiply(5);
                Point2D p = base.add(normal), q = base.subtract(normal);
                draw.filledPolygon(new double[]{punta.getX(), p.getX(), q.getX()},
                        new double[]{alto-punta.getY(), alto-p.getY(), alto-q.getY()});
            }
            draw.text(trazo.etiqueta().getX(), alto-trazo.etiqueta().getY(), trazo.peso());
        }
        for (Vertice v : vertices) {
            draw.setPenColor(v.resaltado() ? new java.awt.Color(255, 215, 0) : new java.awt.Color(173, 216, 230));
            draw.filledCircle(v.x(), alto-v.y(), RADIO_NODO);
            draw.setPenColor(java.awt.Color.DARK_GRAY);
            draw.circle(v.x(), alto-v.y(), RADIO_NODO);
            draw.text(v.x(), alto-v.y(), v.etiqueta());
        }
        draw.show();
        //Sacamos la imagen que pintó Draw para mostrarla dentro de nuestra ventana.
        Icon icono = draw.getJLabel().getIcon();
        BufferedImage raster = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = raster.createGraphics();
        try { icono.paintIcon(draw.getJLabel(), g, 0, 0); }
        finally { g.dispose(); }
        return raster;
    }

    public void dispose() {
        cerrado = true;
        grafoActual = null;
        enSwing(() -> {
            if (draw != null) {
                Window ventana = SwingUtilities.getWindowAncestor(draw.getJLabel());
                if (ventana != null) ventana.dispose();
                draw = null;
            }
        });
    }

    private static void enSwing(Runnable accion) {
        try {
            if (SwingUtilities.isEventDispatchThread()) accion.run();
            else SwingUtilities.invokeAndWait(accion);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió el dibujo del grafo", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("No se pudo dibujar el grafo con Draw", e.getCause());
        }
    }

    private static String formato(double valor) { return Double.toString(valor); }
    private record AristaReferencia(int id) { }
    private record Vertice(double x, double y, String etiqueta, boolean resaltado) { }
    private record Trazo(List<Point2D> puntos, Point2D etiqueta, String peso, boolean dirigido) { }
}
