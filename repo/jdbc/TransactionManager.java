package repo.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestor de transacciones para operaciones que requieren múltiples queries atómicas
 * Utiliza ThreadLocal para mantener la conexión por hilo
 */
public class TransactionManager {
  // ThreadLocal para mantener la conexión de transacción por hilo
  private static final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();
  
  /**
   * Inicia una nueva transacción
   * 
   * @return Connection para usar en las operaciones dentro de la transacción
   * @throws SQLException si hay error al obtener la conexión
   */
  public static Connection beginTransaction() throws SQLException {
    Connection conn = DatabaseConnection.getConnection();
    conn.setAutoCommit(false); // Desactivar auto-commit para control manual
    transactionConnection.set(conn);
    return conn;
  }
  
  /**
   * Confirma la transacción actual
   * 
   * @throws SQLException si hay error al hacer commit
   */
  public static void commit() throws SQLException {
    Connection conn = transactionConnection.get();
    if (conn != null) {
      try {
        if (!conn.isClosed()) {
          conn.commit();
        }
      } finally {
        try {
          conn.setAutoCommit(true); // Restaurar auto-commit
        } catch (SQLException e) {
          System.err.println("Error al restaurar auto-commit: " + e.getMessage());
        }
        DatabaseConnection.closeConnection(conn);
        transactionConnection.remove();
      }
    }
  }
  
  /**
   * Revierte la transacción actual
   */
  public static void rollback() {
    Connection conn = transactionConnection.get();
    if (conn != null) {
      try {
        if (!conn.isClosed()) {
          conn.rollback();
        }
      } catch (SQLException e) {
        System.err.println("Error al hacer rollback: " + e.getMessage());
      } finally {
        try {
          conn.setAutoCommit(true); // Restaurar auto-commit
        } catch (SQLException e) {
          System.err.println("Error al restaurar auto-commit: " + e.getMessage());
        }
        DatabaseConnection.closeConnection(conn);
        transactionConnection.remove();
      }
    }
  }
  
  /**
   * Obtiene la conexión de la transacción actual, o null si no hay transacción activa
   * 
   * @return Connection de la transacción o null
   */
  public static Connection getCurrentConnection() {
    return transactionConnection.get();
  }
  
  /**
   * Verifica si hay una transacción activa
   * 
   * @return true si hay transacción activa, false en caso contrario
   */
  public static boolean isTransactionActive() {
    Connection conn = transactionConnection.get();
    if (conn == null) {
      return false;
    }
    try {
      return !conn.isClosed();
    } catch (SQLException e) {
      // Si hay error al verificar, asumimos que no está activa
      return false;
    }
  }
}

