# Hilos en JavaFX — Práctica

Aplicación de escritorio en **JavaFX** que simula el procesamiento de registros
de la empresa **NicaData Solutions S.A.**, usando **hilos** (`Thread` +
`Runnable`) para no bloquear la interfaz.

**Universidad Americana** — Programación de Aplicaciones de Escritorio
Unidad III: Programación concurrente

**Estudiante:** *tu nombre*
**Docente:** *nombre del docente*

---

## Qué hace

Procesa registros de **clientes** y **productos**. Cada proceso tarda varios
segundos, así que **no se ejecuta en el hilo de la interfaz**: cada uno corre en
su propio hilo y actualiza los controles con `Platform.runLater()`.

| Proceso | Registros | Pausa | Duración |
|---|---|---|---|
| Clientes | 100 | 100 ms | ~10 s |
| Productos | 50 | 180 ms | ~9 s |

La ventana tiene dos barras de progreso, una etiqueta de porcentaje por proceso,
una etiqueta de estado global y los botones: **Iniciar clientes**,
**Detener clientes**, **Iniciar productos**, **Detener productos** y
**Reiniciar todo**.

---

## Cómo ejecutarlo

En Windows:

```powershell
.\mvnw.cmd clean javafx:run
```

En Linux o macOS:

```bash
./mvnw clean javafx:run
```

También se puede ejecutar la clase `Launcher` desde IntelliJ.

Requiere **JDK 21 o superior**.

---

## Estructura

```text
PracticaHilosJavaFX/
├── pom.xml
├── README.md
├── capturas/
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── ni/edu/uam/practicahilos/practicahilosjavafx/
    │       ├── Launcher.java               # Punto de entrada
    │       ├── MainApp.java                # Inicia JavaFX y carga el FXML
    │       ├── ProcesadorController.java   # Práctica guiada (un hilo)
    │       └── MonitorController.java      # Reto final (dos hilos)
    └── resources/
        └── ni/edu/uam/practicahilos/practicahilosjavafx/
            ├── procesador-view.fxml
            └── monitor-view.fxml
```

---

## Explicación de los hilos

**1. La tarea se define con `Runnable` y se ejecuta con un `Thread`:**

```java
hiloClientes = new Thread(
        crearTarea(TOTAL_CLIENTES, MS_CLIENTES,
                barraClientes, lblClientes, "Clientes"),
        "Hilo-Clientes");

hiloClientes.setDaemon(true);
hiloClientes.start();
```

Se llama **`start()`**, nunca `run()`: `run()` ejecutaría el bucle en el hilo de
la interfaz y la ventana se congelaría.

**2. Un solo método `crearTarea(...)` genera las dos tareas.** Recibe el total de
registros, la pausa, la barra y la etiqueta, y devuelve el `Runnable`. Así no
hay código duplicado y se puede agregar un tercer proceso fácilmente.

**3. La interfaz solo se actualiza desde el hilo de JavaFX:**

```java
Thread.sleep(msPorRegistro);

final int avance = i;

Platform.runLater(() -> {
    barra.setProgress(avance / (double) total);
    porcentajeYEstado.setText(avance + "% - Procesando...");
});
```

- `final int avance = i;` es necesario porque un lambda no puede capturar una
  variable que cambia.
- `avance / (double) total` evita la **división entera**, que dejaría la barra
  en 0 % hasta el final.

**4. La detención es cooperativa, con `interrupt()`.** No se mata el hilo: se le
pide que termine. Si está en `Thread.sleep()`, recibe `InterruptedException`, sale
del bucle, avisa a la interfaz y termina solo.

```java
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // se vuelve a marcar la bandera
    ...
}
```

**5. Los dos hilos son independientes.** Cada uno tiene su propia referencia
(`hiloClientes`, `hiloProductos`), su barra y su etiqueta. Por eso se puede
detener clientes mientras productos sigue avanzando.

**6. Cierre limpio.** `MainApp` avisa al controlador al cerrar la ventana:

```java
stage.setOnCloseRequest(event -> controller.detenerAlCerrar());
```

**7. Evitar iniciar dos veces.** Cada proceso empieza con esta verificación:

```java
if (hiloClientes != null && hiloClientes.isAlive()) {
    return;
}
```

---

## Evidencia

Las capturas están en la carpeta `capturas/`:

| Archivo | Qué demuestra |
|---|---|
| `01_clientes_en_proceso.png` | Solo clientes procesando; productos en 0 % |
| `02_procesos_simultaneos.png` | Los dos procesos avanzando a distinta velocidad |
| `03_detener_clientes.png` | Clientes detenido y productos todavía avanzando |
| `04_procesos_finalizados.png` | Ambos al 100 % |
| `05_ventana_receptiva.png` | La ventana se mueve mientras procesa: no se bloquea |

---

## Referencias

- [Oracle – `Thread`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Thread.html)
- [Oracle – `Runnable`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Runnable.html)
- [OpenJFX – `Platform`](https://openjfx.io/javadoc/25/javafx.graphics/javafx/application/Platform.html)
