package repo.jdbc;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.Insumo;
import domain.enums.EstadoInsumo;
import exceptions.DatabaseException;
import repo.InsumoRepository;
import repo.jdbc.TransactionManager;

/**
 * Implementación JDBC del repositorio de insumos
 * Utiliza ArrayList para almacenar resultados y arreglos para parámetros
 */
public class InsumoJDBC implements InsumoRepository {
  
  @Override
  public Optional<Insumo> findByCodigo(String codigo) {
    String sql = "SELECT codigo, nombre, unidad, stock, stock_minimo, estado, fecha_vencimiento " +
                 "FROM insumos WHERE codigo = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, codigo);
      
      rs = stmt.executeQuery();
      
      if (rs.next()) {
        Insumo insumo = mapResultSetToInsumo(rs);
        return Optional.of(insumo);
      }
      
      return Optional.empty();
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al buscar insumo por código: " + codigo, e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public List<Insumo> searchByNombre(String nombreParcial) {
    String sql = "SELECT codigo, nombre, unidad, stock, stock_minimo, estado, fecha_vencimiento " +
                 "FROM insumos WHERE nombre LIKE ? ORDER BY nombre";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      // Usar LIKE con comodines para búsqueda parcial
      stmt.setString(1, "%" + nombreParcial + "%");
      
      rs = stmt.executeQuery();
      
      // Usar ArrayList para almacenar resultados
      List<Insumo> insumos = new ArrayList<>();
      
      while (rs.next()) {
        Insumo insumo = mapResultSetToInsumo(rs);
        insumos.add(insumo);
      }
      
      return insumos;
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al buscar insumos por nombre: " + nombreParcial, e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public List<Insumo> findCriticos() {
    String sql = "SELECT codigo, nombre, unidad, stock, stock_minimo, estado, fecha_vencimiento " +
                 "FROM insumos WHERE stock <= stock_minimo ORDER BY (stock - stock_minimo) ASC";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      rs = stmt.executeQuery();
      
      // Usar ArrayList para almacenar resultados
      List<Insumo> insumos = new ArrayList<>();
      
      while (rs.next()) {
        Insumo insumo = mapResultSetToInsumo(rs);
        insumos.add(insumo);
      }
      
      return insumos;
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al obtener insumos críticos", e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public List<Insumo> findAll() {
    String sql = "SELECT codigo, nombre, unidad, stock, stock_minimo, estado, fecha_vencimiento " +
                 "FROM insumos ORDER BY codigo";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      rs = stmt.executeQuery();
      
      // Usar ArrayList para almacenar resultados
      List<Insumo> insumos = new ArrayList<>();
      
      while (rs.next()) {
        Insumo insumo = mapResultSetToInsumo(rs);
        insumos.add(insumo);
      }
      
      return insumos;
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al obtener todos los insumos", e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public void save(Insumo i) {
    String sql = "INSERT INTO insumos (codigo, nombre, unidad, stock, stock_minimo, estado, fecha_vencimiento) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      
      // Usar arreglo de parámetros
      stmt.setString(1, i.getCodigo());
      stmt.setString(2, i.getNombre());
      stmt.setString(3, i.getUnidad());
      stmt.setInt(4, i.getStock());
      stmt.setInt(5, i.getStockMinimo());
      stmt.setString(6, i.getEstado().name());
      
      // Manejar fecha_vencimiento que puede ser NULL
      if (i.getFechaVencimiento() != null) {
        stmt.setDate(7, Date.valueOf(i.getFechaVencimiento()));
      } else {
        stmt.setNull(7, Types.DATE);
      }
      
      int rowsAffected = stmt.executeUpdate();
      
      if (rowsAffected == 0) {
        throw new DatabaseException("No se pudo insertar el insumo");
      }
      
    } catch (SQLException e) {
      if (e.getSQLState().equals("23000")) { // Violación de clave única
        throw new IllegalArgumentException("Código de insumo duplicado: " + i.getCodigo(), e);
      }
      throw new DatabaseException("Error al guardar insumo", e);
    } finally {
      closeResources(conn, stmt, null);
    }
  }

  @Override
  public void update(Insumo i) {
    String sql = "UPDATE insumos SET nombre = ?, unidad = ?, stock = ?, stock_minimo = ?, " +
                 "estado = ?, fecha_vencimiento = ? WHERE codigo = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      
      // Usar arreglo de parámetros
      stmt.setString(1, i.getNombre());
      stmt.setString(2, i.getUnidad());
      stmt.setInt(3, i.getStock());
      stmt.setInt(4, i.getStockMinimo());
      stmt.setString(5, i.getEstado().name());
      
      // Manejar fecha_vencimiento que puede ser NULL
      if (i.getFechaVencimiento() != null) {
        stmt.setDate(6, Date.valueOf(i.getFechaVencimiento()));
      } else {
        stmt.setNull(6, Types.DATE);
      }
      
      stmt.setString(7, i.getCodigo());
      
      int rowsAffected = stmt.executeUpdate();
      
      if (rowsAffected == 0) {
        throw new IllegalArgumentException("Insumo no encontrado: " + i.getCodigo());
      }
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al actualizar insumo", e);
    } finally {
      closeResources(conn, stmt, null);
    }
  }

  /**
   * Mapea un ResultSet a un objeto Insumo
   */
  private Insumo mapResultSetToInsumo(ResultSet rs) throws SQLException {
    String codigo = rs.getString("codigo");
    String nombre = rs.getString("nombre");
    String unidad = rs.getString("unidad");
    int stock = rs.getInt("stock");
    int stockMinimo = rs.getInt("stock_minimo");
    EstadoInsumo estado = EstadoInsumo.valueOf(rs.getString("estado"));
    
    // Manejar fecha_vencimiento que puede ser NULL
    Date fechaVencimientoDate = rs.getDate("fecha_vencimiento");
    LocalDate fechaVencimiento = (fechaVencimientoDate != null) 
        ? fechaVencimientoDate.toLocalDate() 
        : null;
    
    Insumo insumo = new Insumo(codigo, nombre, unidad, stock, stockMinimo, estado, fechaVencimiento);
    return insumo;
  }

  /**
   * Obtiene una conexión: usa la de transacción si existe, sino crea una nueva
   */
  private Connection getConnection() throws SQLException {
    Connection transConn = TransactionManager.getCurrentConnection();
    if (transConn != null) {
      return transConn; // Usar conexión de transacción
    }
    return DatabaseConnection.getConnection(); // Crear nueva conexión
  }

  /**
   * Cierra los recursos de forma segura
   * NO cierra la conexión si está en una transacción
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
    // Solo cerrar la conexión si NO está en una transacción
    if (!TransactionManager.isTransactionActive()) {
      DatabaseConnection.closeConnection(conn);
    }
  }
}

