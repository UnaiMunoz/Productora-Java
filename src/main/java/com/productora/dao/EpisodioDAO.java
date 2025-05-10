package com.productora.dao;

import com.productora.AppData;
import com.productora.model.Episodio;
import com.productora.model.Temporada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase DAO para operaciones de Episodios en la base de datos
 */
public class EpisodioDAO {
    private final AppData appData;

    public EpisodioDAO() {
        this.appData = AppData.getInstance();
    }

    /**
     * Obtiene todos los episodios de una temporada
     */
    public List<Episodio> getAllByTemporada(int temporadaId) {
        return Episodio.obtenerPorTemporada(temporadaId);
    }

    /**
     * Crea un nuevo episodio
     */
    public Episodio create(int temporadaId, int numero, String titulo) {
        Episodio episodio = new Episodio(temporadaId, numero, titulo);
        
        // Actualizar el contador de episodios en la temporada
        Temporada temporada = new Temporada(temporadaId);
        temporada.actualizarNumEpisodios();
        
        return episodio;
    }

    /**
     * Obtiene un episodio por su ID
     */
    public Episodio getById(int id) {
        String sql = "SELECT id FROM Episodios WHERE id = " + id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            return new Episodio(id);
        }
        return null;
    }

    /**
     * Elimina un episodio por su ID
     */
    public boolean delete(int id) {
        Episodio episodio = getById(id);
        if (episodio != null) {
            int temporadaId = episodio.getTemporadaId();
            boolean result = episodio.delete();
            
            // Actualizar el contador de episodios en la temporada
            if (result) {
                Temporada temporada = new Temporada(temporadaId);
                temporada.actualizarNumEpisodios();
            }
            
            return result;
        }
        return false;
    }

    /**
     * Obtiene el siguiente número disponible para un episodio en una temporada
     */
    public int getNextEpisodeNumber(int temporadaId) {
        String sql = "SELECT MAX(numero) as max_num FROM Episodios WHERE temporada_id = " + temporadaId;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        
        if (resultado.size() > 0 && resultado.get(0).get("max_num") != null) {
            return ((Number) resultado.get(0).get("max_num")).intValue() + 1;
        }
        return 1; // Si no hay episodios, empezamos con el 1
    }
}