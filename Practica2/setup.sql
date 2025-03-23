/* Connect to a different database (e.g., postgres) before dropping */
\c postgres

/* Drop the database if it exist */
DROP DATABASE IF EXISTS serie;

/* Crear la base de datos "serie" con el usuario administrador (por ejemplo: postgres) */
CREATE DATABASE serie;

/* Conectarse a la base de datos "serie" */
\c serie

/* Deshabilitar temporalmente las restricciones para eliminar las tablas */
ALTER TABLE character_in_episode DROP CONSTRAINT IF EXISTS character_in_episode_pkey;
ALTER TABLE character DROP CONSTRAINT IF EXISTS character_id_origin_fkey;
ALTER TABLE character DROP CONSTRAINT IF EXISTS character_id_location_fkey;

/* Eliminar las tablas en el orden correcto para evitar conflictos por relaciones */
DROP TABLE IF EXISTS character_in_episode;
DROP TABLE IF EXISTS character;
DROP TABLE IF EXISTS episode;
DROP TABLE IF EXISTS location;

/* Revocar privilegios */
REVOKE ALL PRIVILEGES ON SCHEMA public FROM usuariodev;
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM usuariodev;
REVOKE ALL PRIVILEGES ON DATABASE serie FROM usuariodev;

/* Revocar privilegios por defecto */
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON TABLES FROM usuariodev;

/* Eliminar el rol */
DROP ROLE usuariodev;



/* Crear tablas */ 
CREATE TABLE location (
    id INT PRIMARY KEY, 
    name VARCHAR(255),
    type VARCHAR(255),
    dimension VARCHAR(255)
);

CREATE TABLE episode (
    id INT PRIMARY KEY, 
    name VARCHAR(255),
    air_date DATE,
    episode VARCHAR(20)
);

CREATE TABLE character (
    id INT PRIMARY KEY, 
    name VARCHAR(255),
    status VARCHAR(50),
    species VARCHAR(255),
    type VARCHAR(255),
    gender VARCHAR(50),
    id_origin INT REFERENCES location(id),
    id_location INT REFERENCES location(id)
);

CREATE TABLE character_in_episode (
    id_character INT REFERENCES character(id),
    id_episode INT REFERENCES episode(id),
    PRIMARY KEY (id_character, id_episode)
);


/* Insertar la ubicación "unknown" */
INSERT INTO location (id, name, type, dimension) VALUES (0, 'unknown', NULL, NULL);


/* Crear usuario dev */
CREATE USER usuariodev WITH PASSWORD '123';
GRANT CONNECT ON DATABASE serie TO usuariodev;
GRANT USAGE ON SCHEMA public TO usuariodev;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO usuariodev;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO usuariodev;



/* Procedures a continuación */

/* Procedimiento add_location
   Inserta una localización (id, nombre, tipo, dimensión) en la tabla location. */
CREATE OR REPLACE PROCEDURE add_location(
    p_id INT,
    p_name VARCHAR,
    p_type VARCHAR,
    p_dimension VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO location (id, name, type, dimension)
    VALUES (p_id, p_name, p_type, p_dimension)
    ON CONFLICT (id) DO NOTHING;
END;
$$;

/* Procedimiento add_episode
   Inserta un episodio (id, nombre, fecha de emisión, código de episodio) en la tabla episode. */
CREATE OR REPLACE PROCEDURE add_episode(
    p_id INT,
    p_name VARCHAR,
    p_air_date DATE,
    p_episode VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO episode (id, name, air_date, episode)
    VALUES (p_id, p_name, p_air_date, p_episode)
    ON CONFLICT (id) DO NOTHING;
END;
$$;

/* Procedimiento add_character
   Inserta un personaje (id, nombre, estado, especie, tipo, género, id_origen, id_ubicación)
   en la tabla character. */
CREATE OR REPLACE PROCEDURE add_character(
    p_id INT,
    p_name VARCHAR,
    p_status VARCHAR,
    p_species VARCHAR,
    p_type VARCHAR,
    p_gender VARCHAR,
    p_id_origin INT,
    p_id_location INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO character (id, name, status, species, type, gender, id_origin, id_location)
    VALUES (p_id, p_name, p_status, p_species, p_type, p_gender, p_id_origin, p_id_location)
    ON CONFLICT (id) DO NOTHING;
END;
$$;

/* Procedimiento add_character_in_episode
   Inserta una relación entre un personaje y un episodio en la tabla character_in_episode. */
CREATE OR REPLACE PROCEDURE add_character_in_episode(
    p_id_character INT,
    p_id_episode INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO character_in_episode (id_character, id_episode)
    VALUES (p_id_character, p_id_episode)
    ON CONFLICT DO NOTHING;
END;
$$;



/* Función de búsqueda por texto
   Esta función recibe un texto y retorna una tabla con id y nombre de personajes
   cuyos nombres contengan el texto. No sensible a mayúsculas. */
CREATE OR REPLACE FUNCTION search_characters(p_text TEXT)
RETURNS TABLE(id INT, name VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id, c.name
    FROM character c
    WHERE LOWER(c.name) LIKE LOWER('%' || p_text || '%');
END;
$$ LANGUAGE plpgsql;

/* Función personajes sin episodio
   Retorna la lista de personajes que no tienen ningún episodio asociado. */
CREATE OR REPLACE FUNCTION characters_without_episodes()
RETURNS TABLE(id INT, name VARCHAR)
AS $$
    SELECT c.id, c.name
    FROM character c
    WHERE NOT EXISTS (
        SELECT 1
        FROM character_in_episode ce
        WHERE ce.id_character = c.id
    );
$$ LANGUAGE sql;



/* Procedimiento extra
 Dado el nombre de un personaje, elimina todas sus relaciones con episodios,
 es decir, lo quita de la tabla character_in_episode. */
CREATE OR REPLACE PROCEDURE remove_episodes_from_character(p_name VARCHAR)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM character_in_episode
    WHERE id_character IN (
        SELECT id FROM character
        WHERE LOWER(name) = LOWER(p_name)
    );
END;
$$;