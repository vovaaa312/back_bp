package project.model.exception;

public class ImageObjectNotFoundException extends RuntimeException{
    public ImageObjectNotFoundException() {
    }

    public ImageObjectNotFoundException(String message) {
        super(message);
    }
}
