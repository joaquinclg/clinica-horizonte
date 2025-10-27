package usecase;

import java.time.LocalDate;
import java.util.List;
import domain.Movimiento;
import repo.MovimientoRepository;

public class ReportesService {
  private final MovimientoRepository movimientos;

  public ReportesService(MovimientoRepository movimientos) {
    this.movimientos = movimientos;
  }

  /**
   * Obtiene los movimientos en un período y opcionalmente filtrados por servicio
   * 
   * @param desde      Fecha inicio del período
   * @param hasta      Fecha fin del período
   * @param servicioId ID del servicio (opcional)
   * @throws IllegalArgumentException si las fechas son inválidas
   */
  public List<Movimiento> movimientosPorPeriodoYServicio(LocalDate desde, LocalDate hasta, Integer servicioId) {
    if (desde == null || hasta == null) {
      throw new IllegalArgumentException("Las fechas son requeridas");
    }
    if (hasta.isBefore(desde)) {
      throw new IllegalArgumentException("La fecha hasta debe ser posterior a la fecha desde");
    }
    if (desde.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("La fecha desde no puede ser futura");
    }

    return movimientos.findByPeriodoYServicio(desde, hasta, servicioId);
  }

  /**
   * Obtiene los movimientos del último mes para un servicio específico
   * 
   * @throws IllegalArgumentException si el servicioId es inválido
   */
  public List<Movimiento> movimientosUltimoMes(int servicioId) {
    if (servicioId <= 0) {
      throw new IllegalArgumentException("ID de servicio inválido");
    }

    LocalDate hasta = LocalDate.now();
    LocalDate desde = hasta.minusMonths(1);
    return movimientosPorPeriodoYServicio(desde, hasta, servicioId);
  }

  /**
   * Obtiene los movimientos del día actual
   */
  public List<Movimiento> movimientosDelDia() {
    LocalDate hoy = LocalDate.now();
    return movimientosPorPeriodoYServicio(hoy, hoy, null);
  }

  /**
   * Obtiene los movimientos de la última semana
   */
  public List<Movimiento> movimientosUltimaSemana() {
    LocalDate hasta = LocalDate.now();
    LocalDate desde = hasta.minusWeeks(1);
    return movimientosPorPeriodoYServicio(desde, hasta, null);
  }
}