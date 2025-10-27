package domain;

public class Servicio {
  private int id;
  private String nombre;

  public Servicio(int id, String nombre) {
    this.id = id;
    this.nombre = nombre;
  }

  public int getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }
}