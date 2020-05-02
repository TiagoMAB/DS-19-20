package pt.tecnico.sauron.silo.client;

import pt.tecnico.sauron.silo.grpc.*;

import java.util.*;

public class Cache {

    private final int limit;
    private List<Observation> trails;
    private List<Observation> spots;

    public Cache(int limit) {
        this.limit = limit;
        this.spots = new ArrayList<Observation>();
        this.trails = new ArrayList<Observation>();
    }

    public void addObservation(Observation obs) {

        //checks if observation already exists in cache and removes it
        for (Observation o: spots) {
            if (o.getIdentifier().equals(obs.getIdentifier())) {
                spots.remove(o);
                break;
            }
        }

        //if cache limit is reached, removes old information from cache
        if (spots.size() == limit) {
            spots.remove(spots.get(0));
        }

        spots.add(obs);
    }

    public void addTrailObservations(List<Observation> observationList) {

        List<Observation> removeList = new ArrayList<>();
        List<Observation> addList = new ArrayList<>();

        //if obtained observation list is empty doesn't update cache
        if (observationList.isEmpty()) {
            return;
        }

        //searches for every observation with traced id already in cache
        Iterator it = trails.iterator();
        while (it.hasNext()) {
            Observation o = (Observation) it.next();

            //if match is found adds old cached observation to list to be removed in the future
            if (o.getIdentifier().equals(observationList.get(0).getIdentifier())) {
                removeList.add(o);
            }
        }

        //removes every old observation with traced id already in cache
        trails.removeAll(removeList);

        //if incoming observations surpass cache size, select only most recent observations
        int i = observationList.size() > limit ? limit - 1: observationList.size() - 1;
        while (i >= 0) {
            addList.add(observationList.get(i));
            i--;
        }

        //if cache limit is reached, removes old information from cache
        while(trails.size() + addList.size() > limit) {
            trails.remove(0);
        }

        //adds new observations to the cache
        trails.addAll(addList);
    }

    public void addSpotObservations(List<Observation> observationList) {

        //adds every observation in observation list received from trackMatch
        for (Observation o: observationList) {
            addObservation(o);
        }
    }

    public TrackResponse coherentTrack(TrackResponse r, String id, Type t) {

        Observation obs = r.getObservation();
        Observation cached_obs = null;

        //checks if observation already exists in spots cache
        for (Observation o: spots) {
            if (o.getIdentifier().equals(id) && o.getType() == t) {
                cached_obs = o;
            }
        }

        //checks if there is an observation in trails cache that is more recent than the one found in spots cache
        //or if none was found in spots cache saves the most recent found in trails cache
        for (Observation o: trails) {
            if (o.getIdentifier().equals(id) && o.getType() == t && (cached_obs == null || o.getDate().getSeconds() > cached_obs.getDate().getSeconds())) {
                cached_obs = o;
            }
        }

        if (cached_obs != null && obs != Observation.getDefaultInstance()) {

            //returns the most recent observation between the one received from incoherent track response and cached observation
            Observation response_obs = cached_obs.getDate().getSeconds() > obs.getDate().getSeconds() ? cached_obs : obs;
            return TrackResponse.newBuilder().setObservation(response_obs).build();
        }
        else if (cached_obs != null){

            //returns cached observation since none was received from incoherent track response
            return TrackResponse.newBuilder().setObservation(cached_obs).build();
        }
        else {

            //no information was found in cache so returns track response received from server
            return r;
        }

    }

    public TrackMatchResponse coherentTrackMatch(TrackMatchResponse r, String partialId, Type t) {

        List<Observation> obs = r.getObservationsList();
        HashMap<String, Observation> cached_obs = new HashMap<String, Observation>();
        TrackMatchResponse.Builder response = TrackMatchResponse.newBuilder();

        //gets pattern to search based on partial id
        String pattern = getPattern(partialId);

        //checks if observation that matches partial id already exists in spots cache
        for (Observation o: spots) {
            if (o.getIdentifier().matches(pattern) && o.getType() == t) {
                cached_obs.put(o.getIdentifier(), o);
            }
        }

        //checks if there is an observation in trails cache that is more recent than the one found in spots cache
        //or if none was found in spots cache saves the most recent found in trails cache
        for (Observation o: trails) {
            if (o.getIdentifier().matches(pattern) && o.getType() == t &&
                    (cached_obs.get(o.getIdentifier()) == null || o.getDate().getSeconds() > cached_obs.get(o.getIdentifier()).getDate().getSeconds())) {
                cached_obs.put(o.getIdentifier(), o);
            }
        }

        //if observation list received from incoherent trackMatch response is not empty
        //compares list to cached information and selects the most recent observation out of the two
        if (!obs.isEmpty()) {
            for (Observation o : obs) {
                Observation cached = cached_obs.get(o.getIdentifier());
                Observation response_obs;

                if (cached != null) {

                    //chooses most recent observation between cached and received from server
                    response_obs = cached.getDate().getSeconds() > cached.getDate().getSeconds() ? cached : o;
                    cached_obs.remove(o.getIdentifier());
                } else {
                    response_obs = o;
                }

                response.addObservations(response_obs);
            }
        }

        //adds observations from partial ids that are in cache but were not received from server
        if (!cached_obs.isEmpty()) {
            for (Observation o : cached_obs.values()) {
                response.addObservations(o);
            }
        }

        return response.build();
    }

    public TraceResponse coherentTrace(TraceResponse r, String id, Type t) {

        List<Observation> obs = r.getObservationsList();
        List<Observation> cached_obs = new ArrayList<Observation>();
        TraceResponse.Builder response = TraceResponse.newBuilder();
        List<Observation> responseList = new ArrayList<>();

        //gets all observations that match id and type in trails cache
        for (Observation o: trails) {
            if (o.getIdentifier().equals(id) && o.getType() == t) {
                cached_obs.add(o);
            }
        }

        Collections.reverse(cached_obs);
        if(!obs.isEmpty()) {
            for (int i = 0; i < obs.size(); ) {
                Observation received = obs.get(i);

                //if there are no more cached observations just add all observations received from incoherent response
                if (cached_obs.isEmpty()) {
                    responseList.add(received);
                    i++;
                    continue;
                }

                Observation cached = cached_obs.get(0);
                if (received.getDate().equals(cached.getDate()) && received.getName().equals(cached.getName())) {

                    //if cached observation is equal to observation received from incoherent response adds it only one time
                    responseList.add(cached);
                    cached_obs.remove(0);
                    i++;
                }
                else if (received.getDate().getSeconds() > cached.getDate().getSeconds()) {

                    //if cached observation older than received from incoherent response adds received observation and continues cycle
                    responseList.add(received);
                    i++;
                }
                else {

                    //if cached observation more recent than received from incoherent response adds cached observation and continues cycle
                    responseList.add(cached);
                    cached_obs.remove(0);
                }
            }
        }

        //adds all observations that were cached but were not in the incoherent response from server
        responseList.addAll(cached_obs);

        //also checks if there is any observation resultant of a cached spot observation that matches our id
        Observation cached = null;
        for (Observation o: spots) {
            if (o.getIdentifier().equals(id) && o.getType() == t) {
                cached = o;
                break;
            }
        }

        if (cached != null) {
            for (int i = 0; i < responseList.size(); i++) {
                if (cached.getDate().getSeconds() == responseList.get(i).getDate().getSeconds()) {

                    //if cached observation is equal to observation already in coherent answer returns coherent answer
                    return response.addAllObservations(responseList).build();
                }
                if (cached.getDate().getSeconds() > responseList.get(i).getDate().getSeconds()) {

                    //if cached observation is not in coherent answer, adds cached observation and returns coherent answer
                    responseList.add(i, cached);
                    return response.addAllObservations(responseList).build();
                }
            }

            responseList.add(cached);
        }

        //returns a coherent trace response
        return response.addAllObservations(responseList).build();
    }

    public String getPattern(String s) {

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
        }

        pattern = pattern + "\\Z";
        return pattern;
    }
}