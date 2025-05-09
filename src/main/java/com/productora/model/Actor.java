package com.productora.model;

import com.productora.AppData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que representa un Actor.
 * Implementa la persistencia automática en la base de datos.
 */
public class Actor {
    private int id;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private String biografia;
    private String imagenUrl;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private final AppData appData;

    // Formato para fechas
    private static final DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructor para crear un nuevo actor
     */
    public Actor(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.appData = AppData.getInstance();

        // Insertar en la base de datos al crear
        String sql = "INSERT INTO Actores (nombre, apellido, fecha_creacion, fecha_modificacion) VALUES ('"
                + this.nombre + "', '" + this.apellido + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        this.id = appData.insertAndGetId(sql);
        loadFromDatabase();
    }

    /**
     * Constructor para cargar un actor existente
     */
    public Actor(int id) {
        this.id = id;
        this.appData = AppData.getInstance();
        loadFromDatabase();
    }

    /**
     * Carga los datos del actor desde la base de datos
     */
    private void loadFromDatabase() {
        String sql = "SELECT * FROM Actores WHERE id = " + this.id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            HashMap<String, Object> row = resultado.get(0);

            this.nombre = (String) row.get("nombre");
            this.apellido = (String) row.get("apellido");
            this.nacionalidad = (String) row.get("nacionalidad");
            this.biografia = (String) row.get("biografia");
            this.imagenUrl = (String) row.get("imagen_url");

            // Convertir strings de fecha a LocalDate y LocalDateTime
            String fechaNacimientoStr = (String) row.get("fecha_nacimiento");
            String fechaCreacionStr = (String) row.get("fecha_creacion");
            String fechaModificacionStr = (String) row.get("fecha_modificacion");

            if (fechaNacimientoStr != null) {
                this.fechaNacimiento = LocalDate.parse(fechaNacimientoStr, formatterDate);
            }

            if (fechaCreacionStr != null) {
                this.fechaCreacion = LocalDateTime.parse(fechaCreacionStr, formatterDateTime);
            }

            if (fechaModificacionStr != null) {
                this.fechaModificacion = LocalDateTime.parse(fechaModificacionStr, formatterDateTime);
            }
        }
    }

    /**
     * Actualiza el actor en la base de datos
     */
    private void updateDatabase() {
        // Actualizar la fecha de modificación
        this.fechaModificacion = LocalDateTime.now();

        StringBuilder sql = new StringBuilder("UPDATE Actores SET ");
        sql.append("nombre = '").append(this.nombre.replace("'", "''")).append("', ");
        sql.append("apellido = '").append(this.apellido.replace("'", "''")).append("', ");

        // Añadir campos que pueden ser nulos con comprobación
        sql.append("fecha_nacimiento = ").append(this.fechaNacimiento != null ? 
                  "'" + this.fechaNacimiento.format(formatterDate) + "'" : "NULL").append(", ");
        sql.append("nacionalidad = ").append(this.nacionalidad != null ? 
                  "'" + this.nacionalidad.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append("biografia = ").append(this.biografia != null ? 
                  "'" + this.biografia.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append("imagen_url = ").append(this.imagenUrl != null ? 
                  "'" + this.imagenUrl.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append("fecha_modificacion = CURRENT_TIMESTAMP ");

        sql.append("WHERE id = ").append(this.id);

        appData.update(sql.toString());
    }

    /**
     * Elimina el actor de la base de datos
     */
    public boolean delete() {
        try {
            String sql = "DELETE FROM Actores WHERE id = " + this.id;
            appData.update(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar el actor: " + e.getMessage());
            return false;
        }
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public String getNombre() {
        loadFromDatabase();
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        updateDatabase();
    }

    public String getApellido() {
        loadFromDatabase();
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
        updateDatabase();
    }

    public LocalDate getFechaNacimiento() {
        loadFromDatabase();
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
        updateDatabase();
    }

    public String getNacionalidad() {
        loadFromDatabase();
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
        updateDatabase();
    }

    public String getBiografia() {
        loadFromDatabase();
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
        updateDatabase();
    }

    public String getImagenUrl() {
        loadFromDatabase();
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
        updateDatabase();
    }

    public LocalDateTime getFechaCreacion() {
        loadFromDatabase();
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        loadFromDatabase();
        return fechaModificacion;
    }

    /**
     * Obtiene todos los actores de la base de datos
     */
    public static ArrayList<Actor> obtenerTodos() {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Actores ORDER BY nombre, apellido";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Actor> actores = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            actores.add(new Actor(id));
        }

        return actores;
    }

    @Override
    public String toString() {
        return this.nombre + " " + this.apellido;
    }
}