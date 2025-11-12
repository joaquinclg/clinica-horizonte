package repo.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.Servicio;
import exceptions.DatabaseException;
import repo.ServicioRepository;

/**
 * Implementación JDBC del repositorio de servicios
 * Utiliza ArrayList para almacenar resultados
 */
public class ServicioJDBC implements ServicioRepository {
  
  @Override
  public Optional<Servicio> findById(int id) {
    String sql = "SELECT id, nombre FROM servicios WHERE id = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, id);
      
      rs = stmt.executeQuery();
      
      if (rs.next()) {
        Servicio servicio = mapResultSetToServicio(rs);
        return Optional.of(servicio);
      }
      
      return Optional.empty();
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al buscar servicio por id: " + id, e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public Optional<Servicio> findByNombre(String nombre) {
    String sql = "SELECT id, nombre FROM servicios WHERE nombre = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, nombre);
      
      rs = stmt.executeQuery();
      
      if (rs.next()) {
        Servicio servicio = mapResultSetToServicio(rs);
        return Optional.of(servicio);
      }
      
      return Optional.empty();
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al buscar servicio por nombre: " + nombre, e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public List<Servicio> findAll() {
    String sql = "SELECT id, nombre FROM servicios ORDER BY id";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      rs = stmt.executeQuery();
      
      // Usar ArrayList para almacenar resultados
      List<Servicio> servicios = new ArrayList<>();
      
      while (rs.next()) {
        Servicio servicio = mapResultSetToServicio(rs);
        servicios.add(servicio);
      }
      
      return servicios;
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al obtener todos los servicios", e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public void save(Servicio s) {
    String sql = "INSERT INTO servicios (id, nombre) VALUES (?, ?)";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      
      stmt.setInt(1, s.getId());
      stmt.setString(2, s.getNombre());
      
      int rowsAffected = stmt.executeUpdate();
      
      if (rowsAffected == 0) {
        throw new DatabaseException("No se pudo insertar el servicio");
      }
      
    } catch (SQLException e) {
      if (e.getSQLState().equals("23000")) { // Violación de clave única
        throw new IllegalArgumentException("ID de servicio duplicado: " + s.getId(), e);
      }
      throw new DatabaseException("Error al guardar servicio", e);
    } finally {
      closeResources(conn, stmt, null);
    }
  }

  /**
   * Mapea un ResultSet a un objeto Servicio
   */
  private Servicio mapResultSetToServicio(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String nombre = rs.getString("nombre");
    return new Servicio(id, nombre);
  }

  /**
   * Cierra los recursos de forma segura
   */
  private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        System.err.println("Error al cerrar ResultSet: " + e.getMessage());
      }
    }
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        System.err.println("Error al cerrar Statement: " + e.getMessage());
      }
    }
    DatabaseConnection.closeConnection(conn);
  }
}

