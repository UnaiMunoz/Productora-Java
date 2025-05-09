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
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador simplificado para la vista de Series
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
    private Label lblEstado;

    @FXML
    private VBox detailPane;

    private SerieDAO serieDAO;
    private ObservableList<Serie> seriesList;
    private Serie serieActual;

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
            }
        });
    }

    /**
     * Carga todas las series en la tabla
     */
    private void cargarSeries() {
        seriesList.clear();
        seriesList.addAll(serieDAO.getAll());
        tableSeries.setItems(seriesList);
        
        // Actualizar estado
        lblEstado.setText("Series cargadas: " + seriesList.size());
    }

    /**
     * Muestra los detalles de una serie en el formulario
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
                lblEstado.setText("Serie creada: " + nuevaSerie.getTitulo());
            }
        });
    }

/**
 * Acción del botón guardar cambios
 */
@FXML
private void onGuardarAction(ActionEvent event) {
    if (serieActual != null) {
        // Validación del título
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) {
            mostrarAlerta("Error", "El título no puede estar vacío.");
            txtTitulo.requestFocus();
            return;
        }
        serieActual.setTitulo(titulo);
        
        // Descripción - puede ser null
        String descripcion = txtDescripcion.getText().trim();
        serieActual.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        
        // Género - puede ser null
        String genero = txtGenero.getText().trim();
        serieActual.setGenero(genero.isEmpty() ? null : genero);

        // Año de inicio - validación numérica
        String anyoInicioStr = txtAnyoInicio.getText().trim();
        if (!anyoInicioStr.isEmpty()) {
            try {
                int anyoInicio = Integer.parseInt(anyoInicioStr);
                if (anyoInicio < 1900 || anyoInicio > 2025) {
                    mostrarAlerta("Error", "El año de inicio debe estar entre 1900 y 2025.");
                    txtAnyoInicio.requestFocus();
                    return;
                }
                serieActual.setAnyoInicio(anyoInicio);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El año de inicio debe ser un número válido.");
                txtAnyoInicio.requestFocus();
                return;
            }
        } else {
            serieActual.setAnyoInicio(null);
        }

        // Año de fin - validación numérica
        String anyoFinStr = txtAnyoFin.getText().trim();
        if (!anyoFinStr.isEmpty()) {
            try {
                int anyoFin = Integer.parseInt(anyoFinStr);
                if (anyoFin < 1900 || anyoFin > 2025) {
                    mostrarAlerta("Error", "El año de fin debe estar entre 1900 y 2025.");
                    txtAnyoFin.requestFocus();
                    return;
                }
                
                // Validar que año fin sea posterior a año inicio
                Integer anyoInicio = serieActual.getAnyoInicio();
                if (anyoInicio != null && anyoFin < anyoInicio) {
                    mostrarAlerta("Error", "El año de fin debe ser posterior o igual al año de inicio.");
                    txtAnyoFin.requestFocus();
                    return;
                }
                
                serieActual.setAnyoFin(anyoFin);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El año de fin debe ser un número válido.");
                txtAnyoFin.requestFocus();
                return;
            }
        } else {
            serieActual.setAnyoFin(null);
        }

        // Productora - puede ser null
        String productora = txtProductora.getText().trim();
        serieActual.setProductora(productora.isEmpty() ? null : productora);

        // Presupuesto - validación numérica
        String presupuestoStr = txtPresupuesto.getText().trim();
        if (!presupuestoStr.isEmpty()) {
            try {
                double presupuesto = Double.parseDouble(presupuestoStr);
                if (presupuesto < 0) {
                    mostrarAlerta("Error", "El presupuesto no puede ser negativo.");
                    txtPresupuesto.requestFocus();
                    return;
                }
                serieActual.setPresupuesto(presupuesto);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El presupuesto debe ser un número válido.");
                txtPresupuesto.requestFocus();
                return;
            }
        } else {
            serieActual.setPresupuesto(null);
        }

        // Estado - validamos que sea uno de los estados permitidos
        String estado = cmbEstado.getValue();
        if (estado != null && !estado.isEmpty()) {
            if (!estado.equals(Serie.ESTADO_PRODUCCION) && 
                !estado.equals(Serie.ESTADO_FINALIZADA) &&
                !estado.equals(Serie.ESTADO_CANCELADA) &&
                !estado.equals(Serie.ESTADO_PREPRODUCCION)) {
                mostrarAlerta("Error", "Estado de serie no válido.");
                return;
            }
            serieActual.setEstado(estado);
        } else {
            serieActual.setEstado(null);
        }
        
        // Rating - desde el slider
        double rating = sliderRating.getValue();
        serieActual.setRating(rating > 0 ? rating : null);

        // Refrescar la tabla
        int selectedIndex = tableSeries.getSelectionModel().getSelectedIndex();
        tableSeries.getItems().set(selectedIndex, serieActual);
        
        lblEstado.setText("Serie actualizada: " + serieActual.getTitulo());
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
                    lblEstado.setText("Serie eliminada: " + serieActual.getTitulo());
                    mostrarAlerta("Éxito", "La serie se ha eliminado correctamente.");
                    
                    // Limpiar selección
                    if (!seriesList.isEmpty()) {
                        tableSeries.getSelectionModel().select(0);
                    } else {
                        limpiarFormulario();
                    }
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la serie.");
                }
            }
        }
    }
    
    /**
     * Limpia el formulario de detalles
     */
    private void limpiarFormulario() {
        txtTitulo.clear();
        txtDescripcion.clear();
        txtGenero.clear();
        txtAnyoInicio.clear();
        txtAnyoFin.clear();
        txtProductora.clear();
        txtPresupuesto.clear();
        cmbEstado.setValue(null);
        sliderRating.setValue(0);
        lblRating.setText("0.0");
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
}