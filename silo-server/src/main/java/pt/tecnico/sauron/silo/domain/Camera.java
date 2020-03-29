package pt.tecnico.sauron.silo.domain;

public class Camera {

    private String name;
    private Place place;

    public Camera(String name, Place place) {
        this.name = name;
        this.place = place;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Place getPlace() {
        return this.place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}
