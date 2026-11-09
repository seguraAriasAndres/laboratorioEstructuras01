package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.MatrizAdyacencia;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Presenta y permite editar una matriz de adyacencia.
 */
public class PanelMatriz extends ScrollPane {

    private TextField[][] celdas = new TextField[0][0];
    private MatrizAdyacencia matrizMostrada;
    private boolean actualizando;
    private Consumer<EdicionCelda> receptor;

    private final GridPane tabla = new GridPane();
    private final Label mensajeError = new Label();

    // Construye el panel desplazable que contiene la matriz editable.
    // Entrada: ninguna; salida: panel de matriz inicializado.
    public PanelMatriz() {
        tabla.setHgap(6);
        tabla.setVgap(6);
        tabla.setPadding(new Insets(8));

        mensajeError.setWrapText(true);
        mensajeError.setStyle("-fx-text-fill: #b00020;");

        VBox contenido = new VBox(6, mensajeError, tabla);
        contenido.setPadding(new Insets(6));

        setContent(contenido);
        setFitToWidth(true);
        setPannable(true);
        setMinViewportWidth(300);
        setPrefViewportHeight(360);
    }

    // Muestra las etiquetas y los valores de una matriz.
    // Entrada: matriz que se desea presentar; salida: no devuelve valor.
    public void mostrar(MatrizAdyacencia matriz) {
        actualizando = true;
        try {
            matrizMostrada = matriz;
            tabla.getChildren().clear();
            mensajeError.setText("");

            if (matriz == null) {
                celdas = new TextField[0][0];
                return;
            }

            int cantidad = matriz.size();
            celdas = new TextField[cantidad][cantidad];

            Label esquina = new Label("Nodo");
            esquina.setMinWidth(72);
            esquina.setAlignment(Pos.CENTER);
            tabla.add(esquina, 0, 0);

            for (int columna = 0; columna < cantidad; columna++) {
                int id = matriz.getIdsNodos().get(columna);
                Label encabezado = new Label(
                        matriz.getEtiqueta(columna) + " (#" + id + ")");
                encabezado.setMinWidth(78);
                encabezado.setAlignment(Pos.CENTER);
                tabla.add(encabezado, columna + 1, 0);
            }

            for (int fila = 0; fila < cantidad; fila++) {
                int id = matriz.getIdsNodos().get(fila);
                Label encabezado = new Label(
                        matriz.getEtiqueta(fila) + " (#" + id + ")");
                encabezado.setMinWidth(72);
                encabezado.setAlignment(Pos.CENTER_LEFT);
                tabla.add(encabezado, 0, fila + 1);

                for (int columna = 0; columna < cantidad; columna++) {
                    TextField celda = crearCelda(
                            fila,
                            columna,
                            matriz.getValor(fila, columna));
                    celdas[fila][columna] = celda;
                    tabla.add(celda, columna + 1, fila + 1);
                }
            }
        } finally {
            actualizando = false;
        }
    }

    // Registra el consumidor que recibirá las ediciones confirmadas.
    // Entrada: consumidor de ediciones o null para desconectarlo; salida: no devuelve valor.
    public void alEditarCelda(Consumer<EdicionCelda> receptor) {
        this.receptor = receptor;
    }

    // Muestra un mensaje de error relacionado con una edición.
    // Entrada: texto del error; salida: no devuelve valor.
    public void mostrarError(String mensaje) {
        mensajeError.setText(mensaje == null ? "" : mensaje);
    }

    // Desconecta los campos para que no envíen más ediciones.
    // Entrada: ninguna; salida: no devuelve valor.
    public void desconectar() {
        receptor = null;
        for (TextField[] fila : celdas) {
            for (TextField celda : fila) {
                celda.setOnAction(null);
                celda.focusedProperty().addListener(
                        (observable, anterior, enfocado) -> {
                        });
            }
        }
        celdas = new TextField[0][0];
        matrizMostrada = null;
        tabla.getChildren().clear();
    }

    // Crea un campo de texto y conecta sus eventos de confirmación.
    // Entrada: fila, columna y valor inicial; salida: campo editable.
    protected TextField crearCelda(int fila, int columna, double valor) {
        TextField celda = new TextField(Double.toString(valor));
        celda.setPrefColumnCount(7);
        celda.setAlignment(Pos.CENTER_RIGHT);

        celda.setOnAction(evento ->
                confirmarEdicion(fila, columna, celda.getText()));

        celda.focusedProperty().addListener(
                (observable, teniaFoco, tieneFoco) -> {
                    if (!tieneFoco) {
                        confirmarEdicion(fila, columna, celda.getText());
                    }
                });

        return celda;
    }

    // Convierte y envía el valor escrito para una celda.
    // Entrada: fila, columna y texto escrito; salida: no devuelve valor.
    private void confirmarEdicion(int fila, int columna, String texto) {
        if (actualizando || matrizMostrada == null
                || fila >= matrizMostrada.size()
                || columna >= matrizMostrada.size()) {
            return;
        }

        TextField celda = celdas[fila][columna];
        try {
            double valor = Double.parseDouble(texto.trim());
            if (!Double.isFinite(valor)) {
                throw new IllegalArgumentException(
                        "El valor debe ser un número finito");
            }

            double valorActual = matrizMostrada.getValor(fila, columna);
            if (Double.compare(valor, valorActual) == 0) {
                mensajeError.setText("");
                celda.setText(Double.toString(valorActual));
                return;
            }

            if (receptor == null) {
                throw new IllegalArgumentException(
                        "No hay un controlador conectado a la matriz");
            }

            mensajeError.setText("");
            receptor.accept(new EdicionCelda(fila, columna, valor));
        } catch (IllegalArgumentException excepcion) {
            mensajeError.setText(excepcion.getMessage());
            celda.setText(Double.toString(
                    matrizMostrada.getValor(fila, columna)));
        }
    }
}
