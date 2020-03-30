package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Type;
import pt.tecnico.sauron.silo.domain.exceptions.*;

import java.sql.Timestamp;
import java.util.*;

public class Silo {

    private HashMap objects = new HashMap<String, Object>();
    private TreeSet observations = new TreeSet<Observation>();
    private HashSet cameras = new HashSet<Camera>();

    public Silo() { }

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

}
