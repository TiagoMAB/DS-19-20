package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.InvalidIdentifierException;
import pt.tecnico.sauron.silo.grpc.Type;

public class Object {

    private Type type;
    private String identifier;

    public Object(Type type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) throws InvalidIdentifierException {
        checkIdentifier(identifier);
        this.identifier = identifier;
    }

    public void checkIdentifier(String identifier) throws InvalidIdentifierException {
        if (identifier.isBlank()) {
            throw new InvalidIdentifierException(identifier);
        }

        if (type == Type.CAR && identifier.length() == 6) {

            String[] patterns = {"^\\d{2}[A-Z]{2}\\d{2}", "^[A-Z]{2}\\d{4}", "^\\d{4}[A-Z]{2}", "^\\d{2}[A-Z]{4}", "^[A-Z]{4}\\d{2}", "^[A-Z]{2}\\d{2}[A-Z]{2}"};
            for (String pattern: patterns) {
                if (identifier.matches(pattern)) {
                    this.identifier = identifier;
                    return;
                }
            }
        }

        if (type == Type.PERSON) {
            Long n;

            try {
                n = Long.parseUnsignedLong(identifier);
            }
            catch (NumberFormatException e) {
                throw new InvalidIdentifierException(identifier);
            }

            if (n > 9223372036854775807L) {
                throw new InvalidIdentifierException(identifier);
            }

            this.identifier = identifier;
            return;
        }

        throw new InvalidIdentifierException(identifier);
    }

}
