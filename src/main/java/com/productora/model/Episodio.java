package com.productora.model;

import com.productora.AppData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que representa un Episodio de una serie de televisión.
 * Implementa la persistencia automática en la base de datos.
 */
public class Episodio {
    private int id;
    private int numero;
    private String titulo;
    private String descripcion;
    private Integer duracion; // en minutos
    private int temporadaId;
    private LocalDate fechaEstreno;
    private String director;
    private String guionista;
    private Double rating;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private final AppData appData;

    // Formato para fechas
    private static final DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructor para crear un nuevo episodio
     */
    public Episodio(int temporadaId, int numero, String titulo) {
        this.temporadaId = temporadaId;
        this.numero = numero;
        this.titulo = titulo;
        this.appData = AppData.getInstance();

        // Insertar en la base de datos al crear
        String sql = "INSERT INTO Episodios (temporada_id, numero, titulo, fecha_creacion, fecha_modificacion) "
                + "VALUES (" + this.temporadaId + ", " + this.numero + ", '" + this.titulo 
                + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        this.id = appData.insertAndGetId(sql);
        loadFromDatabase();
    }

    /**
     * Constructor para cargar un episodio existente

     */
    public Episodio(int id) {
        this.id = id;
        this.appData = AppData.getInstance();
        loadFromDatabase();
    }

    /**
     * Carga los datos del episodio desde la base de datos
     */
    private void loadFromDatabase() {
        String sql = "SELECT * FROM Episodios WHERE id = " + this.id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            HashMap<String, Object> row = resultado.get(0);

            this.numero = ((Number) row.get("numero")).intValue();
            this.titulo = (String) row.get("titulo");
            this.descripcion = (String) row.get("descripcion");
            this.duracion = row.get("duracion") != null ? ((Number) row.get("duracion")).intValue() : null;
            this.temporadaId = ((Number) row.get("temporada_id")).intValue();
            this.director = (String) row.get("director");
            this.guionista = (String) row.get("guionista");
            this.rating = row.get("rating") != null ? ((Number) row.get("rating")).doubleValue() : null;

            // Convertir strings de fecha a LocalDateTime y LocalDate
            String fechaEstrenoStr = (String) row.get("fecha_estreno");
            String fechaCreacionStr = (String) row.get("fecha_creacion");
            String fechaModificacionStr = (String) row.get("fecha_modificacion");

            if (fechaEstrenoStr != null) {
                this.fechaEstreno = LocalDate.parse(fechaEstrenoStr, formatterDate);
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
     * Actualiza el episodio en la base de datos
     */
    private void updateDatabase() {
        // Actualizar la fecha de modificación
        this.fechaModificacion = LocalDateTime.now();

        StringBuilder sql = new StringBuilder("UPDATE Episodios SET ");
        sql.append("numero = ").append(this.numero).append(", ");
        sql.append("titulo = '").append(this.titulo.replace("'", "''")).append("', ");
        sql.append("temporada_id = ").append(this.temporadaId).append(", ");

        // Añadir campos que pueden ser nulos con comprobación
        sql.append("descripcion = ").append(this.descripcion != null ? "'" + this.descripcion.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append("duracion = ").append(this.duracion != null ? this.duracion : "NULL").append(", ");
        sql.append("fecha_estreno = ").append(this.fechaEstreno != null ? "'" + this.fechaEstreno.format(formatterDate) + "'" : "NULL").append(", ");
        sql.append("director = ").append(this.director != null ? "'" + this.director.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append("guionista = ").append(this.guionista != null ? "'" + this.guionista.replace("'", "''") + "'" : "NULL").append(", ");
        sql.append("rating = ").append(this.rating != null ? this.rating : "NULL").append(", ");
        sql.append("fecha_modificacion = CURRENT_TIMESTAMP ");

        sql.append("WHERE id = ").append(this.id);

        appData.update(sql.toString());
    }

    /**
     * Elimina el episodio de la base de datos

     */
    public boolean delete() {
        try {
            String sql = "DELETE FROM Episodios WHERE id = " + this.id;
            appData.update(sql);
            return true;
        } catch (Exception e) {
            System.out.println("Error al eliminar el episodio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una representación de la temporada a la que pertenece el episodio
     */
    public Temporada getTemporada() {
        return new Temporada(this.temporadaId);
    }

    /**
     * Obtiene una representación de la serie a la que pertenece el episodio
     */
    public Serie getSerie() {
        // Primero obtenemos la temporada
        String sql = "SELECT serie_id FROM Temporadas WHERE id = " + this.temporadaId;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        
        if (resultado.size() > 0) {
            int serieId = ((Number) resultado.get(0).get("serie_id")).intValue();
            return new Serie(serieId);
        }
        return null;
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

    public String getDescripcion() {
        loadFromDatabase();
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        updateDatabase();
    }

    public Integer getDuracion() {
        loadFromDatabase();
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
        updateDatabase();
    }

    public int getTemporadaId() {
        loadFromDatabase();
        return temporadaId;
    }

    public void setTemporadaId(int temporadaId) {
        this.temporadaId = temporadaId;
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

    public String getDirector() {
        loadFromDatabase();
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
        updateDatabase();
    }

    public String getGuionista() {
        loadFromDatabase();
        return guionista;
    }

    public void setGuionista(String guionista) {
        this.guionista = guionista;
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

    public LocalDateTime getFechaCreacion() {
        loadFromDatabase();
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        loadFromDatabase();
        return fechaModificacion;
    }

    /**
     * Obtiene todos los episodios de una temporada
     */
    public static ArrayList<Episodio> obtenerPorTemporada(int temporadaId) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Episodios WHERE temporada_id = " + temporadaId + " ORDER BY numero";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Episodio> episodios = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            episodios.add(new Episodio(id));
        }

        return episodios;
    }

    /**
     * Obtiene todos los episodios de una serie
     */
    public static ArrayList<Episodio> obtenerPorSerie(int serieId) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT e.id FROM Episodios e " +
                     "JOIN Temporadas t ON e.temporada_id = t.id " +
                     "WHERE t.serie_id = " + serieId + " " +
                     "ORDER BY t.numero, e.numero";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Episodio> episodios = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            episodios.add(new Episodio(id));
        }

        return episodios;
    }

    /**
     * Busca episodios por título
     * 
     * @param titulo Texto a buscar en el título
     * @return ArrayList con los episodios que coinciden
     */
    public static ArrayList<Episodio> buscarPorTitulo(String titulo) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Episodios WHERE titulo LIKE '%" + titulo + "%' ORDER BY titulo";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Episodio> episodios = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            episodios.add(new Episodio(id));
        }

        return episodios;
    }

    @Override
    public String toString() {
        return "S" + getTemporada().getNumero() + "E" + this.numero + " - " + this.titulo;
    }
}