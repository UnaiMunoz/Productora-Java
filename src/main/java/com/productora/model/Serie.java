package com.productora.model;

import com.productora.AppData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que representa una Serie de televisión.
 * Implementa la persistencia automática en la base de datos.
 */
public class Serie {
    private int id;
    private String titulo;
    private String descripcion;
    private String genero;
    private Integer anyoInicio;
    private Integer anyoFin;
    private String productora;
    private Double presupuesto;
    private Double rating;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private final AppData appData;

    // Constantes para los estados posibles
    public static final String ESTADO_PRODUCCION = "En producción";
    public static final String ESTADO_FINALIZADA = "Finalizada";
    public static final String ESTADO_CANCELADA = "Cancelada";
    public static final String ESTADO_PREPRODUCCION = "En preproducción";

    // Formato para fechas
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor para crear una nueva serie
     */
    public Serie(String titulo) {
        this.titulo = titulo;
        this.appData = AppData.getInstance();

        // Insertar en la base de datos al crear
        String sql = "INSERT INTO Series (titulo, fecha_creacion, fecha_modificacion) VALUES ('"
                + this.titulo + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        this.id = appData.insertAndGetId(sql);
        loadFromDatabase();
    }

    /**
     * Constructor para cargar una serie existente
     */
    public Serie(int id) {
        this.id = id;
        this.appData = AppData.getInstance();
        loadFromDatabase();
    }

    /**
     * Carga los datos de la serie desde la base de datos
     */
    private void loadFromDatabase() {
        String sql = "SELECT * FROM Series WHERE id = " + this.id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            HashMap<String, Object> row = resultado.get(0);

            this.titulo = (String) row.get("titulo");
            this.descripcion = (String) row.get("descripcion");
            this.genero = (String) row.get("genero");
            this.anyoInicio = row.get("anyo_inicio") != null ? ((Number) row.get("anyo_inicio")).intValue() : null;
            this.anyoFin = row.get("anyo_fin") != null ? ((Number) row.get("anyo_fin")).intValue() : null;
            this.productora = (String) row.get("productora");
            this.presupuesto = row.get("presupuesto") != null ? ((Number) row.get("presupuesto")).doubleValue() : null;
            this.rating = row.get("rating") != null ? ((Number) row.get("rating")).doubleValue() : null;
            this.estado = (String) row.get("estado");

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
     * Actualiza la serie en la base de datos
     */
    private void updateDatabase() {
        // Actualizar la fecha de modificación
        this.fechaModificacion = LocalDateTime.now();

        StringBuilder sql = new StringBuilder("UPDATE Series SET ");
        sql.append("titulo = '").append(this.titulo).append("', ");

        // Añadir campos que pueden ser nulos con comprobación
        sql.append("descripcion = ").append(this.descripcion != null ? "'" + this.descripcion + "'" : "NULL")
                .append(", ");
        sql.append("genero = ").append(this.genero != null ? "'" + this.genero + "'" : "NULL").append(", ");
        sql.append("anyo_inicio = ").append(this.anyoInicio != null ? this.anyoInicio : "NULL").append(", ");
        sql.append("anyo_fin = ").append(this.anyoFin != null ? this.anyoFin : "NULL").append(", ");
        sql.append("productora = ").append(this.productora != null ? "'" + this.productora + "'" : "NULL").append(", ");
        sql.append("presupuesto = ").append(this.presupuesto != null ? this.presupuesto : "NULL").append(", ");
        sql.append("rating = ").append(this.rating != null ? this.rating : "NULL").append(", ");
        sql.append("estado = ").append(this.estado != null ? "'" + this.estado + "'" : "NULL").append(", ");
        sql.append("fecha_modificacion = CURRENT_TIMESTAMP ");

        sql.append("WHERE id = ").append(this.id);

        appData.update(sql.toString());
    }

    /**
     * Elimina la serie de la base de datos

     */
    public boolean delete() {
        try {
            String sql = "DELETE FROM Series WHERE id = " + this.id;
            appData.update(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar la serie: " + e.getMessage());
            return false;
        }
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public String getTitulo() {
        // Recargar desde la BD para asegurar datos actualizados
        loadFromDatabase();
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        updateDatabase();
    }

    public String getDescripcion() {
        loadFromDatabase();
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        updateDatabase();
    }

    public String getGenero() {
        loadFromDatabase();
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
        updateDatabase();
    }

    public Integer getAnyoInicio() {
        loadFromDatabase();
        return anyoInicio;
    }

    public void setAnyoInicio(Integer anyoInicio) {
        this.anyoInicio = anyoInicio;
        updateDatabase();
    }

    public Integer getAnyoFin() {
        loadFromDatabase();
        return anyoFin;
    }

    public void setAnyoFin(Integer anyoFin) {
        this.anyoFin = anyoFin;
        updateDatabase();
    }

    public String getProductora() {
        loadFromDatabase();
        return productora;
    }

    public void setProductora(String productora) {
        this.productora = productora;
        updateDatabase();
    }

    public Double getPresupuesto() {
        loadFromDatabase();
        return presupuesto;
    }

    public void setPresupuesto(Double presupuesto) {
        this.presupuesto = presupuesto;
        updateDatabase();
    }

    public Double getRating() {
        loadFromDatabase();
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
        updateDatabase();
    }

    public String getEstado() {
        loadFromDatabase();
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
     * Obtiene todas las series de la base de datos
     */
    public static ArrayList<Serie> obtenerTodas() {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Series ORDER BY titulo";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Serie> series = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            series.add(new Serie(id));
        }

        return series;
    }

    /**
     * Busca series por título
     */
    public static ArrayList<Serie> buscarPorTitulo(String titulo) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Series WHERE titulo LIKE '%" + titulo + "%' ORDER BY titulo";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Serie> series = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            series.add(new Serie(id));
        }

        return series;
    }

    @Override
    public String toString() {
        return this.titulo;
    }
}