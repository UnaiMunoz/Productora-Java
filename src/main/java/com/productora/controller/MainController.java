package com.productora.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    @FXML
    private StackPane contentPane;

    @FXML
    private ImageView logoImageView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @FXML
    private void onSeriesAction(ActionEvent event) {
        cargarVista("/fxml/SerieView.fxml", "Vista de Series");
    }

    @FXML
    private void onTemporadasAction(ActionEvent event) {
        cargarVista("/fxml/TemporadaView.fxml", "Vista de Temporadas");
    }

    @FXML
    private void onEpisodiosAction(ActionEvent event) {
        cargarVista("/fxml/EpisodioView.fxml", "Vista de Episodios");
    }

    @FXML
    private void onActoresAction(ActionEvent event) {
        cargarVista("/fxml/ActorView.fxml", "Vista de Actores");
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

}