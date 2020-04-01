package pt.tecnico.sauron.silo.domain.exceptions;

public class InvalidCoordinateException extends Exception {

    public InvalidCoordinateException(double d) {
        super("This coordinate is out of bounds: " + d);
    }
}
