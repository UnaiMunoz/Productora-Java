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
     * 
     * @param temporadaId ID de la temporada
     * @return Lista de episodios de la temporada
     */
    public List<Episodio> getAllByTemporada(int temporadaId) {
        return Episodio.obtenerPorTemporada(temporadaId);
    }

    /**
     * Obtiene todos los episodios de una serie
     * 
     * @param serieId ID de la serie
     * @return Lista de episodios de la serie
     */
    public List<Episodio> getAllBySerie(int serieId) {
        return Episodio.obtenerPorSerie(serieId);
    }

    /**
     * Busca episodios por título
     * 
     * @param titulo Texto a buscar en el título
     * @return Lista de episodios que coinciden
     */
    public List<Episodio> findByTitle(String titulo) {
        return Episodio.buscarPorTitulo(titulo);
    }

    /**
     * Crea un nuevo episodio
     * 
     * @param temporadaId ID de la temporada a la que pertenece
     * @param numero Número del episodio en la temporada
     * @param titulo Título del episodio
     * @return Nueva instancia de Episodio
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
     * 
     * @param id ID del episodio
     * @return Episodio correspondiente o null si no existe
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
     * 
     * @param id ID del episodio a eliminar
     * @return true si se eliminó correctamente
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
     * Busca episodios por director
     * 
     * @param director Nombre del director a buscar
     * @return Lista de episodios del director especificado
     */
    public List<Episodio> findByDirector(String director) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Episodios WHERE director LIKE '%" + director + "%' ORDER BY titulo";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Episodio> episodios = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            episodios.add(new Episodio(id));
        }

        return episodios;
    }

    /**
     * Obtiene los episodios mejor valorados
     * 
     * @param limit Número máximo de episodios a retornar
     * @return Lista de episodios ordenados por rating descendente
     */
    public List<Episodio> getTopRated(int limit) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM Episodios WHERE rating IS NOT NULL ORDER BY rating DESC LIMIT " + limit;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<Episodio> episodios = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            episodios.add(new Episodio(id));
        }

        return episodios;
    }

    /**
     * Obtiene el siguiente número disponible para un episodio en una temporada
     * 
     * @param temporadaId ID de la temporada
     * @return Siguiente número de episodio
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