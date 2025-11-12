package domain;

import java.time.LocalDateTime;
import domain.enums.TipoMovimiento;

public class Movimiento {
  private int id;
  private TipoMovimiento tipo;
  private LocalDateTime fecha;
  private int cantidad;
  private Usuario usuario;
  private Insumo insumo;
  private Servicio servicio; // null si INGRESO

  public Movimiento(int id, TipoMovimiento tipo, LocalDateTime fecha, int cantidad,
      Usuario usuario, Insumo insumo, Servicio servicio) {
  
  if (tipo == null) {
    throw new IllegalArgumentException("El tipo de movimiento es requerido");
  }
  if (cantidad <= 0) {
    throw new IllegalArgumentException("La cantidad debe ser positiva");
  }
  if (usuario == null) {
    throw new IllegalArgumentException("El usuario es requerido");
  }
  if (insumo == null) {
    throw new IllegalArgumentException("El insumo es requerido");
  }
  
  // Regla de negocio: INGRESO sin servicio, EGRESO con servicio
  if (tipo == TipoMovimiento.INGRESO && servicio != null) {
    throw new IllegalArgumentException("Los movimientos de tipo INGRESO no pueden tener servicio");
  }
  if (tipo == TipoMovimiento.EGRESO && servicio == null) {
    throw new IllegalArgumentException("Los movimientos de tipo EGRESO deben tener un servicio");
  }
  
  this.id = id;
  this.tipo = tipo;
  this.fecha = fecha;
  this.cantidad = cantidad;
  this.usuario = usuario;
  this.insumo = insumo;
  this.servicio = servicio;
}

  public int getId() {
    return id;
  }

  public TipoMovimiento getTipo() {
    return tipo;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public int getCantidad() {
    return cantidad;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public Insumo getInsumo() {
    return insumo;
  }

  public Servicio getServicio() {
    return servicio;
  }
}