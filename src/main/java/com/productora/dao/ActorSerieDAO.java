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
     */
    public List<ActorSerie> getAllByActor(int actorId) {
        return ActorSerie.obtenerPorActor(actorId);
    }

    /**
     * Obtiene todas las participaciones en una serie
     */
    public List<ActorSerie> getAllBySerie(int serieId) {
        return ActorSerie.obtenerPorSerie(serieId);
    }

    /**
     * Crea una nueva participación
     */
    public ActorSerie create(int actorId, int serieId, String personaje, String rol, String temporadas) {
        return new ActorSerie(actorId, serieId, personaje, rol, temporadas);
    }

    /**
     * Obtiene una participación por su ID
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
     */
    public boolean delete(int id) {
        ActorSerie actorSerie = getById(id);
        if (actorSerie != null) {
            return actorSerie.delete();
        }
        return false;
    }
}