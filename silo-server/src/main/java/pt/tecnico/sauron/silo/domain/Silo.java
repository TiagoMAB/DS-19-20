package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.*;

import java.util.*;

public class Silo {

    private HashMap<String, Object> objects = new HashMap<String, Object>();
    private TreeSet<Observation> observations = new TreeSet<Observation>();
    private HashMap<String, Camera> cameras = new HashMap<String, Camera>();

    public Silo() { }

    public void report(List<Observation> ol) {
        Iterator it = ol.iterator();
        while (it.hasNext()) {
            observations.add((Observation) it.next());
        }
    }

    public Observation track(Object.Type t, String i) throws NoObservationFoundException {                                  //TODO: validation of identifier?

        //searches list of observations from the most recent to the oldest for a match in both type and identifier
        Iterator it = observations.descendingIterator();
        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object o = observation.getObject();

            if (o.getIdentifier().equals(i) && o.getType() == t) {
                return observation;
            }
        }

        throw new NoObservationFoundException(i);
    }
    
    public Camera getCamera(String name) throws CameraNameNotFoundException {
        if(cameras.containsKey(name)){
            return cameras.get(name);
        }
        else{
            throw new CameraNameNotFoundException("Camera name not found: " + name);
        }
    }

    public double getCameraLongitude(String name) throws CameraNameNotFoundException {
        if(cameras.containsKey(name)){
            return cameras.get(name).getLongitude();
        }
        else{
            throw new CameraNameNotFoundException("Camera name not found: " + name);
        }
    }

    public void registerCamera(String name, double latitude, double longitude) throws InvalidCameraNameException, InvalidCoordinateException {
        if(cameras.containsKey(name)){
            throw new InvalidCameraNameException("Invalid Name - Duplicate " + '"' + name +'"' );
        }
        else {
            Camera camera = new Camera(name, latitude, longitude);
            cameras.put(name, camera);
        }
    }
    
    public List<Observation> trackMatch(Object.Type t, String s) throws NoObservationFoundException, InvalidPartialIdentifierException {

        List<Observation> list = new ArrayList<>();
        HashSet<Object> objects = new HashSet<>();

        //evaluates identifier provided and returns a valid pattern for search in data
        String pattern = getPattern(s);

        //searches list of observations from the most recent to the oldest for a match in both type and identifier
        Iterator it = observations.descendingIterator();
        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object o = observation.getObject();

            if (o.getType() == t && o.getIdentifier().matches(pattern) && !objects.contains(o)) {
                list.add(observation);
                objects.add(o);
            }
        }

        if (list.isEmpty()) {
            throw new NoObservationFoundException(s);
        }
        else {
            return list;
        }

    }

    public List<Observation> trace(Object.Type t, String s) throws NoObservationFoundException {                            //TODO: validation of identifier?

        List<Observation> list = new ArrayList<>();

        //searches list of observations from the most recent to the oldest for all matches in both type and identifiers
        Iterator it = observations.descendingIterator();
        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object o = observation.getObject();

            if (o.getIdentifier().equals(s) && o.getType() == t) {
                list.add(observation);
            }
        }

        if (list.isEmpty()) {
            throw new NoObservationFoundException(s);
        }
        else {
            return list;
        }
    }

    public String getPattern(String s) throws InvalidPartialIdentifierException {

        if (s.isBlank()) {
            throw new InvalidPartialIdentifierException();
        }

        String pattern = "\\A";
        int anyCount = 0;

        for (int i = 0; i < s.length() && anyCount <= 1; i++) {
            if (s.charAt(i) == '*') {
                pattern = pattern + ".*";
                anyCount++;
            }
            else if (s.charAt(i) >= 'A' && s.charAt(i) <= 'Z' || s.charAt(i) >= '0' && s.charAt(i) <= '9' ) {
                pattern = pattern + s.charAt(i);
            }
            else {
                throw new InvalidPartialIdentifierException();
            }

        }

        if (anyCount != 1) {
            throw new InvalidPartialIdentifierException();
        }

        pattern = pattern + "\\Z";
        return pattern;
    }
}
