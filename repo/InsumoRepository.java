package repo;

import java.util.*;
import domain.Insumo;

public interface InsumoRepository {
  Optional<Insumo> findByCodigo(String codigo);

  List<Insumo> searchByNombre(String nombreParcial);

  List<Insumo> findCriticos(); // stock <= stockMinimo

  List<Insumo> findAll();

  void save(Insumo i);

  void update(Insumo i);
}