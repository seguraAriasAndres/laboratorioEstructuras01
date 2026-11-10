# Laboratorio de grafos

Aplicación Java 21 / JavaFX 21 organizada según el UML: modelo independiente de la interfaz, servicios de conversión y recorridos, y presentación MVC. El dibujo usa **Draw de Princeton** dentro del panel JavaFX.

## Ejecutar

Se requiere un **JDK 21 o superior** (no solamente un JRE) y un escritorio gráfico. Maven se incluye mediante el wrapper; la primera ejecución descarga sus dependencias.

También puedes abrir `src/main/java/cr/ac/una/est/laboratorioestructuras/Main.java` en el IDE y ejecutar su método `main`. Esa clase arranca la ventana de `AplicacionPrincipal`.

Desde PowerShell, en esta carpeta:

```powershell
.\mvnw.cmd javafx:run
```

`JAVA_HOME` debe apuntar al JDK. Si hay varias instalaciones, se puede seleccionar una para la terminal actual; por ejemplo, con el JDK disponible en este equipo:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.4'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd javafx:run
```

En Linux/macOS: `./mvnw javafx:run`.

## Uso

1. Indicar cantidad de nodos y marcar **Dirigido** cuando corresponda; pulsar **Crear grafo**. Los IDs empiezan en 0 y aparecen en los encabezados de la matriz.
2. En **Matriz → Grafo**, escribir pesos y confirmar con Enter o al salir de la celda. Un cero elimina la conexión. En grafos no dirigidos se actualiza también la celda simétrica.
3. En **Dibujar → Matriz**, elegir una herramienta: **Crear nodo** agrega un nodo al pulsar en el fondo; **Conectar** une dos nodos pulsados consecutivamente; **Seleccionar** permite arrastrarlos; **Eliminar** borra el nodo o la arista pulsados. Pulsar dos veces el mismo nodo con Conectar crea un bucle.
4. Escribir un peso y pulsar **Aplicar peso** antes de crear las conexiones que deban usarlo.
5. Indicar un ID inicial y ejecutar **BFS** o **DFS**. Se muestra el orden de visita y se resaltan los nodos alcanzables. **Redistribuir** ordena los nodos en un círculo.

El dibujo y la matriz se sincronizan automáticamente. Los recorridos se conservan al mover nodos y se limpian cuando cambian nodos, conexiones o pesos.

## Convenciones del modelo

- Se permite una arista por par ordenado en grafos dirigidos, y una por par sin orientación en no dirigidos. Reconectar un par actualiza el peso de su arista existente.
- Los pesos pueden ser positivos o negativos, deben ser finitos y distintos de cero. **0 representa ausencia de arista** en la matriz y en las listas importadas.
- Se permiten bucles. Las aristas dirigidas recíprocas se dibujan con curvas distintas.
- Los IDs son enteros no negativos; eliminar nodos puede dejar huecos. Las conversiones usan los IDs reales, no los índices de la matriz.
- Las matrices son inmutables, cuadradas y simétricas si el grafo no es dirigido. Las importaciones conservan las posiciones de nodos existentes y distribuyen los nuevos.
- BFS y DFS visitan únicamente la componente alcanzable desde el inicio y respetan la dirección del grafo y el orden de inserción de vecinos.

## Clases del UML

| Capa | Clases |
| --- | --- |
| Modelo | `Grafo`, `Nodo`, `Arista`, `MatrizAdyacencia`, `Posicion`, `GrafoListener`, `TipoCambio` |
| Servicios | `GrafoService`, `ConversorRepresentaciones`, `DistribuidorCircular`, `RecorridoService` |
| Presentación | `AplicacionPrincipal`, `ControladorGrafo`, `PanelDibujo`, `PanelMatriz`, `PanelRecorrido`, `EdicionCelda`, `HerramientaEdicion` |

`PanelDibujo` utiliza Draw para producir la imagen y conserva grupos JavaFX transparentes para seleccionar nodos y aristas. La geometría de dibujo y selección es compartida. Draw/Swing se ejecuta en su hilo EDT usando instantáneas del modelo; los controles y el modelo se coordinan desde JavaFX. Al cerrar se liberan listeners y la ventana interna de Draw.

## Verificación

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

La suite incluye pruebas del modelo, conversiones matriz/lista, pesos, IDs discontinuos, simetría, distribución circular, BFS/DFS y la integración JavaFX/Draw: creación, conexiones, arrastre, eliminación, edición de celdas y arranque/cierre de la aplicación. Las pruebas gráficas necesitan un escritorio; se omiten si Java detecta un entorno sin pantalla.

La prueba de dibujo genera `target/validacion-grafo.png`. Los resultados JUnit quedan en `target/surefire-reports/`.

## Biblioteca de dibujo

Se incluyen sin modificaciones los archivos oficiales `Draw.java` y `DrawListener.java` en `src/main/java/edu/princeton/cs/algs4/`, con sus avisos de autoría y licencia. No se necesita instalar un JAR de Draw manualmente. Ver [THIRD_PARTY.md](THIRD_PARTY.md) para las fuentes y licencia.
