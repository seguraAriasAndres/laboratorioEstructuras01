package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.Arista;
import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.modelo.Nodo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

/**
 * Dibuja los nodos y las aristas de un grafo.
 */
public class PanelDibujo extends Pane {

    private static final double RADIO_NODO = 22.0;

    private final Map<Integer, Group> nodosVisuales = new HashMap<>();
    private final Map<Integer, Group> aristasVisuales = new HashMap<>();
    private final Set<Integer> nodosResaltados = new HashSet<>();

    // Construye el área donde se dibuja el grafo.
    // Entrada: ninguna; salida: panel de dibujo inicializado.
    public PanelDibujo() {
        setMinSize(320, 240);
        setPrefSize(720, 560);
        setStyle("-fx-background-color: white; -fx-border-color: #b7c2cc;");
    }

    // Reconstruye el dibujo a partir del estado actual del grafo.
    // Entrada: grafo que se desea mostrar; salida: no devuelve valor.
    public void reconciliar(Grafo grafo) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser null");
        }

        getChildren().clear();
        nodosVisuales.clear();
        aristasVisuales.clear();

        for (Arista arista : grafo.getAristas()) {
            Group visual = crearAristaVisual(arista, grafo.isDirigido());
            aristasVisuales.put(arista.getId(), visual);
            getChildren().add(visual);
        }

        for (Nodo nodo : grafo.getNodos()) {
            Group visual = crearNodoVisual(nodo);
            nodosVisuales.put(nodo.getId(), visual);
            getChildren().add(visual);
        }

        actualizarColoresNodos();
    }

    // Resalta los nodos que aparecen en un recorrido.
    // Entrada: IDs ordenados del recorrido; salida: no devuelve valor.
    public void resaltar(List<Integer> orden) {
        if (orden == null) {
            throw new IllegalArgumentException(
                    "El orden del recorrido no puede ser null");
        }

        nodosResaltados.clear();
        nodosResaltados.addAll(orden);
        actualizarColoresNodos();
    }

    // Retira el resaltado de todos los nodos.
    // Entrada: ninguna; salida: no devuelve valor.
    public void limpiarResaltado() {
        nodosResaltados.clear();
        actualizarColoresNodos();
    }

    // Busca el ID del nodo asociado al elemento visual seleccionado.
    // Entrada: elemento JavaFX; salida: ID del nodo o OptionalInt vacío.
    public OptionalInt idNodoDe(javafx.scene.Node objeto) {
        javafx.scene.Node actual = objeto;

        while (actual != null) {
            Object dato = actual.getUserData();
            if (dato instanceof Integer id && nodosVisuales.containsKey(id)) {
                return OptionalInt.of(id);
            }
            actual = actual.getParent();
        }

        return OptionalInt.empty();
    }

    // Busca el ID de la arista asociada al elemento visual seleccionado.
    // Entrada: elemento JavaFX; salida: ID de la arista o OptionalInt vacío.
    public OptionalInt idAristaDe(javafx.scene.Node objeto) {
        javafx.scene.Node actual = objeto;

        while (actual != null) {
            Object dato = actual.getUserData();
            if (dato instanceof AristaReferencia referencia
                    && aristasVisuales.containsKey(referencia.id())) {
                return OptionalInt.of(referencia.id());
            }
            actual = actual.getParent();
        }

        return OptionalInt.empty();
    }

    // Convierte la posición del evento al sistema de coordenadas del panel.
    // Entrada: evento del mouse; salida: punto local del panel.
    public Point2D posicionLocal(MouseEvent evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser null");
        }
        return sceneToLocal(evento.getSceneX(), evento.getSceneY());
    }

    // Registra un manejador para los eventos de interacción del mouse.
    // Entrada: manejador de eventos; salida: no devuelve valor.
    public void instalarManejador(EventHandler<MouseEvent> manejador) {
        if (manejador == null) {
            throw new IllegalArgumentException(
                    "El manejador no puede ser null");
        }

        addEventHandler(MouseEvent.MOUSE_PRESSED, manejador);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, manejador);
        addEventHandler(MouseEvent.MOUSE_RELEASED, manejador);
        addEventHandler(MouseEvent.MOUSE_CLICKED, manejador);
    }

    // Retira un manejador previamente registrado.
    // Entrada: manejador que se desea retirar; salida: no devuelve valor.
    public void retirarManejador(EventHandler<MouseEvent> manejador) {
        if (manejador == null) {
            return;
        }

        removeEventHandler(MouseEvent.MOUSE_PRESSED, manejador);
        removeEventHandler(MouseEvent.MOUSE_DRAGGED, manejador);
        removeEventHandler(MouseEvent.MOUSE_RELEASED, manejador);
        removeEventHandler(MouseEvent.MOUSE_CLICKED, manejador);
    }

    // Crea el grupo visual para un nodo y le asigna su ID.
    // Entrada: nodo del modelo; salida: grupo visual del nodo.
    protected Group crearNodoVisual(Nodo nodo) {
        Group grupo = new Group();
        Circle circulo = new Circle(RADIO_NODO, Color.LIGHTBLUE);
        circulo.setStroke(Color.DARKSLATEGRAY);

        Text etiqueta = new Text(nodo.getEtiqueta());
        etiqueta.setTextOrigin(VPos.CENTER);
        etiqueta.setX(-etiqueta.getLayoutBounds().getWidth() / 2.0);
        etiqueta.setY(0);

        grupo.getChildren().addAll(circulo, etiqueta);
        grupo.setUserData(nodo.getId());
        grupo.setLayoutX(nodo.getX());
        grupo.setLayoutY(nodo.getY());
        return grupo;
    }

    // Crea el grupo visual para una arista y, si corresponde, su flecha.
    // Entrada: arista y dirección del grafo; salida: grupo visual de la arista.
    protected Group crearAristaVisual(Arista arista, boolean dirigido) {
        Group grupo = new Group();
        grupo.setUserData(new AristaReferencia(arista.getId()));

        double origenX = arista.getOrigen().getX();
        double origenY = arista.getOrigen().getY();
        double destinoX = arista.getDestino().getX();
        double destinoY = arista.getDestino().getY();

        if (arista.getOrigen().getId() == arista.getDestino().getId()) {
            Circle bucle = new Circle(
                    origenX,
                    origenY - RADIO_NODO - 7,
                    14,
                    Color.TRANSPARENT);
            bucle.setStroke(Color.DARKSLATEGRAY);

            Text peso = new Text(
                    origenX + 16,
                    origenY - RADIO_NODO - 12,
                    Double.toString(arista.getPeso()));
            grupo.getChildren().addAll(bucle, peso);
            return grupo;
        }

        double diferenciaX = destinoX - origenX;
        double diferenciaY = destinoY - origenY;
        double distancia = Math.hypot(diferenciaX, diferenciaY);
        double unidadX = diferenciaX / distancia;
        double unidadY = diferenciaY / distancia;

        double inicioX = origenX + unidadX * RADIO_NODO;
        double inicioY = origenY + unidadY * RADIO_NODO;
        double puntaX = destinoX - unidadX * RADIO_NODO;
        double puntaY = destinoY - unidadY * RADIO_NODO;

        Line linea = new Line(inicioX, inicioY, puntaX, puntaY);
        linea.setStroke(Color.DARKSLATEGRAY);
        grupo.getChildren().add(linea);

        Text peso = new Text(
                (inicioX + puntaX) / 2.0,
                (inicioY + puntaY) / 2.0 - 6,
                Double.toString(arista.getPeso()));
        grupo.getChildren().add(peso);

        if (dirigido) {
            double baseX = puntaX - unidadX * 10;
            double baseY = puntaY - unidadY * 10;
            double normalX = -unidadY * 5;
            double normalY = unidadX * 5;

            Polygon flecha = new Polygon(
                    puntaX, puntaY,
                    baseX + normalX, baseY + normalY,
                    baseX - normalX, baseY - normalY);
            flecha.setFill(Color.DARKSLATEGRAY);
            grupo.getChildren().add(flecha);
        }

        return grupo;
    }

    // Actualiza el color de cada nodo según su estado de resaltado.
    // Entrada: ninguna; salida: no devuelve valor.
    private void actualizarColoresNodos() {
        for (Map.Entry<Integer, Group> entrada : nodosVisuales.entrySet()) {
            Circle circulo = (Circle) entrada.getValue().getChildren().get(0);
            Color color = nodosResaltados.contains(entrada.getKey())
                    ? Color.GOLD
                    : Color.LIGHTBLUE;
            circulo.setFill(color);
        }
    }

    private record AristaReferencia(int id) {
    }
}
