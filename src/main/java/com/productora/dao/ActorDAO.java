package com.productora.dao;

import com.productora.AppData;
import com.productora.model.Actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase DAO para operaciones de Actores en la base de datos
 */
public class ActorDAO {
    private final AppData appData;

    public ActorDAO() {
        this.appData = AppData.getInstance();
    }

    /**
     * Obtiene todos los actores de la base de datos
     */
    public List<Actor> getAll() {
        return Actor.obtenerTodos();
    }

    /**
     * Crea un nuevo actor
     */
    public Actor create(String nombre, String apellido) {
        return new Actor(nombre, apellido);
    }

    /**
     * Obtiene un actor por su ID
     */
    public Actor getById(int id) {
        String sql = "SELECT id FROM Actores WHERE id = " + id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            return new Actor(id);
        }
        return null;
    }

    /**
     * Elimina un actor por su ID
     */
    public boolean delete(int id) {
        Actor actor = getById(id);
        if (actor != null) {
            return actor.delete();
        }
        return false;
    }
}