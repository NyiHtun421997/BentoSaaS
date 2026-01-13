package com.nyihtuun.bentosystem.domain.valueobject;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public final class GeoPoint {

    private final double latitude;   // -90 .. +90
    private final double longitude;  // -180 .. +180

    public GeoPoint(double latitude, double longitude) {
        validate(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static GeoPoint of(double latitude, double longitude) {
        return new GeoPoint(latitude, longitude);
    }

    private static void validate(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90, but was: " + latitude
            );
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180, but was: " + longitude
            );
        }
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoPoint geoPoint)) return false;
        return Double.compare(geoPoint.latitude, latitude) == 0
                && Double.compare(geoPoint.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "GeoPoint{" +
                "lat=" + latitude +
                ", lon=" + longitude +
                '}';
    }
}
