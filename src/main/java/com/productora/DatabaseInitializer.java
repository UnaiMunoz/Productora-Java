package com.productora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
            // Si la base de datos no existe, créala y configúrala
            if (!dbExisted) {
                // Crear el archivo de la base de datos
                dbFile.createNewFile();
                System.out.println("Base de datos creada: " + dbPath);
                
                // Crear las tablas
                System.out.println("Creando esquema de la base de datos...");
                
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
                    
                    // Insertar datos de ejemplo solo si es una nueva base de datos
                    System.out.println("Insertando datos de ejemplo...");
                    insertSampleData(dbPath);
                }
            } else {
                System.out.println("Base de datos encontrada: " + dbPath);
                
                // Verificar que las tablas existen
                if (!tableExists(dbPath, "Series")) {
                    System.out.println("Tabla Series no encontrada. Creando esquema...");
                    
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
                        
                        // Verificar si hay datos en las tablas
                        if (isDatabaseEmpty(dbPath)) {
                            System.out.println("Base de datos vacía. Insertando datos de ejemplo...");
                            insertSampleData(dbPath);
                        }
                    }
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
     * Comprueba si la base de datos está vacía (no tiene datos en las tablas principales)
     * 
     * @param dbPath Ruta a la base de datos
     * @return true si la base de datos está vacía, false en caso contrario
     */
    private static boolean isDatabaseEmpty(String dbPath) {
        String url = "jdbc:sqlite:" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Series")) {
            
            if (rs.next()) {
                int count = rs.getInt("count");
                return count == 0;  // Si count es 0, la tabla está vacía
            }
            return true;  // Si no hay resultados, consideramos que está vacía
        } catch (Exception e) {
            System.err.println("Error al verificar si la base de datos está vacía: " + e.getMessage());
            return true;  // En caso de error, asumimos que está vacía
        }
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
            
            // Activar las claves foráneas para asegurar integridad referencial
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Insertar series de ejemplo
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, anyo_fin, productora, presupuesto, rating, estado) VALUES " +
                         "('Breaking Bad', 'Un profesor de química con cáncer comienza a fabricar metanfetamina', 'Drama', 2008, 2013, 'AMC', 3000000, 9.5, 'Finalizada')");
            
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, productora, presupuesto, rating, estado) VALUES " +
                         "('Stranger Things', 'Un grupo de niños descubre fenómenos sobrenaturales en su pequeña ciudad', 'Ciencia Ficción', 2016, 'Netflix', 8000000, 8.7, 'En producción')");
            
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, anyo_fin, productora, presupuesto, rating, estado) VALUES " +
                         "('Game of Thrones', 'Nobles familias luchan por el control del Trono de Hierro', 'Fantasía', 2011, 2019, 'HBO', 15000000, 9.2, 'Finalizada')");
            
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, productora, presupuesto, rating, estado) VALUES " +
                         "('The Crown', 'La historia del reinado de la Reina Isabel II', 'Drama histórico', 2016, 'Netflix', 10000000, 8.6, 'En producción')");
            
            stmt.execute("INSERT INTO Series (titulo, descripcion, genero, anyo_inicio, anyo_fin, productora, presupuesto, rating, estado) VALUES " +
                         "('Friends', 'Un grupo de amigos viven en Manhattan y comparten sus vidas', 'Comedia', 1994, 2004, 'NBC', 2000000, 8.9, 'Finalizada')");
            
            System.out.println("Series insertadas correctamente");
            
            // Insertar temporadas de ejemplo
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 1, '2008-01-20', '2008-03-09', 7)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(2, 'Segunda Temporada', 1, '2009-03-08', '2009-05-31', 13)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(3, 'Tercera Temporada', 1, '2010-03-21', '2010-06-13', 13)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 2, '2016-07-15', '2016-07-15', 8)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(2, 'Segunda Temporada', 2, '2017-10-27', '2017-10-27', 9)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 3, '2011-04-17', '2011-06-19', 10)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(2, 'Segunda Temporada', 3, '2012-04-01', '2012-06-03', 10)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 4, '2016-11-04', '2016-11-04', 10)");
            
            stmt.execute("INSERT INTO Temporadas (numero, titulo, serie_id, fecha_estreno, fecha_fin, num_episodios) VALUES " +
                         "(1, 'Primera Temporada', 5, '1994-09-22', '1995-05-18', 24)");
            
            System.out.println("Temporadas insertadas correctamente");
            
            // Obtener los IDs de las temporadas para usar las claves foráneas correctas
            ResultSet rs1 = stmt.executeQuery("SELECT id FROM Temporadas WHERE serie_id = 1 AND numero = 1");
            int temporadaBB1Id = rs1.next() ? rs1.getInt("id") : 1;
            
            ResultSet rs2 = stmt.executeQuery("SELECT id FROM Temporadas WHERE serie_id = 2 AND numero = 1");
            int temporadaST1Id = rs2.next() ? rs2.getInt("id") : 4;
            
            ResultSet rs3 = stmt.executeQuery("SELECT id FROM Temporadas WHERE serie_id = 3 AND numero = 1");
            int temporadaGOT1Id = rs3.next() ? rs3.getInt("id") : 6;
            
            ResultSet rs4 = stmt.executeQuery("SELECT id FROM Temporadas WHERE serie_id = 4 AND numero = 1");
            int temporadaCrown1Id = rs4.next() ? rs4.getInt("id") : 8;
            
            // Insertar episodios de ejemplo con las claves foráneas correctas
            // Breaking Bad - Temporada 1
            stmt.execute("INSERT INTO Episodios (numero, titulo, descripcion, duracion, temporada_id, fecha_estreno, director, guionista, rating) VALUES " +
                         "(1, 'Piloto', 'Walter White recibe un diagnóstico de cáncer y decide fabricar metanfetamina', 58, " + temporadaBB1Id + ", '2008-01-20', 'Vince Gilligan', 'Vince Gilligan', 9.0)");
            
            stmt.execute("INSERT INTO Episodios (numero, titulo, descripcion, duracion, temporada_id, fecha_estreno, director, guionista, rating) VALUES " +
                         "(2, 'El gato está en la bolsa', 'Walt y Jesse intentan deshacerse de dos cadáveres', 48, " + temporadaBB1Id + ", '2008-01-27', 'Adam Bernstein', 'Vince Gilligan', 8.7)");
            
            // Stranger Things - Temporada 1
            stmt.execute("INSERT INTO Episodios (numero, titulo, descripcion, duracion, temporada_id, fecha_estreno, director, guionista, rating) VALUES " +
                         "(1, 'Capítulo Uno: La desaparición de Will Byers', 'Un niño desaparece en Hawkins, Indiana', 47, " + temporadaST1Id + ", '2016-07-15', 'Hermanos Duffer', 'Hermanos Duffer', 8.6)");
            
            stmt.execute("INSERT INTO Episodios (numero, titulo, descripcion, duracion, temporada_id, fecha_estreno, director, guionista, rating) VALUES " +
                         "(2, 'Capítulo Dos: La chica rara de la calle Maple', 'Los amigos de Will encuentran a una misteriosa niña en el bosque', 55, " + temporadaST1Id + ", '2016-07-15', 'Hermanos Duffer', 'Hermanos Duffer', 8.5)");
            
            // Game of Thrones - Temporada 1
            stmt.execute("INSERT INTO Episodios (numero, titulo, descripcion, duracion, temporada_id, fecha_estreno, director, guionista, rating) VALUES " +
                         "(1, 'Winter Is Coming', 'Ned Stark es convocado a servir como Mano del Rey', 62, " + temporadaGOT1Id + ", '2011-04-17', 'Tim Van Patten', 'David Benioff & D. B. Weiss', 9.1)");
            
            stmt.execute("INSERT INTO Episodios (numero, titulo, descripcion, duracion, temporada_id, fecha_estreno, director, guionista, rating) VALUES " +
                         "(2, 'The Kingsroad', 'Ned y sus hijas viajan hacia el sur, mientras que Jon Snow se dirige al Muro', 56, " + temporadaGOT1Id + ", '2011-04-24', 'Tim Van Patten', 'David Benioff & D. B. Weiss', 8.8)");
            
            // The Crown - Temporada 1
            stmt.execute("INSERT INTO Episodios (numero, titulo, descripcion, duracion, temporada_id, fecha_estreno, director, guionista, rating) VALUES " +
                         "(1, 'Wolferton Splash', 'Isabel se casa con Felipe en 1947, mientras que el rey Jorge VI enfrenta problemas de salud', 57, " + temporadaCrown1Id + ", '2016-11-04', 'Stephen Daldry', 'Peter Morgan', 8.7)");
            
            System.out.println("Episodios insertados correctamente");
            
            // Insertar actores de ejemplo
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Bryan', 'Cranston', '1956-03-07', 'Estadounidense', 'Actor conocido por su papel de Walter White en Breaking Bad')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Aaron', 'Paul', '1979-08-27', 'Estadounidense', 'Actor conocido por su papel de Jesse Pinkman en Breaking Bad')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Millie Bobby', 'Brown', '2004-02-19', 'Británica', 'Actriz conocida por su papel de Once en Stranger Things')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Finn', 'Wolfhard', '2002-12-23', 'Canadiense', 'Actor conocido por su papel de Mike Wheeler en Stranger Things')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Emilia', 'Clarke', '1986-10-23', 'Británica', 'Actriz conocida por su papel de Daenerys Targaryen en Game of Thrones')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Kit', 'Harington', '1986-12-26', 'Británico', 'Actor conocido por su papel de Jon Snow en Game of Thrones')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Claire', 'Foy', '1984-04-16', 'Británica', 'Actriz conocida por su papel de la Reina Isabel II en The Crown')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Jennifer', 'Aniston', '1969-02-11', 'Estadounidense', 'Actriz conocida por su papel de Rachel Green en Friends')");
            
            stmt.execute("INSERT INTO Actores (nombre, apellido, fecha_nacimiento, nacionalidad, biografia) VALUES " +
                         "('Matthew', 'Perry', '1969-08-19', 'Estadounidense-Canadiense', 'Actor conocido por su papel de Chandler Bing en Friends')");
            
            System.out.println("Actores insertados correctamente");
            
            // Insertar relaciones actor-serie
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(1, 1, 'Walter White', 'Protagonista', '1,2,3,4,5')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(2, 1, 'Jesse Pinkman', 'Protagonista', '1,2,3,4,5')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(3, 2, 'Once / Jane Hopper', 'Protagonista', '1,2,3,4')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(4, 2, 'Mike Wheeler', 'Protagonista', '1,2,3,4')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(5, 3, 'Daenerys Targaryen', 'Protagonista', '1,2,3,4,5,6,7,8')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(6, 3, 'Jon Snow', 'Protagonista', '1,2,3,4,5,6,7,8')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(7, 4, 'Reina Isabel II', 'Protagonista', '1,2')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(8, 5, 'Rachel Green', 'Protagonista', '1,2,3,4,5,6,7,8,9,10')");
            
            stmt.execute("INSERT INTO ActoresSeries (actor_id, serie_id, personaje, rol, temporadas_participacion) VALUES " +
                         "(9, 5, 'Chandler Bing', 'Protagonista', '1,2,3,4,5,6,7,8,9,10')");
            
            System.out.println("Datos de ejemplo insertados correctamente.");
            
        } catch (Exception e) {
            System.err.println("Error al insertar datos de ejemplo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}