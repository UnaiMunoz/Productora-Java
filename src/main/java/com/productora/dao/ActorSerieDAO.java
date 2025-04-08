package com.productora.dao;

import com.productora.AppData;
import com.productora.model.ActorSerie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase DAO para operaciones de relaciones Actor-Serie en la base de datos
 */
public class ActorSerieDAO {
    private final AppData appData;

    public ActorSerieDAO() {
        this.appData = AppData.getInstance();
    }

    /**
     * Obtiene todas las participaciones de un actor
     * 
     * @param actorId ID del actor
     * @return Lista de participaciones del actor
     */
    public List<ActorSerie> getAllByActor(int actorId) {
        return ActorSerie.obtenerPorActor(actorId);
    }

    /**
     * Obtiene todas las participaciones en una serie
     * 
     * @param serieId ID de la serie
     * @return Lista de participaciones en la serie
     */
    public List<ActorSerie> getAllBySerie(int serieId) {
        return ActorSerie.obtenerPorSerie(serieId);
    }

    /**
     * Crea una nueva participación
     * 
     * @param actorId ID del actor
     * @param serieId ID de la serie
     * @param personaje Nombre del personaje
     * @param rol Rol del actor (protagonista, secundario, etc.)
     * @param temporadas Temporadas en las que participa (formato: "1,2,3")
     * @return Nueva instancia de ActorSerie
     */
    public ActorSerie create(int actorId, int serieId, String personaje, String rol, String temporadas) {
        return new ActorSerie(actorId, serieId, personaje, rol, temporadas);
    }

    /**
     * Obtiene una participación por su ID
     * 
     * @param id ID de la participación
     * @return Objeto ActorSerie o null si no existe
     */
    public ActorSerie getById(int id) {
        String sql = "SELECT id FROM ActoresSeries WHERE id = " + id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            return new ActorSerie(id);
        }
        return null;
    }

    /**
     * Elimina una participación por su ID
     * 
     * @param id ID de la participación a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean delete(int id) {
        ActorSerie actorSerie = getById(id);
        if (actorSerie != null) {
            return actorSerie.delete();
        }
        return false;
    }

    /**
     * Busca participaciones por personaje
     * 
     * @param personaje Nombre del personaje a buscar
     * @return Lista de participaciones que coinciden
     */
    public List<ActorSerie> findByCharacter(String personaje) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM ActoresSeries WHERE personaje LIKE '%" + personaje + "%' ORDER BY personaje";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<ActorSerie> participaciones = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            participaciones.add(new ActorSerie(id));
        }

        return participaciones;
    }

    /**
     * Busca participaciones por rol
     * 
     * @param rol Rol a buscar (protagonista, secundario, etc.)
     * @return Lista de participaciones que coinciden
     */
    public List<ActorSerie> findByRole(String rol) {
        AppData appData = AppData.getInstance();
        String sql = "SELECT id FROM ActoresSeries WHERE rol LIKE '%" + rol + "%' ORDER BY personaje";
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);
        ArrayList<ActorSerie> participaciones = new ArrayList<>();

        for (HashMap<String, Object> row : resultado) {
            int id = ((Number) row.get("id")).intValue();
            participaciones.add(new ActorSerie(id));
        }

        return participaciones;
    }
}