package com.productora.model;

import com.productora.AppData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que representa la relación entre un Actor y una Serie.
 * Implementa la persistencia automática en la base de datos.
 */
public class ActorSerie {
    private int id;
    private int actorId;
    private int serieId;
    private String personaje;
    private String rol;
    private String temporadasParticipacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private final AppData appData;

    // Formato para fechas
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor para crear una nueva relación entre actor y serie
     * 
     * @param actorId ID del actor
     * @param serieId ID de la serie
     * @param personaje Nombre del personaje
     * @param rol Rol del actor (protagonista, secundario, etc.)
     * @param temporadasParticipacion Temporadas en las que participa (formato: "1,2,3")
     */
    public int getSerieId() {
        loadFromDatabase();
        return serieId;
    }

    public void setSerieId(int serieId) {
        this.serieId = serieId;
        updateDatabase();
    }

    public String getPersonaje() {
        loadFromDatabase();
        return personaje;
    }

    public void setPersonaje(String personaje) {
        this.personaje = personaje;
        updateDatabase();
    }

    public String getRol() {
        loadFromDatabase();
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
        updateDatabase();
    }

    public String getTemporadasParticipacion() {
        loadFromDatabase();
        return temporadasParticipacion;
    }

    public void setTemporadasParticipacion(String temporadasParticipacion) {
        this.temporadasParticipacion = temporadasParticipacion;
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
     * Obtiene todas las participaciones de un actor
     * 
     * @param actorId ID del actor
     * @return ArrayList con todas las participaciones
     */
    public static ArrayList<ActorSerie> obtenerPorActor(int actorId) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM ActoresSeries WHERE actor_id = " + actorId + " ORDER BY personaje";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<ActorSerie> participaciones = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            participaciones.add(new ActorSerie(id));
        }

        return participaciones;
    }

    /**
     * Obtiene todas las participaciones en una serie
     * 
     * @param serieId ID de la serie
     * @return ArrayList con todas las participaciones
     */
    public static ArrayList<ActorSerie> obtenerPorSerie(int serieId) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM ActoresSeries WHERE serie_id = " + serieId + " ORDER BY personaje";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<ActorSerie> participaciones = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            participaciones.add(new ActorSerie(id));
        }

        return participaciones;
    }

    @Override
    public String toString() {
        return getActor().toString() + " como " + this.personaje + " en " + getSerie().getTitulo();
    } ActorSerie(int actorId, int serieId, String personaje, String rol, String temporadasParticipacion) {
        this.actorId = actorId;
        this.serieId = serieId;
        this.personaje = personaje;
        this.rol = rol;
        this.temporadasParticipacion = temporadasParticipacion;
        this.appData = AppData.getInstance();

        // Insertar en la base de datos al crear
        StringBuilder sql = new StringBuilder("INSERT INTO ActoresSeries ");
        sql.append("(actor_id, serie_id, personaje, rol, temporadas_participacion, fecha_creacion, fecha_modificacion) ");
        sql.append("VALUES (").append(this.actorId).append(", ");
        sql.append(this.serieId).append(", ");
        sql.append("'").append(this.personaje.replace("'", "''")).append("', ");
        sql.append(this.rol != null ? "'" + this.rol.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append(this.temporadasParticipacion != null ? "'" + this.temporadasParticipacion + "'" : "NULL").append(", ");
        sql.append("CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");

        this.id = appData.insertAndGetId(sql.toString());
        loadFromDatabase();
    }

    /**
     * Constructor para cargar una relación existente
     * 
     * @param id Identificador de la relación
     */
    public ActorSerie(int id) {
        this.id = id;
        this.appData = AppData.getInstance();
        loadFromDatabase();
    }

    /**
     * Carga los datos de la relación desde la base de datos
     */
    private void loadFromDatabase() {
        String sql = "SELECT * FROM ActoresSeries WHERE id = " + this.id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            HashMap<String, Object> row = resultado.get(0);

            this.actorId = ((Number) row.get("actor_id")).intValue();
            this.serieId = ((Number) row.get("serie_id")).intValue();
            this.personaje = (String) row.get("personaje");
            this.rol = (String) row.get("rol");
            this.temporadasParticipacion = (String) row.get("temporadas_participacion");

            // Convertir strings de fecha a LocalDateTime
            String fechaCreacionStr = (String) row.get("fecha_creacion");
            String fechaModificacionStr = (String) row.get("fecha_modificacion");

            if (fechaCreacionStr != null) {
                this.fechaCreacion = LocalDateTime.parse(fechaCreacionStr, formatter);
            }

            if (fechaModificacionStr != null) {
                this.fechaModificacion = LocalDateTime.parse(fechaModificacionStr, formatter);
            }
        }
    }

    /**
     * Actualiza la relación en la base de datos
     */
    private void updateDatabase() {
        // Actualizar la fecha de modificación
        this.fechaModificacion = LocalDateTime.now();

        StringBuilder sql = new StringBuilder("UPDATE ActoresSeries SET ");
        sql.append("actor_id = ").append(this.actorId).append(", ");
        sql.append("serie_id = ").append(this.serieId).append(", ");
        sql.append("personaje = '").append(this.personaje.replace("'", "''")).append("', ");
        
        // Añadir campos que pueden ser nulos con comprobación
        sql.append("rol = ").append(this.rol != null ? "'" + this.rol.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append("temporadas_participacion = ").append(this.temporadasParticipacion != null ? 
                  "'" + this.temporadasParticipacion + "'" : "NULL").append(", ");
        sql.append("fecha_modificacion = CURRENT_TIMESTAMP ");

        sql.append("WHERE id = ").append(this.id);

        appData.update(sql.toString());
    }

    /**
     * Elimina la relación de la base de datos
     * 
     * @return true si se eliminó correctamente
     */
    public boolean delete() {
        try {
            String sql = "DELETE FROM ActoresSeries WHERE id = " + this.id;
            appData.update(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar la relación actor-serie: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el objeto Actor asociado
     * 
     * @return Actor
     */
    public Actor getActor() {
        return new Actor(this.actorId);
    }

    /**
     * Obtiene el objeto Serie asociado
     * 
     * @return Serie
     */
    public Serie getSerie() {
        return new Serie(this.serieId);
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public int getActorId() {
        loadFromDatabase();
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
        updateDatabase();
    }

    public