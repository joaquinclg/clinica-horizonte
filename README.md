# Clínica Horizonte - Sistema de Gestión de Stock

Sistema de gestión y seguimiento del stock de insumos médicos para la Clínica Horizonte. Implementado en Java como parte del Seminario de Práctica 1.

## 📄 Documentación Completa

**Toda la información detallada del sistema se encuentra en el archivo `INFORME.pdf`**, incluyendo:

- Objetivos del sistema
- Límites y alcances
- Diagramas (clases, entidad-relación, casos de uso)
- Requerimientos funcionales y no funcionales
- Análisis y diseño
- Etapas del desarrollo
- Y demás documentación del proyecto

Este README contiene información técnica sobre la implementación, instalación y uso del sistema.

## Descripción

Sistema de gestión de stock de insumos médicos que permite:

- **Autenticación de usuarios** con control de accesos por rol
- **Gestión de usuarios** (alta, baja, modificación)
- **Control de ingresos y egresos** de insumos médicos
- **Alertas de stock crítico** y próximos a vencer
- **Reportes por período** y servicio

## Arquitectura

El proyecto sigue una arquitectura en capas con persistencia en base de datos MySQL:

```
clinica-horizonte/
├── app/            # Capa de presentación
│   ├── handlers/   # Manejadores de operaciones
│   │   └── OperacionesHandler.java
│   ├── ui/         # Interfaz de usuario
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
│   ├── jdbc/       # Implementaciones JDBC (MySQL)
│   │   ├── DatabaseConnection.java
│   │   ├── TransactionManager.java
│   │   ├── UsuarioJDBC.java
│   │   ├── InsumoJDBC.java
│   │   ├── MovimientoJDBC.java
│   │   └── ServicioJDBC.java
│   ├── memory/     # Implementaciones en memoria (legacy)
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
│   ├── EntidadNoEncontradaException.java
│   └── DatabaseException.java
├── lib/            # Librerías externas
│   └── mysql-connector-j-*.jar  # Driver de MySQL
├── clinica_horizonte.sql  # Script de creación de BD
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
- **Estructuras repetitivas** (while, for-each, for tradicional)
- **Declaración y creación de objetos** con constructores
- **Algoritmos de ordenación**: `Comparator.comparing()`, `sort()`
- **Algoritmos de búsqueda**: Búsqueda por código, nombre, filtros múltiples
- **Uso de ArrayList**: Para almacenar resultados de consultas y parámetros dinámicos
- **Uso de arreglos**: Parámetros de PreparedStatement en operaciones JDBC
- **Persistencia con JDBC**: Conexión a base de datos MySQL
- **Transacciones**: Manejo de transacciones para operaciones atómicas
- **Reflexión**: Para establecer campos privados (id, creadoEn)

### Seguridad y Control de Acceso

- Sistema de login con validación de credenciales
- Control de acceso por roles (ADMIN / AUXILIAR)
- Gestión completa de usuarios (alta, modificación, baja lógica)
- Validación de seguridad: un usuario no puede darse de baja a sí mismo
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

1. **Repository Pattern**: Abstracción del acceso a datos (JDBC y memoria)
2. **Service Layer Pattern**: Lógica de negocio separada de persistencia
3. **Dependency Injection**: Inyección de dependencias por constructor
4. **Strategy Pattern**: Interfaces implementables de repositorios
5. **Factory Pattern**: Inicialización centralizada de servicios
6. **Transaction Manager Pattern**: Gestión centralizada de transacciones
7. **DAO Pattern**: Data Access Object en repositorios JDBC

## Requisitos del Sistema

- **Java 17 o superior** (LTS recomendado)
- JDK instalado y configurado
- Variable de entorno `JAVA_HOME` configurada
- **MySQL 8.0 o superior** (o MariaDB compatible)
- **Driver de MySQL Connector/J**

## Instalación y Ejecución

### 1. Configurar Base de Datos MySQL

Primero, crea la base de datos y las tablas:

```bash
mysql -u root -p < clinica_horizonte.sql
```

### 2. Configurar Credenciales de Base de Datos

Edita el archivo `repo/jdbc/DatabaseConnection.java` y actualiza las credenciales:

```java
private static final String URL = "jdbc:mysql://localhost:3306/clinica_horizonte";
private static final String USER = "root";        // Tu usuario MySQL
private static final String PASSWORD = "password"; // Tu contraseña MySQL
```

### 3. Descargar Driver de MySQL

El driver es necesario para la conexión JDBC.

1. Ve a: https://dev.mysql.com/downloads/connector/j/
2. Descarga el archivo ZIP "Platform Independent"
3. Extrae el ZIP y copia el archivo `.jar` a la carpeta `lib/`

### 4. Compilar y Ejecutar

**Usando Scripts (Recomendado):**

```bash
# 1. Dar permisos de ejecución
chmod +x compile.sh run.sh

# 2. Compilar
./compile.sh

# 3. Ejecutar
./run.sh
```

### 5. Cargar Datos de Prueba

Si quieres datos de prueba, ejecuta las inserciones que se encuentran en el archivo `clinica_horizonte.sql`:

## Persistencia con JDBC

El sistema utiliza **JDBC** para persistir datos en MySQL. Características implementadas:

### Repositorios JDBC

- **UsuarioJDBC**: Gestión de usuarios con validaciones
- **InsumoJDBC**: CRUD completo de insumos con búsquedas
- **ServicioJDBC**: Gestión de servicios médicos
- **MovimientoJDBC**: Registro de movimientos con relaciones

### Transacciones

Las operaciones críticas utilizan transacciones para garantizar atomicidad:

- **`registrarIngreso()`**: Actualiza stock + crea movimiento (transacción)
- **`registrarEgreso()`**: Actualiza stock + crea movimiento (transacción)

Si alguna operación falla, se hace rollback automático.

### Uso de ArrayList y Arreglos

- **ArrayList**: Para almacenar resultados de consultas (`findAll()`, `findCriticos()`, etc.)
- **Arreglos**: Parámetros de `PreparedStatement` en operaciones INSERT/UPDATE
- **ArrayList dinámico**: Para construir consultas SQL con parámetros variables

### Manejo de Excepciones

- **`DatabaseException`**: Excepción personalizada para errores de BD
- Manejo de `SQLException` con mensajes descriptivos
- Validación de integridad referencial

## Datos de Prueba

### Usuarios Precargados

| Legajo | Password | Rol      | Descripción          |
| ------ | -------- | -------- | -------------------- |
| 1000   | admin123 | ADMIN    | Acceso completo      |
| 2000   | aux123   | AUXILIAR | Operaciones de stock |

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
3) Modificar usuario (ADMIN)
4) Baja usuario (ADMIN)
5) Ingreso de insumo
6) Egreso de insumo
7) Listar todos los insumos
8) Listar insumos críticos
9) Reporte de movimientos
10) Logout
0) Salir
```

## Tecnologías Utilizadas

- **Java 17**: Lenguaje de programación
- **JDBC**: API para acceso a bases de datos
- **MySQL (9.0.1 docker linux y xampp windows)+**: Sistema de gestión de bases de datos relacional
- **MySQL Connector/J**: Driver JDBC para MySQL
- **Bash Scripts**: Scripts de automatización para compilación y ejecución

## Contribuciones

Este proyecto fue desarrollado como parte del Seminario de Práctica 1, implementando:

- Arquitectura en capas
- Persistencia con JDBC
- Manejo de transacciones
- Validaciones de negocio
- Interfaz de consola interactiva
