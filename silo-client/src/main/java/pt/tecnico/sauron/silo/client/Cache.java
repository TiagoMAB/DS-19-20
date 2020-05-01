package pt.tecnico.sauron.silo.client;

import pt.tecnico.sauron.silo.grpc.Observation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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

        //if cache limit is reached, removes old information from cache
        if (spots.size() == limit) {
            spots.remove(0);
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

        //searches for every observation with id already in cache
        while (it.hasNext()) {
            Observation o = (Observation) it.next();

            if (o.getIdentifier().equals(observationList.get(0).getIdentifier())) {
                removeList.add(o);
            }
        }

        //removes old information from cache
        trails.removeAll(removeList);

        //if cache limit is reached, removes old information from cache
        while(trails.size() + observationList.size() > limit) {
            trails.remove(0);
        }

        //adds new observations to the cache
        trails.addAll(observationList);

        System.out.println(trails);
    }

    public void addSpotObservations(List<Observation> observationList) {

        for (Observation o: observationList) {
            addObservation(o);
        }
        System.out.println(spots);
    }
}