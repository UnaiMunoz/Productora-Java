package com.productora;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe que gestiona la connexió a la base de dades utilitzant el patró Singleton.
 * Proporciona mètodes per connectar, tancar la connexió, actualitzar dades, inserir registres
 * i realitzar consultes transformant el ResultSet en una llista de HashMap.
 */
public class AppData {
    private static AppData instance;
    private Connection conn;
    private static final Logger logger = Logger.getLogger(AppData.class.getName());
    private boolean transactionActive = false;

    /**
     * Constructor privat que crea la connexió a la base de dades.
     */
    private AppData() { }

    /**
     * Obté la instància única de AppData (Singleton).
     *
     * @return la instància d'AppData.
     */
    public static AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    /**
     * Estableix la connexió amb la base de dades SQLite.
     * L'arxiu de la base de dades és "./data/exercici1400.sqlite".
     * Es desactiva l'autocommit per permetre el control manual de transaccions.
     */
    public void connect(String filePath) {
        String url = "jdbc:sqlite:" + filePath;
        try {
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
            logger.info("Conexión establecida con la base de datos: " + filePath);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al conectar con la base de datos", e);
        }
    }

    /**
     * Tanca la connexió a la base de dades.
     */
    public void close() {
        try {
            if (conn != null) {
                conn.close();
                logger.info("Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al cerrar la conexión", e);
        }
    }

    /**
     * Inicia una transacción
     * 
     * @return true si se ha iniciado correctamente, false en caso de error
     */
    public boolean beginTransaction() {
        if (conn == null) {
            logger.severe("No hay conexión a la base de datos para iniciar transacción");
            return false;
        }
        
        if (transactionActive) {
            logger.warning("Ya hay una transacción activa");
            return true; // Ya hay una transacción activa, no es un error
        }
        
        try {
            conn.setAutoCommit(false);
            transactionActive = true;
            logger.info("Transacción iniciada");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al iniciar transacción", e);
            return false;
        }
    }
    
    /**
     * Confirma una transacción
     * 
     * @return true si se ha confirmado correctamente, false en caso de error
     */
    public boolean commitTransaction() {
        if (conn == null || !transactionActive) {
            logger.severe("No hay transacción activa para confirmar");
            return false;
        }
        
        try {
            conn.commit();
            transactionActive = false;
            logger.info("Transacción confirmada");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al confirmar transacción", e);
            return false;
        }
    }
    
    /**
     * Cancela una transacción
     * 
     * @return true si se ha cancelado correctamente, false en caso de error
     */
    public boolean rollbackTransaction() {
        if (conn == null) {
            logger.severe("No hay conexión a la base de datos para hacer rollback");
            return false;
        }
        
        try {
            conn.rollback();
            transactionActive = false;
            logger.info("Transacción cancelada (rollback)");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al hacer rollback", e);
            return false;
        }
    }

    /**
     * Executa una actualització a la base de dades (INSERT, UPDATE, DELETE, etc.).
     * Se realiza un commit automático, a menos que haya una transacción activa.
     *
     * @param sql la sentència SQL d'actualització a executar.
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public boolean update(String sql) {
        if (conn == null) {
            logger.severe("No hay conexión a la base de datos para actualizar");
            return false;
        }
        
        boolean shouldCommit = !transactionActive;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            
            if (shouldCommit) {
                conn.commit();
            }
            
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al ejecutar actualización: " + sql, e);
            
            if (shouldCommit) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error en rollback después de fallo en update", ex);
                }
            }
            
            return false;
        }
    }

    /**
     * Executa una inserció a la base de dades i retorna l'identificador generat.
     * Se realiza el commit automáticamente, a menos que haya una transacción activa.
     *
     * @param sql la sentència SQL d'inserció a executar.
     * @return l'identificador generat per la fila inserida, o -1 en cas d'error.
     */
    public int insertAndGetId(String sql) {
        if (conn == null) {
            logger.severe("No hay conexión a la base de datos para insertar");
            return -1;
        }
        
        int generatedId = -1;
        boolean shouldCommit = !transactionActive;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            
            // Obtener el ID generado
            try (ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }
            
            if (shouldCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al ejecutar inserción: " + sql, e);
            
            if (shouldCommit) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error en rollback después de fallo en insert", ex);
                }
            }
        }
        
        return generatedId;
    }

    /**
     * Realitza una consulta a la base de dades i transforma el ResultSet en una ArrayList de HashMap.
     * Cada HashMap representa una fila amb claus que corresponen als noms de columna.
     *
     * @param sql la sentència SQL de consulta.
     * @return una ArrayList de HashMap amb els resultats de la consulta.
     */
    public ArrayList<HashMap<String, Object>> query(String sql) {
        ArrayList<HashMap<String, Object>> resultList = new ArrayList<>();
        
        if (conn == null) {
            logger.severe("No hay conexión a la base de datos para consultar");
            return resultList;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                resultList.add(row);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al ejecutar consulta: " + sql, e);
        }
        
        return resultList;
    }
    
    /**
     * Ejecuta una consulta SQL y devuelve un único valor
     * 
     * @param sql Consulta SQL que debe devolver un único valor
     * @return El valor devuelto o null si hay error o no hay resultado
     */
    public Object queryScalar(String sql) {
        if (conn == null) {
            logger.severe("No hay conexión a la base de datos para consultar valor escalar");
            return null;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getObject(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al ejecutar consulta escalar: " + sql, e);
        }
        
        return null;
    }
    
    /**
     * Comprueba si la conexión está activa
     * 
     * @return true si la conexión está activa
     */
    public boolean isConnected() {
        if (conn == null) {
            return false;
        }
        
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al verificar el estado de la conexión", e);
            return false;
        }
    }
}