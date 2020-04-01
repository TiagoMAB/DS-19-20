package pt.tecnico.sauron.silo.domain.exceptions;

public class InvalidObjectTypeException extends Exception {

    public InvalidObjectTypeException() {
        super("The type provided is not recognized by the server");
    }
}
