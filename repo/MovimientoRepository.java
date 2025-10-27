package repo;

import java.util.List;
import java.time.LocalDate;
import domain.Movimiento;

public interface MovimientoRepository {
  void save(Movimiento m);

  List<Movimiento> findAll();

  List<Movimiento> findByPeriodoYServicio(LocalDate desde, LocalDate hasta, Integer servicioId);
}