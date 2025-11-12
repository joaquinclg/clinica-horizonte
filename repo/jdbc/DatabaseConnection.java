package repo.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para gestionar la conexión a la base de datos MySQL
 */
public class DatabaseConnection {
  private static final String URL = "jdbc:mysql://localhost:3306/clinica_horizonte";
  private static final String USER = "root";
  private static final String PASSWORD = "admin1234";
  
  static {
    try {
      // Cargar el driver de MySQL
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
    }
  }

  /**
   * Obtiene una conexión a la base de datos
   * 
   * @return Connection a la base de datos
   * @throws SQLException si hay un error al conectar
   */
  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }

  /**
   * Cierra una conexión de forma segura
   * 
   * @param conn la conexión a cerrar
   */
  public static void closeConnection(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        System.err.println("Error al cerrar la conexión: " + e.getMessage());
      }
    }
  }
}

