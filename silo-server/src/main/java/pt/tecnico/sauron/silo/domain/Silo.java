package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Type;
import pt.tecnico.sauron.silo.domain.exceptions.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Silo {

    private HashMap objects = new HashMap<String, Object>();
    private TreeSet observations = new TreeSet<Observation>();
    private HashMap cameras = new HashMap();

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

    public void checkCameraName(String name) throws InvalidCameraName {
        
    }

    public void registerCamera(String name, double latitude, double longitude) throws InvalidCameraName, DuplicateCameraName {
        //TODO maybe check latitude and longitude?
        if(!(name.length() >= 3 && name.length() <= 15)) {
            throw new InvalidCameraName(name);
        }
        else{
            if(cameras.containsKey(name)){
//                System.out.printf("Catched!\n");
                throw new DuplicateCameraName("Repeated name " + '"' + name +'"' );
            }
            else {
                Camera camera = new Camera(name, latitude, longitude);
                cameras.put(name, camera);
            }
        }
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
