package com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper;

import com.nyihtuun.bentosystem.domain.valueobject.GeoPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeoPointMapper {

    private static final int SRID = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), SRID);

    private GeoPointMapper() {}

    public static Point toPoint(GeoPoint geoPoint) {
        if (geoPoint == null) {
            return null;
        }

        Point point = GEOMETRY_FACTORY.createPoint(
                new Coordinate(
                        geoPoint.getLongitude(), // x = longitude
                        geoPoint.getLatitude()  // y = latitude
                )
        );
        point.setSRID(SRID);
        return point;
    }
}
