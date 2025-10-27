# Clínica Horizonte - Sistema de Gestión de Stock

Sistema de gestión y seguimiento del stock de insumos médicos para la Clínica Horizonte. Implementado en Java como parte del Seminario de Práctica 1.

## Descripción

Sistema de gestión de stock de insumos médicos que permite:

- **Autenticación de usuarios** con control de accesos por rol
- **Gestión de usuarios** (alta, baja, modificación)
- **Control de ingresos y egresos** de insumos médicos
- **Alertas de stock crítico** y próximos a vencer
- **Reportes por período** y servicio

## Arquitectura

El proyecto sigue una arquitectura en capas:

```
clinica-horizonte/
├── app/            # Capa de presentación
│   ├── handlers/   # Manejadores de operaciones
│   │   └── OperacionesHandler.java
│   ├── ui/        # Interfaz de usuario
│   │   ├── ConsoleUI.java
│   │   └── MenuPrincipal.java
│   └── MainDemo.java
├── bin/            # Archivos compilados (.class)
├── domain/         # Entidades del dominio
│   ├── enums/      # Enumeraciones (Rol, EstadoInsumo, TipoMovimiento)
│   ├── Usuario.java
│   ├── Persona.java
│   ├── Insumo.java
│   ├── Movimiento.java
│   └── Servicio.java
├── repo/           # Capa de persistencia
│   ├── memory/     # Implementaciones en memoria (actual)
│   │   ├── UsuarioInMemory.java
│   │   ├── InsumoInMemory.java
│   │   ├── MovimientoInMemory.java
│   │   └── ServicioInMemory.java
│   ├── UsuarioRepository.java
│   ├── InsumoRepository.java
│   ├── MovimientoRepository.java
│   └── ServicioRepository.java
├── usecase/        # Lógica de negocio
│   ├── AutenticacionService.java
│   ├── GestionUsuariosService.java
│   ├── StockService.java
│   └── ReportesService.java
├── exceptions/     # Excepciones personalizadas
│   ├── CredencialesInvalidasException.java
│   ├── StockInsuficienteException.java
│   └── EntidadNoEncontradaException.java
├── compile.sh      # Script de compilación
└── run.sh          # Script de ejecución
```

## Características Implementadas

### Requisitos Técnicos Cumplidos

- **Correcta utilización de sintaxis, tipos de datos y estructuras de control**
- **Tratamiento y manejo de excepciones** con try-catch y excepciones personalizadas
- **Encapsulamiento**: Atributos privados con getters/setters apropiados
- **Herencia**: `Usuario` extiende `Persona` (clase abstracta)
- **Polimorfismo**: Uso de interfaces en repositorios
- **Abstracción**: Clase abstracta `Persona`, interfaces de repositorios
- **Menú de selección interactivo** con múltiples opciones
- **Estructuras condicionales** (if-else, switch)
- **Estructuras repetitivas** (while, for-each)
- **Declaración y creación de objetos** con constructores
- **Algoritmos de ordenación**: `Comparator.comparing()`, `sort()`
- **Algoritmos de búsqueda**: Búsqueda por código, nombre, filtros múltiples

### Seguridad y Control de Acceso

- Sistema de login con validación de credenciales
- Bloqueo de cuenta tras 3 intentos fallidos
- Control de acceso por roles (ADMIN / AUXILIAR)
- Baja lógica de usuarios (no se eliminan físicamente)

### Gestión de Stock

- Registro de ingresos de insumos
- Registro de egresos con asignación a servicio
- Validación de stock disponible
- Alertas automáticas de stock crítico
- Control de fechas de vencimiento

### Reportes

- Movimientos por período (últimos N días)
- Filtrado por servicio específico
- Listado de insumos críticos
- Historial completo de movimientos

## Patrones de Diseño Implementados

1. **Repository Pattern**: Abstracción del acceso a datos
2. **Service Layer Pattern**: Lógica de negocio separada
3. **Dependency Injection**: Inyección de dependencias por constructor
4. **Strategy Pattern**: Interfaces implementables de repositorios
5. **Factory Pattern**: Inicialización centralizada de servicios

## Requisitos del Sistema

- **Java 17 o superior** (LTS recomendado)
- JDK instalado y configurado
- Variable de entorno `JAVA_HOME` configurada

## Instalación y Ejecución

### Opción 1: Usando Scripts

```bash
# 1. Navegar al directorio del proyecto
cd /path/to/clinica-horizonte

# 2. Dar permisos de ejecución a los scripts
chmod +x compile.sh run.sh

# 3. Compilar usando el script
./compile.sh

# 4. Ejecutar la aplicación
./run.sh
```

### Opción 2: Desde la Terminal (Linux/Mac)

```bash
# Compilar
find . -name "*.java" > sources.txt
javac -d build @sources.txt

# Ejecutar
java -cp build app.MainDemo
```

## Usuarios de Prueba

El sistema incluye usuarios precargados:

| Legajo | Password | Rol      | Descripción          |
| ------ | -------- | -------- | -------------------- |
| 1000   | admin123 | ADMIN    | Acceso completo      |
| 2000   | aux123   | AUXILIAR | Operaciones de stock |

## Insumos Precargados

| Código | Nombre               | Stock | Stock Mínimo |
| ------ | -------------------- | ----- | ------------ |
| GAS-01 | Gasas estériles      | 50    | 10           |
| GUA-01 | Guantes descartables | 40    | 15           |
| BAR-01 | Barbijos quirúrgicos | 25    | 20           |

## Servicios Disponibles

1. **Guardia** - Atención de urgencias
2. **Internación** - Pacientes hospitalizados
3. **Quirófano** - Cirugías
4. **Consultorios** - Atención ambulatoria

## Uso del Sistema

### 1. Login

```
Legajo: 1000
Password: admin123
```

### 2. Menú Principal

```
== Menú Principal ==
1) Listar usuarios (ADMIN)
2) Alta usuario (ADMIN)
3) Ingreso de insumo
4) Egreso de insumo
5) Listar todos los insumos
6) Listar insumos críticos
7) Reporte de movimientos
8) Logout
0) Salir
```

### 3. Ejemplos de Operaciones

#### Listar Todos los Insumos

```
-- Listado de Todos los Insumos --
─────────────────────────────────────────────────────────────────
Código     Nombre                         Unidad     Stock      Mínimo
─────────────────────────────────────────────────────────────────
BAR-01     Barbijos quirúrgicos           caja       15         20         ⚠️ CRÍTICO
GAS-01     Gasas estériles                paquete    50         10
GUA-01     Guantes descartables           caja       40         15
─────────────────────────────────────────────────────────────────
Total de insumos: 3 | Críticos: 1
```

#### Registrar Ingreso de Insumo

```
Código del insumo: GAS-01
Cantidad a ingresar: 20
```

#### Registrar Egreso de Insumo

```
Código del insumo: GAS-01
Cantidad a retirar: 5
Servicio ID: 1 (Guardia)
```

#### Crear Nuevo Usuario (Solo ADMIN)

```
Legajo: 3000
Nombre: María
Apellido: López
Password: maria123
Rol: AUXILIAR
```

## Modelo de Datos

### Diagrama de Clases Principales

```
Persona (abstract)
    ↑
    |
Usuario
    ├── legajo: int (PK)
    ├── password: String
    ├── nombre: String
    ├── apellido: String
    ├── rol: Rol (ENUM)
    ├── activo: boolean
    └── creadoEn: LocalDateTime

Insumo
    ├── codigo: String (PK)
    ├── nombre: String
    ├── unidad: String
    ├── stock: int
    ├── stockMinimo: int
    ├── estado: EstadoInsumo (ENUM)
    └── fechaVencimiento: LocalDate

Movimiento
    ├── id: int (PK)
    ├── tipo: TipoMovimiento (ENUM)
    ├── fecha: LocalDateTime
    ├── cantidad: int
    ├── usuario: Usuario (FK)
    ├── insumo: Insumo (FK)
    └── servicio: Servicio (FK, nullable)

Servicio
    ├── id: int (PK)
    └── nombre: String
```
