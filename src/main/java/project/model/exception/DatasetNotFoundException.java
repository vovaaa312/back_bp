package project.model.exception;

public class DatasetNotFoundException extends RuntimeException{
    public DatasetNotFoundException() {
    }

    public DatasetNotFoundException(String message) {
        super(message);
    }
}
