package pt.tecnico.sauron.silo.client;

import pt.tecnico.sauron.silo.grpc.Observation;
import pt.tecnico.sauron.silo.grpc.TraceResponse;
import pt.tecnico.sauron.silo.grpc.TrackMatchResponse;
import pt.tecnico.sauron.silo.grpc.TrackResponse;

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

    public int getLimit() {
        return limit;
    }

    public List<Observation> getTrails() {
        return trails;
    }

    public List<Observation> getSpots() {
        return spots;
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

        //if obtained list is empty doesn't update cache
        if (observationList.isEmpty()) {
            return;
        }

        Iterator it = trails.iterator();
        List<Observation> removeList = new ArrayList<>();
        List<Observation> addList = new ArrayList<>();

        //searches for every observation with traced id already in cache
        while (it.hasNext()) {
            Observation o = (Observation) it.next();

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

        for (Observation o: observationList) {
            addObservation(o);
        }

    }

    public TrackResponse coherentTrack(TrackResponse r, String id) {

        Observation obs = r.getObservation();
        Observation cached_obs = null;

        for (Observation o: spots) {
            if (o.getIdentifier().equals(id)) {
                cached_obs = o;
            }
        }

        if (cached_obs == null) {
            for (Observation o: trails) {
                if (o.getIdentifier().equals(id)) {
                    cached_obs = o;
                }
            }
        }

        if (cached_obs != null && obs != Observation.getDefaultInstance()) {                    //TODO: check if adds info to cache when track is not coherent (ts_vector diff)
            Observation response_obs = cached_obs.getDate().getSeconds() > obs.getDate().getSeconds() ? cached_obs : obs;
            return TrackResponse.newBuilder().setObservation(response_obs).build();
        }
        else if (cached_obs != null){
            return TrackResponse.newBuilder().setObservation(cached_obs).build();
        }
        else {
            return r;
        }

    }

    public TrackMatchResponse coherentTrackMatch(TrackMatchResponse r, String partialId) {

        List<Observation> obs = r.getObservationsList();
        HashMap<String, Observation> cached_obs = new HashMap<String, Observation>();
        TrackMatchResponse.Builder response = TrackMatchResponse.newBuilder();

        String pattern = getPattern(partialId);

        for (Observation o: spots) {
            if (o.getIdentifier().matches(pattern)) {
                cached_obs.put(o.getIdentifier(), o);
            }
        }

        for (Observation o: trails) {
            if (o.getIdentifier().matches(pattern) &&
                    (cached_obs.get(o.getIdentifier()) == null || o.getDate().getSeconds() > cached_obs.get(o.getIdentifier()).getDate().getSeconds())) {
                cached_obs.put(o.getIdentifier(), o);
            }
        }

        if (!obs.isEmpty()) {
            for (Observation o : obs) {
                Observation cached = cached_obs.get(o.getIdentifier());
                Observation response_obs;

                if (cached != null) {
                    response_obs = cached.getDate().getSeconds() > cached.getDate().getSeconds() ? cached : o;
                    cached_obs.remove(o.getIdentifier());
                } else {
                    response_obs = o;
                }

                response.addObservations(response_obs);
            }
        }

        if (!cached_obs.isEmpty()) {
            for (Observation o : cached_obs.values()) {
                response.addObservations(o);
            }
        }

        return response.build();            //TODO: check if doesn't need default instance when no observations are found
    }

    public TraceResponse coherentTrace(TraceResponse r, String id) {

        List<Observation> obs = r.getObservationsList();
        List<Observation> cached_obs = new ArrayList<Observation>();
        TraceResponse.Builder response = TraceResponse.newBuilder();
        List<Observation> responseList = new ArrayList<>();

        for (Observation o: trails) {
            if (o.getIdentifier().equals(id)) {
                cached_obs.add(o);
            }
        }

        Collections.reverse(cached_obs);
        if(!obs.isEmpty()) {
            for (int i = 0; i < obs.size(); ) {
                Observation received = obs.get(i);

                if (cached_obs.isEmpty()) {
                    responseList.add(received);
                    i++;
                    continue;
                }

                Observation cached = cached_obs.get(0);
                if (received.getDate().equals(cached.getDate()) && received.getName().equals(cached.getName())) {
                    responseList.add(cached);
                    cached_obs.remove(0);
                    i++;
                }
                else if (received.getDate().getSeconds() > cached.getDate().getSeconds()) {
                    responseList.add(received);
                    i++;
                }
                else {
                    responseList.add(cached);
                    cached_obs.remove(0);
                }
            }
        }

        responseList.addAll(cached_obs);
        Observation cached = null;

        for (Observation o: spots) {
            if (o.getIdentifier().equals(id)) {
                cached = o;
                break;
            }
        }

        if (cached != null) {
            for (int i = 0; i < responseList.size(); i++) {
                if (cached.getDate().getSeconds() == responseList.get(i).getDate().getSeconds()) {
                    return response.addAllObservations(responseList).build();
                }
                if (cached.getDate().getSeconds() > responseList.get(i).getDate().getSeconds()) {
                    responseList.add(i, cached);
                    return response.addAllObservations(responseList).build();
                }
            }

            responseList.add(cached);
        }

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