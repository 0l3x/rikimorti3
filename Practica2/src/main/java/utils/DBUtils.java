package utils;

import java.sql.*;
import java.util.Properties;

public class DBUtils {
    private static final String urlDB = "jdbc:postgresql://localhost:5432/serie";
    
    private static final Properties props = new Properties() {{
        setProperty("user", "usuariodev");
        setProperty("password", "123");
    }};

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(urlDB, props);
    }

    public static ResultSet execSELECT(String query, Object... params) throws SQLException {
        Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);

        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }

        return ps.executeQuery();
    }

    public static void execDML(String query, Object... params) throws SQLException {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            ps.executeUpdate();
        }
    }
}
