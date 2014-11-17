package org.traccar.web.shared.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_settings")
public class UserSettings implements Serializable {

    private static final long serialVersionUID = 1;
    public static final short DEFAULT_TIME_PRINT_INTERVAL = 10;

    public static final int DEFAULT_ZOOM_LEVEL = 1;
    public static final double DEFAULT_CENTER_LONGITUDE = 12.5;
    public static final double DEFAULT_CENTER_LATITUDE = 41.9;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public UserSettings() {
        speedUnit = SpeedUnit.knots;
        timePrintInterval = DEFAULT_TIME_PRINT_INTERVAL;
        zoomLevel = DEFAULT_ZOOM_LEVEL;
        centerLongitude = DEFAULT_CENTER_LONGITUDE;
        centerLatitude = DEFAULT_CENTER_LATITUDE;
    }

    public enum SpeedUnit {
        knots("kn", 1d),
        kilometersPerHour("km/h", 1.852),
        milesPerHour("mph", 1.150779);

        final String unit;
        final double factor;

        SpeedUnit(String unit, double factor) {
            this.unit = unit;
            this.factor = factor;
        }

        public double getFactor() {
            return factor;
        }

        public String getUnit() {
            return unit;
        }

        public double toKnots(double speed) {
            return speed / factor;
        }
    }

    public enum MapType {
        OSM("OpenStreetMap"),
        GOOGLE_HYBRID("Google Hybrid"),
        GOOGLE_NORMAL("Google Normal"),
        GOOGLE_SATELLITE("Google Satellite"),
        GOOGLE_TERRAIN("Google Terrain"),
        BING_ROAD("Bing Road"),
        BING_HYBRID("Bing Hybrid"),
        BING_AERIAL("Bing Aerial");

        final String name;

        MapType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getBingKey() {
            return "AseEs0DLJhLlTNoxbNXu7DGsnnH4UoWuGue7-irwKkE3fffaClwc9q_Mr6AyHY8F";
        }
    }

    public enum DistanceUnit {
        km("km", 1d),
        mile("mi", 0.621371192);

        final String unit;
        final double factor;

        DistanceUnit(String unit, double factor) {
            this.unit = unit;
            this.factor = factor;
        }

        public double getFactor() {
            return factor;
        }

        public String getUnit() {
            return unit;
        }
    }

    @Enumerated(EnumType.STRING)
    @Expose
    private SpeedUnit speedUnit;

    public void setSpeedUnit(SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
    }

    public SpeedUnit getSpeedUnit() {
        return speedUnit;
    }

    /**
     * Interval of printing time on recorded trace in minutes based on position time
     */
    @Expose
    private Short timePrintInterval;

    public Short getTimePrintInterval() {
        return timePrintInterval;
    }

    public void setTimePrintInterval(Short timePrintInterval) {
        this.timePrintInterval = timePrintInterval;
    }

    @Expose
    private Integer zoomLevel;
    @Expose
    private Double centerLongitude;
    @Expose
    private Double centerLatitude;
    @Enumerated(EnumType.STRING)
    @Expose
    private MapType mapType;

    public Integer getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(Integer zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public Double getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(Double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }

    public Double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(Double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public MapType getMapType() {
        return mapType;
    }

    public void setMapType(MapType mapType) {
        this.mapType = mapType;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserSettings)) {
            return false;
        }

        UserSettings other = (UserSettings) object;

        return this.id == other.id;
    }
}
