package pt.tecnico.sauron.silo.domain;

import java.sql.Timestamp;

public class Observation implements Comparable<Observation> {

    private Object object;
    private Timestamp time;
    private Camera camera;

    public Observation(Object object, Timestamp time, Camera camera) {
        this.object = object;
        this.time = time;
        this.camera = camera;
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

    public Camera getCamera() { return this.camera; }

    public void setCamera(Camera camera) { this.camera = camera; }

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
