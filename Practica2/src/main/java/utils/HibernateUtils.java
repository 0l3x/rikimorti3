package utils;

import models.Character;
import models.Location;
import models.Episode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HibernateUtils {

    private static final SessionFactory sessionFactory;

    static {
    	Logger.getLogger("org.hibernate").setLevel(Level.SEVERE); // solo muestra errores graves
        try {
        	// Configuración básica para construir una sesión con hibernate
            Configuration config = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/serie")
                .setProperty("hibernate.connection.username", "usuariodev")
                .setProperty("hibernate.connection.password", "123")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                // Agrego las clases de entidad:
                .addAnnotatedClass(Character.class)
                .addAnnotatedClass(Location.class)
                .addAnnotatedClass(Episode.class);

            sessionFactory = config.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error en la inicialización de Hibernate: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}
