package usecase;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import domain.Usuario;
import domain.Insumo;
import domain.Movimiento;
import domain.Servicio;
import domain.enums.TipoMovimiento;
import exceptions.EntidadNoEncontradaException;
import exceptions.StockInsuficienteException;
import repo.InsumoRepository;
import repo.MovimientoRepository;
import repo.ServicioRepository;

public class StockService {
  private final InsumoRepository insumos;
  private final MovimientoRepository movimientos;
  private final ServicioRepository servicios;

  public StockService(InsumoRepository i, MovimientoRepository m, ServicioRepository s) {
    this.insumos = i;
    this.movimientos = m;
    this.servicios = s;
  }

  /**
   * Registra el ingreso de un insumo al stock
   * 
   * @throws IllegalArgumentException     si los parámetros son inválidos
   * @throws EntidadNoEncontradaException si no existe el insumo
   */
  public void registrarIngreso(String codigo, int cant, Usuario actor) {
    // Validaciones
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new IllegalArgumentException("El código no puede estar vacío");
    }
    if (cant <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser positiva");
    }
    if (actor == null) {
      throw new IllegalArgumentException("El usuario es requerido");
    }

    // Lógica de negocio
    Insumo ins = insumos.findByCodigo(codigo)
        .orElseThrow(() -> new EntidadNoEncontradaException("Insumo no encontrado: " + codigo));

    ins.aumentar(cant);

    try {
      // Actualizar stock
      insumos.update(ins);

      // Registrar movimiento
      Movimiento mov = new Movimiento(0, TipoMovimiento.INGRESO, LocalDateTime.now(), cant, actor, ins, null);
      movimientos.save(mov);
    } catch (Exception e) {
      // En el futuro, con JDBC, aquí manejaríamos el rollback de la transacción
      throw new RuntimeException("Error al registrar el ingreso", e);
    }
  }

  /**
   * Registra el egreso de un insumo del stock
   * 
   * @throws IllegalArgumentException     si los parámetros son inválidos
   * @throws EntidadNoEncontradaException si no existe el insumo o servicio
   * @throws StockInsuficienteException   si no hay suficiente stock
   */
  public void registrarEgreso(String codigo, int cant, int servicioId, Usuario actor) {
    // Validaciones
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new IllegalArgumentException("El código no puede estar vacío");
    }
    if (cant <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser positiva");
    }
    if (actor == null) {
      throw new IllegalArgumentException("El usuario es requerido");
    }

    // Lógica de negocio
    Insumo ins = insumos.findByCodigo(codigo)
        .orElseThrow(() -> new EntidadNoEncontradaException("Insumo no encontrado: " + codigo));
    Servicio srv = servicios.findById(servicioId)
        .orElseThrow(() -> new EntidadNoEncontradaException("Servicio no encontrado: " + servicioId));

    if (ins.getStock() < cant) {
      throw new StockInsuficienteException("Stock insuficiente. Disponible: " + ins.getStock());
    }

    try {
      // Actualizar stock
      ins.disminuir(cant);
      insumos.update(ins);

      // Registrar movimiento
      Movimiento mov = new Movimiento(0, TipoMovimiento.EGRESO, LocalDateTime.now(), cant, actor, ins, srv);
      movimientos.save(mov);

      // Verificar si quedó en nivel crítico
      if (ins.esCritico()) {
        // Aquí podríamos agregar lógica para notificar stock crítico
        System.out.println("¡ALERTA! Stock crítico en " + ins.getNombre());
      }
    } catch (Exception e) {
      // En el futuro, con JDBC, aquí manejaríamos el rollback de la transacción
      throw new RuntimeException("Error al registrar el egreso", e);
    }
  }

  /**
   * Obtiene la lista de insumos con stock crítico
   * 
   * @return Lista de insumos con stock menor o igual al mínimo
   */
  public List<Insumo> obtenerInsumosCriticos() {
    return insumos.findAll().stream()
        .filter(Insumo::esCritico)
        .collect(Collectors.toList());
  }

  /**
   * Obtiene la lista de insumos próximos a vencer
   * 
   * @param diasAlerta Días de anticipación para la alerta
   * @return Lista de insumos que vencen en los próximos días
   */
  public List<Insumo> obtenerInsumosProximosAVencer(int diasAlerta) {
    LocalDate fechaLimite = LocalDate.now().plusDays(diasAlerta);
    return insumos.findAll().stream()
        .filter(i -> i.getFechaVencimiento() != null
            && !i.estaVencido()
            && i.getFechaVencimiento().isBefore(fechaLimite))
        .collect(Collectors.toList());
  }

  /**
   * Obtiene la lista de todos los insumos registrados
   * 
   * @return Lista de todos los insumos
   */
  public List<Insumo> obtenerTodosLosInsumos() {
    return insumos.findAll();
  }

  /**
   * Verifica si hay stock suficiente de un insumo
   * 
   * @return true si hay stock suficiente, false en caso contrario
   */
  public boolean hayStockSuficiente(String codigo, int cantidad) {
    return insumos.findByCodigo(codigo)
        .map(i -> i.getStock() >= cantidad)
        .orElse(false);
  }
}