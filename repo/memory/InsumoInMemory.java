package repo.memory;

import java.time.LocalDate;
import java.util.*;
import domain.Insumo;
import domain.enums.EstadoInsumo;
import repo.InsumoRepository;

public class InsumoInMemory implements InsumoRepository {
  private final Map<String, Insumo> data = new HashMap<>();

  public InsumoInMemory() {
    // Datos semilla (código, nombre, unidad, stock, stockMinimo)
    data.put("GAS-01", new Insumo("GAS-01", "Gasas estériles", "paquete", 50, 10, EstadoInsumo.ACTIVO, null));
    data.put("GUA-01", new Insumo("GUA-01", "Guantes descartables", "caja", 40, 15, EstadoInsumo.ACTIVO, null));
    data.put("BAR-01", new Insumo("BAR-01", "Barbijos quirúrgicos", "caja", 25, 20, EstadoInsumo.ACTIVO, null));
    // Ejemplo con fecha de vencimiento (opcional)
    data.get("GAS-01").setFechaVencimiento(LocalDate.now().plusMonths(12));
  }

  @Override
  public Optional<Insumo> findByCodigo(String codigo) {
    return Optional.ofNullable(data.get(codigo));
  }

  @Override
  public List<Insumo> searchByNombre(String nombreParcial) {
    String q = nombreParcial.toLowerCase();
    List<Insumo> out = new ArrayList<>();
    for (Insumo i : data.values()) {
      if (i.getNombre().toLowerCase().contains(q))
        out.add(i);
    }
    // ordeno por nombre para tener determinismo
    out.sort(Comparator.comparing(Insumo::getNombre));
    return out;
  }

  @Override
  public List<Insumo> findCriticos() {
    List<Insumo> out = new ArrayList<>();
    for (Insumo i : data.values())
      if (i.esCritico())
        out.add(i);
    out.sort(Comparator.comparing(i -> i.getStock() - i.getStockMinimo())); // más crítico primero
    return out;
  }

  @Override
  public List<Insumo> findAll() {
    ArrayList<Insumo> out = new ArrayList<>(data.values());
    out.sort(Comparator.comparing(Insumo::getCodigo));
    return out;
  }

  @Override
  public void save(Insumo i) {
    if (data.containsKey(i.getCodigo()))
      throw new IllegalArgumentException("Código de insumo duplicado: " + i.getCodigo());
    data.put(i.getCodigo(), i);
  }

  @Override
  public void update(Insumo i) {
    data.put(i.getCodigo(), i);
  }
}