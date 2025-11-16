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
import repo.InsumoRepository;
import repo.MovimientoRepository;
import repo.ServicioRepository;
import repo.UsuarioRepository;
import repo.jdbc.InsumoJDBC;
import repo.jdbc.MovimientoJDBC;
import repo.jdbc.ServicioJDBC;
import repo.jdbc.UsuarioJDBC;
import usecase.AutenticacionService;
import usecase.GestionUsuariosService;
import usecase.ReportesService;
import usecase.StockService;

/**
 * Clase principal de la aplicación Clínica Horizonte - Sistema de Gestión de Stock
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
   * Usa repositorios JDBC para persistencia en base de datos MySQL
   */
  private static void inicializarServicios() {
    try {
      // Inicializar repositorios JDBC (conexión a MySQL)
      UsuarioRepository usuariosRepo = new UsuarioJDBC();
      ServicioRepository serviciosRepo = new ServicioJDBC();
      InsumoRepository insumosRepo = new InsumoJDBC();
      
      // MovimientoJDBC necesita otros repositorios para cargar relaciones
      MovimientoRepository movRepo = new MovimientoJDBC(insumosRepo, usuariosRepo, serviciosRepo);

      // Inicializar servicios de la capa de casos de uso
      authService = new AutenticacionService(usuariosRepo);
      userService = new GestionUsuariosService(usuariosRepo);
      stockService = new StockService(insumosRepo, movRepo, serviciosRepo);
      reportService = new ReportesService(movRepo);

      // Inicializar scanner para entrada de usuario
      scanner = new Scanner(System.in);
      
      System.out.println("Conexión a base de datos establecida correctamente");
    } catch (Exception e) {
      System.err.println("Error al inicializar servicios: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("No se pudo inicializar la aplicación", e);
    }
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
            manejarModificarUsuario(usuarioLogueado);
            break;
          case 4:
            manejarBajaUsuario(usuarioLogueado);
            break;
          case 5:
            manejarIngresoInsumo(usuarioLogueado);
            break;
          case 6:
            manejarEgresoInsumo(usuarioLogueado);
            break;
          case 7:
            manejarListarInsumos();
            break;
          case 8:
            manejarListarCriticos();
            break;
          case 9:
            manejarReporteMovimientos();
            break;
          case 10:
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
    System.out.println("Ingrese sus credenciales (datos desde base de datos)");
    
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
    System.out.println("3) Modificar usuario (ADMIN)");
    System.out.println("4) Baja usuario (ADMIN)");
    System.out.println("5) Ingreso de insumo");
    System.out.println("6) Egreso de insumo");
    System.out.println("7) Listar todos los insumos");
    System.out.println("8) Listar insumos críticos");
    System.out.println("9) Reporte de movimientos");
    System.out.println("10) Logout");
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
   * Maneja la opción de modificar un usuario existente (solo ADMIN)
   */
  private static void manejarModificarUsuario(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      System.out.println("Acceso denegado: Solo usuarios ADMIN pueden modificar usuarios.");
      return;
    }
    
    System.out.println("\n-- Modificar Usuario --");
    
    int legajo = leerEntero("Legajo del usuario a modificar: ");
    
    // Buscar el usuario existente (activo o inactivo)
    Usuario usuarioExistente;
    try {
      usuarioExistente = userService.obtenerPorLegajo(legajo);
    } catch (EntidadNoEncontradaException e) {
      System.out.println("Error: " + e.getMessage());
      return;
    }
    System.out.println("\nUsuario actual:");
    System.out.printf("  Nombre: %s%n", usuarioExistente.getNombre());
    System.out.printf("  Apellido: %s%n", usuarioExistente.getApellido());
    System.out.printf("  Rol: %s%n", usuarioExistente.getRol());
    System.out.printf("  Activo: %s%n", usuarioExistente.isActivo() ? "Sí" : "No");
    
    System.out.println("\nIngrese los nuevos datos (presione Enter para mantener el valor actual):");
    
    System.out.print("Nombre [" + usuarioExistente.getNombre() + "]: ");
    String nombre = scanner.nextLine().trim();
    if (nombre.isEmpty()) {
      nombre = usuarioExistente.getNombre();
    }
    
    System.out.print("Apellido [" + usuarioExistente.getApellido() + "]: ");
    String apellido = scanner.nextLine().trim();
    if (apellido.isEmpty()) {
      apellido = usuarioExistente.getApellido();
    }
    
    System.out.print("Password (nueva, mínimo 6 caracteres): ");
    String password = scanner.nextLine().trim();
    if (password.isEmpty()) {
      password = usuarioExistente.getPassword();
    }
    
    System.out.print("Rol (ADMIN/AUXILIAR) [" + usuarioExistente.getRol() + "]: ");
    String rolStr = scanner.nextLine().trim().toUpperCase();
    Rol rol;
    if (rolStr.isEmpty()) {
      rol = usuarioExistente.getRol();
    } else {
      rol = Rol.valueOf(rolStr);
    }
    
    // Preguntar por el estado activo
    String estadoActual = usuarioExistente.isActivo() ? "Activo" : "Inactivo";
    System.out.print("Estado (ACTIVO/INACTIVO) [" + estadoActual + "]: ");
    String estadoStr = scanner.nextLine().trim().toUpperCase();
    boolean activo;
    if (estadoStr.isEmpty()) {
      activo = usuarioExistente.isActivo();
    } else {
      activo = estadoStr.equals("ACTIVO") || estadoStr.equals("A");
    }
    
    // Crear usuario actualizado
    Usuario usuarioActualizado = new Usuario(legajo, password, nombre, apellido, rol);
    
    // Aplicar el estado activo/inactivo
    if (activo) {
      usuarioActualizado.activar();
    } else {
      usuarioActualizado.desactivar();
    }
    
    userService.editar(usuarioActualizado);
    System.out.println("Usuario modificado exitosamente.");
  }

  /**
   * Maneja la opción de dar de baja lógica a un usuario (solo ADMIN)
   */
  private static void manejarBajaUsuario(Usuario actor) {
    if (actor.getRol() != Rol.ADMIN) {
      System.out.println("Acceso denegado: Solo usuarios ADMIN pueden dar de baja usuarios.");
      return;
    }
    
    System.out.println("\n-- Baja de Usuario --");
    
    int legajo = leerEntero("Legajo del usuario a dar de baja: ");
    
    // Validar que no se esté dando de baja a sí mismo
    if (actor.getLegajo() == legajo) {
      System.out.println("Error: No puedes darte de baja a ti mismo. Otro administrador debe realizar esta operación.");
      return;
    }
    
    // Verificar que el usuario existe
    Usuario usuario;
    try {
      usuario = userService.obtenerPorLegajo(legajo);
    } catch (EntidadNoEncontradaException e) {
      System.out.println("Error: " + e.getMessage());
      return;
    }
    
    // Verificar que esté activo
    if (!usuario.isActivo()) {
      System.out.println("Error: El usuario ya está inactivo.");
      return;
    }
    System.out.println("\nUsuario a dar de baja:");
    System.out.printf("  Legajo: %d%n", usuario.getLegajo());
    System.out.printf("  Nombre: %s %s%n", usuario.getNombre(), usuario.getApellido());
    System.out.printf("  Rol: %s%n", usuario.getRol());
    
    System.out.print("\n¿Está seguro de dar de baja a este usuario? (s/n): ");
    String confirmacion = scanner.nextLine().trim().toLowerCase();
    
    if (!confirmacion.equals("s") && !confirmacion.equals("si")) {
      System.out.println("Operación cancelada.");
      return;
    }
    
    userService.bajaLogica(legajo);
    System.out.println("Usuario dado de baja exitosamente.");
  }

  /**
   * Maneja la opción de registrar un ingreso de insumo
   */
  private static void manejarIngresoInsumo(Usuario actor) {
    System.out.println("\n-- Ingreso de Insumo --");
    
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
    
    System.out.print("Código del insumo: ");
    String codigo = scanner.nextLine().toUpperCase();
    int cantidad = leerEntero("Cantidad a retirar: ");
    
    // Obtener servicios reales de la base de datos
    var serviciosList = new ArrayList<>(stockService.obtenerTodosLosServicios());
    
    if (serviciosList.isEmpty()) {
      System.out.println("Error: No hay servicios disponibles en la base de datos.");
      return;
    }
    
    // Mostrar servicios numerados secuencialmente (1, 2, 3...)
    System.out.println("\nServicios disponibles:");
    for (int i = 0; i < serviciosList.size(); i++) {
      System.out.printf("  %d - %s%n", i + 1, serviciosList.get(i).getNombre());
    }
    
    int opcionVisual = leerEntero("Servicio (número): ");
    
    // Validar que la opción esté en rango
    if (opcionVisual < 1 || opcionVisual > serviciosList.size()) {
      System.out.println("Error: Opción inválida. Debe estar entre 1 y " + serviciosList.size());
      return;
    }
    
    // Obtener el ID real del servicio seleccionado
    int servicioIdReal = serviciosList.get(opcionVisual - 1).getId();
    
    stockService.registrarEgreso(codigo, cantidad, servicioIdReal, actor);
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
    
    // Obtener servicios reales de la base de datos
    var serviciosList = new ArrayList<>(stockService.obtenerTodosLosServicios());
    
    System.out.println("\nFiltrar por servicio:");
    System.out.println("  0 - Todos");
    if (!serviciosList.isEmpty()) {
      // Mostrar servicios numerados secuencialmente (1, 2, 3...)
      for (int i = 0; i < serviciosList.size(); i++) {
        System.out.printf("  %d - %s%n", i + 1, serviciosList.get(i).getNombre());
      }
    } else {
      System.out.println("  (No hay servicios disponibles)");
    }
    
    int opcionVisual = leerEntero("Servicio (número): ");
    
    LocalDate hoy = LocalDate.now();
    Integer srvFiltro = null;
    
    // Si no es "0 - Todos", convertir el número visual al ID real
    if (opcionVisual != 0) {
      if (opcionVisual < 1 || opcionVisual > serviciosList.size()) {
        System.out.println("Error: Opción inválida. Debe estar entre 0 y " + serviciosList.size());
        return;
      }
      srvFiltro = serviciosList.get(opcionVisual - 1).getId();
    }
    
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