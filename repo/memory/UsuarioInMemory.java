package repo.memory;

import java.util.*;
import domain.enums.Rol;
import domain.Usuario;
import repo.UsuarioRepository;

public class UsuarioInMemory implements UsuarioRepository {
  private final Map<Integer, Usuario> data = new HashMap<>();

  public UsuarioInMemory() {
    data.put(1000, new Usuario(1000, "admin123", "Ana", "García", Rol.ADMIN));
    data.put(2000, new Usuario(2000, "aux123", "Luis", "Pérez", Rol.AUXILIAR));
  }

  public Optional<Usuario> findByLegajo(int legajo) {
    return Optional.ofNullable(data.get(legajo));
  }

  public Optional<Usuario> findByLegajoAndPassword(int legajo, String p) {
    Usuario u = data.get(legajo);
    return (u != null && u.getPassword().equals(p) && u.isActivo())
        ? Optional.of(u)
        : Optional.empty();
  }

  public List<Usuario> findAllActivos() {
    List<Usuario> out = new ArrayList<>();
    for (Usuario u : data.values())
      if (u.isActivo())
        out.add(u);
    return out;
  }

  public void save(Usuario u) {
    if (data.containsKey(u.getLegajo()))
      throw new IllegalArgumentException("Legajo duplicado");
    data.put(u.getLegajo(), u);
  }

  public void update(Usuario u) {
    if (!data.containsKey(u.getLegajo()))
      throw new IllegalArgumentException("Usuario no encontrado");
    data.put(u.getLegajo(), u);
  }

  public void deleteLogico(int legajo) {
    Usuario u = data.get(legajo);
    if (u != null)
      u.desactivar();
  }
}