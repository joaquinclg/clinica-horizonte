package domain;

import java.time.LocalDate;
import domain.enums.EstadoInsumo;

public class Insumo {
  private String codigo, nombre, unidad;
  private int stock, stockMinimo;
  private EstadoInsumo estado = EstadoInsumo.ACTIVO;
  private LocalDate fechaVencimiento; // opcional

  public Insumo(String codigo, String nombre, String unidad, int stock, int stockMinimo, EstadoInsumo estado,
      LocalDate fechaVencimiento) {
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new IllegalArgumentException("El código no puede estar vacío");
    }
    if (nombre == null || nombre.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre no puede estar vacío");
    }
    if (unidad == null || unidad.trim().isEmpty()) {
      throw new IllegalArgumentException("La unidad no puede estar vacía");
    }
    if (stock < 0) {
      throw new IllegalArgumentException("El stock no puede ser negativo");
    }
    if (stockMinimo < 0) {
      throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
    }
    if (estado == null) {
      throw new IllegalArgumentException("El estado no puede ser nulo");
    }

    this.codigo = codigo.trim();
    this.nombre = nombre.trim();
    this.unidad = unidad.trim();
    this.stock = stock;
    this.stockMinimo = stockMinimo;
    this.estado = estado;
    this.fechaVencimiento = fechaVencimiento;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public String getUnidad() {
    return unidad;
  }

  public int getStock() {
    return stock;
  }

  public int getStockMinimo() {
    return stockMinimo;
  }

  public void aumentar(int cant) {
    if (cant <= 0) {
      throw new IllegalArgumentException("La cantidad a aumentar debe ser positiva");
    }
    stock += cant;
  }

  public void disminuir(int cant) {
    if (cant <= 0) {
      throw new IllegalArgumentException("La cantidad a disminuir debe ser positiva");
    }
    if (cant > stock) {
      throw new IllegalStateException("No hay suficiente stock para disminuir");
    }
    stock -= cant;
  }

  public boolean esCritico() {
    return stock <= stockMinimo;
  }

  public boolean estaVencido() {
    return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
  }

  public boolean tieneStock() {
    return stock > 0;
  }

  public EstadoInsumo getEstado() {
    return estado;
  }

  public void setEstado(EstadoInsumo estado) {
    this.estado = estado;
  }

  public LocalDate getFechaVencimiento() {
    return fechaVencimiento;
  }

  public void setFechaVencimiento(LocalDate fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }

}