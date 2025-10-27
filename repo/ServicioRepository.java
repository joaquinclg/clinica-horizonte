package repo;

import java.util.*;
import domain.Servicio;

public interface ServicioRepository {
  Optional<Servicio> findById(int id);

  Optional<Servicio> findByNombre(String nombre);

  List<Servicio> findAll();

  void save(Servicio s);
}