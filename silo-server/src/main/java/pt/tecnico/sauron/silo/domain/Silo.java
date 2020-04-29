package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.*;

import java.util.*;

public class Silo {

    private TreeSet<Observation> observations = new TreeSet<Observation>();
    private HashMap<String, Camera> cameras = new HashMap<String, Camera>();

    public Silo() { }

    public synchronized void camJoin(String name, double latitude, double longitude) throws InvalidCameraNameException, InvalidCoordinateException {
        if(cameras.containsKey(name)){
            if(cameras.get(name).getLatitude() != latitude || cameras.get(name).getLongitude() != longitude)
                throw new InvalidCameraNameException("Duplicate " + '"' + name +'"' );
        }
        else {
            Camera camera = new Camera(name, latitude, longitude);
            cameras.put(name, camera);
        }
    }

    public synchronized Camera camInfo(String name) throws CameraNameNotFoundException, InvalidCameraNameException {
        if (name.isBlank() || name.length() < 3 || name.length() > 15 || !name.matches("[A-Za-z0-9]+")) {
            throw new InvalidCameraNameException(name);
        }

        if(cameras.containsKey(name)){
            return cameras.get(name);
        }
        else{
            throw new CameraNameNotFoundException("Camera name not found: " + name);
        }
    }

    public synchronized void report(List<Observation> ol) {
        Iterator it = ol.iterator();

        // adds all  observations reported to the server list of observations
        while (it.hasNext()) {
            observations.add((Observation) it.next());
        }
    }

    public synchronized Observation track(Object.Type t, String i) throws NoObservationFoundException {                                  //TODO: validation of identifier?

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
    
    public List<Observation> trackMatch(Object.Type t, String s) throws NoObservationFoundException, InvalidPartialIdentifierException {

        List<Observation> list = new ArrayList<>();
        HashSet<String> identifiers = new HashSet<>();

        //evaluates identifier provided and returns a valid pattern for search in data
        String pattern = getPattern(s);

        //searches list of observations from the most recent to the oldest for a match in both type and identifier
        synchronized (this) {
            Iterator it = observations.descendingIterator();
            while (it.hasNext()) {
                Observation observation = (Observation) it.next();

                Object o = observation.getObject();

                if (o.getType() == t && o.getIdentifier().matches(pattern) && !identifiers.contains(o.getIdentifier())) {
                    list.add(observation);
                    identifiers.add(o.getIdentifier());
                }
            }
        }
        if (list.isEmpty()) {
            throw new NoObservationFoundException(s);
        }
        else {
            return list;
        }

    }

    public synchronized List<Observation> trace(Object.Type t, String s) throws NoObservationFoundException {                            //TODO: validation of identifier?

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

        //doesn't allow blank partial identifiers
        if (s.isBlank()) {
            throw new InvalidPartialIdentifierException();
        }

        //creates the pattern for search and creates a counter of * characters
        String pattern = "\\A";
        int anyCount = 0;

       //cycle that transforms partial identifier to pattern and checks if there is any invalid character in the identifier
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

        //if number of * characters is different than 1, the partial identifier is invalid
        if (anyCount != 1) {
            throw new InvalidPartialIdentifierException();
        }

        pattern = pattern + "\\Z";
        return pattern;
    }

    public synchronized void clear() {
        observations.clear();
        cameras.clear();
    }
}
