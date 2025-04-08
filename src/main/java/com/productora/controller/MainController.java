package com.productora.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la vista principal de la aplicación
 */
public class MainController implements Initializable {

    @FXML
    private StackPane contentPane;

    @FXML
    private ImageView logoImageView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar imagen de logo (opcional)
        try {
            //Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
            //logoImageView.setImage(logo);
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen del logo: " + e.getMessage());
        }
    }

    /**
     * Maneja la acción del botón Series
     */
    @FXML
    private void onSeriesAction(ActionEvent event) {
        cargarVista("/fxml/SerieView.fxml", "Vista de Series");
    }

    /**
     * Maneja la acción del botón Temporadas
     */
    @FXML
    private void onTemporadasAction(ActionEvent event) {
        mostrarMensajeNoImplementado("Vista de Temporadas");
    }

    /**
     * Maneja la acción del botón Episodios
     */
    @FXML
    private void onEpisodiosAction(ActionEvent event) {
        mostrarMensajeNoImplementado("Vista de Episodios");
    }

    /**
     * Maneja la acción del botón Actores
     */
    @FXML
    private void onActoresAction(ActionEvent event) {
        mostrarMensajeNoImplementado("Vista de Actores");
    }

    /**
     * Maneja la acción del botón Reportes
     */
    @FXML
    private void onReportesAction(ActionEvent event) {
        mostrarMensajeNoImplementado("Vista de Reportes");
    }

    /**
     * Maneja la acción del botón Ayuda
     */
    @FXML
    private void onAyudaAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ayuda");
        alert.setHeaderText("Sistema de Gestión de Productora");
        alert.setContentText("Esta aplicación permite gestionar series de televisión, temporadas, " +
                "episodios y actores para una productora.\n\n" +
                "Seleccione una opción del menú para comenzar a trabajar con los diferentes módulos.");
        alert.showAndWait();
    }

    /**
     * Carga una vista en el contentPane
     */
    private void cargarVista(String fxmlPath, String title) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            System.out.println("Error al cargar la vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje para funcionalidades no implementadas
     */
    private void mostrarMensajeNoImplementado(String modulo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Módulo no implementado");
        alert.setHeaderText(modulo);
        alert.setContentText("Esta funcionalidad aún no ha sido implementada.");
        alert.showAndWait();
    }
}