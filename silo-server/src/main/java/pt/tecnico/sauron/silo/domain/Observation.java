package pt.tecnico.sauron.silo.domain;

import java.sql.Timestamp;

public class Observation implements Comparable<Observation> {

    private Object object;
    private Timestamp time;

    public Observation(Object object, Timestamp time) {
        this.object = object;
        this.time = time;
    }

    public Object getObject() {
        return this.object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Timestamp getTime() {
        return this.time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Override
    public int compareTo(Observation o) {
        if (this.getTime().before(o.getTime())) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
