package app.ui;

import java.util.Scanner;

/**
 * Clase utilitaria para manejo de interfaz de consola
 */
public class ConsoleUI {
  private static Scanner scanner = new Scanner(System.in);

  /**
   * Lee un entero de la consola con reintentos
   */
  public static int leerEntero(String prompt) {
    while (true) {
      try {
        System.out.print(prompt);
        return Integer.parseInt(scanner.nextLine());
      } catch (NumberFormatException nfe) {
        System.out.println("Debe ingresar un número. Intente nuevamente.");
      }
    }
  }

  /**
   * Lee una cadena de la consola
   */
  public static String leerString(String prompt) {
    System.out.print(prompt);
    return scanner.nextLine();
  }

  /**
   * Muestra un mensaje de error
   */
  public static void mostrarError(String mensaje) {
    System.out.println("Error: " + mensaje);
  }

  /**
   * Muestra un mensaje de éxito
   */
  public static void mostrarExito(String mensaje) {
    System.out.println("✓ " + mensaje);
  }

  /**
   * Muestra un mensaje informativo
   */
  public static void mostrarInfo(String mensaje) {
    System.out.println(mensaje);
  }

  /**
   * Cierra los recursos
   */
  public static void cerrar() {
    if (scanner != null) {
      scanner.close();
    }
  }
}