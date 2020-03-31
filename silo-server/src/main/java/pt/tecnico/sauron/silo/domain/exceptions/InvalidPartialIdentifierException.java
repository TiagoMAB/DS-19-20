package pt.tecnico.sauron.silo.domain.exceptions;

public class InvalidPartialIdentifierException extends Exception {

    public InvalidPartialIdentifierException() {
        super("This partial identifier is not recognized. Allowed types are: 'character(s)*' or 'character(s)*character(s)' or '*character(s)'. Where character is either a digit or a capitalized letter");
    }
}
