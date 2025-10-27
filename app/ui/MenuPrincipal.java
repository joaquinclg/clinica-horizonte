package app.ui;

public class MenuPrincipal {
  public static final String MENU = "\n== Menú ==\n" +
      "1) Listar usuarios (ADMIN)\n" +
      "2) Alta usuario (ADMIN)\n" +
      "3) Ingreso de insumo\n" +
      "4) Egreso de insumo\n" +
      "5) Listar insumos críticos\n" +
      "6) Reporte movimientos (por período y servicio)\n" +
      "7) Logout\n" +
      "0) Salir";

  public static final String[] SERVICIOS = {
      "Todos", "Guardia", "Internación", "Quirófano", "Consultorios"
  };

  public static final String PROMPT_LEGAJO = "Legajo: ";
  public static final String PROMPT_PASSWORD = "Password: ";
  public static final String PROMPT_NOMBRE = "Nombre: ";
  public static final String PROMPT_APELLIDO = "Apellido: ";
  public static final String PROMPT_ROL = "Rol (ADMIN/AUXILIAR): ";
  public static final String PROMPT_CODIGO = "Código insumo: ";
  public static final String PROMPT_CANTIDAD = "Cantidad: ";
  public static final String PROMPT_SERVICIO = "Servicio ID (1 Guardia / 2 Internación / 3 Quirófano / 4 Consultorios): ";
  public static final String PROMPT_OPCION = "Opción: ";
  public static final String PROMPT_DIAS = "Ingrese cantidad de días hacia atrás (ej. 30): ";
}