package pt.tecnico.sauron.silo.domain.exceptions;

public class NoObservationFoundException extends Exception {

    public NoObservationFoundException(String s) {
        super("No observation was found for given (partial) identifier: " + s );
    }

}
