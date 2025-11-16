package app.handlers;

import app.ui.ConsoleUI;
import app.ui.MenuPrincipal;
import domain.Usuario;
import domain.enums.Rol;
import exceptions.EntidadNoEncontradaException;
import usecase.GestionUsuariosService;

/**
 * Handler para operaciones relacionadas con la gestión de usuarios
 */
public class UsuarioHandler {
  private final GestionUsuariosService userService;

  public UsuarioHandler(GestionUsuariosService userService) {
    this.userService = userService;
  }

  /**
   * Lista todos los usuarios activos (solo ADMIN)
   */
  public void listarUsuarios(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      ConsoleUI.mostrarError("Acceso denegado: Solo usuarios ADMIN pueden ver este listado.");
      return;
    }
    
    ConsoleUI.mostrarInfo("\n-- Usuarios Activos --");
    var usuarios = userService.listarActivos();
    
    if (usuarios.isEmpty()) {
      ConsoleUI.mostrarInfo("No hay usuarios activos.");
    } else {
      usuarios.forEach(u -> ConsoleUI.mostrarFormato("%d - %s %s (%s)%n", 
          u.getLegajo(), u.getNombre(), u.getApellido(), u.getRol()));
    }
  }

  /**
   * Da de alta un nuevo usuario (solo ADMIN)
   */
  public void altaUsuario(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      ConsoleUI.mostrarError("Acceso denegado: Solo usuarios ADMIN pueden dar de alta usuarios.");
      return;
    }
    
    ConsoleUI.mostrarInfo("\n-- Alta de Usuario --");
    
    int legajo = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_LEGAJO);
    String nombre = ConsoleUI.leerString(MenuPrincipal.PROMPT_NOMBRE);
    String apellido = ConsoleUI.leerString(MenuPrincipal.PROMPT_APELLIDO);
    String password = ConsoleUI.leerString(MenuPrincipal.PROMPT_PASSWORD);
    String rolStr = ConsoleUI.leerString(MenuPrincipal.PROMPT_ROL).toUpperCase();
    
    Rol rol = Rol.valueOf(rolStr);
    Usuario nuevoUsuario = new Usuario(legajo, password, nombre, apellido, rol);
    userService.alta(nuevoUsuario);
    ConsoleUI.mostrarExito("Usuario creado exitosamente.");
  }

  /**
   * Modifica un usuario existente (solo ADMIN)
   */
  public void modificarUsuario(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      ConsoleUI.mostrarError("Acceso denegado: Solo usuarios ADMIN pueden modificar usuarios.");
      return;
    }
    
    ConsoleUI.mostrarInfo("\n-- Modificar Usuario --");
    
    int legajo = ConsoleUI.leerEntero("Legajo del usuario a modificar: ");
    
    // Buscar el usuario existente (activo o inactivo)
    Usuario usuarioExistente;
    try {
      usuarioExistente = userService.obtenerPorLegajo(legajo);
    } catch (EntidadNoEncontradaException e) {
      ConsoleUI.mostrarError(e.getMessage());
      return;
    }
    
    ConsoleUI.mostrarInfo("\nUsuario actual:");
    ConsoleUI.mostrarFormato("  Nombre: %s%n", usuarioExistente.getNombre());
    ConsoleUI.mostrarFormato("  Apellido: %s%n", usuarioExistente.getApellido());
    ConsoleUI.mostrarFormato("  Rol: %s%n", usuarioExistente.getRol());
    ConsoleUI.mostrarFormato("  Activo: %s%n", usuarioExistente.isActivo() ? "Sí" : "No");
    
    ConsoleUI.mostrarInfo("\nIngrese los nuevos datos (presione Enter para mantener el valor actual):");
    
    ConsoleUI.mostrarFormato("Nombre [%s]: ", usuarioExistente.getNombre());
    String nombre = ConsoleUI.leerStringSilencioso().trim();
    if (nombre.isEmpty()) {
      nombre = usuarioExistente.getNombre();
    }
    
    ConsoleUI.mostrarFormato("Apellido [%s]: ", usuarioExistente.getApellido());
    String apellido = ConsoleUI.leerStringSilencioso().trim();
    if (apellido.isEmpty()) {
      apellido = usuarioExistente.getApellido();
    }
    
    ConsoleUI.mostrarInfo("Password (nueva, mínimo 6 caracteres): ");
    String password = ConsoleUI.leerStringSilencioso().trim();
    if (password.isEmpty()) {
      password = usuarioExistente.getPassword();
    }
    
    ConsoleUI.mostrarFormato("Rol (ADMIN/AUXILIAR) [%s]: ", usuarioExistente.getRol());
    String rolStr = ConsoleUI.leerStringSilencioso().trim().toUpperCase();
    Rol rol;
    if (rolStr.isEmpty()) {
      rol = usuarioExistente.getRol();
    } else {
      rol = Rol.valueOf(rolStr);
    }
    
    // Preguntar por el estado activo
    String estadoActual = usuarioExistente.isActivo() ? "Activo" : "Inactivo";
    ConsoleUI.mostrarFormato("Estado (ACTIVO/INACTIVO) [%s]: ", estadoActual);
    String estadoStr = ConsoleUI.leerStringSilencioso().trim().toUpperCase();
    boolean activo;
    if (estadoStr.isEmpty()) {
      activo = usuarioExistente.isActivo();
    } else {
      activo = estadoStr.equals("ACTIVO") || estadoStr.equals("A");
    }
    
    // Crear usuario actualizado
    Usuario usuarioActualizado = new Usuario(legajo, password, nombre, apellido, rol);
    
    // Aplicar el estado activo/inactivo
    if (activo) {
      usuarioActualizado.activar();
    } else {
      usuarioActualizado.desactivar();
    }
    
    userService.editar(usuarioActualizado);
    ConsoleUI.mostrarExito("Usuario modificado exitosamente.");
  }

  /**
   * Da de baja lógica a un usuario (solo ADMIN)
   */
  public void bajaUsuario(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      ConsoleUI.mostrarError("Acceso denegado: Solo usuarios ADMIN pueden dar de baja usuarios.");
      return;
    }
    
    ConsoleUI.mostrarInfo("\n-- Baja de Usuario --");
    
    int legajo = ConsoleUI.leerEntero("Legajo del usuario a dar de baja: ");
    
    // Validar que no se esté dando de baja a sí mismo
    if (actor.getLegajo() == legajo) {
      ConsoleUI.mostrarError("No puedes darte de baja a ti mismo. Otro administrador debe realizar esta operación.");
      return;
    }
    
    // Verificar que el usuario existe
    Usuario usuario;
    try {
      usuario = userService.obtenerPorLegajo(legajo);
    } catch (EntidadNoEncontradaException e) {
      ConsoleUI.mostrarError(e.getMessage());
      return;
    }
    
    // Verificar que esté activo
    if (!usuario.isActivo()) {
      ConsoleUI.mostrarError("El usuario ya está inactivo.");
      return;
    }
    
    ConsoleUI.mostrarInfo("\nUsuario a dar de baja:");
    ConsoleUI.mostrarFormato("  Legajo: %d%n", usuario.getLegajo());
    ConsoleUI.mostrarFormato("  Nombre: %s %s%n", usuario.getNombre(), usuario.getApellido());
    ConsoleUI.mostrarFormato("  Rol: %s%n", usuario.getRol());
    
    ConsoleUI.mostrarInfo("\n¿Está seguro de dar de baja a este usuario? (s/n): ");
    String confirmacion = ConsoleUI.leerStringSilencioso().trim().toLowerCase();
    
    if (!confirmacion.equals("s") && !confirmacion.equals("si")) {
      ConsoleUI.mostrarInfo("Operación cancelada.");
      return;
    }
    
    userService.bajaLogica(legajo);
    ConsoleUI.mostrarExito("Usuario dado de baja exitosamente.");
  }
}

