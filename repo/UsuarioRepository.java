package repo;

import java.util.*;
import domain.Usuario;

public interface UsuarioRepository {
  Optional<Usuario> findByLegajo(int legajo);

  Optional<Usuario> findByLegajoAndPassword(int legajo, String password);

  List<Usuario> findAllActivos();

  void save(Usuario u);

  void update(Usuario u);

  void deleteLogico(int legajo);
}