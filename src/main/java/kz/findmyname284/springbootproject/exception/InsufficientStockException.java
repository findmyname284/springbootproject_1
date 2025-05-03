package kz.findmyname284.springbootproject.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException() {
        super("Insufficient stock");
    }    

}
