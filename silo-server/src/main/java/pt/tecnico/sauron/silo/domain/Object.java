package pt.tecnico.sauron.silo.domain;

public class Object {

    enum Type {
        CAR, PERSON
    }

    private Type type;
    private String identifier;

    public Object(Type t, String i) {
        this.type = t;
        this.identifier = i;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type t) {
        this.type = t;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
