package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;

public class UpdateLog {                //TODO: check if needs synchronized

    private final int instance;
    private List<Update> updates = new ArrayList<Update>();
    private int[] ts_vector = new int[9];                               //Predefined number os instances

    public UpdateLog(int instance) {
        this.instance = instance;
        for (int i: ts_vector) {
            i = 0;
        }
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public int[] getTs_vector() {
        return ts_vector;
    }

    public void addUpdate(Camera c) {
        Update update = new Update(instance, ts_vector[instance - 1], c);
        updates.add(update);
        ts_vector[instance - 1]++;
    }

    public void addUpdate(Camera c, List<Observation> obs) {
        Update update = new Update(instance, ts_vector[instance - 1], c, obs);
        updates.add(update);
        ts_vector[instance - 1]++;
    }

    public void addUpdate(Camera c, int instance) {
        Update update = new Update(instance, ts_vector[instance - 1], c);
        updates.add(update);
        ts_vector[instance - 1]++;
    }

    public void addUpdate(Camera c, List<Observation> obs, int instance) {
        Update update = new Update(instance, ts_vector[instance - 1], c, obs);
        updates.add(update);
        ts_vector[instance - 1]++;
    }

    public boolean skipUpdate(int instance, int seq_number) {
        if (instance == this.instance || seq_number < this.ts_vector[instance - 1]) {
            return true;
        }

        return false;
    }
}
