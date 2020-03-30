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

    public Silo() {
        try {
            Object o = new Object(Type.CAR, "AABB22");
            objects.put(o.getIdentifier(), o);

            Date date = new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(time);
            observations.add(new Observation(o, ts));

            TimeUnit.SECONDS.sleep(2);

            Object o2 = new Object(Type.CAR, "AABB22");
            objects.put(o2.getIdentifier(), o2);

            date = new Date();
            time = date.getTime();
            ts = new Timestamp(time);

            observations.add(new Observation(o2, ts));

            TimeUnit.SECONDS.sleep(3);

            Object o3 = new Object(Type.CAR, "AABB22");
            objects.put(o3.getIdentifier(), o3);

            date = new Date();
            time = date.getTime();
            ts = new Timestamp(time);

            observations.add(new Observation(o3, ts));

            Object o4 = new Object(Type.CAR, "AABC22");
            objects.put(o4.getIdentifier(), o4);

            date = new Date();
            time = date.getTime();
            ts = new Timestamp(time);

            observations.add(new Observation(o4, ts));
        }
        catch (Exception e) {}
    }

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
