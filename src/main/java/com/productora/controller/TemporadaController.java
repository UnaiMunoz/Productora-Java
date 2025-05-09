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
 * Controlador para la vista de Temporadas
 */
public class TemporadaController implements Initializable {

    @FXML
    private ComboBox<Serie> cmbSeries;

    @FXML
    private TableView<Temporada> tableTemporadas;

    @FXML
    private TableColumn<Temporada, Integer> colId;

    @FXML
    private TableColumn<Temporada, Integer> colNumero;

    @FXML
    private TableColumn<Temporada, String> colTitulo;

    @FXML
    private TableColumn<Temporada, String> colFechaEstreno;

    @FXML
    private TableColumn<Temporada, String> colFechaFin;

    @FXML
    private TableColumn<Temporada, Integer> colNumEpisodios;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextField txtNumero;

    @FXML
    private TextField txtNumEpisodios;

    @FXML
    private DatePicker dateFechaEstreno;

    @FXML
    private DatePicker dateFechaFin;

    @FXML
    private ListView<Episodio> listEpisodios;

    @FXML
    private Label lblEstado;

    @FXML
    private VBox detailPane;

    private SerieDAO serieDAO;
    private TemporadaDAO temporadaDAO;
    private EpisodioDAO episodioDAO;
    private ObservableList<Temporada> temporadasList;
    private ObservableList<Episodio> episodiosList;
    private Temporada temporadaActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serieDAO = new SerieDAO();
        temporadaDAO = new TemporadaDAO();
        episodioDAO = new EpisodioDAO();
        temporadasList = FXCollections.observableArrayList();
        episodiosList = FXCollections.observableArrayList();

        // Configurar columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        // Para la fecha de estreno formateamos a "dd/MM/yyyy"
        colFechaEstreno.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaEstreno();
            if (fecha != null) {
                return new ReadOnlyStringWrapper(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                return new ReadOnlyStringWrapper("");
            }
        });

        // Para la fecha de fin formateamos a "dd/MM/yyyy"
        colFechaFin.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaFin();
            if (fecha != null) {
                return new ReadOnlyStringWrapper(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                return new ReadOnlyStringWrapper("");
            }
        });

        colNumEpisodios.setCellValueFactory(new PropertyValueFactory<>("numEpisodios"));

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
                temporadasList.clear();
                episodiosList.clear();
            }
        });

        // Manejar selección de temporada
        tableTemporadas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                temporadaActual = newSel;
                mostrarDetalleTemporada(temporadaActual);
                cargarEpisodiosDeTemporada(temporadaActual.getId());
            }
        });
    }

    /**
     * Carga todas las series en el ComboBox
     */
    private void cargarSeries() {
        List<Serie> series = serieDAO.getAll();
        cmbSeries.setItems(FXCollections.observableArrayList(series));

        // Si hay series, seleccionar la primera por defecto y cargar sus temporadas
        if (!series.isEmpty()) {
            cmbSeries.getSelectionModel().select(0);
            Serie serieSeleccionada = cmbSeries.getValue();
            if (serieSeleccionada != null) {
                cargarTemporadas(serieSeleccionada.getId());
            }
        }
    }

    /**
     * Carga las temporadas de una serie en la tabla
     */
    private void cargarTemporadas(int serieId) {
        temporadasList.clear();
        temporadasList.addAll(temporadaDAO.getAllBySerie(serieId));
        tableTemporadas.setItems(temporadasList);

        // Actualizar estado
        lblEstado.setText("Temporadas cargadas: " + temporadasList.size());

        // Limpiar formulario si no hay temporadas
        if (temporadasList.isEmpty()) {
            limpiarFormulario();
        } else {
            // Seleccionar la primera temporada
            tableTemporadas.getSelectionModel().select(0);
        }
    }

    /**
     * Carga los episodios de una temporada en la lista
     * 
     * @param temporadaId ID de la temporada
     */
    private void cargarEpisodiosDeTemporada(int temporadaId) {
        episodiosList.clear();
        episodiosList.addAll(episodioDAO.getAllByTemporada(temporadaId));
        listEpisodios.setItems(episodiosList);
    }

    /**
     * Muestra los detalles de una temporada en el formulario
     */
    private void mostrarDetalleTemporada(Temporada temporada) {
        txtTitulo.setText(temporada.getTitulo());
        txtNumero.setText(String.valueOf(temporada.getNumero()));
        txtNumEpisodios.setText(temporada.getNumEpisodios() != null ? temporada.getNumEpisodios().toString() : "0");
        dateFechaEstreno.setValue(temporada.getFechaEstreno());
        dateFechaFin.setValue(temporada.getFechaFin());
    }

    /**
     * Acción del botón nueva temporada
     */
    @FXML
    private void onNuevaTemporadaAction(ActionEvent event) {
        Serie serie = cmbSeries.getValue();
        if (serie == null) {
            mostrarAlerta("Error", "Debe seleccionar una serie para crear una temporada.");
            return;
        }

        int nuevoNumero = temporadaDAO.getNextSeasonNumber(serie.getId());

        TextInputDialog dialog = new TextInputDialog("Temporada " + nuevoNumero);
        dialog.setTitle("Nueva Temporada");
        dialog.setHeaderText("Crear nueva temporada para " + serie.getTitulo());
        dialog.setContentText("Título de la temporada:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(titulo -> {
            if (!titulo.trim().isEmpty()) {
                Temporada nuevaTemporada = temporadaDAO.create(serie.getId(), nuevoNumero, titulo.trim());
                temporadasList.add(nuevaTemporada);
                tableTemporadas.getSelectionModel().select(nuevaTemporada);
                lblEstado.setText("Temporada creada: " + nuevaTemporada.getTitulo());
            }
        });
    }

    /**
     * Acción del botón nuevo episodio
     */
    @FXML
    private void onNuevoEpisodioAction(ActionEvent event) {
        if (temporadaActual == null) {
            mostrarAlerta("Error", "Debe seleccionar una temporada para crear un episodio.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Nuevo Episodio");
        dialog.setHeaderText("Crear nuevo episodio para la " + temporadaActual.getTitulo());
        dialog.setContentText("Título del episodio:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(titulo -> {
            if (!titulo.trim().isEmpty()) {
                int nuevoNumero = episodioDAO.getNextEpisodeNumber(temporadaActual.getId());
                Episodio nuevoEpisodio = episodioDAO.create(temporadaActual.getId(), nuevoNumero, titulo.trim());
                episodiosList.add(nuevoEpisodio);
                lblEstado.setText("Episodio creado: " + nuevoEpisodio.getTitulo());

                // Actualizar el contador de episodios
                temporadaActual.actualizarNumEpisodios();
                txtNumEpisodios.setText(temporadaActual.getNumEpisodios().toString());

                // Refrescar la tabla de temporadas para mostrar el nuevo valor
                int selectedIndex = tableTemporadas.getSelectionModel().getSelectedIndex();
                tableTemporadas.getItems().set(selectedIndex, temporadaActual);
            }
        });
    }

    /**
     * Acción del botón guardar cambios
     */
    @FXML
    private void onGuardarAction(ActionEvent event) {
        if (temporadaActual != null) {
            temporadaActual.setTitulo(txtTitulo.getText());

            try {
                temporadaActual.setNumero(Integer.parseInt(txtNumero.getText()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El número de temporada debe ser un valor numérico.");
                return;
            }

            temporadaActual.setFechaEstreno(dateFechaEstreno.getValue());
            temporadaActual.setFechaFin(dateFechaFin.getValue());

            // Refrescar la tabla
            int selectedIndex = tableTemporadas.getSelectionModel().getSelectedIndex();
            tableTemporadas.getItems().set(selectedIndex, temporadaActual);

            lblEstado.setText("Temporada actualizada: " + temporadaActual.getTitulo());
            mostrarAlerta("Éxito", "La temporada se ha actualizado correctamente.");
        }
    }

    /**
     * Acción del botón eliminar
     */
    @FXML
    private void onEliminarAction(ActionEvent event) {
        if (temporadaActual != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar temporada");
            alert.setContentText(
                    "¿Está seguro que desea eliminar la temporada \"" + temporadaActual.getTitulo() + "\"?\n" +
                            "Esta acción eliminará también todos los episodios asociados.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean eliminado = temporadaDAO.delete(temporadaActual.getId());
                if (eliminado) {
                    temporadasList.remove(temporadaActual);
                    episodiosList.clear();
                    lblEstado.setText("Temporada eliminada: " + temporadaActual.getTitulo());
                    mostrarAlerta("Éxito", "La temporada se ha eliminado correctamente.");

                    // Limpiar selección o seleccionar otra temporada
                    if (!temporadasList.isEmpty()) {
                        tableTemporadas.getSelectionModel().select(0);
                    } else {
                        limpiarFormulario();
                    }
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la temporada.");
                }
            }
        }
    }

    /**
     * Limpia el formulario de detalles
     */
    private void limpiarFormulario() {
        txtTitulo.clear();
        txtNumero.clear();
        txtNumEpisodios.clear();
        dateFechaEstreno.setValue(null);
        dateFechaFin.setValue(null);
        episodiosList.clear();
        listEpisodios.setItems(episodiosList);
        temporadaActual = null;
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