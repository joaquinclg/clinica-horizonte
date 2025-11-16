package app;

import app.handlers.AuthHandler;
import app.handlers.ReporteHandler;
import app.handlers.StockHandler;
import app.handlers.UsuarioHandler;
import app.ui.ConsoleUI;
import app.ui.MenuPrincipal;
import domain.Usuario;
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
  // Handlers de la aplicación
  private static AuthHandler authHandler;
  private static UsuarioHandler usuarioHandler;
  private static StockHandler stockHandler;
  private static ReporteHandler reporteHandler;

  public static void main(String[] args) {
    try {
      inicializarServicios();
      ejecutarAplicacion();
    } catch (Exception e) {
      System.err.println("Error fatal: " + e.getMessage());
      e.printStackTrace();
    } finally {
      ConsoleUI.cerrar();
    }
  }

  /**
   * Inicializa todos los servicios, repositorios y handlers de la aplicación
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
      AutenticacionService authService = new AutenticacionService(usuariosRepo);
      GestionUsuariosService userService = new GestionUsuariosService(usuariosRepo);
      StockService stockService = new StockService(insumosRepo, movRepo, serviciosRepo);
      ReportesService reportService = new ReportesService(movRepo);

      // Inicializar handlers
      authHandler = new AuthHandler(authService);
      usuarioHandler = new UsuarioHandler(userService);
      stockHandler = new StockHandler(stockService);
      reporteHandler = new ReporteHandler(reportService, stockService);
      
      ConsoleUI.mostrarInfo("Conexión a base de datos establecida correctamente");
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

    ConsoleUI.mostrarInfo("=== Clínica Horizonte - Gestión de Stock ===");

    // Loop principal de la aplicación
    while (!salir) {
      try {
        // Si no hay usuario logueado, mostrar login
        if (usuarioLogueado == null) {
          usuarioLogueado = authHandler.realizarLogin();
          continue;
        }

        // Mostrar menú y procesar opción
        ConsoleUI.mostrarInfo(MenuPrincipal.MENU);
        int opcion = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_OPCION);

        // Switch con manejo de las diferentes opciones del menú
        switch (opcion) {
          case 1:
            usuarioHandler.listarUsuarios(usuarioLogueado);
            break;
          case 2:
            usuarioHandler.altaUsuario(usuarioLogueado);
            break;
          case 3:
            usuarioHandler.modificarUsuario(usuarioLogueado);
            break;
          case 4:
            usuarioHandler.bajaUsuario(usuarioLogueado);
            break;
          case 5:
            stockHandler.ingresoInsumo(usuarioLogueado);
            break;
          case 6:
            stockHandler.egresoInsumo(usuarioLogueado);
            break;
          case 7:
            stockHandler.listarInsumos();
            break;
          case 8:
            stockHandler.listarCriticos();
            break;
          case 9:
            reporteHandler.reporteMovimientos(usuarioLogueado);
            break;
          case 10:
            usuarioLogueado = null;
            ConsoleUI.mostrarInfo("Sesión cerrada.");
            break;
          case 0:
            salir = true;
            ConsoleUI.mostrarInfo("Fin de la aplicación.");
            break;
          default:
            ConsoleUI.mostrarError("Opción inválida. Intente nuevamente.");
        }

      } catch (CredencialesInvalidasException | StockInsuficienteException | EntidadNoEncontradaException e) {
        // Manejo de excepciones de negocio
        ConsoleUI.mostrarError(e.getMessage());
      } catch (IllegalArgumentException e) {
        // Manejo de errores de validación
        ConsoleUI.mostrarError("Entrada inválida: " + e.getMessage());
      } catch (Exception e) {
        // Manejo de errores inesperados
        ConsoleUI.mostrarError("Error inesperado: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }
}
