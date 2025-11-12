package repo.jdbc;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import domain.Insumo;
import domain.Movimiento;
import domain.Servicio;
import domain.Usuario;
import domain.enums.EstadoInsumo;
import domain.enums.Rol;
import domain.enums.TipoMovimiento;
import exceptions.DatabaseException;
import repo.MovimientoRepository;
import repo.jdbc.TransactionManager;

/**
 * Implementación JDBC del repositorio de movimientos
 * Utiliza ArrayList para almacenar resultados y arreglos para parámetros
 */
public class MovimientoJDBC implements MovimientoRepository {
  
  // Dependencias para cargar entidades relacionadas
  private final repo.InsumoRepository insumos;
  private final repo.UsuarioRepository usuarios;
  private final repo.ServicioRepository servicios;
  
  public MovimientoJDBC(repo.InsumoRepository insumos, 
                       repo.UsuarioRepository usuarios, 
                       repo.ServicioRepository servicios) {
    this.insumos = insumos;
    this.usuarios = usuarios;
    this.servicios = servicios;
  }
  
  @Override
  public void save(Movimiento m) {
    String sql = "INSERT INTO movimientos (tipo, fecha, cantidad, usuario_legajo, insumo_codigo, servicio_id) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet generatedKeys = null;
    
    try {
      conn = getConnection();
      // Solicitar que se generen las claves automáticas
      stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      
      // Usar arreglo de parámetros
      stmt.setString(1, m.getTipo().name());
      stmt.setTimestamp(2, Timestamp.valueOf(m.getFecha()));
      stmt.setInt(3, m.getCantidad());
      stmt.setInt(4, m.getUsuario().getLegajo());
      stmt.setString(5, m.getInsumo().getCodigo());
      
      // Manejar servicio_id que puede ser NULL para INGRESO
      if (m.getServicio() != null) {
        stmt.setInt(6, m.getServicio().getId());
      } else {
        stmt.setNull(6, Types.INTEGER);
      }
      
      int rowsAffected = stmt.executeUpdate();
      
      if (rowsAffected == 0) {
        throw new DatabaseException("No se pudo insertar el movimiento");
      }
      
      // Obtener el ID generado
      generatedKeys = stmt.getGeneratedKeys();
      if (generatedKeys.next()) {
        // Usar reflexión para establecer el ID en el objeto Movimiento
        try {
          java.lang.reflect.Field idField = Movimiento.class.getDeclaredField("id");
          idField.setAccessible(true);
          idField.set(m, generatedKeys.getInt(1));
        } catch (Exception e) {
          // Si no se puede establecer el ID, continuamos sin error
          System.err.println("No se pudo establecer el ID generado: " + e.getMessage());
        }
      }
      
    } catch (SQLException e) {
      if (e.getSQLState().equals("23000")) { // Violación de foreign key
        throw new IllegalArgumentException("Error de integridad referencial: " + e.getMessage(), e);
      }
      throw new DatabaseException("Error al guardar movimiento", e);
    } finally {
      closeResources(conn, stmt, generatedKeys);
    }
  }

  @Override
  public List<Movimiento> findAll() {
    String sql = "SELECT m.id, m.tipo, m.fecha, m.cantidad, " +
                 "m.usuario_legajo, m.insumo_codigo, m.servicio_id " +
                 "FROM movimientos m " +
                 "ORDER BY m.fecha DESC, m.id DESC";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      rs = stmt.executeQuery();
      
      // Usar ArrayList para almacenar resultados
      List<Movimiento> movimientos = new ArrayList<>();
      
      while (rs.next()) {
        Movimiento movimiento = mapResultSetToMovimiento(rs);
        movimientos.add(movimiento);
      }
      
      return movimientos;
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al obtener todos los movimientos", e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public List<Movimiento> findByPeriodoYServicio(LocalDate desde, LocalDate hasta, Integer servicioId) {
    // Construir la consulta dinámicamente según los parámetros
    StringBuilder sqlBuilder = new StringBuilder(
        "SELECT m.id, m.tipo, m.fecha, m.cantidad, " +
        "m.usuario_legajo, m.insumo_codigo, m.servicio_id " +
        "FROM movimientos m WHERE 1=1"
    );
    
    // Usar ArrayList para almacenar los parámetros dinámicos
    List<Object> params = new ArrayList<>();
    
    if (desde != null) {
      sqlBuilder.append(" AND DATE(m.fecha) >= ?");
      params.add(desde);
    }
    
    if (hasta != null) {
      sqlBuilder.append(" AND DATE(m.fecha) <= ?");
      params.add(hasta);
    }
    
    if (servicioId != null) {
      sqlBuilder.append(" AND m.servicio_id = ?");
      params.add(servicioId);
    }
    
    sqlBuilder.append(" ORDER BY m.fecha DESC, m.id DESC");
    
    String sql = sqlBuilder.toString();
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = getConnection();
      stmt = conn.prepareStatement(sql);
      
      // Establecer parámetros usando el ArrayList
      for (int i = 0; i < params.size(); i++) {
        Object param = params.get(i);
        if (param instanceof LocalDate) {
          stmt.setDate(i + 1, Date.valueOf((LocalDate) param));
        } else if (param instanceof Integer) {
          stmt.setInt(i + 1, (Integer) param);
        }
      }
      
      rs = stmt.executeQuery();
      
      // Usar ArrayList para almacenar resultados
      List<Movimiento> movimientos = new ArrayList<>();
      
      while (rs.next()) {
        Movimiento movimiento = mapResultSetToMovimiento(rs);
        movimientos.add(movimiento);
      }
      
      return movimientos;
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al buscar movimientos por período y servicio", e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  /**
   * Mapea un ResultSet a un objeto Movimiento
   * Carga las entidades relacionadas desde los repositorios
   */
  private Movimiento mapResultSetToMovimiento(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    TipoMovimiento tipo = TipoMovimiento.valueOf(rs.getString("tipo"));
    Timestamp fechaTimestamp = rs.getTimestamp("fecha");
    LocalDateTime fecha = fechaTimestamp.toLocalDateTime();
    int cantidad = rs.getInt("cantidad");
    
    // Cargar entidades relacionadas
    int usuarioLegajo = rs.getInt("usuario_legajo");
    Usuario usuario = usuarios.findByLegajo(usuarioLegajo)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioLegajo));
    
    String insumoCodigo = rs.getString("insumo_codigo");
    Insumo insumo = insumos.findByCodigo(insumoCodigo)
        .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + insumoCodigo));
    
    // Servicio puede ser NULL
    Servicio servicio = null;
    int servicioId = rs.getInt("servicio_id");
    if (!rs.wasNull()) {
      servicio = servicios.findById(servicioId)
          .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + servicioId));
    }
    
    return new Movimiento(id, tipo, fecha, cantidad, usuario, insumo, servicio);
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

