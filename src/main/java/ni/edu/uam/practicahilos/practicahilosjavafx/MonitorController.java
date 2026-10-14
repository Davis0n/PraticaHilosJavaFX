package ni.edu.uam.practicahilos.practicahilosjavafx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class MonitorController {

    // ---------- Clientes ----------
    @FXML
    private ProgressBar barraClientes;

    @FXML
    private Label lblClientes;

    @FXML
    private Button btnIniciarClientes;

    @FXML
    private Button btnDetenerClientes;

    // ---------- Productos ----------
    @FXML
    private ProgressBar barraProductos;

    @FXML
    private Label lblProductos;

    @FXML
    private Button btnIniciarProductos;

    @FXML
    private Button btnDetenerProductos;

    // ---------- Comunes ----------
    @FXML
    private Label lblEstadoGlobal;

    @FXML
    private Button btnReiniciarTodo;

    // ---------- Estado de los hilos ----------
    private Thread hiloClientes;
    private Thread hiloProductos;

    private static final int TOTAL_CLIENTES = 100;
    private static final long MS_CLIENTES = 100;   // ~10 s

    private static final int TOTAL_PRODUCTOS = 50;
    private static final long MS_PRODUCTOS = 180;  // ~9 s

    // =====================================================
    // Tarea genérica: un solo Runnable para los dos procesos
    // =====================================================
    private Runnable crearTarea(int total,
                                long msPorRegistro,
                                ProgressBar barra,
                                Label porcentajeYEstado,
                                String nombreProceso) {

        return () -> {
            try {
                for (int i = 1; i <= total; i++) {

                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(msPorRegistro);

                    final int avance = i;

                    Platform.runLater(() -> {
                        barra.setProgress(avance / (double) total);
                        porcentajeYEstado.setText(
                                avance + "% - Procesando..."
                        );
                    });
                }

                Platform.runLater(() -> {
                    porcentajeYEstado.setText(total + "% - Finalizado");
                    lblEstadoGlobal.setText(nombreProceso + ": finalizado");
                    actualizarBotones(nombreProceso);
                });

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                Platform.runLater(() -> {
                    porcentajeYEstado.setText(
                            "Detenido en el registro " +
                                    Math.round(barra.getProgress() * total)
                    );
                    lblEstadoGlobal.setText(nombreProceso + ": detenido");
                    actualizarBotones(nombreProceso);
                });
            }
        };
    }

    // =====================================================
    // Clientes
    // =====================================================
    @FXML
    private void iniciarClientes() {

        if (hiloClientes != null && hiloClientes.isAlive()) {
            return;   // Requerimiento 8: no iniciar dos veces
        }

        barraClientes.setProgress(0);
        lblClientes.setText("0% - Procesando...");
        lblEstadoGlobal.setText("Procesando clientes...");

        btnIniciarClientes.setDisable(true);
        btnDetenerClientes.setDisable(false);
        btnReiniciarTodo.setDisable(true);

        hiloClientes = new Thread(
                crearTarea(TOTAL_CLIENTES, MS_CLIENTES,
                        barraClientes, lblClientes, "Clientes"),
                "Hilo-Clientes");

        hiloClientes.setDaemon(true);
        hiloClientes.start();
    }

    @FXML
    private void detenerClientes() {

        if (hiloClientes != null && hiloClientes.isAlive()) {

            btnDetenerClientes.setDisable(true);
            lblEstadoGlobal.setText("Deteniendo clientes...");

            hiloClientes.interrupt();
        }
    }

    // =====================================================
    // Productos
    // =====================================================
    @FXML
    private void iniciarProductos() {

        if (hiloProductos != null && hiloProductos.isAlive()) {
            return;   // Requerimiento 8: no iniciar dos veces
        }

        barraProductos.setProgress(0);
        lblProductos.setText("0% - Procesando...");
        lblEstadoGlobal.setText("Procesando productos...");

        btnIniciarProductos.setDisable(true);
        btnDetenerProductos.setDisable(false);
        btnReiniciarTodo.setDisable(true);

        hiloProductos = new Thread(
                crearTarea(TOTAL_PRODUCTOS, MS_PRODUCTOS,
                        barraProductos, lblProductos, "Productos"),
                "Hilo-Productos");

        hiloProductos.setDaemon(true);
        hiloProductos.start();
    }

    @FXML
    private void detenerProductos() {

        if (hiloProductos != null && hiloProductos.isAlive()) {

            btnDetenerProductos.setDisable(true);
            lblEstadoGlobal.setText("Deteniendo productos...");

            hiloProductos.interrupt();
        }
    }

    // =====================================================
    // Reiniciar todo (solo si ningún hilo está vivo)
    // =====================================================
    @FXML
    private void reiniciarTodo() {

        if (estaActivo(hiloClientes) || estaActivo(hiloProductos)) {
            return;
        }

        barraClientes.setProgress(0);
        lblClientes.setText("0% - En espera");

        barraProductos.setProgress(0);
        lblProductos.setText("0% - En espera");

        lblEstadoGlobal.setText("Listo para iniciar");

        btnIniciarClientes.setDisable(false);
        btnDetenerClientes.setDisable(true);

        btnIniciarProductos.setDisable(false);
        btnDetenerProductos.setDisable(true);

        btnReiniciarTodo.setDisable(false);
    }

    // =====================================================
    // Auxiliares
    // =====================================================
    private boolean estaActivo(Thread hilo) {
        return hilo != null && hilo.isAlive();
    }

    private void actualizarBotones(String nombreProceso) {

        if ("Clientes".equals(nombreProceso)) {
            btnIniciarClientes.setDisable(false);
            btnDetenerClientes.setDisable(true);
        } else {
            btnIniciarProductos.setDisable(false);
            btnDetenerProductos.setDisable(true);
        }

        if (!estaActivo(hiloClientes) && !estaActivo(hiloProductos)) {
            btnReiniciarTodo.setDisable(false);
        }
    }

    // Se invoca desde MainApp al cerrar la ventana
    public void detenerAlCerrar() {

        if (estaActivo(hiloClientes)) {
            hiloClientes.interrupt();
        }

        if (estaActivo(hiloProductos)) {
            hiloProductos.interrupt();
        }
    }
}