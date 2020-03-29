package pt.tecnico.sauron.silo.domain;

public class Object {

    enum Type {
        CAR, PERSON
    }

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

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
