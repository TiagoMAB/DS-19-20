package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;

public class UpdateLog {                //TODO: check if needs synchronized

    private final int instance;
    private int seq_number = 0;
    private List<Update> updates = new ArrayList<Update>();

    UpdateLog(int instance) {
        this.instance = instance;
    }

    void addUpdate(Camera c) {
        Update update = new Update(instance, seq_number, c);
        updates.add(update);
        seq_number++;
    }

    void addUpdate(Camera c, List<Observation> obs) {
        Update update = new Update(instance, seq_number, c, obs);
        updates.add(update);
        seq_number++;
    }
}
