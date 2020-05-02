package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;

public class UpdateLog {

    private final int instance;
    private List<Update> updates = new ArrayList<Update>();
    private int[][] ts_vector = new int[9][9];                               //Predefined number os instances

    public UpdateLog(int instance) {
        this.instance = instance;
        for (int[] vector: ts_vector) {
            for (int i: vector) {
                i = 0;
            }
        }
    }

    public int getInstance() {
        return instance;
    }

    public synchronized List<Update> getUpdates() {
        return updates;
    }

    public synchronized int[] getTs_vector() {
        return ts_vector[instance - 1];
    }

    public synchronized void updateTs_vector(int instance, List<Integer> ts_vector) {
        for (int index = 0; index < ts_vector.size(); index++) {
            this.ts_vector[instance - 1][index] = ts_vector.get(index);
        }
    }

    public synchronized void addUpdate(Camera c) {
        Update update = new Update(instance, ts_vector[instance - 1][instance - 1], c);
        updates.add(update);
        ts_vector[instance - 1][instance - 1]++;
    }

    public synchronized void addUpdate(Camera c, List<Observation> obs) {
        Update update = new Update(instance, ts_vector[instance - 1][instance - 1], c, obs);
        updates.add(update);
        ts_vector[instance - 1][instance - 1]++;
    }

    public synchronized void addUpdate(Camera c, int instance) {
        Update update = new Update(instance, ts_vector[this.instance - 1][instance - 1], c);
        updates.add(update);
        ts_vector[this.instance - 1][instance - 1]++;
    }

    public synchronized void addUpdate(Camera c, List<Observation> obs, int instance) {
        Update update = new Update(instance, ts_vector[this.instance - 1][instance - 1], c, obs);
        updates.add(update);
        ts_vector[this.instance - 1][instance - 1]++;
    }

    public synchronized boolean skipUpdate(int instance, int seq_number) {
        if (instance == this.instance || seq_number < this.ts_vector[this.instance - 1][instance - 1]) {
            return true;
        }

        return false;
    }

    public synchronized boolean skipUpdate(int dest_instance, int update_instance, int seq_number) {
        if (dest_instance == update_instance || seq_number < this.ts_vector[dest_instance - 1][update_instance - 1]) {
            return true;
        }

        return false;
    }
}
