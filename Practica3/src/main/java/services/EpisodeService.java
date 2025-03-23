package services;

import utils.HibernateUtils;
import utils.IntValidator;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import jakarta.persistence.PersistenceException;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class EpisodeService {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Muestra todos los episodios disponibles con todos sus atributos
     */
    public static void listarTodosLosEpisodios() {
        try (Session session = HibernateUtils.getSession()) {
        	 List<Object[]> resultados = session.createNativeQuery(
        	            "SELECT id, name, air_date, episode FROM episode ORDER BY id"
        	        ).list();

        	        if (resultados.isEmpty()) {
        	            System.out.println("No hay episodios registrados.");
        	            return;
        	        }

        	        System.out.println("\nLista completa de episodios:");
        	        for (Object[] fila : resultados) {
        	            Integer id = (Integer) fila[0];
        	            String name = (String) fila[1];
        	            Date airDate = (Date) fila[2];
        	            String code = (String) fila[3];

        	            System.out.println("ID: " + id + " | Nombre: " + name +
        	                               " | Fecha emisión: " + airDate +
        	                               " | Código: " + code);
        	        }
        } catch (PersistenceException e) {
            System.out.println("Error de persistencia al listar los episodios: " + e.getMessage());
        }
    }
    
    /**
     * Inserta un nuevo episodio en la base de datos utilizando Native Query
     */
    public static void insertarEpisodio() {
        try (Session session = HibernateUtils.getSession()) {
            int id = obtenerSiguienteIdDesdeBD(session);
            System.out.println("ID automático asignado: " + id);

            // Nombre del episodio
            String name;
            do {
                System.out.print("Introduce el nombre del episodio: ");
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("El nombre no puede estar vacío.");
                }
            } while (name.isEmpty());

            // Fecha de emisión
            Date airDate = null;
            while (airDate == null) {
                System.out.print("Introduce la fecha de emisión (YYYY-MM-DD): ");
                String fechaStr = scanner.nextLine().trim();
                try {
                    airDate = Date.valueOf(fechaStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("Formato inválido. Usa el formato correcto!! (YYYY-MM-DD).");
                }
            }

            // Código del episodio (validación formato SxxExx)
            String code;
            do {
                System.out.print("Introduce el código del episodio (ej. S01E01): ");
                code = scanner.nextLine().trim().toUpperCase();
                if (!code.matches("^S\\d{2}E\\d{2}$")) {
                    System.out.println("Formato inválido. Debe ser tipo 'S01E01'.");
                    code = null;
                }
            } while (code == null);

            // Confirmación antes de insertarlo en la BBDD
            System.out.println("\nResumen de datos a insertar:");
            System.out.println("ID: " + id);
            System.out.println("Nombre: " + name);
            System.out.println("Fecha de emisión: " + airDate);
            System.out.println("Código: " + code);
            System.out.print("¿Deseas confirmar la inserción? ('si' para confirmar, resto cancela la inserción): ");
            String confirmar = scanner.nextLine().trim();
            if (!confirmar.equalsIgnoreCase("si")) {
                System.out.println("Inserción cancelada por el usuario.");
                return;
            }

            // Inserción en la BD
            Transaction tx = session.beginTransaction();
            session.createNativeQuery("CALL add_episode(:id, :name, :air_date, :code)")
                    .setParameter("id", id)
                    .setParameter("name", name)
                    .setParameter("air_date", airDate)
                    .setParameter("code", code)
                    .executeUpdate();
            tx.commit();

            System.out.println("Episodio insertado con éxito.");
            
            System.out.println("ID: " + id +
                    " | Nombre: " + name +
                    " | Fecha emisión: " + airDate +
                    " | Código: " + code);
            
//            asignarPersonajeAEpisodio();
                
        } catch (PersistenceException e) {
            System.out.println("Error de persistencia al insertar el episodio: " + e.getMessage());
    	} catch (Exception e) {
            System.out.println("Error al insertar el episodio: " + e.getMessage());
        }
    }

    /**
     * Calcula el siguiente ID disponible para insertar un episodio nuevo.
     * @param session Sesión activa
     * @return ID siguiente (MAX(id) + 1 o 1 si no hay episodios)
     */
    private static int obtenerSiguienteIdDesdeBD(Session session) {
        Object maxId = session.createNativeQuery("SELECT MAX(id) FROM episode").uniqueResult();
        return (maxId == null) ? 1 : ((Number) maxId).intValue() + 1;
    }

    /**
     * Permite buscar episodios por texto en el nombre (no sensible a mayúsculas)
     */
    public static void buscarEpisodiosPorTexto() {
        System.out.print("Introduce el texto a buscar en el nombre del episodio: ");
        String texto = scanner.nextLine().trim();

        try (Session session = HibernateUtils.getSession()) {
            List<Object[]> resultados = session.createNativeQuery(
                    "SELECT id, name, air_date, episode FROM episode WHERE LOWER(name) LIKE LOWER(:text) ORDER BY id")
                    .setParameter("text", "%" + texto + "%")
                    .list();

            if (resultados.isEmpty()) {
                System.out.println("No se encontraron episodios con ese texto.");
            } else {
                System.out.println("\nEpisodios encontrados:");
                for (Object[] fila : resultados) {
                	 Integer id = (Integer) fila[0];
                     String name = (String) fila[1];
                     Date airDate = (Date) fila[2];
                     String code = (String) fila[3];

                     System.out.println("ID: " + id + " | Nombre: " + name +
                                        " | Fecha emisión: " + airDate +
                                        " | Código: " + code);
                }
            }
        } catch (HibernateException e) {
            System.out.println("Error al buscar episodios: " + e.getMessage());
        }
    }

    /**
     * Busca y muestra el episodio en el que aparecen más personajes
     */
    public static void buscarEpisodioConMasPersonajes() {
        try (Session session = HibernateUtils.getSession()) {
            Object[] resultado = (Object[]) session.createNativeQuery(
                "SELECT e.id, e.name, COUNT(ce.id_character) AS total " +
                "FROM episode e JOIN character_in_episode ce ON e.id = ce.id_episode " +
                "GROUP BY e.id ORDER BY total DESC LIMIT 1")
                .uniqueResult();

            if (resultado != null) {
                System.out.println("\nEpisodio con más personajes:");
                System.out.println("ID: " + resultado[0] + " | Nombre: " + resultado[1] + " | Nº de personajes: " + resultado[2]);
            } else {
                System.out.println("No se encontraron episodios con personajes asociados.");
            }
        }
    }

    /**
     * Permite modificar un episodio por ID, con validación de entrada de datos correctos y confirmación.
     */
    public static void modificarEpisodio() {
        listarTodosLosEpisodios();
        System.out.print("Introduce el ID del episodio a modificar: ");
        int id = IntValidator.validarEntero(scanner);
        
        Transaction tx = null;

        try (Session session = HibernateUtils.getSession()) {
            Object[] episodio = (Object[]) session.createNativeQuery("SELECT id, name, air_date, episode FROM episode WHERE id = :id")
                    .setParameter("id", id)
                    .uniqueResult();

            if (episodio == null) {
                System.out.println("No se encontró el episodio con ID " + id);
                return;
            }

            String nuevoNombre;
            System.out.print("Nuevo nombre (deja vacío para mantener): ");
            nuevoNombre = scanner.nextLine().trim();
            if (nuevoNombre.isEmpty()) nuevoNombre = (String) episodio[1];

            Date nuevaFecha = null;
            while (nuevaFecha == null) {
                System.out.print("Nueva fecha (YYYY-MM-DD, deja vacío para mantener): ");
                String fechaStr = scanner.nextLine().trim();
                if (fechaStr.isEmpty()) {
                    nuevaFecha = (Date) episodio[2];
                    break;
                }
                try {
                    nuevaFecha = Date.valueOf(fechaStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("Formato inválido. Usa 'YYYY-MM-DD'.");
                }
            }

            String nuevoCodigo;
            do {
                System.out.print("Nuevo código (ej. S01E01, deja vacío para mantener): ");
                nuevoCodigo = scanner.nextLine().trim().toUpperCase();
                if (nuevoCodigo.isEmpty()) {
                    nuevoCodigo = (String) episodio[3];
                    break;
                }
                if (!nuevoCodigo.matches("^S\\d{2}E\\d{2}$")) {
                    System.out.println("Formato inválido. Debe seguir 'S01E01'.");
                    nuevoCodigo = null;
                }
            } while (nuevoCodigo == null);

            System.out.println("\nCambios propuestos:");
            System.out.println("Nombre: " + nuevoNombre);
            System.out.println("Fecha: " + nuevaFecha);
            System.out.println("Código: " + nuevoCodigo);

            System.out.print("¿Confirmas los cambios? ('si' para confirmar, resto cancela la modificación): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("si")) {
                System.out.println("Modificación cancelada.");
                return;
            }

            tx = session.beginTransaction();
            session.createNativeQuery("UPDATE episode SET name = :name, air_date = :date, episode = :code WHERE id = :id")
                    .setParameter("name", nuevoNombre)
                    .setParameter("date", nuevaFecha)
                    .setParameter("code", nuevoCodigo)
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();

            System.out.println("Episodio modificado con éxito.");
            
            System.out.println("ID: " + id +
                    " | Nombre: " + nuevoNombre +
                    " | Fecha emisión: " + nuevaFecha +
                    " | Código: " + nuevoCodigo);

        } catch (PersistenceException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error de persistencia al modificar episodio: " + e.getMessage());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Error inesperado al modificar episodio: " + e.getMessage());
        }
    }


    /**
     * Borra un episodio por ID, confirmando previamente con el usuario
     * e informando de los personajes asociados si existen.
     */
    public static void borrarEpisodio() {
        listarTodosLosEpisodios();
        System.out.print("Introduce el ID del episodio a borrar: ");
        int id = IntValidator.validarEntero(scanner);
        
        Transaction tx = null;

        try (Session session = HibernateUtils.getSession()) {
            List<Object[]> personajes = session.createNativeQuery("SELECT c.id, c.name FROM character c JOIN character_in_episode ce ON c.id = ce.id_character WHERE ce.id_episode = :id")
                    .setParameter("id", id)
                    .list();

            if (!personajes.isEmpty()) {
                System.out.println("El episodio está asociado a los siguientes personajes:");
                for (Object[] c : personajes) {
                    System.out.println("ID: " + c[0] + " | Nombre: " + c[1]);
                }
            } else {
                System.out.println("Este episodio no tiene personajes asociados.");
            }

            System.out.print("¿Estás seguro de que quieres borrarlo? ('si' para confirmar, resto cancela el borrado): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("si")) {
                System.out.println("Borrado cancelado.");
                return;
            }

            tx = session.beginTransaction();
            session.createNativeQuery("DELETE FROM character_in_episode WHERE id_episode = :id").setParameter("id", id).executeUpdate();
            session.createNativeQuery("DELETE FROM episode WHERE id = :id").setParameter("id", id).executeUpdate();
            tx.commit();

            System.out.println("Episodio eliminado correctamente.");

        } catch (PersistenceException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error de persistencia al borrar el episodio: " + e.getMessage());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Error al borrar episodio: " + e.getMessage());
        }
    }
    
    
    /**
     * Asigna uno o más personajes a un episodio.
     * Muestra todos los personajes disponibles, el usuario introduce los IDs uno por uno
     * y finaliza pulsando Enter sin escribir nada.
     */
    public static void asignarPersonajeAEpisodio() {
        listarTodosLosEpisodios();
        System.out.print("Introduce el ID del episodio al que quieres asignar personajes: ");
        int idEpisodio = IntValidator.validarEntero(scanner);
        
        Transaction tx = null;
        
        try (Session session = HibernateUtils.getSession()) {        	
            // Comprobar si el episodio existe
            Object[] episodio = (Object[]) session.createNativeQuery("SELECT id, name FROM episode WHERE id = :id")
                .setParameter("id", idEpisodio)
                .uniqueResult();

            if (episodio == null) {
                System.out.println("No se encontró el episodio.");
                return;
            }

            // Mostrar personajes disponibles
            List<Object[]> personajes = session.createNativeQuery("SELECT id, name FROM character ORDER BY id").list();

            if (personajes.isEmpty()) {
                System.out.println("No hay personajes disponibles.");
                return;
            }

            System.out.println("\nPersonajes disponibles:");
            for (Object[] p : personajes) {
                System.out.println("ID: " + p[0] + " | Nombre: " + p[1]);
            }

            System.out.println("\nIntroduce los ID de los personajes a añadir uno por uno. Deja vacío para terminar.");

            tx = session.beginTransaction();

            while (true) {
                System.out.print("ID personaje: ");
                String entrada = scanner.nextLine().trim();
                if (entrada.isEmpty()) break;

                try {
                    int idPersonaje = Integer.parseInt(entrada);

                    // Comprobar si el personaje existe
                    Object existe = session.createNativeQuery("SELECT id FROM character WHERE id = :id")
                            .setParameter("id", idPersonaje)
                            .uniqueResult();

                    if (existe == null) {
                        System.out.println("Personaje no encontrado.");
                        continue;
                    }

                    // Comprobar si ya está asignado
                    Object asignado = session.createNativeQuery("""
                            SELECT 1 FROM character_in_episode 
                            WHERE id_character = :charId AND id_episode = :epId
                        """)
                        .setParameter("charId", idPersonaje)
                        .setParameter("epId", idEpisodio)
                        .uniqueResult();

                    if (asignado != null) {
                        System.out.println("El personaje ya está asignado a este episodio.");
                        continue;
                    }

                    // Insertar relación
                    session.createNativeQuery("""
                            INSERT INTO character_in_episode (id_character, id_episode)
                            VALUES (:charId, :epId)
                        """)
                        .setParameter("charId", idPersonaje)
                        .setParameter("epId", idEpisodio)
                        .executeUpdate();

                    System.out.println("Personaje añadido con éxito.");

                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Introduce un número válido o deja vacío para salir.");
                } 
            }

            tx.commit();
            System.out.println("Todos los personajes han sido procesados.");

        } catch (PersistenceException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error de persistencia al meter personajes al episodio: " + e.getMessage());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Error al meter personajes al episodio: " + e.getMessage());
        }
    }


}
