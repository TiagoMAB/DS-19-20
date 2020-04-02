package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.*;

public class Object {

    public enum Type {
        car, person
    }

    private Type type;
    private String identifier;

    public Object(int type, String identifier) throws InvalidIdentifierException, InvalidObjectTypeException {
        setType(type);
        setIdentifier(identifier);
    }

    public Type getType() {
        return this.type;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setType(int t) throws InvalidObjectTypeException {
        
        for (Type type: Type.values()) {
            if (type.ordinal() == t + 1) {
                this.type = type;
                return;
            }
        }
        throw new InvalidObjectTypeException();
    }

    public void setIdentifier(String identifier) throws InvalidIdentifierException {

        if (type == Type.car && !identifier.isBlank() && identifier.length() == 6) {
            String[] patterns = {"^\\d{2}[A-Z]{2}\\d{2}", "^[A-Z]{2}\\d{4}", "^\\d{4}[A-Z]{2}", "^\\d{2}[A-Z]{4}", "^[A-Z]{4}\\d{2}", "^[A-Z]{2}\\d{2}[A-Z]{2}"};

            for (String pattern: patterns) {
                if (identifier.matches(pattern)) {
                    this.identifier = identifier;
                    return;
                }
            }
        }

        if (type == Type.person && !identifier.isBlank()) {
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

    public static Type findType(int t) throws InvalidObjectTypeException {
        for (Type type: Type.values()) {
            if (type.ordinal() == t + 1) {
                return type;
            }
        }
        throw new InvalidObjectTypeException();
    }

    @Override
    public String toString() {
        return "Object identified by \"" + this.identifier + "\" of type:" + this.type;
    }
}
