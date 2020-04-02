package pt.tecnico.sauron.silo.domain;

import java.sql.Timestamp;

public class Observation implements Comparable<Observation> {

    private Object object;
    private Timestamp timestamp;
    private Camera camera;

    public Observation(Object object, Timestamp timestamp, Camera camera) {
        this.object = object;
        this.timestamp = timestamp;
        this.camera = camera;
    }

    public Object getObject() {
        return this.object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTime(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Camera getCamera() { return this.camera; }

    public void setCamera(Camera camera) { this.camera = camera; }

    @Override
    public int compareTo(Observation o) {
        if (this.getTimestamp().before(o.getTimestamp())) {
            return -1;
        }
        else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "Observation reported by " + this.camera + "\" of " + this.object + " with timestamp: " + timestamp;
    }
}
