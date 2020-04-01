package pt.tecnico.sauron.silo.domain.exceptions;

public class InvalidCameraNameException extends Exception {

    public InvalidCameraNameException(String s) {
        super("This camera name is invalid: " + s);
    }
}
