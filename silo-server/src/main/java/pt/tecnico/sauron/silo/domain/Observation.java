package pt.tecnico.sauron.silo.domain;

import java.time.LocalDateTime;

public class Observation {

    private Object object;
    private String identifier;
    private LocalDateTime time;

    public Observation(Object object, String identifier, LocalDateTime time) {
        this.object = object;
        this.identifier = identifier;
        this.time = time;
    }

    public Object getObject() {
        return this.object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public LocalDateTime getTime() {
        return this.time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
