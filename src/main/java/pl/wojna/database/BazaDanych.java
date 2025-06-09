package pl.wojna.database;

import pl.wojna.config.ConfigLoader;

import java.sql.*;

public class BazaDanych
{

    public static void initialize()
    {
        ConfigLoader.load();
        String DB_URL = ConfigLoader.getDbUrl();
        String DB_USER = ConfigLoader.getDbUser();
        String DB_PASS = ConfigLoader.getDbPass();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement())
        {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS wojna");
            System.out.println(" Baza danych gotowa.");

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement())
        {
            String sql = """
                CREATE TABLE IF NOT EXISTS rozdane_karty (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    gracz_id INT,
                    karta VARCHAR(10),
                    data TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.executeUpdate(sql);
            System.out.println(" Tabela 'rozdane_karty' gotowa.");

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void saveDeck(int graczId, String karta)
    {
        ConfigLoader.load();
        String DB_URL = ConfigLoader.getDbUrl();
        String DB_USER = ConfigLoader.getDbUser();
        String DB_PASS = ConfigLoader.getDbPass();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO rozdane_karty (gracz_id, karta) VALUES (?, ?)"))
        {
            ps.setInt(1, graczId);
            ps.setString(2, karta);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
