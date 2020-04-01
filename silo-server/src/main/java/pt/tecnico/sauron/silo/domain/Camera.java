package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraNameException;

public class Camera {

    private String name;
    private double latitude, longitude;

    public Camera(String name, double latitude, double longitude) throws InvalidCameraNameException {
        setName(name);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) throws InvalidCameraNameException {

        if (name.length() >= 3 && name.length() <= 15 && name.matches("[A-Za-z0-9]+")) {
            this.name = name;
        }
        else {
            throw new InvalidCameraNameException(name);
        }

    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
