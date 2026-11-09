package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.servicio.ConversorRepresentaciones;
import cr.ac.una.est.laboratorioestructuras.servicio.DistribuidorCircular;
import cr.ac.una.est.laboratorioestructuras.servicio.GrafoService;
import cr.ac.una.est.laboratorioestructuras.servicio.RecorridoService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Inicia la aplicación JavaFX y ensambla sus componentes.
 */
public class AplicacionPrincipal extends Application {

    private ControladorGrafo controlador;

    // Construye los servicios, paneles y controles de la aplicación.
    // Entrada: ventana principal; salida: interfaz mostrada y grafo inicializado.
    @Override
    public void start(Stage stage) {
        Grafo grafo = new Grafo(false);
        ConversorRepresentaciones conversor =
                new ConversorRepresentaciones();
        DistribuidorCircular distribuidor =
                new DistribuidorCircular(45);
        GrafoService servicio =
                new GrafoService(grafo, conversor, distribuidor);
        RecorridoService recorridos = new RecorridoService();

        PanelDibujo dibujo = new PanelDibujo();
        PanelMatriz matriz = new PanelMatriz();
        PanelRecorrido recorrido = new PanelRecorrido();

        controlador = new ControladorGrafo(
                servicio,
                recorridos,
                dibujo,
                matriz,
                recorrido);
        controlador.inicializar();

        TextField cantidad = new TextField("5");
        cantidad.setPrefColumnCount(4);
        CheckBox dirigido = new CheckBox("Dirigido");
        Button botonNuevo = new Button("Crear grafo");
        botonNuevo.setOnAction(evento -> ejecutarAccion(stage, () ->
                controlador.crearNuevo(
                        dirigido.isSelected(),
                        Integer.parseInt(cantidad.getText().trim()))));

        HBox controlesNuevo = new HBox(
                8,
                new Label("Nodos:"),
                cantidad,
                dirigido,
                botonNuevo);
        controlesNuevo.setPadding(new Insets(6));

        ToggleGroup grupoHerramientas = new ToggleGroup();
        ToggleButton seleccionar = crearBotonHerramienta(
                "Seleccionar",
                HerramientaEdicion.SELECCIONAR,
                grupoHerramientas,
                stage);
        ToggleButton crearNodo = crearBotonHerramienta(
                "Crear nodo",
                HerramientaEdicion.CREAR_NODO,
                grupoHerramientas,
                stage);
        ToggleButton conectar = crearBotonHerramienta(
                "Conectar",
                HerramientaEdicion.CONECTAR,
                grupoHerramientas,
                stage);
        ToggleButton eliminar = crearBotonHerramienta(
                "Eliminar",
                HerramientaEdicion.ELIMINAR,
                grupoHerramientas,
                stage);
        seleccionar.setSelected(true);

        TextField peso = new TextField("1.0");
        peso.setPrefColumnCount(6);
        Button aplicarPeso = new Button("Aplicar peso");
        aplicarPeso.setOnAction(evento -> ejecutarAccion(stage, () ->
                controlador.setPesoNuevaArista(
                        Double.parseDouble(peso.getText().trim()))));

        HBox controlesDibujo = new HBox(
                8,
                seleccionar,
                crearNodo,
                conectar,
                eliminar,
                new Label("Peso:"),
                peso,
                aplicarPeso);
        controlesDibujo.setPadding(new Insets(6));

        TextField inicioId = new TextField("0");
        inicioId.setPrefColumnCount(4);
        Button bfs = new Button("Ejecutar BFS");
        bfs.setOnAction(evento -> ejecutarAccion(stage, () ->
                controlador.ejecutarBfs(
                        Integer.parseInt(inicioId.getText().trim()))));
        Button dfs = new Button("Ejecutar DFS");
        dfs.setOnAction(evento -> ejecutarAccion(stage, () ->
                controlador.ejecutarDfs(
                        Integer.parseInt(inicioId.getText().trim()))));
        Button redistribuir = new Button("Redistribuir");
        redistribuir.setOnAction(evento -> ejecutarAccion(
                stage,
                controlador::redistribuir));

        HBox controlesRecorrido = new HBox(
                8,
                new Label("ID inicial:"),
                inicioId,
                bfs,
                dfs,
                redistribuir);
        controlesRecorrido.setPadding(new Insets(6));

        VBox barra = new VBox(
                controlesNuevo,
                controlesDibujo,
                controlesRecorrido);

        VBox panelLateral = new VBox(
                8,
                new Label("Matriz editable"),
                matriz,
                new Label("Resultado del recorrido"),
                recorrido);
        panelLateral.setPadding(new Insets(8));
        VBox.setVgrow(matriz, Priority.ALWAYS);

        SplitPane contenido = new SplitPane(dibujo, panelLateral);
        contenido.setDividerPositions(0.62);

        BorderPane raiz = new BorderPane();
        raiz.setTop(barra);
        raiz.setCenter(contenido);
        raiz.setPadding(new Insets(8));

        Scene escena = new Scene(raiz, 1320, 820);
        stage.setTitle("Laboratorio de grafos");
        stage.setScene(escena);
        stage.show();

        ejecutarAccion(stage, () -> controlador.crearNuevo(false, 5));
    }

    // Libera los recursos del controlador al cerrar la aplicación.
    // Entrada: ninguna; salida: no devuelve valor.
    @Override
    public void stop() {
        if (controlador != null) {
            controlador.dispose();
        }
    }

    // Crea un botón de herramienta conectado al controlador.
    // Entrada: texto, herramienta, grupo y ventana; salida: botón configurado.
    private ToggleButton crearBotonHerramienta(
            String texto,
            HerramientaEdicion herramienta,
            ToggleGroup grupo,
            Stage stage) {
        ToggleButton boton = new ToggleButton(texto);
        boton.setToggleGroup(grupo);
        boton.setOnAction(evento -> ejecutarAccion(
                stage,
                () -> controlador.setHerramienta(herramienta)));
        return boton;
    }

    // Ejecuta una acción de interfaz y muestra sus errores al usuario.
    // Entrada: ventana y acción; salida: no devuelve valor.
    private void ejecutarAccion(Stage stage, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException excepcion) {
            mostrarError(stage, excepcion.getMessage());
        }
    }

    // Presenta un diálogo con el mensaje de error recibido.
    // Entrada: ventana propietaria y texto del error; salida: no devuelve valor.
    private void mostrarError(Stage stage, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(stage);
        alerta.setTitle("No se pudo completar la acción");
        alerta.setHeaderText(null);
        alerta.setContentText(
                mensaje == null ? "Ocurrió un error." : mensaje);
        alerta.showAndWait();
    }
}
