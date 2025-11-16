package app.handlers;

import java.util.ArrayList;
import java.util.Comparator;

import app.ui.ConsoleUI;
import app.ui.MenuPrincipal;
import domain.Insumo;
import domain.Usuario;
import usecase.StockService;

/**
 * Handler para operaciones relacionadas con stock e insumos
 */
public class StockHandler {
  private final StockService stockService;

  public StockHandler(StockService stockService) {
    this.stockService = stockService;
  }

  /**
   * Registra un ingreso de insumo
   */
  public void ingresoInsumo(Usuario actor) {
    ConsoleUI.mostrarInfo("\n-- Ingreso de Insumo --");
    
    String codigo = ConsoleUI.leerString(MenuPrincipal.PROMPT_CODIGO).toUpperCase();
    int cantidad = ConsoleUI.leerEntero("Cantidad a ingresar: ");
    
    stockService.registrarIngreso(codigo, cantidad, actor);
    ConsoleUI.mostrarExito("Ingreso registrado exitosamente.");
  }

  /**
   * Registra un egreso de insumo
   */
  public void egresoInsumo(Usuario actor) {
    ConsoleUI.mostrarInfo("\n-- Egreso de Insumo --");
    
    String codigo = ConsoleUI.leerString(MenuPrincipal.PROMPT_CODIGO).toUpperCase();
    int cantidad = ConsoleUI.leerEntero("Cantidad a retirar: ");
    
    // Obtener servicios reales de la base de datos
    var serviciosList = new ArrayList<>(stockService.obtenerTodosLosServicios());
    
    if (serviciosList.isEmpty()) {
      ConsoleUI.mostrarError("No hay servicios disponibles en la base de datos.");
      return;
    }
    
    // Mostrar servicios numerados secuencialmente (1, 2, 3...)
    ConsoleUI.mostrarInfo("\nServicios disponibles:");
    for (int i = 0; i < serviciosList.size(); i++) {
      ConsoleUI.mostrarFormato("  %d - %s%n", i + 1, serviciosList.get(i).getNombre());
    }
    
    int opcionVisual = ConsoleUI.leerEntero("Servicio (número): ");
    
    // Validar que la opción esté en rango
    if (opcionVisual < 1 || opcionVisual > serviciosList.size()) {
      ConsoleUI.mostrarError("Opción inválida. Debe estar entre 1 y " + serviciosList.size());
      return;
    }
    
    // Obtener el ID real del servicio seleccionado
    int servicioIdReal = serviciosList.get(opcionVisual - 1).getId();
    
    stockService.registrarEgreso(codigo, cantidad, servicioIdReal, actor);
    ConsoleUI.mostrarExito("Egreso registrado exitosamente.");
  }

  /**
   * Lista todos los insumos disponibles
   */
  public void listarInsumos() {
    ConsoleUI.mostrarInfo("\n-- Listado de Todos los Insumos --");
    
    var todosInsumos = new ArrayList<>(stockService.obtenerTodosLosInsumos());
    
    if (todosInsumos.isEmpty()) {
      ConsoleUI.mostrarInfo("No hay insumos registrados en el sistema.");
    } else {
      todosInsumos.sort(Comparator.comparing(Insumo::getCodigo)); // Algoritmo de ordenación
      
      ConsoleUI.mostrarInfo("─────────────────────────────────────────────────────────────────");
      ConsoleUI.mostrarFormato("%-10s %-30s %-10s %-10s %-10s%n", 
          "Código", "Nombre", "Unidad", "Stock", "Mínimo");
      ConsoleUI.mostrarInfo("─────────────────────────────────────────────────────────────────");
      
      todosInsumos.forEach(i -> {
        String estado = i.esCritico() ? " ⚠️ CRÍTICO" : "";
        ConsoleUI.mostrarFormato("%-10s %-30s %-10s %-10d %-10d%s%n",
            i.getCodigo(), 
            i.getNombre(), 
            i.getUnidad(), 
            i.getStock(), 
            i.getStockMinimo(),
            estado);
      });
      
      ConsoleUI.mostrarInfo("─────────────────────────────────────────────────────────────────");
      
      long criticos = todosInsumos.stream().filter(Insumo::esCritico).count();
      ConsoleUI.mostrarFormato("Total de insumos: %d | Críticos: %d%n", 
          todosInsumos.size(), criticos);
    }
  }

  /**
   * Lista los insumos con stock crítico
   */
  public void listarCriticos() {
    ConsoleUI.mostrarInfo("\n-- Insumos Críticos (stock <= mínimo) --");
    
    var lista = new ArrayList<>(stockService.obtenerInsumosCriticos());
    lista.sort(Comparator.comparing(Insumo::getNombre)); // Algoritmo de ordenación
    
    if (lista.isEmpty()) {
      ConsoleUI.mostrarInfo("No hay insumos críticos en este momento.");
    } else {
      lista.forEach(i -> ConsoleUI.mostrarFormato("%s - %s [stock=%d, mínimo=%d]%n",
          i.getCodigo(), i.getNombre(), i.getStock(), i.getStockMinimo()));
    }
  }
}

