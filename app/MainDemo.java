package app;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import domain.Insumo;
import domain.Usuario;
import domain.enums.Rol;
import exceptions.CredencialesInvalidasException;
import exceptions.EntidadNoEncontradaException;
import exceptions.StockInsuficienteException;
import repo.memory.InsumoInMemory;
import repo.memory.MovimientoInMemory;
import repo.memory.ServicioInMemory;
import repo.memory.UsuarioInMemory;
import usecase.AutenticacionService;
import usecase.GestionUsuariosService;
import usecase.ReportesService;
import usecase.StockService;

/**
 * Clase principal de la aplicación Clínica Horizonte - Sistema de Gestión de Stock
 * Implementa un menú de consola interactivo para gestionar insumos médicos
 */
public class MainDemo {
  // Servicios de la aplicación
  private static AutenticacionService authService;
  private static GestionUsuariosService userService;
  private static StockService stockService;
  private static ReportesService reportService;
  private static Scanner scanner;

  public static void main(String[] args) {
    try {
      inicializarServicios();
      ejecutarAplicacion();
    } catch (Exception e) {
      System.err.println("Error fatal: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (scanner != null) {
        scanner.close();
      }
    }
  }

  /**
   * Inicializa todos los servicios y repositorios de la aplicación
   */
  private static void inicializarServicios() {
    // Inicializar repositorios en memoria (en la 4ta entrega serán repositorios JDBC)
    UsuarioInMemory usuariosRepo = new UsuarioInMemory();
    ServicioInMemory serviciosRepo = new ServicioInMemory();
    InsumoInMemory insumosRepo = new InsumoInMemory();
    MovimientoInMemory movRepo = new MovimientoInMemory();

    // Inicializar servicios de la capa de casos de uso
    authService = new AutenticacionService(usuariosRepo);
    userService = new GestionUsuariosService(usuariosRepo);
    stockService = new StockService(insumosRepo, movRepo, serviciosRepo);
    reportService = new ReportesService(movRepo);

    // Inicializar scanner para entrada de usuario
    scanner = new Scanner(System.in);
  }

  /**
   * Método principal que ejecuta el loop de la aplicación
   */
  private static void ejecutarAplicacion() {
    Usuario usuarioLogueado = null;
    boolean salir = false;

    System.out.println("=== Clínica Horizonte - Gestión de Stock ===");

    // Loop principal de la aplicación
    while (!salir) {
      try {
        // Si no hay usuario logueado, mostrar login
        if (usuarioLogueado == null) {
          usuarioLogueado = realizarLogin();
          continue;
        }

        // Mostrar menú y procesar opción
        mostrarMenu();
        int opcion = leerEntero("Opción: ");

        // Switch con manejo de las diferentes opciones del menú
        switch (opcion) {
          case 1:
            manejarListarUsuarios(usuarioLogueado);
            break;
          case 2:
            manejarAltaUsuario(usuarioLogueado);
            break;
          case 3:
            manejarIngresoInsumo(usuarioLogueado);
            break;
          case 4:
            manejarEgresoInsumo(usuarioLogueado);
            break;
          case 5:
            manejarListarInsumos();
            break;
          case 6:
            manejarListarCriticos();
            break;
          case 7:
            manejarReporteMovimientos();
            break;
          case 8:
            usuarioLogueado = null;
            System.out.println("Sesión cerrada.");
            break;
          case 0:
            salir = true;
            System.out.println("Fin de la aplicación.");
            break;
          default:
            System.out.println("Opción inválida. Intente nuevamente.");
        }

      } catch (CredencialesInvalidasException | StockInsuficienteException | EntidadNoEncontradaException e) {
        // Manejo de excepciones de negocio
        System.out.println("Error: " + e.getMessage());
      } catch (IllegalArgumentException e) {
        // Manejo de errores de validación
        System.out.println("Entrada inválida: " + e.getMessage());
      } catch (Exception e) {
        // Manejo de errores inesperados
        System.out.println("Error inesperado: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  /**
   * Realiza el proceso de login del usuario
   * @return Usuario autenticado o null si falló
   */
  private static Usuario realizarLogin() {
    System.out.println("\n== Login ==");
    System.out.println("Usuarios de prueba:");
    System.out.println("  - 1000 / admin123 (ADMIN)");
    System.out.println("  - 2000 / aux123 (AUXILIAR)");
    
    try {
      int legajo = leerEntero("Legajo: ");
      System.out.print("Password: ");
      String password = scanner.nextLine();
      
      Usuario usuario = authService.login(legajo, password);
      System.out.printf("Bienvenido %s [%s]%n", usuario.getNombreCompleto(), usuario.getRol());
      return usuario;
    } catch (CredencialesInvalidasException e) {
      System.out.println("Error: " + e.getMessage());
      return null;
    }
  }

  /**
   * Muestra el menú principal de opciones
   */
  private static void mostrarMenu() {
    System.out.println("\n== Menú Principal ==");
    System.out.println("1) Listar usuarios (ADMIN)");
    System.out.println("2) Alta usuario (ADMIN)");
    System.out.println("3) Ingreso de insumo");
    System.out.println("4) Egreso de insumo");
    System.out.println("5) Listar todos los insumos");
    System.out.println("6) Listar insumos críticos");
    System.out.println("7) Reporte de movimientos");
    System.out.println("8) Logout");
    System.out.println("0) Salir");
  }

  /**
   * Maneja la opción de listar usuarios (solo ADMIN)
   */
  private static void manejarListarUsuarios(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      System.out.println("Acceso denegado: Solo usuarios ADMIN pueden ver este listado.");
      return;
    }
    
    System.out.println("\n-- Usuarios Activos --");
    var usuarios = userService.listarActivos();
    
    if (usuarios.isEmpty()) {
      System.out.println("No hay usuarios activos.");
    } else {
      usuarios.forEach(u -> System.out.printf("%d - %s %s (%s)%n", 
          u.getLegajo(), u.getNombre(), u.getApellido(), u.getRol()));
    }
  }

  /**
   * Maneja la opción de dar de alta un usuario (solo ADMIN)
   */
  private static void manejarAltaUsuario(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      System.out.println("Acceso denegado: Solo usuarios ADMIN pueden dar de alta usuarios.");
      return;
    }
    
    System.out.println("\n-- Alta de Usuario --");
    
    int legajo = leerEntero("Legajo: ");
    System.out.print("Nombre: ");
    String nombre = scanner.nextLine();
    System.out.print("Apellido: ");
    String apellido = scanner.nextLine();
    System.out.print("Password: ");
    String password = scanner.nextLine();
    System.out.print("Rol (ADMIN/AUXILIAR): ");
    String rolStr = scanner.nextLine().toUpperCase();
    
    Rol rol = Rol.valueOf(rolStr);
    Usuario nuevoUsuario = new Usuario(legajo, password, nombre, apellido, rol);
    userService.alta(nuevoUsuario);
    System.out.println("Usuario creado exitosamente.");
  }

  /**
   * Maneja la opción de registrar un ingreso de insumo
   */
  private static void manejarIngresoInsumo(Usuario actor) {
    System.out.println("\n-- Ingreso de Insumo --");
    System.out.println("Insumos disponibles: GAS-01, GUA-01, BAR-01");
    
    System.out.print("Código del insumo: ");
    String codigo = scanner.nextLine().toUpperCase();
    int cantidad = leerEntero("Cantidad a ingresar: ");
    
    stockService.registrarIngreso(codigo, cantidad, actor);
    System.out.println("Ingreso registrado exitosamente.");
  }

  /**
   * Maneja la opción de registrar un egreso de insumo
   */
  private static void manejarEgresoInsumo(Usuario actor) {
    System.out.println("\n-- Egreso de Insumo --");
    System.out.println("Insumos disponibles: GAS-01, GUA-01, BAR-01");
    
    System.out.print("Código del insumo: ");
    String codigo = scanner.nextLine().toUpperCase();
    int cantidad = leerEntero("Cantidad a retirar: ");
    
    System.out.println("\nServicios disponibles:");
    System.out.println("  1 - Guardia");
    System.out.println("  2 - Internación");
    System.out.println("  3 - Quirófano");
    System.out.println("  4 - Consultorios");
    int servicioId = leerEntero("Servicio ID: ");
    
    stockService.registrarEgreso(codigo, cantidad, servicioId, actor);
    System.out.println("Egreso registrado exitosamente.");
  }

  /**
   * Maneja la opción de listar todos los insumos disponibles
   */
  private static void manejarListarInsumos() {
    System.out.println("\n-- Listado de Todos los Insumos --");
    
    var todosInsumos = new ArrayList<>(stockService.obtenerTodosLosInsumos());
    
    if (todosInsumos.isEmpty()) {
      System.out.println("No hay insumos registrados en el sistema.");
    } else {
      todosInsumos.sort(Comparator.comparing(Insumo::getCodigo)); // Algoritmo de ordenación
      
      System.out.println("─────────────────────────────────────────────────────────────────");
      System.out.printf("%-10s %-30s %-10s %-10s %-10s%n", 
          "Código", "Nombre", "Unidad", "Stock", "Mínimo");
      System.out.println("─────────────────────────────────────────────────────────────────");
      
      todosInsumos.forEach(i -> {
        String estado = i.esCritico() ? " ⚠️ CRÍTICO" : "";
        System.out.printf("%-10s %-30s %-10s %-10d %-10d%s%n",
            i.getCodigo(), 
            i.getNombre(), 
            i.getUnidad(), 
            i.getStock(), 
            i.getStockMinimo(),
            estado);
      });
      
      System.out.println("─────────────────────────────────────────────────────────────────");
      
      long criticos = todosInsumos.stream().filter(Insumo::esCritico).count();
      System.out.printf("Total de insumos: %d | Críticos: %d%n", 
          todosInsumos.size(), criticos);
    }
  }

  /**
   * Maneja la opción de listar insumos con stock crítico
   */
  private static void manejarListarCriticos() {
    System.out.println("\n-- Insumos Críticos (stock <= mínimo) --");
    
    var lista = new ArrayList<>(stockService.obtenerInsumosCriticos());
    lista.sort(Comparator.comparing(Insumo::getNombre)); // Algoritmo de ordenación
    
    if (lista.isEmpty()) {
      System.out.println("No hay insumos críticos en este momento.");
    } else {
      lista.forEach(i -> System.out.printf("%s - %s [stock=%d, mínimo=%d]%n",
          i.getCodigo(), i.getNombre(), i.getStock(), i.getStockMinimo()));
    }
  }

  /**
   * Maneja la opción de generar reportes de movimientos
   */
  private static void manejarReporteMovimientos() {
    System.out.println("\n-- Reporte de Movimientos --");
    
    int dias = leerEntero("Días hacia atrás (ej. 30): ");
    
    System.out.println("\nFiltrar por servicio:");
    System.out.println("  0 - Todos");
    System.out.println("  1 - Guardia");
    System.out.println("  2 - Internación");
    System.out.println("  3 - Quirófano");
    System.out.println("  4 - Consultorios");
    int srvId = leerEntero("Servicio ID: ");
    
    LocalDate hoy = LocalDate.now();
    Integer srvFiltro = (srvId == 0 ? null : srvId);
    
    var lista = reportService.movimientosPorPeriodoYServicio(
        hoy.minusDays(dias), hoy, srvFiltro);
    
    if (lista.isEmpty()) {
      System.out.println("Sin movimientos en el período/servicio indicado.");
    } else {
      System.out.printf("\nMovimientos desde %s hasta %s%n", hoy.minusDays(dias), hoy);
      System.out.println("-------------------------------------------------------");
      lista.forEach(m -> System.out.printf("#%d | %s | %s x%d | %s | %s%n",
          m.getId(),
          m.getTipo(),
          m.getInsumo().getCodigo(),
          m.getCantidad(),
          (m.getServicio() == null ? "N/A" : m.getServicio().getNombre()),
          m.getFecha()));
    }
  }

  /**
   * Lee un número entero desde la consola con validación
   * Implementa estructuras repetitivas y manejo de excepciones
   * 
   * @param prompt Mensaje a mostrar al usuario
   * @return Número entero válido ingresado por el usuario
   */
  private static int leerEntero(String prompt) {
    while (true) {
      try {
        System.out.print(prompt);
        return Integer.parseInt(scanner.nextLine());
      } catch (NumberFormatException e) {
        System.out.println("Debe ingresar un número válido. Intente nuevamente.");
      }
    }
  }
}