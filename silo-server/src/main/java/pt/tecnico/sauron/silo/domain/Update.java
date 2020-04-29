package pt.tecnico.sauron.silo.domain;

import java.util.List;

public class Update {

    public enum Type {
        camJoin, report
    }

    private final int instance;
    private final int seq_number;
    private final Update.Type type;

    private final Camera camera;
    private List<Observation> observations;

    Update(int i, int s, Camera c) {
        this.instance = i;
        this.seq_number = s;
        this.camera = c;
        this.type = Type.camJoin;
    }

    Update(int i, int s, Camera c, List<Observation> obs) {
        this.instance = i;
        this.seq_number = s;
        this.camera = c;
        this.observations = obs;
        this.type = Type.report;
    }

    public int getInstance() {
        return instance;
    }

    public int getSeq_number() {
        return seq_number;
    }

    public Type getType() {
        return type;
    }

    public Camera getCamera() {
        return camera;
    }

    public List<Observation> getObservations() {
        return observations;
    }

}
