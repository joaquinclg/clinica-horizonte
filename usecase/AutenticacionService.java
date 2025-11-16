package usecase;

import domain.Usuario;
import repo.UsuarioRepository;
import exceptions.CredencialesInvalidasException;

public class AutenticacionService {
  private final UsuarioRepository usuarios;

  public AutenticacionService(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  /**
   * Realiza el login del usuario
   * 
   * @throws CredencialesInvalidasException si las credenciales son inválidas
   * @throws IllegalArgumentException       si los parámetros son inválidos
   */
  public Usuario login(int legajo, String pass) {
    if (legajo <= 0) {
      throw new IllegalArgumentException("Legajo inválido");
    }
    if (pass == null || pass.trim().isEmpty()) {
      throw new IllegalArgumentException("Contraseña inválida");
    }

    Usuario usuario = usuarios.findByLegajoAndPassword(legajo, pass)
        .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

    return usuario;
  }
}
