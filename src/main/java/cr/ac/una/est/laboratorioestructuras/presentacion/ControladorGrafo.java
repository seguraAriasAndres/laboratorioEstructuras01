package cr.ac.una.est.laboratorioestructuras.presentacion;

import cr.ac.una.est.laboratorioestructuras.modelo.Grafo;
import cr.ac.una.est.laboratorioestructuras.modelo.GrafoListener;
import cr.ac.una.est.laboratorioestructuras.modelo.MatrizAdyacencia;
import cr.ac.una.est.laboratorioestructuras.modelo.Nodo;
import cr.ac.una.est.laboratorioestructuras.modelo.TipoCambio;
import cr.ac.una.est.laboratorioestructuras.servicio.GrafoService;
import cr.ac.una.est.laboratorioestructuras.servicio.RecorridoService;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

/**
 * Coordina las acciones de la interfaz con los servicios del grafo.
 */
public class ControladorGrafo implements GrafoListener {

    private final GrafoService servicio;
    private final RecorridoService recorridos;
    private final PanelDibujo dibujo;
    private final PanelMatriz matriz;
    private final PanelRecorrido recorrido;
    private final EventHandler<MouseEvent> manejadorMouse = this::handle;

    private MatrizAdyacencia matrizActual;
    private boolean actualizando;
    private boolean modoMatriz = true;
    private HerramientaEdicion herramienta = HerramientaEdicion.SELECCIONAR;
    private Integer origenConexion;
    private Integer nodoArrastrado;
    private Point2D desplazamientoArrastre = Point2D.ZERO;
    private boolean huboArrastre;
    private double pesoNuevaArista = 1.0;

    // Crea el controlador con servicios y paneles ya construidos.
    // Entrada: servicio, recorridos y paneles; salida: controlador inicializado.
    public ControladorGrafo(
            GrafoService servicio,
            RecorridoService recorridos,
            PanelDibujo dibujo,
            PanelMatriz matriz,
            PanelRecorrido recorrido) {
        if (servicio == null || recorridos == null || dibujo == null
                || matriz == null || recorrido == null) {
            throw new IllegalArgumentException(
                    "El controlador requiere todos sus servicios y paneles");
        }

        this.servicio = servicio;
        this.recorridos = recorridos;
        this.dibujo = dibujo;
        this.matriz = matriz;
        this.recorrido = recorrido;
    }

    // Conecta los paneles y escucha los cambios del grafo.
    // Entrada: ninguna; salida: no devuelve valor.
    public void inicializar() {
        servicio.getGrafo().addListener(this);
        matriz.alEditarCelda(this::editarCelda);
        dibujo.instalarManejador(manejadorMouse);
        matriz.setEditable(modoMatriz);
        actualizarVistas(TipoCambio.TOPOLOGIA);
    }

    // Activa el flujo de matriz o el flujo de dibujo.
    // Entrada: true para Matriz a Grafo o false para Dibujar a Matriz; salida: no devuelve valor.
    public void setModoMatriz(boolean modoMatriz) {
        this.modoMatriz = modoMatriz;
        matriz.setEditable(modoMatriz);

        if (modoMatriz) {
            origenConexion = null;
            nodoArrastrado = null;
            huboArrastre = false;
        }
    }

    // Actualiza las vistas cuando el grafo notifica un cambio.
    // Entrada: grafo cambiado y tipo de cambio; salida: no devuelve valor.
    @Override
    public void grafoCambiado(Grafo grafo, TipoCambio tipo) {
        if (grafo != servicio.getGrafo() || actualizando) {
            return;
        }
        actualizarVistas(tipo);
    }

    // Dirige cada evento del mouse al proceso correspondiente.
    // Entrada: evento del mouse; salida: no devuelve valor.
    public void handle(MouseEvent evento) {
        if (modoMatriz || evento == null) {
            return;
        }

        if (evento.getEventType() == MouseEvent.MOUSE_PRESSED) {
            procesarPulsacion(evento);
        } else if (evento.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            procesarArrastre(evento);
        } else if (evento.getEventType() == MouseEvent.MOUSE_RELEASED) {
            procesarLiberacion(evento);
        } else if (evento.getEventType() == MouseEvent.MOUSE_CLICKED) {
            procesarClick(evento);
        }
    }

    // Reinicia el grafo y crea la cantidad solicitada de nodos.
    // Entrada: dirección y cantidad; salida: no devuelve valor.
    public void crearNuevo(boolean dirigido, int cantidad) {
        actualizando = true;
        try {
            servicio.crearNuevo(
                    dirigido,
                    cantidad,
                    anchoDisponible(),
                    altoDisponible());
        } finally {
            actualizando = false;
        }
        actualizarVistas(TipoCambio.TOPOLOGIA);
    }

    // Aplica el valor editado a la matriz actual y actualiza el grafo.
    // Entrada: edición con fila, columna y valor; salida: no devuelve valor.
    public void editarCelda(EdicionCelda edicion) {
        if (edicion == null || matrizActual == null) {
            throw new IllegalArgumentException(
                    "Se necesita una matriz y una edición");
        }
        if (edicion.fila() >= matrizActual.size()
                || edicion.columna() >= matrizActual.size()) {
            throw new IllegalArgumentException(
                    "La celda editada está fuera de la matriz");
        }

        cargarMatriz(matrizActual.conValor(
                edicion.fila(),
                edicion.columna(),
                edicion.valor()));
    }

    // Aplica una matriz externa al grafo actual.
    // Entrada: matriz de adyacencia; salida: no devuelve valor.
    public void cargarMatriz(MatrizAdyacencia matriz) {
        if (matriz == null) {
            throw new IllegalArgumentException("La matriz no puede ser null");
        }

        actualizando = true;
        try {
            servicio.aplicarMatriz(
                    matriz,
                    anchoDisponible(),
                    altoDisponible());
        } finally {
            actualizando = false;
        }

        actualizarVistas(TipoCambio.TOPOLOGIA);
    }

    // Importa una lista de adyacencia al grafo actual.
    // Entrada: lista y dirección; salida: no devuelve valor.
    public void cargarLista(
            Map<Integer, Map<Integer, Double>> lista,
            boolean dirigido) {
        actualizando = true;
        try {
            servicio.importarLista(
                    lista,
                    dirigido,
                    anchoDisponible(),
                    altoDisponible());
        } finally {
            actualizando = false;
        }

        actualizarVistas(TipoCambio.TOPOLOGIA);
    }

    // Ejecuta BFS y muestra y resalta su orden.
    // Entrada: ID inicial; salida: no devuelve valor.
    public void ejecutarBfs(int inicioId) {
        var orden = recorridos.bfs(servicio.getGrafo(), inicioId);
        recorrido.mostrar("BFS", orden, servicio.getGrafo());
        dibujo.resaltar(orden);
    }

    // Ejecuta DFS y muestra y resalta su orden.
    // Entrada: ID inicial; salida: no devuelve valor.
    public void ejecutarDfs(int inicioId) {
        var orden = recorridos.dfs(servicio.getGrafo(), inicioId);
        recorrido.mostrar("DFS", orden, servicio.getGrafo());
        dibujo.resaltar(orden);
    }

    // Distribuye todos los nodos en el área de dibujo.
    // Entrada: ninguna; salida: no devuelve valor.
    public void redistribuir() {
        servicio.redistribuir(anchoDisponible(), altoDisponible());
    }

    // Cambia la herramienta activa y cancela acciones pendientes.
    // Entrada: herramienta elegida; salida: no devuelve valor.
    public void setHerramienta(HerramientaEdicion herramienta) {
        if (herramienta == null) {
            throw new IllegalArgumentException(
                    "La herramienta no puede ser null");
        }

        this.herramienta = herramienta;
        origenConexion = null;
        nodoArrastrado = null;
        huboArrastre = false;
    }

    // Define el peso usado para las nuevas aristas.
    // Entrada: peso finito distinto de cero; salida: no devuelve valor.
    public void setPesoNuevaArista(double peso) {
        if (!Double.isFinite(peso) || peso == 0.0) {
            throw new IllegalArgumentException(
                    "El peso debe ser finito y distinto de cero");
        }
        pesoNuevaArista = peso;
    }

    // Desconecta los paneles y deja de escuchar cambios del grafo.
    // Entrada: ninguna; salida: no devuelve valor.
    public void dispose() {
        servicio.getGrafo().removeListener(this);
        dibujo.retirarManejador(manejadorMouse);
        matriz.desconectar();
    }

    // Actualiza el dibujo y, cuando cambia la topología, también la matriz.
    // Entrada: tipo de cambio; salida: no devuelve valor.
    private void actualizarVistas(TipoCambio tipo) {
        actualizando = true;
        try {
            dibujo.reconciliar(servicio.getGrafo());

            if (tipo == TipoCambio.TOPOLOGIA) {
                matrizActual = servicio.exportarMatriz();
                matriz.mostrar(matrizActual);
                recorrido.limpiar();
                dibujo.limpiarResaltado();
            }
        } finally {
            actualizando = false;
        }
    }

    // Inicia el arrastre de un nodo cuando está activa la selección.
    // Entrada: evento de pulsación; salida: no devuelve valor.
    private void procesarPulsacion(MouseEvent evento) {
        if (herramienta != HerramientaEdicion.SELECCIONAR) {
            nodoArrastrado = null;
            return;
        }

        javafx.scene.Node objeto = obtenerObjeto(evento);
        OptionalInt idNodo = dibujo.idNodoDe(objeto);
        if (idNodo.isEmpty()) {
            nodoArrastrado = null;
            return;
        }

        nodoArrastrado = idNodo.getAsInt();
        Point2D punto = dibujo.posicionLocal(evento);
        Nodo nodo = servicio.getGrafo()
                .buscarNodo(nodoArrastrado)
                .orElseThrow();
        desplazamientoArrastre = new Point2D(
                punto.getX() - nodo.getX(),
                punto.getY() - nodo.getY());
        huboArrastre = false;
    }

    // Mueve el nodo que se está arrastrando.
    // Entrada: evento de arrastre; salida: no devuelve valor.
    private void procesarArrastre(MouseEvent evento) {
        if (nodoArrastrado == null) {
            return;
        }

        Point2D punto = dibujo.posicionLocal(evento);
        double x = punto.getX() - desplazamientoArrastre.getX();
        double y = punto.getY() - desplazamientoArrastre.getY();
        servicio.moverNodo(nodoArrastrado, x, y);
        huboArrastre = true;
        evento.consume();
    }

    // Finaliza el arrastre actual sin activar una acción de clic.
    // Entrada: evento de liberación; salida: no devuelve valor.
    private void procesarLiberacion(MouseEvent evento) {
        if (nodoArrastrado != null) {
            nodoArrastrado = null;
            evento.consume();
        }
    }

    // Ejecuta la herramienta activa sobre el elemento seleccionado.
    // Entrada: evento de clic; salida: no devuelve valor.
    private void procesarClick(MouseEvent evento) {
        if (huboArrastre) {
            huboArrastre = false;
            return;
        }

        javafx.scene.Node objeto = obtenerObjeto(evento);
        OptionalInt idNodo = dibujo.idNodoDe(objeto);
        OptionalInt idArista = dibujo.idAristaDe(objeto);

        switch (herramienta) {
            case CREAR_NODO -> crearNodoEnClick(evento, idNodo, idArista);
            case CONECTAR -> conectarNodoEnClick(idNodo);
            case ELIMINAR -> eliminarElementoEnClick(idNodo, idArista);
            case SELECCIONAR -> {
            }
        }
    }

    // Crea un nodo en el área vacía donde ocurrió el clic.
    // Entrada: evento y detecciones de nodo y arista; salida: no devuelve valor.
    private void crearNodoEnClick(
            MouseEvent evento,
            OptionalInt idNodo,
            OptionalInt idArista) {
        if (idNodo.isPresent() || idArista.isPresent()) {
            return;
        }

        Point2D punto = dibujo.posicionLocal(evento);
        servicio.agregarNodo(
                siguienteEtiqueta(),
                punto.getX(),
                punto.getY());
        evento.consume();
    }

    // Conecta dos nodos seleccionados en secuencia.
    // Entrada: ID del nodo clicado o vacío; salida: no devuelve valor.
    private void conectarNodoEnClick(OptionalInt idNodo) {
        if (idNodo.isEmpty()) {
            origenConexion = null;
            return;
        }

        int idSeleccionado = idNodo.getAsInt();
        if (origenConexion == null) {
            origenConexion = idSeleccionado;
            return;
        }

        servicio.conectar(
                origenConexion,
                idSeleccionado,
                pesoNuevaArista);
        origenConexion = null;
    }

    // Elimina el nodo o la arista seleccionados.
    // Entrada: IDs detectados por el dibujo; salida: no devuelve valor.
    private void eliminarElementoEnClick(
            OptionalInt idNodo,
            OptionalInt idArista) {
        if (idNodo.isPresent()) {
            servicio.eliminarNodo(idNodo.getAsInt());
        } else if (idArista.isPresent()) {
            servicio.eliminarArista(idArista.getAsInt());
        }
    }

    // Obtiene el objeto JavaFX que originó el evento.
    // Entrada: evento del mouse; salida: nodo JavaFX o null.
    private javafx.scene.Node obtenerObjeto(MouseEvent evento) {
        if (evento.getTarget() instanceof javafx.scene.Node objeto) {
            return objeto;
        }
        return null;
    }

    // Busca una etiqueta N disponible para el siguiente nodo.
    // Entrada: ninguna; salida: etiqueta no usada con formato N seguido de número.
    private String siguienteEtiqueta() {
        Set<String> etiquetasUsadas = new HashSet<>();
        for (Nodo nodo : servicio.getGrafo().getNodos()) {
            etiquetasUsadas.add(nodo.getEtiqueta());
        }

        int indice = 1;
        while (etiquetasUsadas.contains("N" + indice)) {
            indice++;
        }
        return "N" + indice;
    }

    // Devuelve el ancho actual o preferido del panel de dibujo.
    // Entrada: ninguna; salida: ancho disponible para distribuir nodos.
    private double anchoDisponible() {
        return dibujo.getWidth() > 0
                ? dibujo.getWidth()
                : dibujo.getPrefWidth();
    }

    // Devuelve el alto actual o preferido del panel de dibujo.
    // Entrada: ninguna; salida: alto disponible para distribuir nodos.
    private double altoDisponible() {
        return dibujo.getHeight() > 0
                ? dibujo.getHeight()
                : dibujo.getPrefHeight();
    }
}
