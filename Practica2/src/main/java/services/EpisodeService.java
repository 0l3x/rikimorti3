package services;

import models.Episode;
import models.Character;
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
     * Muestra todos los episodios disponibles con su ID y nombre
     */
    public static void listarTodosLosEpisodios() {
        try (Session session = HibernateUtils.getSession()) {
            List<Object[]> resultados = session.createNativeQuery("SELECT id, name FROM episode").list();
            System.out.println("\nLista de episodios disponibles:");
            for (Object[] fila : resultados) {
                System.out.println("ID: " + fila[0] + " | Nombre: " + fila[1]);
            }
        } catch (PersistenceException e) {
            System.out.println("Error de persistencia al listar los episodios: " + e.getMessage());
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
                    System.out.println("❌ Formato inválido. Debe ser tipo 'S01E01'.");
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
        } catch (PersistenceException e) {
            System.out.println("Error de persistencia al insertar el episodio: " + e.getMessage());
    	} catch (Exception e) {
            System.out.println("Error al insertar el episodio: " + e.getMessage());
        }
    }


    /**
     * Permite buscar episodios por texto en el nombre (no sensible a mayúsculas)
     */
    public static void buscarEpisodiosPorTexto() {
        System.out.print("Introduce el texto a buscar en el nombre del episodio: ");
        String texto = scanner.nextLine().trim();

        try (Session session = HibernateUtils.getSession()) {
            List<Object[]> resultados = session.createNativeQuery(
                    "SELECT id, name FROM episode WHERE LOWER(name) LIKE LOWER(:text)")
                    .setParameter("text", "%" + texto + "%")
                    .list();

            if (resultados.isEmpty()) {
                System.out.println("No se encontraron episodios con ese texto.");
            } else {
                System.out.println("\nEpisodios encontrados:");
                for (Object[] fila : resultados) {
                    System.out.println("ID: " + fila[0] + " | Nombre: " + fila[1]);
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
     * Permite modificar un episodio por ID
     */
    public static void modificarEpisodio() {
        listarTodosLosEpisodios();
        System.out.print("Introduce el ID del episodio a modificar: ");
        int id = IntValidator.validarEntero(scanner);

        try (Session session = HibernateUtils.getSession()) {
            Object[] episodio = (Object[]) session.createNativeQuery("SELECT id, name, air_date, episode FROM episode WHERE id = :id")
                    .setParameter("id", id)
                    .uniqueResult();

            if (episodio == null) {
                System.out.println("No se encontró el episodio con ID " + id);
                return;
            }

            System.out.print("Nuevo nombre (deja vacío para mantener): ");
            String nuevoNombre = scanner.nextLine().trim();
            if (nuevoNombre.isEmpty()) nuevoNombre = (String) episodio[1];

            System.out.print("Nueva fecha (YYYY-MM-DD, deja vacío para mantener): ");
            String nuevaFecha = scanner.nextLine().trim();
            Date fechaFinal = nuevaFecha.isEmpty() ? (Date) episodio[2] : Date.valueOf(nuevaFecha);

            System.out.print("Nuevo código (deja vacío para mantener): ");
            String nuevoCodigo = scanner.nextLine().trim();
            if (nuevoCodigo.isEmpty()) nuevoCodigo = (String) episodio[3];

            System.out.println("\nCambios propuestos:");
            System.out.println("Nombre: " + nuevoNombre);
            System.out.println("Fecha: " + fechaFinal);
            System.out.println("Código: " + nuevoCodigo);

            System.out.print("¿Confirmas los cambios? (si/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("si")) {
                System.out.println("Modificación cancelada.");
                return;
            }

            Transaction tx = session.beginTransaction();
            session.createNativeQuery("UPDATE episode SET name = :name, air_date = :date, episode = :code WHERE id = :id")
                    .setParameter("name", nuevoNombre)
                    .setParameter("date", fechaFinal)
                    .setParameter("code", nuevoCodigo)
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();

            System.out.println("Episodio modificado con éxito.");

        } catch (Exception e) {
            System.out.println("Error al modificar episodio: " + e.getMessage());
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

            System.out.print("¿Estás seguro de que quieres borrarlo? (si/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("si")) {
                System.out.println("Borrado cancelado.");
                return;
            }

            Transaction tx = session.beginTransaction();
            session.createNativeQuery("DELETE FROM character_in_episode WHERE id_episode = :id").setParameter("id", id).executeUpdate();
            session.createNativeQuery("DELETE FROM episode WHERE id = :id").setParameter("id", id).executeUpdate();
            tx.commit();

            System.out.println("Episodio eliminado correctamente.");

        } catch (Exception e) {
            System.out.println("Error al borrar episodio: " + e.getMessage());
        }
    }

}
