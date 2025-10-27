package usecase;

import java.util.List;

import domain.Usuario;
import exceptions.EntidadNoEncontradaException;
import repo.UsuarioRepository;

public class GestionUsuariosService {
  private final UsuarioRepository usuarios;
  private static final int MIN_PASSWORD_LENGTH = 6;

  public GestionUsuariosService(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  /**
   * Da de alta un nuevo usuario
   * 
   * @throws IllegalArgumentException si los datos son inválidos
   */
  public void alta(Usuario u) {
    validarDatosUsuario(u);

    // Verificar que el legajo no exista
    if (usuarios.findByLegajo(u.getLegajo()).isPresent()) {
      throw new IllegalArgumentException("Ya existe un usuario con ese legajo");
    }

    usuarios.save(u);
  }

  /**
   * Edita un usuario existente
   * 
   * @throws IllegalArgumentException     si los datos son inválidos
   * @throws EntidadNoEncontradaException si el usuario no existe
   */
  public void editar(Usuario u) {
    validarDatosUsuario(u);

    // Verificar que el usuario exista
    usuarios.findByLegajo(u.getLegajo())
        .orElseThrow(() -> new EntidadNoEncontradaException("Usuario no encontrado"));

    usuarios.update(u);
  }

  /**
   * Da de baja lógica un usuario
   * 
   * @throws IllegalArgumentException si el legajo es inválido
   */
  public void bajaLogica(int legajo) {
    if (legajo <= 0) {
      throw new IllegalArgumentException("Legajo inválido");
    }
    usuarios.deleteLogico(legajo);
  }

  /**
   * Lista todos los usuarios activos
   */
  public List<Usuario> listarActivos() {
    return usuarios.findAllActivos();
  }

  /**
   * Valida los datos de un usuario
   * 
   * @throws IllegalArgumentException si los datos son inválidos
   */
  private void validarDatosUsuario(Usuario u) {
    if (u == null) {
      throw new IllegalArgumentException("El usuario no puede ser null");
    }
    if (u.getLegajo() <= 0) {
      throw new IllegalArgumentException("Legajo inválido");
    }
    if (u.getNombre() == null || u.getNombre().trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre es requerido");
    }
    if (u.getApellido() == null || u.getApellido().trim().isEmpty()) {
      throw new IllegalArgumentException("El apellido es requerido");
    }
    if (u.getPassword() == null || u.getPassword().length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException("La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
    }
    if (u.getRol() == null) {
      throw new IllegalArgumentException("El rol es requerido");
    }
  }
}