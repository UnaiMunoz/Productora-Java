package com.productora.dao;

import com.productora.AppData;
import com.productora.model.Serie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase DAO para operaciones de Series en la base de datos
 */
public class SerieDAO {
    private final AppData appData;

    public SerieDAO() {
        this.appData = AppData.getInstance();
    }

    /**
     * Obtiene todas las series de la base de datos
     */
    public List<Serie> getAll() {
        return Serie.obtenerTodas();
    }

    /**
     * Busca series por título
     */
    public List<Serie> findByTitle(String titulo) {
        return Serie.buscarPorTitulo(titulo);
    }

    /**
     * Crea una nueva serie
     */
    public Serie create(String titulo) {
        return new Serie(titulo);
    }

    /**
     * Obtiene una serie por su ID
     */
    public Serie getById(int id) {
        String sql = "SELECT id FROM Series WHERE id = " + id;
        ArrayList<HashMap<String, Object>> resultado = appData.query(sql);

        if (resultado.size() > 0) {
            return new Serie(id);
        }
        return null;
    }

    /**
     * Elimina una serie por su ID
     */
    public boolean delete(int id) {
        Serie serie = getById(id);
        if (serie != null) {
            return serie.delete();
        }
        return false;
    }
}