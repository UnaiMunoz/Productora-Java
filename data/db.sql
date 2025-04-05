-- Creación de la tabla Series
CREATE TABLE IF NOT EXISTS Series (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo TEXT NOT NULL,
    descripcion TEXT,
    genero TEXT,
    anyo_inicio INTEGER,
    anyo_fin INTEGER,
    productora TEXT,
    presupuesto REAL,
    rating REAL,
    estado TEXT,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Creación de la tabla Temporadas
CREATE TABLE IF NOT EXISTS Temporadas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero INTEGER NOT NULL,
    titulo TEXT,
    serie_id INTEGER NOT NULL,
    fecha_estreno TEXT,
    fecha_fin TEXT,
    num_episodios INTEGER,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (serie_id) REFERENCES Series(id) ON DELETE CASCADE
);

-- Creación de la tabla Episodios
CREATE TABLE IF NOT EXISTS Episodios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero INTEGER NOT NULL,
    titulo TEXT NOT NULL,
    descripcion TEXT,
    duracion INTEGER, -- en minutos
    temporada_id INTEGER NOT NULL,
    fecha_estreno TEXT,
    director TEXT,
    guionista TEXT,
    rating REAL,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (temporada_id) REFERENCES Temporadas(id) ON DELETE CASCADE
);

-- Creación de la tabla Actores
CREATE TABLE IF NOT EXISTS Actores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    fecha_nacimiento TEXT,
    nacionalidad TEXT,
    biografia TEXT,
    imagen_url TEXT,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Creación de la tabla ActoresSeries (relación muchos a muchos)
CREATE TABLE IF NOT EXISTS ActoresSeries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    actor_id INTEGER NOT NULL,
    serie_id INTEGER NOT NULL,
    personaje TEXT NOT NULL,
    rol TEXT,
    temporadas_participacion TEXT, -- Por ejemplo: "1,2,3"
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES Actores(id) ON DELETE CASCADE,
    FOREIGN KEY (serie_id) REFERENCES Series(id) ON DELETE CASCADE,
    UNIQUE(actor_id, serie_id, personaje)
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_temporadas_serie ON Temporadas(serie_id);
CREATE INDEX IF NOT EXISTS idx_episodios_temporada ON Episodios(temporada_id);
CREATE INDEX IF NOT EXISTS idx_actores_series_actor ON ActoresSeries(actor_id);
CREATE INDEX IF NOT EXISTS idx_actores_series_serie ON ActoresSeries(serie_id);