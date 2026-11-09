package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.modelo.Nodo;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * Presenta el resultado de un recorrido del grafo.
 */
public class PanelRecorrido extends VBox {

    private final Label titulo = new Label("Recorrido");
    private final TextArea resultado = new TextArea();

    // Construye el panel que muestra el orden de un recorrido.
    // Entrada: ninguna; salida: panel de recorrido inicializado.
    public PanelRecorrido() {
        setSpacing(6);
        resultado.setEditable(false);
        resultado.setWrapText(true);
        resultado.setPrefRowCount(5);
        getChildren().addAll(titulo, resultado);
    }

    // Muestra el tipo de recorrido y sus nodos en orden.
    // Entrada: tipo, IDs visitados y grafo; salida: no devuelve valor.
    public void mostrar(String tipo, List<Integer> orden, Grafo grafo) {
        if (tipo == null || orden == null || grafo == null) {
            throw new IllegalArgumentException(
                    "El tipo, orden y grafo son obligatorios");
        }

        titulo.setText("Recorrido " + tipo);
        StringBuilder texto = new StringBuilder();

        for (Integer id : orden) {
            if (!texto.isEmpty()) {
                texto.append(" → ");
            }

            Nodo nodo = grafo.buscarNodo(id).orElse(null);
            if (nodo == null) {
                texto.append(id);
            } else {
                texto.append(nodo.getEtiqueta())
                        .append(" (#")
                        .append(id)
                        .append(")");
            }
        }

        resultado.setText(texto.toString());
    }

    // Limpia el título y el resultado mostrado.
    // Entrada: ninguna; salida: no devuelve valor.
    public void limpiar() {
        titulo.setText("Recorrido");
        resultado.clear();
    }
}
