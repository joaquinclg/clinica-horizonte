package repo.memory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import domain.Movimiento;
import repo.MovimientoRepository;

public class MovimientoInMemory implements MovimientoRepository {
  private final List<Movimiento> data = new ArrayList<>();
  private final AtomicInteger secuencia = new AtomicInteger(1);

  @Override
  public void save(Movimiento m) {
    if (m == null) {
      throw new IllegalArgumentException("El movimiento no puede ser null");
    }

    try {
      // Asignar ID si es necesario
      Field idField = Movimiento.class.getDeclaredField("id");
      idField.setAccessible(true);
      if ((int) idField.get(m) == 0) {
        idField.set(m, secuencia.getAndIncrement());
      }

      // Asignar fecha si es necesario
      Field fechaField = Movimiento.class.getDeclaredField("fecha");
      fechaField.setAccessible(true);
      if (fechaField.get(m) == null) {
        fechaField.set(m, LocalDateTime.now());
      }

      // Validar datos obligatorios
      if (m.getInsumo() == null) {
        throw new IllegalArgumentException("El insumo es requerido");
      }
      if (m.getCantidad() <= 0) {
        throw new IllegalArgumentException("La cantidad debe ser positiva");
      }
      if (m.getUsuario() == null) {
        throw new IllegalArgumentException("El usuario es requerido");
      }
      if (m.getTipo() == null) {
        throw new IllegalArgumentException("El tipo de movimiento es requerido");
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Error al procesar el movimiento", e);
    }

    data.add(m);
  }

  @Override
  public List<Movimiento> findAll() {
    ArrayList<Movimiento> out = new ArrayList<>(data);
    out.sort(Comparator.comparing(Movimiento::getFecha).reversed());
    return out;
  }

  @Override
  public List<Movimiento> findByPeriodoYServicio(LocalDate desde, LocalDate hasta, Integer servicioId) {
    List<Movimiento> out = new ArrayList<>();

    for (Movimiento m : data) {
      LocalDate fechaMovimiento = m.getFecha().toLocalDate();

      // Verificar rango de fechas
      boolean enRango = (desde == null || !fechaMovimiento.isBefore(desde)) &&
          (hasta == null || !fechaMovimiento.isAfter(hasta));

      // Verificar servicio si se especificó
      boolean coincideServicio = (servicioId == null) ||
          (m.getServicio() != null && m.getServicio().getId() == servicioId);

      if (enRango && coincideServicio) {
        out.add(m);
      }
    }

    // Ordenar por fecha descendente
    out.sort(Comparator.comparing(Movimiento::getFecha).reversed());
    return Collections.unmodifiableList(out); // Retornar lista inmutable
  }
}