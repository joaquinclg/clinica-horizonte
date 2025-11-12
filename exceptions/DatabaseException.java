package exceptions;

/**
 * Excepción personalizada para errores relacionados con la base de datos
 */
public class DatabaseException extends RuntimeException {
  
  public DatabaseException(String message) {
    super(message);
  }
  
  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}

