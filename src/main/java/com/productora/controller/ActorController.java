package com.productora.controller;

import com.productora.dao.ActorDAO;
import com.productora.dao.ActorSerieDAO;
import com.productora.dao.SerieDAO;
import com.productora.model.Actor;
import com.productora.model.ActorSerie;
import com.productora.model.Serie;

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
 * Controlador para la vista de Actores
 */
public class ActorController implements Initializable {

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<Actor> tableActores;

    @FXML
    private TableColumn<Actor, Integer> colId;

    @FXML
    private TableColumn<Actor, String> colNombre;

    @FXML
    private TableColumn<Actor, String> colApellido;

    @FXML
    private TableColumn<Actor, String> colNacimiento;

    @FXML
    private TableColumn<Actor, String> colNacionalidad;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private DatePicker dateFechaNacimiento;

    @FXML
    private TextField txtNacionalidad;

    @FXML
    private TextArea txtBiografia;

    @FXML
    private TextField txtImagenUrl;

    @FXML
    private TableView<ActorSerie> tableParticipaciones;

    @FXML
    private TableColumn<ActorSerie, String> colSerie;

    @FXML
    private TableColumn<ActorSerie, String> colPersonaje;

    @FXML
    private TableColumn<ActorSerie, String> colRol;

    @FXML
    private TableColumn<ActorSerie, String> colTemporadas;

    @FXML
    private Label lblEstado;

    @FXML
    private VBox detailPane;

    private ActorDAO actorDAO;
    private ActorSerieDAO actorSerieDAO;
    private SerieDAO serieDAO;
    private ObservableList<Actor> actoresList;
    private ObservableList<ActorSerie> participacionesList;
    private Actor actorActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actorDAO = new ActorDAO();
        actorSerieDAO = new ActorSerieDAO();
        serieDAO = new SerieDAO();
        actoresList = FXCollections.observableArrayList();
        participacionesList = FXCollections.observableArrayList();

        // Configurar columnas de la tabla de actores
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        
        // Para la fecha de nacimiento formateamos a "dd/MM/yyyy"
        colNacimiento.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaNacimiento();
            if (fecha != null) {
                return new ReadOnlyStringWrapper(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                return new ReadOnlyStringWrapper("");
            }
        });
        
        colNacionalidad.setCellValueFactory(new PropertyValueFactory<>("nacionalidad"));
        
        // Configurar columnas de la tabla de participaciones
        colSerie.setCellValueFactory(cellData -> {
            Serie serie = cellData.getValue().getSerie();
            return new ReadOnlyStringWrapper(serie != null ? serie.getTitulo() : "");
        });
        
        colPersonaje.setCellValueFactory(new PropertyValueFactory<>("personaje"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colTemporadas.setCellValueFactory(new PropertyValueFactory<>("temporadasParticipacion"));

        // Cargar actores iniciales
        cargarActores();

        // Manejar selección de actor
        tableActores.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                actorActual = newSel;
                mostrarDetalleActor(actorActual);
                cargarParticipaciones(actorActual.getId());
            }
        });
    }

    /**
     * Carga todos los actores en la tabla
     */
    private void cargarActores() {
        actoresList.clear();
        actoresList.addAll(actorDAO.getAll());
        tableActores.setItems(actoresList);
        
        // Actualizar estado
        lblEstado.setText("Actores cargados: " + actoresList.size());
        
        // Si hay actores, seleccionar el primero
        if (!actoresList.isEmpty()) {
            tableActores.getSelectionModel().select(0);
        } else {
            limpiarFormulario();
        }
    }

    /**
     * Carga las participaciones de un actor en series
     * 
     * @param actorId ID del actor
     */
    private void cargarParticipaciones(int actorId) {
        participacionesList.clear();
        participacionesList.addAll(actorSerieDAO.getAllByActor(actorId));
        tableParticipaciones.setItems(participacionesList);
    }

    /**
     * Muestra los detalles de un actor en el formulario
     * 
     * @param actor Actor a mostrar
     */
    private void mostrarDetalleActor(Actor actor) {
        txtNombre.setText(actor.getNombre());
        txtApellido.setText(actor.getApellido());
        dateFechaNacimiento.setValue(actor.getFechaNacimiento());
        txtNacionalidad.setText(actor.getNacionalidad());
        txtBiografia.setText(actor.getBiografia());
        txtImagenUrl.setText(actor.getImagenUrl());
    }

    /**
     * Acción del botón buscar
     */
    @FXML
    private void onBuscarAction(ActionEvent event) {
        String textoBusqueda = txtBuscar.getText().trim();
        if (!textoBusqueda.isEmpty()) {
            actoresList.clear();
            actoresList.addAll(actorDAO.findByName(textoBusqueda));
            tableActores.setItems(actoresList);
            lblEstado.setText("Resultados de búsqueda: " + actoresList.size());
        } else {
            cargarActores();
        }
    }

    /**
     * Acción del botón nuevo actor
     */
    @FXML
    private void onNuevoActorAction(ActionEvent event) {
        Dialog<Actor> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Actor");
        dialog.setHeaderText("Crear nuevo actor");

        // Configurar botones
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // Crear campos del formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre");
        TextField apellido = new TextField();
        apellido.setPromptText("Apellido");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(apellido, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                String nombreStr = nombre.getText().trim();
                String apellidoStr = apellido.getText().trim();
                
                if (!nombreStr.isEmpty() && !apellidoStr.isEmpty()) {
                    return actorDAO.create(nombreStr, apellidoStr);
                }
            }
            return null;
        });

        Optional<Actor> result = dialog.showAndWait();
        result.ifPresent(nuevoActor -> {
            actoresList.add(nuevoActor);
            tableActores.getSelectionModel().select(nuevoActor);
            lblEstado.setText("Actor creado: " + nuevoActor.getNombre() + " " + nuevoActor.getApellido());
        });
    }

    /**
     * Acción del botón guardar cambios
     */
    @FXML
    private void onGuardarAction(ActionEvent event) {
        if (actorActual != null) {
            actorActual.setNombre(txtNombre.getText());
            actorActual.setApellido(txtApellido.getText());
            actorActual.setFechaNacimiento(dateFechaNacimiento.getValue());
            actorActual.setNacionalidad(txtNacionalidad.getText());
            actorActual.setBiografia(txtBiografia.getText());
            actorActual.setImagenUrl(txtImagenUrl.getText());

            // Refrescar la tabla
            int selectedIndex = tableActores.getSelectionModel().getSelectedIndex();
            tableActores.getItems().set(selectedIndex, actorActual);
            
            lblEstado.setText("Actor actualizado: " + actorActual.getNombre() + " " + actorActual.getApellido());
            mostrarAlerta("Éxito", "El actor se ha actualizado correctamente.");
        }
    }

    /**
     * Acción del botón eliminar
     */
    @FXML
    private void onEliminarAction(ActionEvent event) {
        if (actorActual != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar actor");
            alert.setContentText("¿Está seguro que desea eliminar el actor \"" + actorActual.getNombre() + " " + 
                               actorActual.getApellido() + "\"?\n" +
                               "Esta acción eliminará también todas sus participaciones en series.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean eliminado = actorDAO.delete(actorActual.getId());
                if (eliminado) {
                    actoresList.remove(actorActual);
                    participacionesList.clear();
                    lblEstado.setText("Actor eliminado: " + actorActual.getNombre() + " " + actorActual.getApellido());
                    mostrarAlerta("Éxito", "El actor se ha eliminado correctamente.");
                    
                    // Limpiar selección o seleccionar otro actor
                    if (!actoresList.isEmpty()) {
                        tableActores.getSelectionModel().select(0);
                    } else {
                        limpiarFormulario();
                    }
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el actor.");
                }
            }
        }
    }

    /**
     * Acción del botón nueva participación
     */
    @FXML
    private void onNuevaParticipacionAction(ActionEvent event) {
        if (actorActual == null) {
            mostrarAlerta("Error", "Debe seleccionar un actor para añadir una participación.");
            return;
        }

        Dialog<ActorSerie> dialog = new Dialog<>();
        dialog.setTitle("Nueva Participación");
        dialog.setHeaderText("Añadir participación de " + actorActual.getNombre() + " " + actorActual.getApellido() + " en una serie");

        // Configurar botones
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // Crear campos del formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<Serie> serieCmb = new ComboBox<>();
        serieCmb.setItems(FXCollections.observableArrayList(serieDAO.getAll()));
        serieCmb.setConverter(new StringConverter<Serie>() {
            @Override
            public String toString(Serie serie) {
                return serie != null ? serie.getTitulo() : "";
            }

            @Override
            public Serie fromString(String string) {
                return null;
            }
        });
        
        TextField personaje = new TextField();
        personaje.setPromptText("Personaje");
        
        TextField rol = new TextField();
        rol.setPromptText("Protagonista/Secundario/Recurrente");
        
        TextField temporadas = new TextField();
        temporadas.setPromptText("1,2,3...");

        grid.add(new Label("Serie:"), 0, 0);
        grid.add(serieCmb, 1, 0);
        grid.add(new Label("Personaje:"), 0, 1);
        grid.add(personaje, 1, 1);
        grid.add(new Label("Rol:"), 0, 2);
        grid.add(rol, 1, 2);
        grid.add(new Label("Temporadas:"), 0, 3);
        grid.add(temporadas, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                Serie serie = serieCmb.getValue();
                String personajeStr = personaje.getText().trim();
                String rolStr = rol.getText().trim();
                String temporadasStr = temporadas.getText().trim();
                
                if (serie != null && !personajeStr.isEmpty()) {
                    return actorSerieDAO.create(actorActual.getId(), serie.getId(), personajeStr, rolStr, temporadasStr);
                }
            }
            return null;
        });

        Optional<ActorSerie> result = dialog.showAndWait();
        result.ifPresent(nuevaParticipacion -> {
            participacionesList.add(nuevaParticipacion);
            lblEstado.setText("Participación añadida: " + nuevaParticipacion.getPersonaje() + " en " + 
                            nuevaParticipacion.getSerie().getTitulo());
        });
    }

    /**
     * Acción del botón eliminar participación
     */
    @FXML
    private void onEliminarParticipacionAction(ActionEvent event) {
        ActorSerie participacion = tableParticipaciones.getSelectionModel().getSelectedItem();
        if (participacion == null) {
            mostrarAlerta("Error", "Debe seleccionar una participación para eliminarla.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar participación");
        alert.setContentText("¿Está seguro que desea eliminar la participación como \"" + 
                           participacion.getPersonaje() + "\" en " + participacion.getSerie().getTitulo() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean eliminado = actorSerieDAO.delete(participacion.getId());
            if (eliminado) {
                participacionesList.remove(participacion);
                lblEstado.setText("Participación eliminada: " + participacion.getPersonaje());
            } else {
                mostrarAlerta("Error", "No se pudo eliminar la participación.");
            }
        }
    }
    
    /**
     * Limpia el formulario de detalles
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        dateFechaNacimiento.setValue(null);
        txtNacionalidad.clear();
        txtBiografia.clear();
        txtImagenUrl.clear();
        participacionesList.clear();
        tableParticipaciones.setItems(participacionesList);
        actorActual = null;
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