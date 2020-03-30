package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Type;
import pt.tecnico.sauron.silo.domain.exceptions.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Silo {

    private HashMap objects = new HashMap<String, Object>();
    private TreeSet observations = new TreeSet<Observation>();
    private HashSet cameras = new HashSet<Camera>();

    public Silo() {}

    public Timestamp track(Type t, String s) throws NoObservationFound {

        Iterator it = observations.descendingIterator();
        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object object = observation.getObject();

            if (object.getIdentifier().equals(s) && object.getType() == t) {
                return observation.getTime();
            }
        }

        throw new NoObservationFound(s);
    }

    public List<Observation> trace(Type t, String s) throws NoObservationFound {

        List<Observation> list = new ArrayList<Observation>();

        Iterator it = observations.descendingIterator();

        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object object = observation.getObject();

            if (object.getIdentifier().equals(s) && object.getType() == t) {
                list.add(observation);
            }
        }
        if (list.isEmpty()) {
            throw new NoObservationFound(s);
        }
        else {
            return list;
        }
    }
}
