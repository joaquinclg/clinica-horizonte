package exceptions;

public class StockInsuficienteException extends RuntimeException {
  public StockInsuficienteException() {
    super("Stock insuficiente");
  }

  public StockInsuficienteException(String message) {
    super(message);
  }

  public StockInsuficienteException(String message, Throwable cause) {
    super(message, cause);
  }
}