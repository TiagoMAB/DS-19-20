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

    public Observation track(int t, String i) throws NoObservationFound, InvalidObjectTypeException {
      
        //validation of type to avoid unnecessary searches through data                                                     TODO:maybe do the same for identifier
        if (!checkType(t)) {
            throw new InvalidObjectTypeException();
        }

        //searches list of observations from the most recent to the oldest for a match in both type and identifier
        Iterator it = observations.descendingIterator();
        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object o = observation.getObject();

            if (o.getIdentifier().equals(i) && o.getType().ordinal() == t) {
                return observation;
            }
        }

        throw new NoObservationFound(i);
    }
    
    public double getCameraLatitude(String name) throws CameraNameNotFoundException {
        if(cameras.containsKey(name)){
            return cameras.get(name).getLatitude();
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

    public void registerCamera(String name, double latitude, double longitude) throws InvalidCameraNameException {
        //TODO maybe check latitude and longitude?
        if(!(name.length() >= 3 && name.length() <= 15)) {
            throw new InvalidCameraNameException("Invalid Length " + '"' + name + '"');
        }
        else{
            if(cameras.containsKey(name)){
//                System.out.printf("Catched!\n");
                throw new InvalidCameraNameException("Invalid Name - Duplicate " + '"' + name +'"' );
            }
            else {
                Camera camera = new Camera(name, latitude, longitude);
                cameras.put(name, camera);
            }
        }
    }
    
    public List<Observation> trackMatch(int t, String s) throws NoObservationFound, InvalidObjectTypeException, InvalidPartialIdentifierException {

        List<Observation> list = new ArrayList<>();
        HashSet<Object> objects = new HashSet<>();

        //validation of type to avoid unnecessary searches through data                                                     TODO:maybe do the same for identifier
        if (!checkType(t)) {
            throw new InvalidObjectTypeException();
        }

        String pattern = getPattern(s);

        Iterator it = observations.descendingIterator();
        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object o = observation.getObject();

            if (o.getType().ordinal() == t && o.getIdentifier().matches(pattern) && !objects.contains(o)) {
                list.add(observation);
                objects.add(o);
            }
        }

        if (list.isEmpty()) {
            throw new NoObservationFound(s);
        }
        else {
            return list;
        }

    }

    public List<Observation> trace(int t, String s) throws NoObservationFound, InvalidObjectTypeException {

        List<Observation> list = new ArrayList<>();

        //validation of type to avoid unnecessary searches through data                                                     TODO:maybe do the same for identifier
        if (!checkType(t)) {
            throw new InvalidObjectTypeException();
        }

        Iterator it = observations.descendingIterator();

        while (it.hasNext()) {
            Observation observation = (Observation) it.next();

            Object o = observation.getObject();

            if (o.getIdentifier().equals(s) && o.getType().ordinal() == t) {
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

    public boolean checkType(int t) {                                                           //TODO: Maybe create package util with these functions/methods

        for (Object.Type type: Object.Type.values()) {
            if (type.ordinal() == t) {
                return true;
            }
        }
        return false;
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
