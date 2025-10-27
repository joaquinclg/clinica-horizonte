package domain;

import domain.enums.Rol;
import java.time.LocalDateTime;

public class Usuario extends Persona {
  private int legajo;
  private String password; // por ahora texto plano
  private Rol rol;
  private boolean activo = true;
  private LocalDateTime creadoEn; // para coincidir con la base de datos en la proxima entrega, ya que en el diagrama de clases no lo tenia pero si lo agregue en el diagrama de entidad-relacion

  public Usuario(int legajo, String password, String nombre, String apellido, Rol rol) {
    super(nombre, apellido);
    this.legajo = legajo;
    this.password = password;
    this.rol = rol;
    this.creadoEn = LocalDateTime.now();
  }

  public int getLegajo() {
    return legajo;
  }

  public String getPassword() {
    return password;
  }

  public Rol getRol() {
    return rol;
  }

  public boolean isActivo() {
    return activo;
  }

  public void desactivar() {
    this.activo = false;
  }

  public void activar() {
    this.activo = true;
  }

  public void setPassword(String password) {
    if (password == null || password.length() < 6) {
      throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
    }
    this.password = password;
  }

  public void setRol(Rol rol) {
    if (rol == null) {
      throw new IllegalArgumentException("El rol no puede ser nulo");
    }
    this.rol = rol;
  }

  public LocalDateTime getCreadoEn() {
    return creadoEn;
  }

  @Override
  public String toString() {
    return String.format("Usuario[legajo=%d, nombre=%s %s, rol=%s, activo=%s, creadoEn=%s]",
        legajo, getNombre(), getApellido(), rol, activo, creadoEn);
  }
}