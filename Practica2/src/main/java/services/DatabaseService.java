package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import utils.DBUtils;
import models.LocationData;
import models.EpisodeData;
import models.CharacterData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



/**
 * Clase de servicio para gestionar la base de datos con datos obtenidos de la API de Rick and Morty.
 */
public class DatabaseService {

    private static final String BASE_URL = "https://rickandmortyapi.com/api/";
    
    /**
     * Rellena la base de datos con los datos de la API (locations, episodes, characters).
     * - Vacía las tablas.
     * - Añade una localización "unknown".
     * - Inserta todas las locations, episodes y characters desde la API.
     * - Inserta las relaciones character_in_episode.
     */
    public static void fillDatabase() {
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            // 1. Vaciar la BD
            try {
                System.out.println("Vaciando la base de datos...");
                clearDatabase(conn);
            } catch (SQLException ex) {
                System.out.println("Error al vaciar la base de datos: " + ex.getMessage());
                conn.rollback();
                return;
            }

            // 2. Añadir location unknown con id=0
            try {
                createUnknownLocation(conn);
                System.out.println("Localización 'unknown' añadida");
            } catch (SQLException ex) {
                System.out.println("Error al crear el registro 'unknown' en location: " + ex.getMessage());
                conn.rollback();
                return;
            }

            // 3. Insertar locations
            List<LocationData> allLocations;
            try {
                allLocations = getAllLocationsFromAPI();
            } catch (IOException | InterruptedException ex) {
                System.out.println("Error al obtener las locations de la API: " + ex.getMessage());
                conn.rollback();
                return;
            }
            try {
                insertLocations(conn, allLocations);
                System.out.println("Localizaciones añadidas");
            } catch (SQLException ex) {
                System.out.println("Error al insertar las locations: " + ex.getMessage());
                conn.rollback();
                return;
            }

            // 4. Insertar episodes
            List<EpisodeData> allEpisodes;
            try {
                allEpisodes = getAllEpisodesFromAPI();
            } catch (IOException | InterruptedException ex) {
                System.out.println("Error al obtener los episodios de la API: " + ex.getMessage());
                conn.rollback();
                return;
            }
            try {
                insertEpisodes(conn, allEpisodes);
                System.out.println("Episodios añadidos");
            } catch (SQLException ex) {
                System.out.println("Error al insertar los episodios: " + ex.getMessage());
                conn.rollback();
                return;
            }

            // 5. Insertar characters y sus relaciones con episodes
            List<CharacterData> allCharacters;
            try {
                allCharacters = getAllCharactersFromAPI();
            } catch (IOException | InterruptedException ex) {
                System.out.println("Error al obtener los personajes de la API: " + ex.getMessage());
                conn.rollback();
                return;
            }
            try {
                insertCharactersAndRelations(conn, allCharacters);
                System.out.println("Personajes añadidos");
            } catch (SQLException ex) {
                System.out.println("Error al insertar los personajes y sus relaciones: " + ex.getMessage());
                conn.rollback();
                return;
            }

            conn.commit();
            System.out.println("Base de datos rellenada correctamente.");

        } catch (SQLException ex) {
            System.out.println("Error con la base de datos: " + ex.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.out.println("Error al cerrar la conexión: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Vacía las tablas character_in_episode, character, episode y location en el orden correcto.
     * @param conn Conexión a la base de datos
     * @throws SQLException si ocurre un error en la eliminación
     */
    private static void clearDatabase(Connection conn) throws SQLException {
        // Borramos en el orden adecuado, de más dependientes a menos.
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM character_in_episode")) {
            ps.executeUpdate();
            System.out.println("Tabla 'character_in_episode' vacíada");
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM character")) {
            ps.executeUpdate();
            System.out.println("Tabla 'character' vacíada");
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM episode")) {
            ps.executeUpdate();
            System.out.println("Tabla 'episode' vacíada");
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM location")) {
            ps.executeUpdate();
            System.out.println("Tabla 'location' vacíada");
        }
    }
    
    /**
     * Crea una localización con id=0 y nombre "unknown" en la base de datos.
     * @param conn Conexión a la base de datos
     * @throws SQLException si ocurre un error en la inserción
     */
    private static void createUnknownLocation(Connection conn) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("CALL add_location(?,?,?,?)")) {
            cs.setInt(1, 0);
            cs.setString(2, "unknown");
            cs.setString(3, null);
            cs.setString(4, null);
            cs.execute();
        }
    }
    
    /**
     * Obtiene todas las localizaciones de la API de Rick and Morty paginadas.
     * @return Lista con todas las LocationData obtenidas.
     * @throws IOException si ocurre un error de I/O
     * @throws InterruptedException si la petición HTTP es interrumpida
     */
    private static List<LocationData> getAllLocationsFromAPI() throws IOException, InterruptedException {
        List<LocationData> result = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            String url = BASE_URL + "location?page=" + page;
            String response = doGetRequest(url);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            if (!json.has("results")) {
                break;
            }
            JsonArray results = json.getAsJsonArray("results");

            if (results == null || results.size() == 0) {
                hasMore = false;
                break;
            }

            for (int i = 0; i < results.size(); i++) {
                JsonObject loc = results.get(i).getAsJsonObject();
                int id = loc.get("id").getAsInt();
                String name = loc.get("name").getAsString();
                String type = loc.get("type").getAsString();
                String dimension = loc.get("dimension").getAsString();

                result.add(new LocationData(id, name, type, dimension));
            }

            JsonObject info = json.get("info").getAsJsonObject();
            int pages = info.get("pages").getAsInt();
            page++;
            if (page > pages) {
                hasMore = false;
            }
        }

        return result;
    }
    
    /**
     * Inserta una lista de localizaciones en la base de datos usando el procedimiento almacenado add_location.
     * @param conn Conexión a la base de datos
     * @param locations Lista de objetos LocationData
     * @throws SQLException si ocurre un error en la inserción
     */
    private static void insertLocations(Connection conn, List<LocationData> locations) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("CALL add_location(?,?,?,?)")) {
            for (LocationData loc : locations) {
                cs.setInt(1, loc.getId());
                cs.setString(2, loc.getName());
                cs.setString(3, loc.getType());
                cs.setString(4, loc.getDimension());
                cs.execute();
            }
        }
    }
    
    /**
     * Obtiene todos los episodios de la API de Rick and Morty paginados.
     * Convierte la fecha de emisión (air_date) a java.sql.Date.
     * @return Lista con todos los EpisodeData obtenidos.
     * @throws IOException si ocurre un error de I/O
     * @throws InterruptedException si la petición HTTP es interrumpida
     */
    private static List<EpisodeData> getAllEpisodesFromAPI() throws IOException, InterruptedException {
        List<EpisodeData> result = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

        while (hasMore) {
            String url = BASE_URL + "episode?page=" + page;
            String response = doGetRequest(url);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            if (!json.has("results")) {
                break;
            }
            JsonArray results = json.getAsJsonArray("results");

            if (results == null || results.size() == 0) {
                hasMore = false;
                break;
            }

            for (int i = 0; i < results.size(); i++) {
                JsonObject ep = results.get(i).getAsJsonObject();
                int id = ep.get("id").getAsInt();
                String name = ep.get("name").getAsString();
                String air_date_str = ep.get("air_date").getAsString();
                String episodeCode = ep.get("episode").getAsString();

                java.util.Date parsedDate;
                try {
                    parsedDate = sdf.parse(air_date_str);
                } catch (Exception ex) {
                    parsedDate = new java.util.Date();
                }
                Date air_date = new Date(parsedDate.getTime());

                result.add(new EpisodeData(id, name, air_date, episodeCode));
            }

            JsonObject info = json.get("info").getAsJsonObject();
            int pages = info.get("pages").getAsInt();
            page++;
            if (page > pages) {
                hasMore = false;
            }
        }
        return result;
    }
    
    /**
     * Inserta una lista de episodios en la base de datos usando el procedimiento almacenado add_episode.
     * @param conn Conexión a la base de datos
     * @param episodes Lista de objetos EpisodeData
     * @throws SQLException si ocurre un error en la inserción
     */
    private static void insertEpisodes(Connection conn, List<EpisodeData> episodes) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("CALL add_episode(?,?,?,?)")) {
            for (EpisodeData ep : episodes) {
                cs.setInt(1, ep.getId());
                cs.setString(2, ep.getName());
                cs.setDate(3, ep.getAir_date());
                cs.setString(4, ep.getEpisode());
                cs.execute();
            }
        }
    }
    
    /**
     * Obtiene todos los personajes de la API de Rick and Morty paginados.
     * Para cada personaje obtiene su id, nombre, estado, especie, tipo, género, así como las URLs de su origin
     * y location para extraer sus IDs. También lista los episodios en los que aparece.
     * @return Lista con todos los CharacterData obtenidos.
     * @throws IOException si ocurre un error de I/O
     * @throws InterruptedException si la petición HTTP es interrumpida
     */
    private static List<CharacterData> getAllCharactersFromAPI() throws IOException, InterruptedException {
        List<CharacterData> result = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            String url = BASE_URL + "character?page=" + page;
            String response = doGetRequest(url);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            if (!json.has("results")) {
                break;
            }
            JsonArray results = json.getAsJsonArray("results");

            if (results == null || results.size() == 0) {
                hasMore = false;
                break;
            }

            for (int i = 0; i < results.size(); i++) {
                JsonObject ch = results.get(i).getAsJsonObject();
                int id = ch.get("id").getAsInt();
                String name = ch.get("name").getAsString();
                String status = ch.get("status").getAsString();
                String species = ch.get("species").getAsString();
                String type = ch.get("type").getAsString();
                String gender = ch.get("gender").getAsString();

                JsonObject origin = ch.getAsJsonObject("origin");
                JsonObject location = ch.getAsJsonObject("location");

                int id_origin = getLocationIdFromUrl(origin.get("url").getAsString());
                int id_location = getLocationIdFromUrl(location.get("url").getAsString());

                JsonArray episodes = ch.getAsJsonArray("episode");
                List<Integer> episodeIds = new ArrayList<>();
                for (int e = 0; e < episodes.size(); e++) {
                    String epUrl = episodes.get(e).getAsString();
                    int epId = getIdFromUrl(epUrl);
                    episodeIds.add(epId);
                }

                result.add(new CharacterData(id, name, status, species, type, gender, id_origin, id_location, episodeIds));
            }

            JsonObject info = json.get("info").getAsJsonObject();
            int pages = info.get("pages").getAsInt();
            page++;
            if (page > pages) {
                hasMore = false;
            }
        }
        return result;
    }
    
    /**
     * Inserta personajes y sus relaciones con episodios en la base de datos.
     * - Inserta cada personaje usando add_character.
     * - Inserta cada relación personaje-episodio usando add_character_in_episode.
     * @param conn Conexión a la base de datos
     * @param characters Lista de objetos CharacterData
     * @throws SQLException si ocurre un error en la inserción
     */
    private static void insertCharactersAndRelations(Connection conn, List<CharacterData> characters) throws SQLException {
        try (CallableStatement csChar = conn.prepareCall("CALL add_character(?,?,?,?,?,?,?,?)");
             CallableStatement csRel = conn.prepareCall("CALL add_character_in_episode(?,?)")) {
            for (CharacterData ch : characters) {
                csChar.setInt(1, ch.getId());
                csChar.setString(2, ch.getName());
                csChar.setString(3, ch.getStatus());
                csChar.setString(4, ch.getSpecies());
                csChar.setString(5, ch.getType());
                csChar.setString(6, ch.getGender());
                csChar.setInt(7, ch.getId_origin());
                csChar.setInt(8, ch.getId_location());
                csChar.execute();

                for (Integer epId : ch.getEpisodeIds()) {
                    csRel.setInt(1, ch.getId());
                    csRel.setInt(2, epId);
                    csRel.execute();
                }
            }
        }
    }
    
    /**
     * Realiza una petición HTTP GET a la URL especificada y retorna la respuesta como string.
     * @param url URL a la que se realizará la petición
     * @return Cuerpo de la respuesta HTTP
     * @throws IOException si ocurre un error de I/O
     * @throws InterruptedException si la petición HTTP es interrumpida
     */
    private static String doGetRequest(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    /**
     * Dada una URL de una location o similar, extrae el último segmento numérico y lo retorna como int.
     * Si no se puede extraer, retorna 0.
     * @param url URL de la que extraer el ID
     * @return ID extraído o 0 si falla
     */
    private static int getLocationIdFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return 0;
        }
        String[] parts = url.split("/");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Dada una URL (por ejemplo de un episodio), extrae el último segmento numérico como ID.
     * Si no se puede extraer, retorna 0.
     * @param url URL de la que extraer el ID
     * @return ID extraído o 0 si falla
     */
    private static int getIdFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return 0;
        }
        String[] parts = url.split("/");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
