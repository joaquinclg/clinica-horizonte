package repo.jdbc;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.Usuario;
import domain.enums.Rol;
import exceptions.DatabaseException;
import repo.UsuarioRepository;

/**
 * Implementación JDBC del repositorio de usuarios
 * Utiliza ArrayList para almacenar resultados y arreglos para parámetros
 */
public class UsuarioJDBC implements UsuarioRepository {
  
  @Override
  public Optional<Usuario> findByLegajo(int legajo) {
    String sql = "SELECT legajo, password, nombre, apellido, rol, activo, creado_en " +
                 "FROM usuarios WHERE legajo = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      // Usar arreglo para parámetros
      stmt.setInt(1, legajo);
      
      rs = stmt.executeQuery();
      
      if (rs.next()) {
        Usuario usuario = mapResultSetToUsuario(rs);
        return Optional.of(usuario);
      }
      
      return Optional.empty();
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al buscar usuario por legajo: " + legajo, e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public Optional<Usuario> findByLegajoAndPassword(int legajo, String password) {
    String sql = "SELECT legajo, password, nombre, apellido, rol, activo, creado_en " +
                 "FROM usuarios WHERE legajo = ? AND password = ? AND activo = 1";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      // Usar arreglo para múltiples parámetros
      stmt.setInt(1, legajo);
      stmt.setString(2, password);
      
      rs = stmt.executeQuery();
      
      if (rs.next()) {
        Usuario usuario = mapResultSetToUsuario(rs);
        return Optional.of(usuario);
      }
      
      return Optional.empty();
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al buscar usuario por legajo y password", e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public List<Usuario> findAllActivos() {
    String sql = "SELECT legajo, password, nombre, apellido, rol, activo, creado_en " +
                 "FROM usuarios WHERE activo = 1 ORDER BY legajo";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      rs = stmt.executeQuery();
      
      // Usar ArrayList para almacenar resultados
      List<Usuario> usuarios = new ArrayList<>();
      
      while (rs.next()) {
        Usuario usuario = mapResultSetToUsuario(rs);
        usuarios.add(usuario);
      }
      
      return usuarios;
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al obtener usuarios activos", e);
    } finally {
      closeResources(conn, stmt, rs);
    }
  }

  @Override
  public void save(Usuario u) {
    String sql = "INSERT INTO usuarios (legajo, password, nombre, apellido, rol, activo, creado_en) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      
      // Usar arreglo de parámetros
      stmt.setInt(1, u.getLegajo());
      stmt.setString(2, u.getPassword());
      stmt.setString(3, u.getNombre());
      stmt.setString(4, u.getApellido());
      stmt.setString(5, u.getRol().name());
      stmt.setBoolean(6, u.isActivo());
      stmt.setTimestamp(7, Timestamp.valueOf(u.getCreadoEn()));
      
      int rowsAffected = stmt.executeUpdate();
      
      if (rowsAffected == 0) {
        throw new DatabaseException("No se pudo insertar el usuario");
      }
      
    } catch (SQLException e) {
      if (e.getSQLState().equals("23000")) { // Violación de clave única
        throw new IllegalArgumentException("Legajo duplicado: " + u.getLegajo(), e);
      }
      throw new DatabaseException("Error al guardar usuario", e);
    } finally {
      closeResources(conn, stmt, null);
    }
  }

  @Override
  public void update(Usuario u) {
    String sql = "UPDATE usuarios SET password = ?, nombre = ?, apellido = ?, rol = ?, activo = ? " +
                 "WHERE legajo = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      
      // Usar arreglo de parámetros
      stmt.setString(1, u.getPassword());
      stmt.setString(2, u.getNombre());
      stmt.setString(3, u.getApellido());
      stmt.setString(4, u.getRol().name());
      stmt.setBoolean(5, u.isActivo());
      stmt.setInt(6, u.getLegajo());
      
      int rowsAffected = stmt.executeUpdate();
      
      if (rowsAffected == 0) {
        throw new IllegalArgumentException("Usuario no encontrado: " + u.getLegajo());
      }
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al actualizar usuario", e);
    } finally {
      closeResources(conn, stmt, null);
    }
  }

  @Override
  public void deleteLogico(int legajo) {
    String sql = "UPDATE usuarios SET activo = 0 WHERE legajo = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
      conn = DatabaseConnection.getConnection();
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, legajo);
      
      int rowsAffected = stmt.executeUpdate();
      
      if (rowsAffected == 0) {
        throw new IllegalArgumentException("Usuario no encontrado: " + legajo);
      }
      
    } catch (SQLException e) {
      throw new DatabaseException("Error al desactivar usuario", e);
    } finally {
      closeResources(conn, stmt, null);
    }
  }

  /**
   * Mapea un ResultSet a un objeto Usuario
   * Usa reflexión para establecer campos privados (activo y creadoEn)
   */
  private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
    int legajo = rs.getInt("legajo");
    String password = rs.getString("password");
    String nombre = rs.getString("nombre");
    String apellido = rs.getString("apellido");
    Rol rol = Rol.valueOf(rs.getString("rol"));
    boolean activo = rs.getBoolean("activo");
    Timestamp creadoEnTimestamp = rs.getTimestamp("creado_en");
    LocalDateTime creadoEn = (creadoEnTimestamp != null) 
        ? creadoEnTimestamp.toLocalDateTime() 
        : LocalDateTime.now();
    
    Usuario usuario = new Usuario(legajo, password, nombre, apellido, rol);
    
    // Usar reflexión para establecer campos privados
    try {
      // Establecer activo
      if (!activo) {
        usuario.desactivar();
      }
      
      // Establecer creadoEn usando reflexión
      Field creadoEnField = Usuario.class.getDeclaredField("creadoEn");
      creadoEnField.setAccessible(true);
      creadoEnField.set(usuario, creadoEn);
      
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // Si falla la reflexión, continuamos sin error (usará valores por defecto)
      System.err.println("Advertencia: No se pudo establecer creadoEn usando reflexión: " + e.getMessage());
    }
    
    return usuario;
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

