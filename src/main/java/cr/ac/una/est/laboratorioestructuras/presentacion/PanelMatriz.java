package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.MatrizAdyacencia;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

//Acá mostramos la matriz y dejamos editar sus celdas cuando toca.
public class PanelMatriz extends ScrollPane {

    private TextField[][] celdas = new TextField[0][0];
    private MatrizAdyacencia matrizMostrada;
    private boolean actualizando;
    private boolean editable = true;
    private Consumer<EdicionCelda> receptor;

    private final GridPane tabla = new GridPane();
    private final Label mensajeError = new Label();

    //Armamos la tabla y le ponemos barras para poder movernos por ella.
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

    //Llenamos la tabla con los nombres de los nodos y los pesos.
    public void mostrar(MatrizAdyacencia matriz) {
        actualizando = true;
        try {
            MatrizAdyacencia anterior = matrizMostrada;
            matrizMostrada = matriz;
            if (mismaEstructura(anterior, matriz)) {
                mensajeError.setText("");
                for (int fila = 0; fila < matriz.size(); fila++) {
                    for (int columna = 0; columna < matriz.size(); columna++) {
                        celdas[fila][columna].setText(Double.toString(matriz.getValor(fila, columna)));
                    }
                }
                return;
            }
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

    //Dejamos las celdas editables o solo para verlas.
    public void setEditable(boolean editable) {
        this.editable = editable;
        for (TextField[] fila : celdas) {
            for (TextField celda : fila) {
                celda.setEditable(editable);
            }
        }
    }

    //Guardamos a quién hay que pasarle los cambios de las celdas.
    public void alEditarCelda(Consumer<EdicionCelda> receptor) {
        this.receptor = receptor;
    }

    //Mostramos el mensaje si hubo un problema al editar.
    public void mostrarError(String mensaje) {
        mensajeError.setText(mensaje == null ? "" : mensaje);
    }

    //Desconectamos los campos para que ya no manden cambios.
    public void desconectar() {
        receptor = null;
        for (TextField[] fila : celdas) {
            for (TextField celda : fila) {
                celda.setOnAction(null);
                celda.setEditable(false);
            }
        }
        celdas = new TextField[0][0];
        matrizMostrada = null;
        tabla.getChildren().clear();
    }

    //Armamos una celda y le decimos qué hacer al confirmar lo escrito.
    protected TextField crearCelda(int fila, int columna, double valor) {
        TextField celda = new TextField(Double.toString(valor));
        celda.setPrefColumnCount(7);
        celda.setAlignment(Pos.CENTER_RIGHT);
        celda.setEditable(editable);

        celda.setOnAction(evento -> {
            if (esCeldaActual(fila, columna, celda)) confirmarEdicion(fila, columna, celda.getText());
        });

        celda.focusedProperty().addListener(
                (observable, teniaFoco, tieneFoco) -> {
                    if (!tieneFoco && esCeldaActual(fila, columna, celda)) {
                        confirmarEdicion(fila, columna, celda.getText());
                    }
                });

        return celda;
    }

    private boolean esCeldaActual(int fila, int columna, TextField celda) {
        return fila < celdas.length && columna < celdas[fila].length && celdas[fila][columna] == celda;
    }

    private boolean mismaEstructura(MatrizAdyacencia a, MatrizAdyacencia b) {
        if (a == null || b == null || !a.getIdsNodos().equals(b.getIdsNodos())) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.getEtiqueta(i).equals(b.getEtiqueta(i))) return false;
        }
        return true;
    }

    //Pasamos el texto a número y mandamos el cambio al controlador.
    private void confirmarEdicion(int fila, int columna, String texto) {
        if (!editable || actualizando || matrizMostrada == null
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
