package app.handlers;

import java.time.LocalDate;
import java.util.ArrayList;

import app.ui.ConsoleUI;
import app.ui.MenuPrincipal;
import domain.Movimiento;
import usecase.ReportesService;
import usecase.StockService;

/**
 * Handler para operaciones relacionadas con reportes
 */
public class ReporteHandler {
  private final ReportesService reportService;
  private final StockService stockService;

  public ReporteHandler(ReportesService reportService, StockService stockService) {
    this.reportService = reportService;
    this.stockService = stockService;
  }

  /**
   * Genera y muestra el reporte de movimientos
   */
  public void reporteMovimientos() {
    ConsoleUI.mostrarInfo("\n-- Reporte de Movimientos --");
    
    int dias = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_DIAS);
    
    // Obtener servicios reales de la base de datos
    var serviciosList = new ArrayList<>(stockService.obtenerTodosLosServicios());
    
    ConsoleUI.mostrarInfo("\nFiltrar por servicio:");
    ConsoleUI.mostrarInfo("  0 - Todos");
    if (!serviciosList.isEmpty()) {
      // Mostrar servicios numerados secuencialmente (1, 2, 3...)
      for (int i = 0; i < serviciosList.size(); i++) {
        ConsoleUI.mostrarFormato("  %d - %s%n", i + 1, serviciosList.get(i).getNombre());
      }
    } else {
      ConsoleUI.mostrarInfo("  (No hay servicios disponibles)");
    }
    
    int opcionVisual = ConsoleUI.leerEntero("Servicio (número): ");
    
    LocalDate hoy = LocalDate.now();
    Integer srvFiltro = null;
    
    // Si no es "0 - Todos", convertir el número visual al ID real
    if (opcionVisual != 0) {
      if (opcionVisual < 1 || opcionVisual > serviciosList.size()) {
        ConsoleUI.mostrarError("Opción inválida. Debe estar entre 0 y " + serviciosList.size());
        return;
      }
      srvFiltro = serviciosList.get(opcionVisual - 1).getId();
    }
    
    var lista = reportService.movimientosPorPeriodoYServicio(
        hoy.minusDays(dias), hoy, srvFiltro);
    
    if (lista.isEmpty()) {
      ConsoleUI.mostrarInfo("Sin movimientos en el período/servicio indicado.");
    } else {
      ConsoleUI.mostrarFormato("\nMovimientos desde %s hasta %s%n", hoy.minusDays(dias), hoy);
      ConsoleUI.mostrarInfo("-------------------------------------------------------");
      // Numeración secuencial desde 1 (no el ID de la base de datos)
      int numeroSecuencial = 1;
      for (Movimiento m : lista) {
        // Formatear fecha para mostrar solo día, mes y año
        String fechaFormateada = m.getFecha().toLocalDate().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        ConsoleUI.mostrarFormato("#%d | Tipo: %s | Insumo: %s x%d | Servicio: %s | Legajo: %d | Fecha: %s%n",
            numeroSecuencial,
            m.getTipo(),
            m.getInsumo().getCodigo(),
            m.getCantidad(),
            (m.getServicio() == null ? "N/A" : m.getServicio().getNombre()),
            m.getUsuario().getLegajo(),
            fechaFormateada);
        numeroSecuencial++;
      }
    }
  }
}

