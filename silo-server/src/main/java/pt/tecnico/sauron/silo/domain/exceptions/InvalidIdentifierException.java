package pt.tecnico.sauron.silo.domain.exceptions;

public class InvalidIdentifierException extends Exception {

    public InvalidIdentifierException(String s) {
        super("This identifier is not recognized: " + s);
    }
}
