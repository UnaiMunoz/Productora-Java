package com.productora.controller;

import com.productora.dao.SerieDAO;
import com.productora.model.Serie;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de Series
 */
public class SerieController implements Initializable {

    @FXML
    private TableView<Serie> tableSeries;

    @FXML
    private TableColumn<Serie, Integer> colId;

    @FXML
    private TableColumn<Serie, String> colTitulo;

    @FXML
    private TableColumn<Serie, String> colGenero;

    @FXML
    private TableColumn<Serie, Integer> colAnyoInicio;

    @FXML
    private TableColumn<Serie, String> colEstado;

    @FXML
    private TableColumn<Serie, Double> colRating;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private TextField txtGenero;

    @FXML
    private TextField txtAnyoInicio;

    @FXML
    private TextField txtAnyoFin;

    @FXML
    private TextField txtProductora;

    @FXML
    private TextField txtPresupuesto;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private Slider sliderRating;

    @FXML
    private Label lblRating;

    @FXML
    private VBox detailPane;

    private SerieDAO serieDAO;
    private ObservableList<Serie> seriesList;
    private Serie serieActual;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serieDAO = new SerieDAO();
        seriesList = FXCollections.observableArrayList();

        // Configurar columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnyoInicio.setCellValueFactory(new PropertyValueFactory<>("anyoInicio"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Configurar combo de estado
        cmbEstado.getItems().addAll(
                Serie.ESTADO_PRODUCCION,
                Serie.ESTADO_FINALIZADA,
                Serie.ESTADO_CANCELADA,
                Serie.ESTADO_PREPRODUCCION);

        // Configurar slider rating
        sliderRating.setMin(0);
        sliderRating.setMax(10);
        sliderRating.setValue(0);
        sliderRating.valueProperty().addListener((observable, oldValue, newValue) -> {
            lblRating.setText(String.format("%.1f", newValue.doubleValue()));
        });

        // Cargar series iniciales
        cargarSeries();

        // Manejar selección de serie
        tableSeries.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                serieActual = newSel;
                mostrarDetalleSerie(serieActual);
                detailPane.setVisible(true);
            } else {
                detailPane.setVisible(false);
            }
        });

        // Inicialmente ocultar panel de detalles
        detailPane.setVisible(false);
    }

    /**
     * Carga todas las series en la tabla
     */
    private void cargarSeries() {
        seriesList.clear();
        seriesList.addAll(serieDAO.getAll());
        tableSeries.setItems(seriesList);
    }

    /**
     * Muestra los detalles de una serie en el formulario
     * 
     * @param serie Serie a mostrar
     */
    private void mostrarDetalleSerie(Serie serie) {
        txtTitulo.setText(serie.getTitulo());
        txtDescripcion.setText(serie.getDescripcion());
        txtGenero.setText(serie.getGenero());
        txtAnyoInicio.setText(serie.getAnyoInicio() != null ? serie.getAnyoInicio().toString() : "");
        txtAnyoFin.setText(serie.getAnyoFin() != null ? serie.getAnyoFin().toString() : "");
        txtProductora.setText(serie.getProductora());
        txtPresupuesto.setText(serie.getPresupuesto() != null ? serie.getPresupuesto().toString() : "");
        cmbEstado.setValue(serie.getEstado());

        Double rating = serie.getRating();
        sliderRating.setValue(rating != null ? rating : 0);
        lblRating.setText(rating != null ? String.format("%.1f", rating) : "0.0");
    }

    /**
     * Acción del botón buscar
     */
    @FXML
    private void onBuscarAction(ActionEvent event) {
        String termino = txtBuscar.getText().trim();
        if (termino.isEmpty()) {
            cargarSeries();
        } else {
            seriesList.clear();
            seriesList.addAll(serieDAO.findByTitle(termino));
            tableSeries.setItems(seriesList);
        }
    }

    /**
     * Acción del botón nueva serie
     */
    @FXML
    private void onNuevaSerieAction(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Nueva Serie");
        dialog.setHeaderText("Crear nueva serie");
        dialog.setContentText("Título de la serie:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(titulo -> {
            if (!titulo.trim().isEmpty()) {
                Serie nuevaSerie = serieDAO.create(titulo.trim());
                seriesList.add(nuevaSerie);
                tableSeries.getSelectionModel().select(nuevaSerie);
            }
        });
    }

    /**
     * Acción del botón guardar cambios
     */
    @FXML
    private void onGuardarAction(ActionEvent event) {
        if (serieActual != null) {
            serieActual.setTitulo(txtTitulo.getText());
            serieActual.setDescripcion(txtDescripcion.getText());
            serieActual.setGenero(txtGenero.getText());

            try {
                serieActual.setAnyoInicio(
                        txtAnyoInicio.getText().isEmpty() ? null : Integer.parseInt(txtAnyoInicio.getText()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El año de inicio debe ser un número válido.");
                return;
            }

            try {
                serieActual.setAnyoFin(txtAnyoFin.getText().isEmpty() ? null : Integer.parseInt(txtAnyoFin.getText()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El año de fin debe ser un número válido.");
                return;
            }

            serieActual.setProductora(txtProductora.getText());

            try {
                serieActual.setPresupuesto(
                        txtPresupuesto.getText().isEmpty() ? null : Double.parseDouble(txtPresupuesto.getText()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El presupuesto debe ser un número válido.");
                return;
            }

            serieActual.setEstado(cmbEstado.getValue());
            serieActual.setRating(sliderRating.getValue() > 0 ? sliderRating.getValue() : null);

            // Refrescar la tabla
            int selectedIndex = tableSeries.getSelectionModel().getSelectedIndex();
            tableSeries.getItems().set(selectedIndex, serieActual);

            mostrarAlerta("Éxito", "La serie se ha actualizado correctamente.");
        }
    }

    /**
     * Acción del botón eliminar
     */
    @FXML
    private void onEliminarAction(ActionEvent event) {
        if (serieActual != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar serie");
            alert.setContentText("¿Está seguro que desea eliminar la serie \"" + serieActual.getTitulo() + "\"?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean eliminado = serieDAO.delete(serieActual.getId());
                if (eliminado) {
                    seriesList.remove(serieActual);
                    mostrarAlerta("Éxito", "La serie se ha eliminado correctamente.");
                    detailPane.setVisible(false);
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la serie.");
                }
            }
        }
    }

    /**
     * Muestra una alerta informativa
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Acción para limpiar los campos de búsqueda y recargar series
     */
    @FXML
    private void onLimpiarBusquedaAction(ActionEvent event) {
        txtBuscar.clear();
        cargarSeries();
    }
}