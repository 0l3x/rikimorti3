package services;

import models.Episode;
import utils.HibernateUtils;
import utils.IntValidator;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import jakarta.persistence.PersistenceException;

import java.util.List;
import java.util.Scanner;

public class EpisodeService {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Muestra todos los episodios disponibles con todos sus atributos
     */
    public static void listarTodosLosEpisodios() {
        try (Session session = HibernateUtils.getSession()) {
            List<Episode> episodios = session.createNativeQuery("SELECT * FROM episode ORDER BY id", Episode.class).list();

            if (episodios.isEmpty()) {
                System.out.println("No hay episodios registrados.");
                return;
            }

            System.out.println("\nLista completa de episodios:");
            for (Episode ep : episodios) {
                System.out.println("ID: " + ep.getId() + " | Nombre: " + ep.getName() +
                        " | Fecha emisión: " + ep.getAirDate() +
                        " | Código: " + ep.getEpisode());
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

            String name;
            do {
                System.out.print("Introduce el nombre del episodio: ");
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("El nombre no puede estar vacío.");
                }
            } while (name.isEmpty());

            java.sql.Date airDate = null;
            while (airDate == null) {
                System.out.print("Introduce la fecha de emisión (YYYY-MM-DD): ");
                String fechaStr = scanner.nextLine().trim();
                try {
                    airDate = java.sql.Date.valueOf(fechaStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("Formato inválido. Usa el formato correcto!! (YYYY-MM-DD).");
                }
            }

            String code;
            do {
                System.out.print("Introduce el código del episodio (ej. S01E01): ");
                code = scanner.nextLine().trim().toUpperCase();
                if (!code.matches("^S\\d{2}E\\d{2}$")) {
                    System.out.println("Formato inválido. Debe ser tipo 'S01E01'.");
                    code = null;
                }
            } while (code == null);

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

        } catch (PersistenceException e) {
            System.out.println("Error de persistencia al insertar el episodio: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error al insertar el episodio: " + e.getMessage());
        }
    }

    private static int obtenerSiguienteIdDesdeBD(Session session) {
        Object maxId = session.createNativeQuery("SELECT MAX(id) FROM episode").uniqueResult();
        return (maxId == null) ? 1 : ((Number) maxId).intValue() + 1;
    }

    public static void buscarEpisodiosPorTexto() {
        System.out.print("Introduce el texto a buscar en el nombre del episodio: ");
        String texto = scanner.nextLine().trim();

        try (Session session = HibernateUtils.getSession()) {
            List<Episode> episodios = session.createNativeQuery(
                    "SELECT * FROM episode WHERE LOWER(name) LIKE LOWER(:text) ORDER BY id",
                    Episode.class)
                    .setParameter("text", "%" + texto + "%")
                    .list();

            if (episodios.isEmpty()) {
                System.out.println("No se encontraron episodios con ese texto.");
            } else {
                System.out.println("\nEpisodios encontrados:");
                for (Episode ep : episodios) {
                    System.out.println("ID: " + ep.getId() + " | Nombre: " + ep.getName() +
                            " | Fecha emisión: " + ep.getAirDate() +
                            " | Código: " + ep.getEpisode());
                }
            }
        } catch (HibernateException e) {
            System.out.println("Error al buscar episodios: " + e.getMessage());
        }
    }

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

    // Las funciones modificarEpisodio(), borrarEpisodio() y asignarPersonajeAEpisodio() pueden mantenerse igual
    // si prefieres seguir usando Native Queries para estas partes específicas.

}
