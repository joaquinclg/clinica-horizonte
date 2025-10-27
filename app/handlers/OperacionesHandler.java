package app.handlers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.ui.ConsoleUI;
import app.ui.MenuPrincipal;
import domain.Insumo;
import domain.Movimiento;
import domain.Usuario;
import domain.enums.Rol;
import usecase.AutenticacionService;
import usecase.GestionUsuariosService;
import usecase.ReportesService;
import usecase.StockService;

public class OperacionesHandler {
  private final AutenticacionService authService;
  private final GestionUsuariosService userService;
  private final StockService stockService;
  private final ReportesService reportService;

  public OperacionesHandler(
      AutenticacionService authService,
      GestionUsuariosService userService,
      StockService stockService,
      ReportesService reportService) {
    this.authService = authService;
    this.userService = userService;
    this.stockService = stockService;
    this.reportService = reportService;
  }

  public Usuario realizarLogin() {
    ConsoleUI.mostrarInfo("\n== Login ==");
    int legajo = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_LEGAJO);
    String password = ConsoleUI.leerString(MenuPrincipal.PROMPT_PASSWORD);

    Usuario usuario = authService.login(legajo, password);
    ConsoleUI.mostrarExito(String.format("Bienvenido %s [%s]",
        usuario.getNombreCompleto(), usuario.getRol()));
    return usuario;
  }

  public void listarUsuarios(Usuario usuario) {
    validarAdmin(usuario);
    ConsoleUI.mostrarInfo("-- Usuarios activos --");

    List<Usuario> usuarios = userService.listarActivos();
    if (usuarios.isEmpty()) {
      ConsoleUI.mostrarInfo("No hay usuarios activos.");
      return;
    }

    usuarios.forEach(u -> ConsoleUI.mostrarInfo(String.format("%d - %s (%s)",
        u.getLegajo(), u.getNombreCompleto(), u.getRol())));
  }

  public void altaUsuario(Usuario usuario) {
    validarAdmin(usuario);
    ConsoleUI.mostrarInfo("-- Alta de usuario --");

    int legajo = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_LEGAJO);
    String nombre = ConsoleUI.leerString(MenuPrincipal.PROMPT_NOMBRE);
    String apellido = ConsoleUI.leerString(MenuPrincipal.PROMPT_APELLIDO);
    String password = ConsoleUI.leerString(MenuPrincipal.PROMPT_PASSWORD);
    String rolStr = ConsoleUI.leerString(MenuPrincipal.PROMPT_ROL);

    Rol rol = Rol.valueOf(rolStr.toUpperCase());
    Usuario nuevoUsuario = new Usuario(legajo, password, nombre, apellido, rol);

    userService.alta(nuevoUsuario);
    ConsoleUI.mostrarExito("Usuario creado correctamente");
  }

  public void ingresoInsumo(Usuario usuario) {
    ConsoleUI.mostrarInfo("-- Ingreso de insumo --");

    String codigo = ConsoleUI.leerString(MenuPrincipal.PROMPT_CODIGO);
    int cantidad = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_CANTIDAD);

    stockService.registrarIngreso(codigo, cantidad, usuario);
    ConsoleUI.mostrarExito("Ingreso registrado correctamente");
  }

  public void egresoInsumo(Usuario usuario) {
    ConsoleUI.mostrarInfo("-- Egreso de insumo --");

    String codigo = ConsoleUI.leerString(MenuPrincipal.PROMPT_CODIGO);
    int cantidad = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_CANTIDAD);
    int servicioId = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_SERVICIO);

    stockService.registrarEgreso(codigo, cantidad, servicioId, usuario);
    ConsoleUI.mostrarExito("Egreso registrado correctamente");
  }

  public void listarCriticos() {
    ConsoleUI.mostrarInfo("-- Insumos críticos (stock <= mínimo) --");

    List<Insumo> criticos = new ArrayList<>(stockService.obtenerInsumosCriticos());
    if (criticos.isEmpty()) {
      ConsoleUI.mostrarInfo("No hay insumos críticos.");
      return;
    }

    criticos.sort(Comparator.comparing(Insumo::getNombre));
    criticos.forEach(i -> ConsoleUI.mostrarInfo(String.format("%s - %s [stock=%d, min=%d]",
        i.getCodigo(), i.getNombre(), i.getStock(), i.getStockMinimo())));
  }

  public void reporteMovimientos() {
    ConsoleUI.mostrarInfo("-- Reporte de movimientos por período y servicio --");
    
    int dias = ConsoleUI.leerEntero(MenuPrincipal.PROMPT_DIAS);
    String servicioPrompt = String.format("Servicio ID (0 %s): ", 
        String.join(", 1 ", MenuPrincipal.SERVICIOS));
    int servicioId = ConsoleUI.leerEntero(servicioPrompt);
    
    LocalDate hoy = LocalDate.now();
    Integer filtroServicio = (servicioId == 0 ? null : servicioId);
    
    var movimientos = reportService.movimientosPorPeriodoYServicio(
        hoy.minusDays(dias), hoy, filtroServicio);
        
    if (movimientos.isEmpty()) {
      ConsoleUI.mostrarInfo("Sin movimientos en el período/servicio indicado.");
      return;
    }
    
    ConsoleUI.mostrarInfo(String.format("Movimientos desde %s hasta %s (servicio=%s)",
        hoy.minusDays(dias), hoy, 
        filtroServicio == null ? "Todos" : MenuPrincipal.SERVICIOS[servicioId]));
        
    movimientos.forEach(m -> ConsoleUI.mostrarInfo(String.format("#%d %s %s x%d | %s | %s",
        m.getId(),
        m.getTipo(),
        m.getInsumo().getCodigo(),
        m.getCantidad(),
        (m.getServicio() == null ? "-" : m.getServicio().getNombre()),
        m.getFecha())));
  }

  private void validarAdmin(Usuario usuario) {
    if (usuario.getRol() != Rol.ADMIN) {
      throw new IllegalStateException("Esta operación requiere rol ADMIN");
    }
  }
}