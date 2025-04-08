package com.productora.controller;

import com.productora.dao.SerieDAO;
import com.productora.model.Serie;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

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
    private ComboBox<String> cmbFiltroGenero;

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
    private Label lblTotalSeries;
    
    @FXML
    private Label lblRatingPromedio;
    
    @FXML
    private Label lblSeriesProduccion;

    @FXML
    private VBox detailPane;
    
    @FXML
    private BarChart<String, Number> barChartRatings;
    
    @FXML
    private ComboBox<String> cmbTopSeries;

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
                
        // Configurar combo de top series
        cmbTopSeries.getItems().clear();
        cmbTopSeries.getItems().addAll("5", "10", "15", "20");
        cmbTopSeries.setValue("10");

        // Configurar slider rating
        sliderRating.setMin(0);
        sliderRating.setMax(10);
        sliderRating.setValue(0);
        sliderRating.valueProperty().addListener((observable, oldValue, newValue) -> {
            lblRating.setText(String.format("%.1f", newValue.doubleValue()));
        });
        
        // Configurar combo de filtro por género
        cargarGenerosEnCombo();
        
        // Configurar combo de top series
        if (cmbTopSeries.getItems().isEmpty()) {
            cmbTopSeries.getItems().addAll("5", "10", "15", "20");
            cmbTopSeries.setValue("10");
        }

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
        
        // Cargar estadísticas y gráficos iniciales
        actualizarEstadisticas();
        actualizarGraficoRatings();
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
     * Carga los géneros disponibles en el combo de filtro
     */
    private void cargarGenerosEnCombo() {
        // Obtener la lista de series para extraer los géneros disponibles
        List<Serie> todasLasSeries = serieDAO.getAll();
        Set<String> generos = new HashSet<>();
        
        // Extraer géneros únicos de las series existentes
        for (Serie serie : todasLasSeries) {
            if (serie.getGenero() != null && !serie.getGenero().isEmpty()) {
                generos.add(serie.getGenero());
            }
        }
        
        // Convertir el conjunto a una lista ordenada
        ObservableList<String> listaGeneros = FXCollections.observableArrayList();
        listaGeneros.add(""); // Opción vacía para mostrar todas
        listaGeneros.addAll(generos.stream().sorted().collect(Collectors.toList()));
        
        cmbFiltroGenero.setItems(listaGeneros);
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
        String genero = cmbFiltroGenero.getValue();
        
        // Primero obtener todas las series que coinciden con el término de búsqueda
        List<Serie> resultados;
        if (termino.isEmpty()) {
            resultados = serieDAO.getAll();
        } else {
            resultados = serieDAO.findByTitle(termino);
        }
        
        // Luego filtrar por género si es necesario
        if (genero != null && !genero.isEmpty()) {
            resultados = resultados.stream()
                .filter(s -> s.getGenero() != null && s.getGenero().equals(genero))
                .collect(Collectors.toList());
        }
        
        // Actualizar la tabla
        seriesList.clear();
        seriesList.addAll(resultados);
        tableSeries.setItems(seriesList);
        
        // Actualizar el estado
        lblEstado.setText("Series encontradas: " + seriesList.size());
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
                
                // Actualizar estadísticas y gráficos
                actualizarEstadisticas();
                actualizarGraficoRatings();
                
                // Actualizar géneros en el combo
                cargarGenerosEnCombo();
            }
        });
    }

    /**
     * Acción del botón guardar cambios
     */
    @FXML
    private void onGuardarAction(ActionEvent event) {
        if (serieActual != null) {
            // Guardar el género anterior para detectar cambios
            String generoAnterior = serieActual.getGenero();
            
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
            
            // Si cambió el género, actualizar el combo de filtro
            if ((generoAnterior == null && serieActual.getGenero() != null) ||
                (generoAnterior != null && !generoAnterior.equals(serieActual.getGenero()))) {
                cargarGenerosEnCombo();
            }
            
            // Actualizar estadísticas y gráficos
            actualizarEstadisticas();
            actualizarGraficoRatings();

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
                    
                    // Actualizar estadísticas y gráficos
                    actualizarEstadisticas();
                    actualizarGraficoRatings();
                    
                    // Actualizar géneros en el combo
                    cargarGenerosEnCombo();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la serie.");
                }
            }
        }
    }
    
    /**
     * Actualiza las estadísticas mostradas en la pestaña de estadísticas
     */
    @FXML
    private void onActualizarEstadisticasAction(ActionEvent event) {
        actualizarEstadisticas();
    }
    
    /**
     * Actualiza el gráfico de ratings
     */
    @FXML
    private void onActualizarGraficoAction(ActionEvent event) {
        actualizarGraficoRatings();
    }
    
    /**
     * Actualiza las estadísticas
     */
    private void actualizarEstadisticas() {
        List<Serie> todasLasSeries = serieDAO.getAll();
        
        // Total de series
        int totalSeries = todasLasSeries.size();
        lblTotalSeries.setText(String.valueOf(totalSeries));
        
        // Rating promedio
        double sumaRatings = 0;
        int seriesConRating = 0;
        
        // Series en producción
        int seriesEnProduccion = 0;
        
        for (Serie serie : todasLasSeries) {
            if (serie.getRating() != null) {
                sumaRatings += serie.getRating();
                seriesConRating++;
            }
            
            if (Serie.ESTADO_PRODUCCION.equals(serie.getEstado())) {
                seriesEnProduccion++;
            }
        }
        
        double ratingPromedio = seriesConRating > 0 ? sumaRatings / seriesConRating : 0;
        lblRatingPromedio.setText(String.format("%.1f", ratingPromedio));
        
        lblSeriesProduccion.setText(String.valueOf(seriesEnProduccion));
    }
    
    /**
     * Actualiza el gráfico de ratings
     */
    private void actualizarGraficoRatings() {
        // Limpiar gráfico existente
        barChartRatings.getData().clear();
        
        // Obtener el número de series a mostrar
        int topN = 10;
        try {
            if (cmbTopSeries != null && cmbTopSeries.getValue() != null) {
                topN = Integer.parseInt(cmbTopSeries.getValue());
            }
        } catch (NumberFormatException e) {
            // Si hay error, usar 10 por defecto
            topN = 10;
        }
        
        // Obtener las mejores series según rating
        List<Serie> mejoresSeries = serieDAO.getTopRated(topN);
        
        // Crear serie de datos para el gráfico
        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Rating");
        
        for (Serie serie : mejoresSeries) {
            if (serie.getRating() != null) {
                dataSeries.getData().add(new XYChart.Data<>(serie.getTitulo(), serie.getRating()));
            }
        }
        
        barChartRatings.getData().add(dataSeries);
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
        cmbFiltroGenero.setValue("");
        cargarSeries();
    }
}