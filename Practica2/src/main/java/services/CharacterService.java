package services;

import utils.HibernateUtils;
import utils.IntValidator;

import java.util.List;
import java.util.Scanner;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import models.Character;
import models.Episode;
import models.Location;

import org.hibernate.query.Query;

/**
 * Clase encargada de la gestión de personajes en la base de datos
 * utilizando Hibernate. Permite insertar, modificar, borrar y buscar personajes.
 */
public class CharacterService {

    private static final Scanner scanner = new Scanner(System.in);

	/**
     * Pide el nombre del personaje, validando que no esté vacío ni supere los 100 caracteres.
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
     * Busca personajes cuyo nombre contenga un texto determinado (no sensible a mayúsculas).
     * Muestra los resultados por consola.
     */
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
        } catch (HibernateException e) {
            System.out.println("Error al buscar personajes: " + e.getMessage());
        }
	}
	
	/**
     * Busca personajes que no estén asociados a ningún episodio.
     * Utiliza HQL para realizar una subconsulta y mostrar los personajes sin episodios.
     */
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
	    } catch (HibernateException e) {
	        System.out.println("Error al buscar personajes sin episodios: " + e.getMessage());
	    }
	}
	
	/**
     * Inserta un nuevo personaje en la base de datos utilizando Hibernate.
     * Solicita todos los datos necesarios al usuario.
     * Muestra los detalles del personaje insertado al finalizar.
     */
	public static void insertarPersonajeHQL() {
	    try (Session session = HibernateUtils.getSession()) {
	        session.beginTransaction();

	        int nextId = obtenerSiguienteIdDesdeHibernate(session);

	        String name = pedirNombre();
	        String status = seleccionarStatusDesdeBD();
	        String species = seleccionarEspeciesDesdeBD();
	        String type = seleccionarTipoDesdeBD();
	        String gender = seleccionarGeneroDesdeBD();

	        // Mostrar localizaciones disponibles y obtener el id suyo
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

	    } catch (HibernateException e) {
	        System.out.println("Error al insertar personaje: " + e.getMessage());
	    }
	}
	
	/**
     * Muestra por consola todos los atributos del personaje recibido.
     * 
     * @param character Objeto de tipo Character a mostrar.
     */
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
	
	/**
     * Devuelve el siguiente ID disponible para insertar un personaje nuevo.
     * 
     * @param session Sesión Hibernate abierta.
     * @return El próximo ID disponible (máximo ID actual + 1, o 1 si no hay personajes).
     */
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
    
	
    /**
     * Solicita al usuario la selección de un valor para un campo de tipo (como status, species...).
     * 
     * @param session Sesión Hibernate abierta.
     * @param campo Nombre del campo en la entidad Character (ej. "status").
     * @param descripcionCampo Descripción para mostrar al usuario.
     * @return Valor seleccionado por el usuario.
     */
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
	
	/**
     * Devuelve una lista de valores únicos existentes en la base de datos para un campo de tipo.
     * 
     * @param session Sesión Hibernate abierta.
     * @param campo Campo de la entidad Character a consultar.
     * @return Lista de valores únicos encontrados.
     */
	private static List<String> obtenerValoresUnicos(Session session, String campo) {
        String hql = "SELECT DISTINCT c." + campo + " FROM Character c WHERE c." + campo + " IS NOT NULL AND c." + campo + " <> '' ORDER BY c." + campo;
        return session.createQuery(hql, String.class).list();
    }
	
	/**
     * Devuelve una lista con todas las localizaciones disponibles en la base de datos.
     * 
     * @param session Sesión Hibernate abierta.
     * @return Lista de objetos Location.
     */
	private static List<Location> obtenerLocalizacionesDesdeBD(Session session) {
	    return session.createQuery("FROM Location", Location.class).list();
	}
	
	/**
     * Muestra las localizaciones disponibles y solicita al usuario que introduzca un ID válido.
     * 
     * @param locations Lista de localizaciones disponibles.
     * @param tipo Tipo de localización (origen o ubicación actual).
     * @return ID de la localización seleccionada por el usuario.
     */
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
	
	/**
     * Permite modificar un personaje existente.
     * Solicita al usuario cada cambio por atributo, muestra los cambios realizados y pide confirmación antes de guardar definitivamente.
     * Si se confirma, aplica los cambios mediante la transacción de Hibernate. Si no, se cancela.
     */
	public static void modificarPersonajeHQL() {
		listarTodosLosPersonajes();
	    Transaction tx = null;
	    
	    try (Session session = HibernateUtils.getSession()) {
	        System.out.print("Introduce el ID del personaje a modificar: ");
	        int id = IntValidator.validarEntero(scanner);

	        Character personaje = session.get(Character.class, id);

	        if (personaje == null) {
	            System.out.println("No se encontró el personaje con ID " + id);
	            return;
	        }

	        System.out.println("\nModificando personaje actual:");
	        mostrarPersonaje(personaje);

	        System.out.print("Nuevo nombre (dejalo vacío para mantener el mismo): ");
	        String nombre = scanner.nextLine().trim();
	        if (!nombre.isEmpty()) personaje.setName(nombre);

	        System.out.print("¿Deseas cambiar el estado? ('si' para cambiarlo, sino pasa al siguiente atributo): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
	            String status = seleccionarValorDesdeBD(session, "status", "Estatus");
	            personaje.setStatus(status);
	        }

	        System.out.print("¿Deseas cambiar la especie? ('si' para cambiarlo, sino pasa al siguiente atributo): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
	            String species = seleccionarValorDesdeBD(session, "species", "Especies");
	            personaje.setSpecies(species);
	        }

	        System.out.print("¿Deseas cambiar el tipo? ('si' para cambiarlo, sino pasa al siguiente atributo): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
	            String type = seleccionarValorDesdeBD(session, "type", "Tipos");
	            personaje.setType(type);
	        }

	        System.out.print("¿Deseas cambiar el género? ('si' para cambiarlo, sino pasa al siguiente atributo): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
	            String gender = seleccionarValorDesdeBD(session, "gender", "Géneros");
	            personaje.setGender(gender);
	        }

	        System.out.print("¿Deseas cambiar la localización de origen? ('si' para cambiarlo, sino pasa al siguiente atributo): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
	            List<Location> locations = obtenerLocalizacionesDesdeBD(session);
	            int idOrigin = seleccionarLocationDeLista(locations, "origen");
	            Location origin = session.get(Location.class, idOrigin);
	            personaje.setOrigin(origin);
	        }

	        System.out.print("¿Deseas cambiar la localización actual? ('si' para cambiarlo, sino pasa a la siguiente opción): ");
	        if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
	            List<Location> locations = obtenerLocalizacionesDesdeBD(session);
	            int idLocation = seleccionarLocationDeLista(locations, "ubicación actual");
	            Location location = session.get(Location.class, idLocation);
	            personaje.setLocation(location);
	        }

	        // === CONFIRMAR CAMBIOS ANTES DE GUARDAR ===
	        System.out.println("\nCambios a aplicar:");
	        mostrarPersonaje(personaje);
	        System.out.print("¿Confirmas los cambios? ('si' para aplicarlos, sino cancela los cambios y anula la transacción): ");
	        String confirmacion = scanner.nextLine().trim();

	        if (!confirmacion.equalsIgnoreCase("si")) {
	            System.out.println("Cambios cancelados. No se ha modificado nada.");
	            return; // sale de la modificación si el usuario no confirma la realización de la transacción
	        }

	        // === APLICAR CAMBIOS ===
	        tx = session.beginTransaction();
	        session.merge(personaje);
	        tx.commit();

	        System.out.println("Personaje modificado con éxito.");
	        mostrarPersonaje(personaje);

	    } catch (HibernateException e) {
	        if (tx != null) {
	            tx.rollback(); // rollback en caso de error
	        }
	        System.out.println("Error al modificar personaje: " + e.getMessage());
	    }
	}
	
	/**
     * Elimina un personaje de la base de datos utilizando su ID.
     * Valida si el personaje existe antes de proceder a su eliminación.
     */
	public static void borrarPersonajeHQL() {
	    Transaction tx = null;
	    
		listarTodosLosPersonajes();
	    System.out.println("Introduce el ID del personaje a borrar: ");
	    int id = IntValidator.validarEntero(scanner);

	    try (Session session = HibernateUtils.getSession()) {
	        Character personaje = session.get(Character.class, id);

	        if (personaje == null) {
	            System.out.println("No se encontró el personaje con ID " + id);
	            return;
	        }

	        List<Episode> episodios = personaje.getEpisodes();
	        if (!episodios.isEmpty()) {
	            System.out.println("El personaje está asociado a los siguientes episodios:");
	            for (Episode ep : episodios) {
	                System.out.println("ID: " + ep.getId() + " | Nombre: " + ep.getName());
	            }
	        } else {
	            System.out.println("El personaje no tiene episodios asociados.");
	        }

	        System.out.print("¿Seguro que deseas eliminar este personaje? ('si' para eliminarlo definitivamente, sino cancela el borrado): ");
	        String confirmar = scanner.nextLine().trim();
	        if (!confirmar.equalsIgnoreCase("si")) {
	            System.out.println("Operación cancelada.");
	            return;
	        }

	        tx = session.beginTransaction();

	        // Desasociar el personaje de todos los episodios
	        personaje.getEpisodes().clear();
	        session.merge(personaje);

	        // Eliminar el personaje
	        session.remove(personaje);

	        tx.commit();
	        System.out.println("Personaje eliminado con éxito.");
	    } catch (HibernateException e) {
	    	 if (tx != null) {
	             tx.rollback(); // rollback si hubo error
	         }
	        System.out.println("Error al eliminar personaje: " + e.getMessage());
	    }
	}
	
	public static void listarTodosLosPersonajes() {
		Session session = HibernateUtils.getSession();
				
	    List<Character> personajes = session.createQuery("FROM Character ORDER BY id", Character.class).list();

	    if (personajes.isEmpty()) {
	        System.out.println("No hay personajes en la base de datos.");
	        return;
	    }

	    System.out.println("Listado de personajes:");
	    for (Character p : personajes) {
	        System.out.println("ID: " + p.getId() + " | Nombre: " + p.getName());
	    }
	}

	
}
