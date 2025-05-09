package com.productora.model;

import com.productora.AppData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que representa una Temporada de una serie de televisión.
 * Implementa la persistencia automática en la base de datos.
 */
public class Temporada {
    private int id;
    private int numero;
    private String titulo;
    private int serieId;
    private LocalDate fechaEstreno;
    private LocalDate fechaFin;
    private Integer numEpisodios;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private final AppData appData;

    // Formato para fechas
    private static final DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructor para crear una nueva temporada
     */
    public Temporada(int serieId, int numero, String titulo) {
        this.serieId = serieId;
        this.numero = numero;
        this.titulo = titulo != null ? titulo : "Temporada " + numero;
        this.appData = AppData.getInstance();

        // Insertar en la base de datos al crear
        String sql = "INSERT INTO Temporadas (serie_id, numero, titulo, fecha_creacion, fecha_modificacion) "
                + "VALUES (" + this.serieId + ", " + this.numero + ", '" + this.titulo 
                + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        this.id = appData.insertAndGetId(sql);
        loadFromDatabase();
    }

    /**
     * Constructor sobrecargado si no se proporciona título
     */
    public Temporada(int serieId, int numero) {
        this(serieId, numero, null);
    }

    /**
     * Constructor para cargar una temporada existente
     */
    public Temporada(int id) {
        this.id = id;
        this.appData = AppData.getInstance();
        loadFromDatabase();
    }

    /**
     * Carga los datos de la temporada desde la base de datos
     */
    private void loadFromDatabase() {
        String sql = "SELECT * FROM Temporadas WHERE id = " + this.id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            HashMap<String, Object> row = resultado.get(0);

            this.numero = ((Number) row.get("numero")).intValue();
            this.titulo = (String) row.get("titulo");
            this.serieId = ((Number) row.get("serie_id")).intValue();
            this.numEpisodios = row.get("num_episodios") != null ? 
                                ((Number) row.get("num_episodios")).intValue() : null;

            // Convertir strings de fecha a LocalDate y LocalDateTime
            String fechaEstrenoStr = (String) row.get("fecha_estreno");
            String fechaFinStr = (String) row.get("fecha_fin");
            String fechaCreacionStr = (String) row.get("fecha_creacion");
            String fechaModificacionStr = (String) row.get("fecha_modificacion");

            if (fechaEstrenoStr != null) {
                this.fechaEstreno = LocalDate.parse(fechaEstrenoStr, formatterDate);
            }

            if (fechaFinStr != null) {
                this.fechaFin = LocalDate.parse(fechaFinStr, formatterDate);
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
     * Actualiza la temporada en la base de datos
     */
    private void updateDatabase() {
        // Actualizar la fecha de modificación
        this.fechaModificacion = LocalDateTime.now();

        StringBuilder sql = new StringBuilder("UPDATE Temporadas SET ");
        sql.append("numero = ").append(this.numero).append(", ");
        sql.append("titulo = '").append(this.titulo.replace("'", "''")).append("', ");
        sql.append("serie_id = ").append(this.serieId).append(", ");

        // Añadir campos que pueden ser nulos con comprobación
        sql.append("fecha_estreno = ").append(this.fechaEstreno != null ? 
                  "'" + this.fechaEstreno.format(formatterDate) + "'" : "NULL").append(", ");
        sql.append("fecha_fin = ").append(this.fechaFin != null ? 
                  "'" + this.fechaFin.format(formatterDate) + "'" : "NULL").append(", ");
        sql.append("num_episodios = ").append(this.numEpisodios != null ? this.numEpisodios : "NULL").append(", ");
        sql.append("fecha_modificacion = CURRENT_TIMESTAMP ");

        sql.append("WHERE id = ").append(this.id);

        appData.update(sql.toString());
    }

    /**
     * Elimina la temporada de la base de datos
     */
    public boolean delete() {
        try {
            String sql = "DELETE FROM Temporadas WHERE id = " + this.id;
            appData.update(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar la temporada: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una representación de la serie a la que pertenece la temporada
     */
    public Serie getSerie() {
        return new Serie(this.serieId);
    }

    /**
     * Actualiza el contador de episodios
     */
    public void actualizarNumEpisodios() {
        String sql = "SELECT COUNT(*) as total FROM Episodios WHERE temporada_id = " + this.id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        
        if (resultado.size() > 0) {
            this.numEpisodios = ((Number) resultado.get(0).get("total")).intValue();
            updateDatabase();
        }
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public int getNumero() {
        loadFromDatabase();
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
        updateDatabase();
    }

    public String getTitulo() {
        loadFromDatabase();
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        updateDatabase();
    }

    public int getSerieId() {
        loadFromDatabase();
        return serieId;
    }

    public void setSerieId(int serieId) {
        this.serieId = serieId;
        updateDatabase();
    }

    public LocalDate getFechaEstreno() {
        loadFromDatabase();
        return fechaEstreno;
    }

    public void setFechaEstreno(LocalDate fechaEstreno) {
        this.fechaEstreno = fechaEstreno;
        updateDatabase();
    }

    public LocalDate getFechaFin() {
        loadFromDatabase();
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
        updateDatabase();
    }

    public Integer getNumEpisodios() {
        loadFromDatabase();
        return numEpisodios;
    }

    public void setNumEpisodios(Integer numEpisodios) {
        this.numEpisodios = numEpisodios;
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
     * Obtiene todas las temporadas de una serie
     */
    public static ArrayList<Temporada> obtenerPorSerie(int serieId) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Temporadas WHERE serie_id = " + serieId + " ORDER BY numero";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Temporada> temporadas = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            temporadas.add(new Temporada(id));
        }

        return temporadas;
    }

    @Override
    public String toString() {
        Serie serie = getSerie();
        return serie.getTitulo() + " - Temporada " + this.numero + (this.titulo != null ? " (" + this.titulo + ")" : "");
    }
}