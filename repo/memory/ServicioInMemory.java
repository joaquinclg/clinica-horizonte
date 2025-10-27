package repo.memory;

import java.util.*;
import domain.Servicio;
import repo.ServicioRepository;

public class ServicioInMemory implements ServicioRepository {
  private final Map<Integer, Servicio> data = new HashMap<>();

  public ServicioInMemory() {
    // Datos semilla
    data.put(1, new Servicio(1, "Guardia"));
    data.put(2, new Servicio(2, "Internación"));
    data.put(3, new Servicio(3, "Quirófano"));
    data.put(4, new Servicio(4, "Consultorios"));
  }

  @Override
  public Optional<Servicio> findById(int id) {
    return Optional.ofNullable(data.get(id));
  }

  @Override
  public Optional<Servicio> findByNombre(String nombre) {
    return data.values().stream()
        .filter(s -> s.getNombre().equalsIgnoreCase(nombre))
        .findFirst();
  }

  @Override
  public List<Servicio> findAll() {
    ArrayList<Servicio> out = new ArrayList<>(data.values());
    out.sort(Comparator.comparingInt(Servicio::getId));
    return out;
  }

  @Override
  public void save(Servicio s) {
    if (data.containsKey(s.getId()))
      throw new IllegalArgumentException("ID de servicio duplicado: " + s.getId());
    data.put(s.getId(), s);
  }
}