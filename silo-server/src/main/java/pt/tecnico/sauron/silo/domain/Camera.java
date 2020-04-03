package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraNameException;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidCoordinateException;

public class Camera {

    private String name;
    private double latitude, longitude;

    public Camera(String name, double latitude, double longitude) throws InvalidCameraNameException, InvalidCoordinateException {
        setName(name);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) throws InvalidCameraNameException {

        if (!name.isBlank() && name.length() >= 3 && name.length() <= 15 && name.matches("[A-Za-z0-9]+")) {
            this.name = name;
        }
        else {
            throw new InvalidCameraNameException(name);
        }

    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) throws InvalidCoordinateException {
        if (latitude >= -90 && latitude <= 90) {
            this.latitude = latitude;
        }
        else {
            throw new InvalidCoordinateException(latitude);
        }
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) throws InvalidCoordinateException {
        if (longitude >= -180 && longitude <= 180) {
            this.longitude = longitude;
        }
        else {
            throw new InvalidCoordinateException(longitude);
        }
    }

    @Override
    public String toString() {
        return "Camera named \"" + this.name + "\" located at (" + this.latitude + " | " + this.longitude + ")";
    }
}
