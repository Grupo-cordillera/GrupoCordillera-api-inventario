package api.inventario.exception;

public class IndicadorStockNotFoundException extends RuntimeException {
    public IndicadorStockNotFoundException(String message) {
        super(message);
    }
}