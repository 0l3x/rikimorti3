package services;

import utils.DBUtils;
import utils.HibernateUtils;
import utils.IntValidator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.hibernate.Session;
import models.Character;
import models.Location;

import org.hibernate.query.Query;

public class CharacterService {

    private static final Scanner scanner = new Scanner(System.in);

	/**
     * Pide el nombre del personaje, validando que no esté vacío ni supere 50 caracteres.
     */
    private static String pedirNombre() {
        String name;
        boolean vacio = false;
        do {
            System.out.println("Introduce el nombre del personaje:");
            name = scanner.nextLine().trim();
            if (name.isEmpty() || name.length() > 100) {
                System.out.println("El nombre no puede estar vacío o ser mayor de 100 caracteres.");
                vacio = false;
            } else {
                vacio = true;
            }
        } while (!vacio);
        return name;
    }

    /**
     * Este método:
     * - Pide el nombre de un personaje.
     * - Llama al procedimiento remove_episodes_from_character(p_name).
     * - Llama a showCharactersWithoutEpisodes() para mostrar los personajes sin episodio.
     */
    public static void removeEpisodesFromCharacterAndShow() {
        System.out.println("Introduce el nombre del personaje para eliminar sus episodios:");
        String characterName = scanner.nextLine().trim();

        try (Connection con = DBUtils.getConnection()) {
            con.setAutoCommit(false);

            String callQuery = "CALL remove_episodes_from_character(?)";
            try (CallableStatement cs = con.prepareCall(callQuery)) {
                cs.setString(1, characterName);
                cs.execute();
                con.commit();
                System.out.println("Episodios del personaje '" + characterName + "' eliminados con éxito.");
            } catch (SQLException e) {
                System.out.println("Error al eliminar episodios del personaje: " + e.getMessage());
                con.rollback();
                return;
            }

            // Ahora mostramos los personajes sin episodio
            buscarPersonajesSinEpisodiosHQL();

        } catch (SQLException e) {
            System.out.println("Error con la base de datos: " + e.getMessage());
        }
    }
    
    // hql madness

	public static void buscarPersonajesPorTexto() {
        System.out.print("Introduce el texto a buscar en el nombre del personaje: ");
        String searchText = scanner.nextLine().trim();

        try (Session session = HibernateUtils.getSession()) {
            Query<Character> query = session.createQuery(
                "FROM Character c WHERE LOWER(c.name) LIKE LOWER(:name)", Character.class);
            query.setParameter("name", "%" + searchText + "%"); // Búsqueda parcial
            
            List<Character> results = query.list();
            
            if (results.isEmpty()) {
                System.out.println("No se encontraron personajes con el nombre que contiene: " + searchText);
            } else {
                System.out.println("\nResultados de la búsqueda:");
                for (Character character : results) {
                    System.out.println("ID: " + character.getId() + " | Nombre: " + character.getName());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al buscar personajes: " + e.getMessage());
        }
	}
	
	public static void buscarPersonajesSinEpisodiosHQL() {
	    try (Session session = HibernateUtils.getSession()) {
	        String hql = "SELECT c FROM Character c WHERE c.id NOT IN " +
	                     "(SELECT ce.id FROM Character ce JOIN ce.episodes e)";

	        List<Character> results = session.createQuery(hql, Character.class).list();

	        if (results.isEmpty()) {
	            System.out.println("\nNo hay personajes sin episodios.");
	        } else {
	            System.out.println("\nPersonajes sin episodios:");
	            for (Character character : results) {
	                System.out.println("ID: " + character.getId() + " | Nombre: " + character.getName());
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("Error al buscar personajes sin episodios: " + e.getMessage());
	    }
	}
	
	
	public static void insertarPersonajeHQL() {
	    try (Session session = HibernateUtils.getSession()) {
	        session.beginTransaction();

	        int nextId = obtenerSiguienteIdDesdeHibernate(session);

	        String name = pedirNombre();
	        String status = seleccionarStatusDesdeBD();
	        String species = seleccionarEspeciesDesdeBD();
	        String type = seleccionarTipoDesdeBD();
	        String gender = seleccionarGeneroDesdeBD();

	        // Mostrar localizaciones disponibles y obtener id
	        List<Location> locations = obtenerLocalizacionesDesdeBD(session);

	        if (locations.isEmpty()) {
	            System.out.println("No hay localizaciones disponibles. No se puede continuar.");
	            return;
	        }

	        int idOrigin = seleccionarLocationDeLista(locations, "origen");
	        int idLocation = seleccionarLocationDeLista(locations, "ubicación actual");

	        Location origin = session.get(Location.class, idOrigin);
	        Location location = session.get(Location.class, idLocation);

	        if (origin == null || location == null) {
	            System.out.println("Una de las localizaciones seleccionadas no existe.");
	            return;
	        }

	        Character character = new Character();
	        character.setId(nextId);
	        character.setName(name);
	        character.setStatus(status);
	        character.setSpecies(species);
	        character.setType(type);
	        character.setGender(gender);
	        character.setOrigin(origin);
	        character.setLocation(location);

	        session.persist(character);
	        session.getTransaction().commit();
	        System.out.println("Personaje insertado con éxito.");
	        
	        mostrarPersonaje(character);

	    } catch (Exception e) {
	        System.out.println("Error al insertar personaje: " + e.getMessage());
	    }
	}
	
	public static void mostrarPersonaje(Character character) {
	    if (character == null) {
	        System.out.println("El personaje no existe o es nulo.");
	        return;
	    }

	    System.out.println("\nDetalles del personaje:");
	    System.out.println("ID: " + character.getId());
	    System.out.println("Nombre: " + character.getName());
	    System.out.println("Estado: " + character.getStatus());
	    System.out.println("Especie: " + character.getSpecies());
	    System.out.println("Tipo: " + character.getType());
	    System.out.println("Género: " + character.getGender());
	    System.out.println("Origen: " + (character.getOrigin() != null ? character.getOrigin().getName() : "Desconocido"));
	    System.out.println("Ubicación actual: " + (character.getLocation() != null ? character.getLocation().getName() : "Desconocida"));
	}

	@SuppressWarnings("deprecation")
	private static int obtenerSiguienteIdDesdeHibernate(Session session) {
	    Integer maxId = (Integer) session.createQuery("SELECT MAX(c.id) FROM Character c").uniqueResult();
	    return (maxId == null) ? 1 : maxId + 1;
	}
	
	public static String seleccionarStatusDesdeBD() {
        try (Session session = HibernateUtils.getSession()) {
            return seleccionarValorDesdeBD(session, "status", "Estatus");
        }
    }

    public static String seleccionarEspeciesDesdeBD() {
        try (Session session = HibernateUtils.getSession()) {
            return seleccionarValorDesdeBD(session, "species", "Especies");
        }
    }

    public static String seleccionarTipoDesdeBD() {
        try (Session session = HibernateUtils.getSession()) {
            return seleccionarValorDesdeBD(session, "type", "Tipos");
        }
    }

    public static String seleccionarGeneroDesdeBD() {
        try (Session session = HibernateUtils.getSession()) {
            return seleccionarValorDesdeBD(session, "gender", "Géneros");
        }
    }
	
	private static String seleccionarValorDesdeBD(Session session, String campo, String descripcionCampo) {
        List<String> valores = obtenerValoresUnicos(session, campo);

        if (valores.isEmpty()) {
            System.out.println("No existen valores registrados para " + descripcionCampo + ".");
            return "";
        }

        int opcion = -1;
        do {
            System.out.println(descripcionCampo + " disponibles en la BD:");
            for (int i = 0; i < valores.size(); i++) {
                System.out.println((i + 1) + ". " + valores.get(i));
            }
            System.out.print("Selecciona una opción (1-" + valores.size() + "): ");
            String input = scanner.nextLine().trim();
            try {
                opcion = Integer.parseInt(input);
                if (opcion < 1 || opcion > valores.size()) {
                    System.out.println("Opción inválida. Inténtalo de nuevo.\n");
                    opcion = -1;
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduce un número válido.\n");
            }
        } while (opcion == -1);

        return valores.get(opcion - 1);
    }
	
	private static List<String> obtenerValoresUnicos(Session session, String campo) {
        String hql = "SELECT DISTINCT c." + campo + " FROM Character c WHERE c." + campo + " IS NOT NULL AND c." + campo + " <> '' ORDER BY c." + campo;
        return session.createQuery(hql, String.class).list();
    }

	private static List<Location> obtenerLocalizacionesDesdeBD(Session session) {
	    return session.createQuery("FROM Location", Location.class).list();
	}

	private static int seleccionarLocationDeLista(List<Location> locations, String tipo) {
	    System.out.println("\nLocalizaciones disponibles para " + tipo + ":");
	    for (Location loc : locations) {
	        System.out.println("ID: " + loc.getId() + " - Nombre: " + loc.getName());
	    }

	    int id;
	    boolean valido;
	    do {
	        System.out.print("Introduce el ID de la localización de " + tipo + ": ");
	        id = IntValidator.validarEntero(scanner);
	        int idABuscar = id;
	        valido = locations.stream().anyMatch(loc -> loc.getId() == idABuscar);
	        if (!valido) {
	            System.out.println("ID inválido. Intenta nuevamente.");
	        }
	    } while (!valido);

	    return id;
	}

	// aun no
	
	public static void modificarPersonajeHQL() {
	    try (Session session = HibernateUtils.getSession()) {
	        System.out.print("Introduce el ID del personaje a modificar: ");
	        int id = IntValidator.validarEntero(scanner);

	        Character personaje = session.get(Character.class, id);

	        if (personaje == null) {
	            System.out.println("❌ No se encontró el personaje con ID " + id);
	            return;
	        }

	        System.out.println("\n📝 Modificando personaje actual:");
	        mostrarPersonaje(personaje);

	        System.out.print("Nuevo nombre (deja vacío para mantener): ");
	        String nombre = scanner.nextLine().trim();
	        if (!nombre.isEmpty()) personaje.setName(nombre);

	        System.out.print("¿Deseas cambiar el estado? (s/n): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
	            String status = seleccionarValorDesdeBD(session, "status", "Estatus");
	            personaje.setStatus(status);
	        }

	        System.out.print("¿Deseas cambiar la especie? (s/n): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
	            String species = seleccionarValorDesdeBD(session, "species", "Especies");
	            personaje.setSpecies(species);
	        }

	        System.out.print("¿Deseas cambiar el tipo? (s/n): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
	            String type = seleccionarValorDesdeBD(session, "type", "Tipos");
	            personaje.setType(type);
	        }

	        System.out.print("¿Deseas cambiar el género? (s/n): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
	            String gender = seleccionarValorDesdeBD(session, "gender", "Géneros");
	            personaje.setGender(gender);
	        }

	        System.out.print("¿Deseas cambiar la localización de origen? (s/n): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
	            List<Location> locations = obtenerLocalizacionesDesdeBD(session);
	            int idOrigin = seleccionarLocationDeLista(locations, "origen");
	            Location origin = session.get(Location.class, idOrigin);
	            personaje.setOrigin(origin);
	        }

	        System.out.print("¿Deseas cambiar la localización actual? (s/n): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
	            List<Location> locations = obtenerLocalizacionesDesdeBD(session);
	            int idLocation = seleccionarLocationDeLista(locations, "ubicación actual");
	            Location location = session.get(Location.class, idLocation);
	            personaje.setLocation(location);
	        }

	        session.beginTransaction();
	        session.merge(personaje);
	        session.getTransaction().commit();

	        System.out.println("✅ Personaje modificado con éxito.");
	        mostrarPersonaje(personaje);

	    } catch (Exception e) {
	        System.out.println("❌ Error al modificar personaje: " + e.getMessage());
	    }
	}
	
	public static void borrarPersonajeHQL() {
	    System.out.print("Introduce el ID del personaje a borrar: ");
	    int id = IntValidator.validarEntero(scanner);

	    try (Session session = HibernateUtils.getSession()) {
	        Character personaje = session.get(Character.class, id);

	        if (personaje == null) {
	            System.out.println("❌ No se encontró el personaje con ID " + id);
	            return;
	        }

	        session.beginTransaction();
	        session.remove(personaje);
	        session.getTransaction().commit();

	        System.out.println("Personaje eliminado con éxito.");
	    } catch (Exception e) {
	        System.out.println("Error al eliminar personaje: " + e.getMessage());
	    }
	}


	
}
