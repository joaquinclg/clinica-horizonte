package exceptions;

public class CredencialesInvalidasException extends RuntimeException {
  public CredencialesInvalidasException() {
    super("Credenciales inválidas");
  }

  public CredencialesInvalidasException(String message) {
    super(message);
  }

  public CredencialesInvalidasException(String message, Throwable cause) {
    super(message, cause);
  }
}