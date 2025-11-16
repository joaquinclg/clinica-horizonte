package app.ui;

/**
 * Constantes para el menú principal y prompts de la aplicación
 */
public class MenuPrincipal {
  public static final String MENU = "\n== Menú Principal ==\n" +
      "1) Listar usuarios (ADMIN)\n" +
      "2) Alta usuario (ADMIN)\n" +
      "3) Modificar usuario (ADMIN)\n" +
      "4) Baja usuario (ADMIN)\n" +
      "5) Ingreso de insumo\n" +
      "6) Egreso de insumo\n" +
      "7) Listar todos los insumos\n" +
      "8) Listar insumos críticos\n" +
      "9) Reporte de movimientos\n" +
      "10) Logout\n" +
      "0) Salir";

  // Prompts para entrada de datos
  public static final String PROMPT_LEGAJO = "Legajo: ";
  public static final String PROMPT_PASSWORD = "Password: ";
  public static final String PROMPT_NOMBRE = "Nombre: ";
  public static final String PROMPT_APELLIDO = "Apellido: ";
  public static final String PROMPT_ROL = "Rol (ADMIN/AUXILIAR): ";
  public static final String PROMPT_CODIGO = "Código del insumo: ";
  public static final String PROMPT_CANTIDAD = "Cantidad: ";
  public static final String PROMPT_OPCION = "Opción: ";
  public static final String PROMPT_DIAS = "Días hacia atrás (ej. 30): ";
}