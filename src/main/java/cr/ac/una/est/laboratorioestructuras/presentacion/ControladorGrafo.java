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
import javafx.scene.input.MouseButton;

//Acá conectamos lo que pasa en la ventana con las operaciones del grafo.
public class ControladorGrafo implements GrafoListener {

    private final GrafoService servicio;
    private final RecorridoService recorridos;
    private final PanelDibujo dibujo;
    private final PanelMatriz matriz;
    private final PanelRecorrido recorrido;
    private final EventHandler<MouseEvent> manejadorMouse = this::handle;

    private MatrizAdyacencia matrizActual;
    private boolean actualizando;
    private boolean inicializado;
    private boolean modoMatriz = true;
    private HerramientaEdicion herramienta = HerramientaEdicion.SELECCIONAR;
    private Integer origenConexion;
    private Integer nodoArrastrado;
    private Point2D desplazamientoArrastre = Point2D.ZERO;
    private boolean huboArrastre;
    private double pesoNuevaArista = 1.0;

    //Guardamos los servicios y los paneles que vamos a usar.
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

    //Conectamos los paneles y nos apuntamos para recibir los cambios del grafo.
    public void inicializar() {
        if (inicializado) return;
        inicializado = true;
        servicio.getGrafo().addListener(this);
        matriz.alEditarCelda(this::editarCelda);
        dibujo.instalarManejador(manejadorMouse);
        matriz.setEditable(modoMatriz);
        actualizarVistas(TipoCambio.TOPOLOGIA);
    }

    //Elegimos si vamos a editar desde la matriz o desde el dibujo.
    public void setModoMatriz(boolean modoMatriz) {
        this.modoMatriz = modoMatriz;
        matriz.setEditable(modoMatriz);

        if (modoMatriz) {
            origenConexion = null;
            nodoArrastrado = null;
            huboArrastre = false;
        }
    }

    //Si cambió nuestro grafo, ponemos al día los paneles.
    @Override
    public void grafoCambiado(Grafo grafo, TipoCambio tipo) {
        if (grafo != servicio.getGrafo() || actualizando) {
            return;
        }
        actualizarVistas(tipo);
    }

    //Vemos qué hizo el mouse y mandamos el evento al método que toca.
    public void handle(MouseEvent evento) {
        if (modoMatriz || evento == null || evento.getButton() != MouseButton.PRIMARY) {
            return;
        }

        try {
            if (evento.getEventType() == MouseEvent.MOUSE_PRESSED) {
                procesarPulsacion(evento);
            } else if (evento.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                procesarArrastre(evento);
            } else if (evento.getEventType() == MouseEvent.MOUSE_RELEASED) {
                procesarLiberacion(evento);
            } else if (evento.getEventType() == MouseEvent.MOUSE_CLICKED) {
                procesarClick(evento);
            }
        } catch (IllegalArgumentException excepcion) {
            origenConexion = null;
            nodoArrastrado = null;
            matriz.mostrarError(excepcion.getMessage());
        }
    }

    //Creamos un grafo nuevo con la cantidad de nodos que nos pidieron.
    public void crearNuevo(boolean dirigido, int cantidad) {
        cancelarInteraccion();
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

    //Tomamos el cambio de una celda y lo aplicamos al grafo.
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

    //Cargamos una matriz nueva en el grafo que estamos usando.
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

    //Cargamos el grafo desde una lista de adyacencia.
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

    //Hacemos BFS, mostramos el orden y pintamos los nodos visitados.
    public void ejecutarBfs(int inicioId) {
        var orden = recorridos.bfs(servicio.getGrafo(), inicioId);
        recorrido.mostrar("BFS", orden, servicio.getGrafo());
        dibujo.resaltar(orden);
    }

    //Hacemos DFS, mostramos el orden y pintamos los nodos visitados.
    public void ejecutarDfs(int inicioId) {
        var orden = recorridos.dfs(servicio.getGrafo(), inicioId);
        recorrido.mostrar("DFS", orden, servicio.getGrafo());
        dibujo.resaltar(orden);
    }

    //Volvemos a acomodar los nodos dentro del dibujo.
    public void redistribuir() {
        servicio.redistribuir(anchoDisponible(), altoDisponible());
    }

    //Cambiamos la herramienta y cancelamos lo que haya quedado a medias.
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

    //Guardamos el peso que van a llevar las próximas conexiones.
    public void setPesoNuevaArista(double peso) {
        if (!Double.isFinite(peso) || peso == 0.0) {
            throw new IllegalArgumentException(
                    "El peso debe ser finito y distinto de cero");
        }
        pesoNuevaArista = peso;
    }

    //Desconectamos los paneles y dejamos de recibir avisos del grafo.
    public void dispose() {
        inicializado = false;
        cancelarInteraccion();
        servicio.getGrafo().removeListener(this);
        dibujo.retirarManejador(manejadorMouse);
        matriz.desconectar();
        dibujo.dispose();
    }

    //Ponemos al día el dibujo; la matriz solo cambia si cambiaron nodos o conexiones.
    private void actualizarVistas(TipoCambio tipo) {
        actualizando = true;
        try {
            dibujo.reconciliar(servicio.getGrafo());

            if (tipo == TipoCambio.TOPOLOGIA) {
                cancelarInteraccion();
                matrizActual = servicio.exportarMatriz();
                matriz.mostrar(matrizActual);
                recorrido.limpiar();
                dibujo.limpiarResaltado();
            }
        } finally {
            actualizando = false;
        }
    }

    //Si estamos seleccionando, agarramos el nodo para empezar a moverlo.
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

    //Movemos el nodo siguiendo el mouse.
    private void procesarArrastre(MouseEvent evento) {
        if (nodoArrastrado == null) {
            return;
        }

        Point2D punto = dibujo.posicionLocal(evento);
        double x = limitar(punto.getX() - desplazamientoArrastre.getX(), anchoDisponible());
        double y = limitar(punto.getY() - desplazamientoArrastre.getY(), altoDisponible());
        servicio.moverNodo(nodoArrastrado, x, y);
        huboArrastre = true;
        evento.consume();
    }

    //Soltamos el nodo cuando termina el arrastre.
    private void procesarLiberacion(MouseEvent evento) {
        if (nodoArrastrado != null) {
            nodoArrastrado = null;
            evento.consume();
        }
    }

    //Revisamos dónde se hizo clic y usamos la herramienta que esté seleccionada.
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

    //Si el clic fue en un espacio vacío, ponemos un nodo ahí.
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
                limitar(punto.getX(), anchoDisponible()),
                limitar(punto.getY(), altoDisponible()));
        evento.consume();
    }

    //El primer clic elige el origen y el segundo el destino de la conexión.
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

    //Borramos el nodo o la arista donde se hizo clic.
    private void eliminarElementoEnClick(
            OptionalInt idNodo,
            OptionalInt idArista) {
        if (idNodo.isPresent()) {
            servicio.eliminarNodo(idNodo.getAsInt());
        } else if (idArista.isPresent()) {
            servicio.eliminarArista(idArista.getAsInt());
        }
    }

    //Sacamos el objeto de la ventana que recibió el evento.
    private javafx.scene.Node obtenerObjeto(MouseEvent evento) {
        if (evento.getTarget() instanceof javafx.scene.Node objeto) {
            return objeto;
        }
        return null;
    }

    //Buscamos un nombre tipo N1 o N2 que todavía no esté usado.
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

    private void cancelarInteraccion() {
        origenConexion = null;
        nodoArrastrado = null;
        huboArrastre = false;
    }

    private static double limitar(double valor, double dimension) {
        double margen = Math.min(22, dimension / 2);
        return Math.max(margen, Math.min(dimension - margen, valor));
    }

    //Usamos el ancho real del dibujo o el tamaño inicial si todavía no se ha mostrado.
    private double anchoDisponible() {
        return dibujo.getWidth() > 0
                ? dibujo.getWidth()
                : dibujo.getPrefWidth();
    }

    //Usamos el alto real del dibujo o el tamaño inicial si todavía no se ha mostrado.
    private double altoDisponible() {
        return dibujo.getHeight() > 0
                ? dibujo.getHeight()
                : dibujo.getPrefHeight();
    }
}
