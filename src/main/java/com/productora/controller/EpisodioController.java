package com.productora.controller;

import com.productora.dao.EpisodioDAO;
import com.productora.dao.SerieDAO;
import com.productora.dao.TemporadaDAO;
import com.productora.model.Episodio;
import com.productora.model.Serie;
import com.productora.model.Temporada;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de Episodios
 */
public class EpisodioController implements Initializable {

    @FXML
    private ComboBox<Serie> cmbSeries;

    @FXML
    private ComboBox<Temporada> cmbTemporadas;

    @FXML
    private TableView<Episodio> tableEpisodios;

    @FXML
    private TableColumn<Episodio, Integer> colId;

    @FXML
    private TableColumn<Episodio, Integer> colNumero;

    @FXML
    private TableColumn<Episodio, String> colTitulo;

    @FXML
    private TableColumn<Episodio, String> colDuracion;

    @FXML
    private TableColumn<Episodio, String> colFechaEstreno;

    @FXML
    private TableColumn<Episodio, Double> colRating;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private TextField txtNumero;

    @FXML
    private TextField txtDuracion;

    @FXML
    private DatePicker dateFechaEstreno;

    @FXML
    private TextField txtDirector;

    @FXML
    private TextField txtGuionista;

    @FXML
    private Slider sliderRating;

    @FXML
    private Label lblRating;

    @FXML
    private Label lblEstado;

    @FXML
    private VBox detailPane;

    private SerieDAO serieDAO;
    private TemporadaDAO temporadaDAO;
    private EpisodioDAO episodioDAO;
    private ObservableList<Episodio> episodiosList;
    private Episodio episodioActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serieDAO = new SerieDAO();
        temporadaDAO = new TemporadaDAO();
        episodioDAO = new EpisodioDAO();
        episodiosList = FXCollections.observableArrayList();

        // Configurar columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        // Para la duración formateamos a "XX minutos"
        colDuracion.setCellValueFactory(cellData -> {
            Integer duracion = cellData.getValue().getDuracion();
            return new ReadOnlyStringWrapper(duracion != null ? duracion + " min." : "");
        });

        // Para la fecha de estreno formateamos a "dd/MM/yyyy"
        colFechaEstreno.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaEstreno();
            if (fecha != null) {
                return new ReadOnlyStringWrapper(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                return new ReadOnlyStringWrapper("");
            }
        });

        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Configurar slider rating
        sliderRating.setMin(0);
        sliderRating.setMax(10);
        sliderRating.setValue(0);
        sliderRating.valueProperty().addListener((observable, oldValue, newValue) -> {
            lblRating.setText(String.format("%.1f", newValue.doubleValue()));
        });

        // Configurar ComboBox de Series
        cargarSeries();
        cmbSeries.setConverter(new StringConverter<Serie>() {
            @Override
            public String toString(Serie serie) {
                return serie != null ? serie.getTitulo() : "";
            }

            @Override
            public Serie fromString(String string) {
                return null; // No necesitamos convertir de String a Serie
            }
        });

        cmbSeries.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarTemporadas(newVal.getId());
            } else {
                cmbTemporadas.getItems().clear();
                episodiosList.clear();
            }
        });

        // Configurar ComboBox de Temporadas
        cmbTemporadas.setConverter(new StringConverter<Temporada>() {
            @Override
            public String toString(Temporada temporada) {
                return temporada != null ? "Temporada " + temporada.getNumero() : "";
            }

            @Override
            public Temporada fromString(String string) {
                return null; // No necesitamos convertir de String a Temporada
            }
        });

        cmbTemporadas.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarEpisodios(newVal.getId());
            } else {
                episodiosList.clear();
            }
        });

        // Manejar selección de episodio
        tableEpisodios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                episodioActual = newSel;
                mostrarDetalleEpisodio(episodioActual);
            }
        });
    }

    /**
     * Carga todas las series en el ComboBox
     */
    private void cargarSeries() {
        List<Serie> series = serieDAO.getAll();
        cmbSeries.setItems(FXCollections.observableArrayList(series));

        // Si hay series, seleccionar la primera por defecto
        if (!series.isEmpty()) {
            cmbSeries.getSelectionModel().select(0);
            Serie serieSeleccionada = cmbSeries.getValue();
            if (serieSeleccionada != null) {
                cargarTemporadas(serieSeleccionada.getId());
            }
        }
    }

    /**
     * Carga las temporadas de una serie en el ComboBox
     * 
     * @param serieId ID de la serie seleccionada
     */
    private void cargarTemporadas(int serieId) {
        List<Temporada> temporadas = temporadaDAO.getAllBySerie(serieId);
        cmbTemporadas.setItems(FXCollections.observableArrayList(temporadas));

        // Si hay temporadas, seleccionar la primera por defecto y cargar sus episodios
        if (!temporadas.isEmpty()) {
            cmbTemporadas.getSelectionModel().select(0);
            Temporada temporadaSeleccionada = cmbTemporadas.getValue();
            if (temporadaSeleccionada != null) {
                cargarEpisodios(temporadaSeleccionada.getId());
            }
        } else {
            episodiosList.clear();
            tableEpisodios.setItems(episodiosList);
            limpiarFormulario();
        }
    }

    /**
     * Carga los episodios de una temporada en la tabla
     * 
     * @param temporadaId ID de la temporada seleccionada
     */
    private void cargarEpisodios(int temporadaId) {
        episodiosList.clear();
        episodiosList.addAll(episodioDAO.getAllByTemporada(temporadaId));
        tableEpisodios.setItems(episodiosList);

        // Actualizar estado
        lblEstado.setText("Episodios cargados: " + episodiosList.size());

        // Limpiar formulario si no hay episodios
        if (episodiosList.isEmpty()) {
            limpiarFormulario();
        } else {
            // Seleccionar el primer episodio
            tableEpisodios.getSelectionModel().select(0);
        }
    }

    /**
     * Muestra los detalles de un episodio en el formulario
     * 
     * @param episodio Episodio a mostrar
     */
    private void mostrarDetalleEpisodio(Episodio episodio) {
        txtTitulo.setText(episodio.getTitulo());
        txtDescripcion.setText(episodio.getDescripcion());
        txtNumero.setText(String.valueOf(episodio.getNumero()));
        txtDuracion.setText(episodio.getDuracion() != null ? episodio.getDuracion().toString() : "");
        dateFechaEstreno.setValue(episodio.getFechaEstreno());
        txtDirector.setText(episodio.getDirector());
        txtGuionista.setText(episodio.getGuionista());

        Double rating = episodio.getRating();
        sliderRating.setValue(rating != null ? rating : 0);
        lblRating.setText(rating != null ? String.format("%.1f", rating) : "0.0");
    }

    /**
     * Acción del botón nuevo episodio
     */
    @FXML
    private void onNuevoEpisodioAction(ActionEvent event) {
        Temporada temporada = cmbTemporadas.getValue();
        if (temporada == null) {
            mostrarAlerta("Error", "Debe seleccionar una temporada para crear un episodio.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Nuevo Episodio");
        dialog.setHeaderText("Crear nuevo episodio para la Temporada " + temporada.getNumero());
        dialog.setContentText("Título del episodio:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(titulo -> {
            if (!titulo.trim().isEmpty()) {
                int nuevoNumero = episodioDAO.getNextEpisodeNumber(temporada.getId());
                Episodio nuevoEpisodio = episodioDAO.create(temporada.getId(), nuevoNumero, titulo.trim());
                episodiosList.add(nuevoEpisodio);
                tableEpisodios.getSelectionModel().select(nuevoEpisodio);
                lblEstado.setText("Episodio creado: " + nuevoEpisodio.getTitulo());
            }
        });
    }

    /**
     * Acción del botón guardar cambios
     */
    @FXML
    private void onGuardarAction(ActionEvent event) {
        if (episodioActual != null) {
            episodioActual.setTitulo(txtTitulo.getText());
            episodioActual.setDescripcion(txtDescripcion.getText());

            try {
                episodioActual.setNumero(Integer.parseInt(txtNumero.getText()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El número de episodio debe ser un valor numérico.");
                return;
            }

            try {
                episodioActual.setDuracion(
                        txtDuracion.getText().isEmpty() ? null : Integer.parseInt(txtDuracion.getText()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "La duración debe ser un número válido.");
                return;
            }

            episodioActual.setFechaEstreno(dateFechaEstreno.getValue());
            episodioActual.setDirector(txtDirector.getText().isEmpty() ? null : txtDirector.getText());
            episodioActual.setGuionista(txtGuionista.getText().isEmpty() ? null : txtGuionista.getText());
            episodioActual.setRating(sliderRating.getValue() > 0 ? sliderRating.getValue() : null);

            // Refrescar la tabla
            int selectedIndex = tableEpisodios.getSelectionModel().getSelectedIndex();
            tableEpisodios.getItems().set(selectedIndex, episodioActual);

            lblEstado.setText("Episodio actualizado: " + episodioActual.getTitulo());
            mostrarAlerta("Éxito", "El episodio se ha actualizado correctamente.");
        }
    }

    /**
     * Acción del botón eliminar
     */
    @FXML
    private void onEliminarAction(ActionEvent event) {
        if (episodioActual != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar episodio");
            alert.setContentText("¿Está seguro que desea eliminar el episodio \"" + episodioActual.getTitulo() + "\"?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean eliminado = episodioDAO.delete(episodioActual.getId());
                if (eliminado) {
                    episodiosList.remove(episodioActual);
                    lblEstado.setText("Episodio eliminado: " + episodioActual.getTitulo());
                    mostrarAlerta("Éxito", "El episodio se ha eliminado correctamente.");

                    // Limpiar selección o seleccionar otro episodio
                    if (!episodiosList.isEmpty()) {
                        tableEpisodios.getSelectionModel().select(0);
                    } else {
                        limpiarFormulario();
                    }
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el episodio.");
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
        txtNumero.clear();
        txtDuracion.clear();
        dateFechaEstreno.setValue(null);
        txtDirector.clear();
        txtGuionista.clear();
        sliderRating.setValue(0);
        lblRating.setText("0.0");
        episodioActual = null;
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