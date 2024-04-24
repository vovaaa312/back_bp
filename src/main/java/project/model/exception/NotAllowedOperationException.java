package project.model.exception;

public class NotAllowedOperationException extends Exception{
    public NotAllowedOperationException() {
    }

    public NotAllowedOperationException(String message) {
        super(message);
    }
}
