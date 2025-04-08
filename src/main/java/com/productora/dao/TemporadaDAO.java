package com.productora.dao;

import com.productora.AppData;
import com.productora.model.Temporada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase DAO para operaciones de Temporadas en la base de datos
 */
public class TemporadaDAO {
    private final AppData appData;

    public TemporadaDAO() {
        this.appData = AppData.getInstance();
    }

    /**
     * Obtiene todas las temporadas de una serie
     * 
     * @param serieId ID de la serie
     * @return Lista de temporadas de la serie
     */
    public List<Temporada> getAllBySerie(int serieId) {
        return Temporada.obtenerPorSerie(serieId);
    }

    /**
     * Crea una nueva temporada
     * 
     * @param serieId ID de la serie a la que pertenece
     * @param numero Número de la temporada
     * @param titulo Título de la temporada (opcional)
     * @return Nueva instancia de Temporada
     */
    public Temporada create(int serieId, int numero, String titulo) {
        return new Temporada(serieId, numero, titulo);
    }

    /**
     * Crea una nueva temporada sin título específico
     * 
     * @param serieId ID de la serie a la que pertenece
     * @param numero Número de la temporada
     * @return Nueva instancia de Temporada
     */
    public Temporada create(int serieId, int numero) {
        return new Temporada(serieId, numero);
    }

    /**
     * Obtiene una temporada por su ID
     * 
     * @param id ID de la temporada
     * @return Temporada correspondiente o null si no existe
     */
    public Temporada getById(int id) {
        String sql = "SELECT id FROM Temporadas WHERE id = " + id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            return new Temporada(id);
        }
        return null;
    }

    /**
     * Elimina una temporada por su ID
     * 
     * @param id ID de la temporada a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean delete(int id) {
        Temporada temporada = getById(id);
        if (temporada != null) {
            return temporada.delete();
        }
        return false;
    }

    /**
     * Obtiene el siguiente número disponible para una temporada en una serie
     * 
     * @param serieId ID de la serie
     * @return Siguiente número de temporada
     */
    public int getNextSeasonNumber(int serieId) {
        String sql = "SELECT MAX(numero) as max_num FROM Temporadas WHERE serie_id = " + serieId;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        
        if (resultado.size() > 0 && resultado.get(0).get("max_num") != null) {
            return ((Number) resultado.get(0).get("max_num")).intValue() + 1;
        }
        return 1; // Si no hay temporadas, empezamos con la 1
    }
}