package app.handlers;

import app.ui.ConsoleUI;
import app.ui.MenuPrincipal;
import domain.Usuario;
import exceptions.CredencialesInvalidasException;
import usecase.AutenticacionService;

/**
 * Handler para operaciones de autenticación
 */
public class AuthHandler {
  private final AutenticacionService authService;

  public AuthHandler(AutenticacionService authService) {
    this.authService = authService;
  }

  /**
   * Realiza el proceso de login del usuario
   * @return Usuario autenticado o null si falló
   */
  public Usuario realizarLogin() {
    ConsoleUI.mostrarInfo("\n== Login ==");
    ConsoleUI.mostrarInfo("Ingrese sus credenciales (datos desde base de datos)");
    
    try {
      int legajo = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_LEGAJO);
      String password = ConsoleUI.leerString(MenuPrincipal.PROMPT_PASSWORD);
      
      Usuario usuario = authService.login(legajo, password);
      ConsoleUI.mostrarFormato("Bienvenido %s [%s]%n", usuario.getNombreCompleto(), usuario.getRol());
      return usuario;
    } catch (CredencialesInvalidasException e) {
      ConsoleUI.mostrarError(e.getMessage());
      return null;
    }
  }
}

