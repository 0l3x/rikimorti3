package services;

import models.Location;
import models.Character;
import utils.HibernateUtils;
import utils.IntValidator;

import java.util.List;
import java.util.Scanner;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Clase encargada de la gestión de localizaciones en la base de datos
 * utilizando Hibernate y HQL. Permite listar, buscar, insertar, modificar y borrar locations.
 */
public class LocationService {

    private static final Scanner scanner = new Scanner(System.in);
    
    /**
     * Lista todas las localizaciones registradas en la base de datos,
     * ordenadas por ID, mostrando también su tipo y dimensión.
     */
    public static void listarTodasLasLocalizaciones() {
        try (Session session = HibernateUtils.getSession()) {
            List<Location> locations = session.createQuery("FROM Location ORDER BY id", Location.class).list();
            if (locations.isEmpty()) {
                System.out.println("No hay localizaciones registradas.");
                return;
            }

            System.out.println("\nListado de localizaciones:");
            for (Location loc : locations) {
                System.out.println("ID: " + loc.getId() +
                                   " | Nombre: " + loc.getName() +
                                   " | Tipo: " + (loc.getType() != null ? loc.getType() : "unknown") +
                                   " | Dimensión: " + (loc.getDimension() != null ? loc.getDimension() : "unknown"));
            }
        } catch (HibernateException e) {
            System.out.println("Error al listar localizaciones: " + e.getMessage());
        }
    }
    
    /**
     * Busca localizaciones cuyo nombre contenga un texto introducido por el usuario.
     * La búsqueda no es sensible a mayúsculas.
     */
    public static void buscarLocalizacionesPorTexto() {
        System.out.print("Introduce el texto a buscar: ");
        String texto = scanner.nextLine().trim();

        try (Session session = HibernateUtils.getSession()) {
            String hql = "FROM Location l WHERE LOWER(l.name) LIKE LOWER(:text)";
            Query<Location> query = session.createQuery(hql, Location.class);
            query.setParameter("text", "%" + texto + "%");

            List<Location> resultados = query.list();

            if (resultados.isEmpty()) {
                System.out.println("No se encontraron localizaciones con ese texto.");
            } else {
                System.out.println("\nLocalizaciones encontradas:");
                for (Location loc : resultados) {
                	System.out.println("ID: " + loc.getId() +
                            " | Nombre: " + loc.getName() +
                            " | Tipo: " + (loc.getType() != null ? loc.getType() : "unknown") +
                            " | Dimensión: " + (loc.getDimension() != null ? loc.getDimension() : "unknown"));
                }
            }
        } catch (HibernateException e) {
            System.out.println("Error al buscar localizaciones: " + e.getMessage());
        }
    }
    
    /**
     * Muestra las localizaciones que no tienen personajes asociados
     * ni como origen ni como ubicación actual.
     */
    public static void buscarLocationsSinPersonajes() {
        try (Session session = HibernateUtils.getSession()) {
            String hql = """
                FROM Location l WHERE size(l.originCharacters) = 0 AND size(l.locationCharacters) = 0
            """;
            List<Location> results = session.createQuery(hql, Location.class).list();

            if (results.isEmpty()) {
                System.out.println("No hay localizaciones sin personajes asociados.");
            } else {
                System.out.println("Localizaciones sin personajes:");
                for (Location loc : results) {
                	System.out.println("ID: " + loc.getId() +
                            " | Nombre: " + loc.getName() +
                            " | Tipo: " + (loc.getType() != null ? loc.getType() : "unknown") +
                            " | Dimensión: " + (loc.getDimension() != null ? loc.getDimension() : "unknown"));
                }
            }
        } catch (HibernateException e) {
            System.out.println("Error al buscar localizaciones sin personajes: " + e.getMessage());
        }
    }
    
    /**
     * Inserta una nueva localización en la base de datos.
     * El nombre es obligatorio. Si no se introducen tipo o dimensión, se guardará "unknown".
     */
    public static void insertarLocation() {
        Transaction tx = null;

        try (Session session = HibernateUtils.getSession()) {
            int id = obtenerSiguienteId(session);

            // Nombre obligatorio
            String name;
            do {
                System.out.print("Introduce el nombre de la localización: ");
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("El nombre no puede estar vacío.");
                }
            } while (name.isEmpty());

            // Tipo y Dimensión opcionales, se guarda "unknown" si se deja vacío
            System.out.print("Introduce el tipo (opcional): ");
            String type = scanner.nextLine().trim();
            if (type.isEmpty()) type = "unknown";

            System.out.print("Introduce la dimensión (opcional): ");
            String dimension = scanner.nextLine().trim();
            if (dimension.isEmpty()) dimension = "unknown";

            // Confirmación
            System.out.println("\nResumen de datos a insertar:");
            System.out.println("ID: " + id);
            System.out.println("Nombre: " + name);
            System.out.println("Tipo: " + type);
            System.out.println("Dimensión: " + dimension);
            System.out.print("¿Deseas confirmar la inserción? ('si' para confirmar, cualquier otra tecla cancela): ");
            String confirm = scanner.nextLine().trim();

            if (!confirm.equalsIgnoreCase("si")) {
                System.out.println("Inserción cancelada por el usuario.");
                return;
            }

            // Insertar en BD
            tx = session.beginTransaction();
            Location loc = new Location(id, name, type, dimension);
            session.persist(loc);
            tx.commit();

            System.out.println("Localización insertada con éxito!");
            
            System.out.println("\nDetalles de la localización insertada:");
            System.out.println("ID: " + loc.getId() +
                               " | Nombre: " + loc.getName() +
                               " | Tipo: " + loc.getType() +
                               " | Dimensión: " + loc.getDimension());


        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error al insertar localización: " + e.getMessage());
        }
    }
    
    /**
     * Modifica una localización existente.
     * Muestra los datos actuales, solicita nuevas entradas, valida cambios
     * y pide confirmación antes de aplicar los cambios en la base de datos.
     */
    public static void modificarLocation() {
        listarTodasLasLocalizaciones();
        System.out.print("Introduce el ID de la localización a modificar: ");
        int id = IntValidator.validarEntero(scanner);

        Transaction tx = null;

        try (Session session = HibernateUtils.getSession()) {
            Location loc = session.get(Location.class, id);

            if (loc == null) {
                System.out.println("No se encontró la localización con ID " + id);
                return;
            }

            System.out.println("\nLocalización actual:");
            System.out.println("Nombre: " + loc.getName() + " | Tipo: " + loc.getType() + " | Dimensión: " + loc.getDimension());

            System.out.print("Nuevo nombre (vacío para mantener): ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) loc.setName(name);

            System.out.print("Nuevo tipo (vacío para mantener): ");
            String type = scanner.nextLine().trim();
            if (!type.isEmpty()) loc.setType(type);

            System.out.print("Nueva dimensión (vacío para mantener): ");
            String dimension = scanner.nextLine().trim();
            if (!dimension.isEmpty()) loc.setDimension(dimension);

            System.out.println("\nCambios propuestos:");
            System.out.println("Nombre: " + loc.getName());
            System.out.println("Tipo: " + loc.getType());
            System.out.println("Dimensión: " + loc.getDimension());

            System.out.print("¿Confirmas los cambios? ('si' para confirmar, resto cancela la modificación): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("si")) {
                System.out.println("Modificación cancelada.");
                return;
            }

            tx = session.beginTransaction();
            session.merge(loc);
            tx.commit();

            System.out.println("Localización modificada con éxito!");
            System.out.println("\nLocalización actualizada:");
            System.out.println("ID: " + loc.getId() +
                               " | Nombre: " + loc.getName() +
                               " | Tipo: " + loc.getType() +
                               " | Dimensión: " + loc.getDimension());


        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error al modificar localización: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una localización de la base de datos.
     * Antes de eliminarla, muestra cuántos personajes están asociados como origen o localización actual.
     * Si existen personajes asociados, se eliminan las referencias a esta localización en cada uno de ellos
     * para evitar conflictos de integridad. Luego se solicita confirmación al usuario antes de proceder con el borrado.
     */
    public static void borrarLocation() {
        listarTodasLasLocalizaciones();
        System.out.print("Introduce el ID de la localización a borrar: ");
        int id = IntValidator.validarEntero(scanner);

        Transaction tx = null;

        try (Session session = HibernateUtils.getSession()) {
            Location loc = session.get(Location.class, id);

            if (loc == null) {
                System.out.println("No se encontró la localización con ID " + id);
                return;
            }

            int asociados = loc.getOriginCharacters().size() + loc.getLocationCharacters().size();

            if (asociados > 0) {
                System.out.println("La localización tiene " + asociados + " personajes asociados.");
            } else {
                System.out.println("No hay personajes asociados a esta localización.");
            }

            System.out.print("¿Deseas eliminarla igualmente? ('si' para confirmar, resto cancela la eliminación): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("si")) {
                System.out.println("Eliminación cancelada.");
                return;
            }

            tx = session.beginTransaction();

            // Desvincula a los personajes que la usan como origin
            for (Character c : loc.getOriginCharacters()) {
                c.setOrigin(null);
                session.merge(c);
            }

            // Desvincula a los personajes que la usan como location
            for (Character c : loc.getLocationCharacters()) {
                c.setLocation(null);
                session.merge(c);
            }

            session.remove(loc);
            tx.commit();

            System.out.println("Localización eliminada con éxito.");

        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error al borrar localización: " + e.getMessage());
        }
    }

    
    /**
     * Devuelve el siguiente ID disponible para insertar una localización nueva.
     * 
     * @param session Sesión Hibernate activa
     * @return siguiente ID (MAX + 1) o 1 si no hay registros
     */
    private static int obtenerSiguienteId(Session session) {
        Integer maxId = (Integer) session.createQuery("SELECT MAX(l.id) FROM Location l").uniqueResult();
        return (maxId == null) ? 1 : maxId + 1;
    }
    
}
