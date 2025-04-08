package com.productora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Clase encargada de inicializar la base de datos SQLite
 * Crea el archivo si no existe y ejecuta el script SQL de creación
 */
public class DatabaseInitializer {

    /**
     * Inicializa la base de datos y devuelve la ruta al archivo
     * @return Ruta al archivo de base de datos
     */
    public static String initializeDatabase() {
        String dbPath = "./data/productora.sqlite";
        File dbFile = new File(dbPath);
        
        // Crear directorio si no existe
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        boolean dbExisted = dbFile.exists();
        
        try {
            // Si no existe, creamos el archivo de la base de datos
            if (!dbExisted) {
                dbFile.createNewFile();
                System.out.println("Base de datos creada: " + dbPath);
            } else {
                System.out.println("Base de datos encontrada: " + dbPath);
            }
            
            // Verificar si las tablas existen
            if (!tableExists(dbPath, "Series")) {
                System.out.println("Tablas no encontradas. Creando esquema...");
                
                // Leer el script SQL desde un archivo
                String sqlScript = readSqlScript("./data/db.sql");
                
                // Crear las tablas usando el script SQL
                String url = "jdbc:sqlite:" + dbPath;
                try (Connection conn = DriverManager.getConnection(url);
                     Statement stmt = conn.createStatement()) {
                    
                    // Ejecutar cada sentencia SQL
                    for (String sql : sqlScript.split(";")) {
                        sql = sql.trim();
                        if (!sql.isEmpty()) {
                            stmt.execute(sql);
                        }
                    }
                    
                    System.out.println("Esquema de base de datos creado correctamente.");
                    
                    // Opcional: Insertar datos de ejemplo solo si acabamos de crear las tablas
                    insertSampleData(dbPath);
                }
            }
            
            return dbPath;
            
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
            return dbPath;
        }
    }
    
    /**
     * Comprueba si una tabla existe en la base de datos
     * @param dbPath Ruta a la base de datos
     * @param tableName Nombre de la tabla a verificar
     * @return true si la tabla existe, false en caso contrario
     */
    private static boolean tableExists(String dbPath, String tableName) {
        String url = "jdbc:sqlite:" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(url)) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, tableName, null);
            
            return rs.next(); // Si hay resultados, la tabla existe
        } catch (Exception e) {
            System.err.println("Error al verificar si la tabla existe: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lee un archivo SQL y devuelve su contenido como String
     * 
     * @param filePath Ruta al archivo SQL
     * @return Contenido del archivo como String
     * @throws IOException Si hay error al leer el archivo
     */
    private static String readSqlScript(String filePath) throws IOException {
        StringBuilder sqlScript = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignorar comentarios
                if (!line.trim().startsWith("--")) {
                    sqlScript.append(line).append("\n");
                }
            }
        }
        
        return sqlScript.toString();
    }
    
    /**
     * Inserta datos de ejemplo en la base de datos
     * 
     * @param dbPath Ruta a la base de datos
     */
    private static void insertSampleData(String dbPath) {
        String url = "jdbc:sqlite:" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            
            // Insertar series de ejemplo
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, anyo_fin, productora, presupuesto, rating, estado) VALUES " +
                         "('Breaking Bad', 'Un profesor de química con cáncer comienza a fabricar metanfetamina', 'Drama', 2008, 2013, 'AMC', 3000000, 9.5, 'Finalizada')");
            
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, productora, presupuesto, rating, estado) VALUES " +
                         "('Stranger Things', 'Un grupo de niños descubre fenómenos sobrenaturales en su pequeña ciudad', 'Ciencia Ficción', 2016, 'Netflix', 8000000, 8.7, 'En producción')");
            
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, anyo_fin, productora, presupuesto, rating, estado) VALUES " +
                         "('Game of Thrones', 'Nobles familias luchan por el control del Trono de Hierro', 'Fantasía', 2011, 2019, 'HBO', 15000000, 9.2, 'Finalizada')");
            
            // Insertar temporadas de ejemplo
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 1, '2008-01-20', '2008-03-09', 7)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 2, '2016-07-15', '2016-07-15', 8)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 3, '2011-04-17', '2011-06-19', 10)");
            
            System.out.println("Datos de ejemplo insertados correctamente.");
            
        } catch (Exception e) {
            System.err.println("Error al insertar datos de ejemplo: " + e.getMessage());
            // No lanzamos la excepción para que la aplicación siga funcionando
        }
    }
}