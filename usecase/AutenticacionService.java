package usecase;

import java.util.Map;
import java.util.HashMap;

import domain.Usuario;
import repo.UsuarioRepository;
import exceptions.CredencialesInvalidasException;

public class AutenticacionService {
  private final UsuarioRepository usuarios;
  private static final int MAX_INTENTOS_LOGIN = 3;
  private final Map<Integer, Integer> intentosFallidos = new HashMap<>();

  public AutenticacionService(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  /**
   * Realiza el login del usuario
   * 
   * @throws CredencialesInvalidasException si las credenciales son inválidas o la
   *                                        cuenta está bloqueada
   * @throws IllegalArgumentException       si los parámetros son inválidos
   */
  public Usuario login(int legajo, String pass) {
    if (legajo <= 0) {
      throw new IllegalArgumentException("Legajo inválido");
    }
    if (pass == null || pass.trim().isEmpty()) {
      throw new IllegalArgumentException("Contraseña inválida");
    }

    // Verificar si la cuenta está bloqueada
    if (intentosFallidos.getOrDefault(legajo, 0) >= MAX_INTENTOS_LOGIN) {
      throw new CredencialesInvalidasException("Cuenta bloqueada por múltiples intentos fallidos");
    }

    try {
      Usuario usuario = usuarios.findByLegajoAndPassword(legajo, pass)
          .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

      // Login exitoso, resetear intentos fallidos
      intentosFallidos.remove(legajo);
      return usuario;
    } catch (CredencialesInvalidasException e) {
      // Incrementar contador de intentos fallidos
      int intentos = intentosFallidos.getOrDefault(legajo, 0) + 1;
      intentosFallidos.put(legajo, intentos);

      if (intentos >= MAX_INTENTOS_LOGIN) {
        throw new CredencialesInvalidasException("Cuenta bloqueada por múltiples intentos fallidos");
      }

      throw e;
    }
  }

  /**
   * Desbloquea una cuenta de usuario
   * 
   * @throws IllegalArgumentException si el legajo es inválido
   */
  public void desbloquearCuenta(int legajo) {
    if (legajo <= 0) {
      throw new IllegalArgumentException("Legajo inválido");
    }
    intentosFallidos.remove(legajo);
  }

  /**
   * Verifica si una cuenta está bloqueada
   */
  public boolean estaBloqueada(int legajo) {
    return intentosFallidos.getOrDefault(legajo, 0) >= MAX_INTENTOS_LOGIN;
  }
}