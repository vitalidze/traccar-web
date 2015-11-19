package org.traccar.web.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "user_settings")
public class UserSettings implements IsSerializable {

    private static final long serialVersionUID = 1;
    public static final short DEFAULT_TIME_PRINT_INTERVAL = 10;

    public static final int DEFAULT_ZOOM_LEVEL = 1;
    public static final double DEFAULT_CENTER_LONGITUDE = 12.5;
    public static final double DEFAULT_CENTER_LATITUDE = 41.9;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @JsonIgnore
    private long id;

    public UserSettings() {
        speedUnit = SpeedUnit.knots;
        timePrintInterval = DEFAULT_TIME_PRINT_INTERVAL;
        zoomLevel = DEFAULT_ZOOM_LEVEL;
        centerLongitude = DEFAULT_CENTER_LONGITUDE;
        centerLatitude = DEFAULT_CENTER_LATITUDE;
        mapType = MapType.OSM;
        overlays = "GEO_FENCES,VECTOR,MARKERS";
    }

    public enum SpeedUnit implements IsSerializable {
        knots("kn", 1d, DistanceUnit.km),
        kilometersPerHour("km/h", 1.852, DistanceUnit.km),
        milesPerHour("mph", 1.150779, DistanceUnit.mile);

        final String unit;
        final double factor;
        final DistanceUnit distanceUnit;

        SpeedUnit(String unit, double factor, DistanceUnit distanceUnit) {
            this.unit = unit;
            this.factor = factor;
            this.distanceUnit = distanceUnit;
        }

        public double getFactor() {
            return factor;
        }

        public String getUnit() {
            return unit;
        }

        public DistanceUnit getDistanceUnit() {
            return distanceUnit;
        }

        public double toKnots(double speed) {
            return speed / factor;
        }
    }

    public enum MapType implements IsSerializable {
        OSM("OpenStreetMap"),
        GOOGLE_HYBRID("Google Hybrid"),
        GOOGLE_NORMAL("Google Normal"),
        GOOGLE_SATELLITE("Google Satellite"),
        GOOGLE_TERRAIN("Google Terrain"),
        BING_ROAD("Bing Road") { @Override public boolean isBing() { return true; } },
        BING_HYBRID("Bing Hybrid") { @Override public boolean isBing() { return true; } },
        BING_AERIAL("Bing Aerial") { @Override public boolean isBing() { return true; } },
        MAPQUEST_ROAD("MapQuest Road"),
        MAPQUEST_AERIAL("MapQuest Aerial"),
        STAMEN_TONER("Stamen Toner");

        final String name;

        MapType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isBing() {
            return false;
        }
    }

    public enum OverlayType implements IsSerializable {
        GEO_FENCES, VECTOR, MARKERS, SEAMARK
    }

    public enum DistanceUnit implements IsSerializable {
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
    private Short timePrintInterval;

    public Short getTimePrintInterval() {
        return timePrintInterval;
    }

    public void setTimePrintInterval(Short timePrintInterval) {
        this.timePrintInterval = timePrintInterval;
    }

    /**
     * Interval to record latest trace (in minutes)
     */
    private Short traceInterval;

    public Short getTraceInterval() {
        return traceInterval;
    }

    public void setTraceInterval(Short traceInterval) {
        this.traceInterval = traceInterval;
    }

    @JsonIgnore
    private String timeZoneId;

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    private Integer zoomLevel;
    private Double centerLongitude;
    private Double centerLatitude;
    @Enumerated(EnumType.STRING)
    private MapType mapType;

    @JsonIgnore
    private String overlays;

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

    public String getOverlays() {
        return overlays;
    }

    public void setOverlays(String overlays) {
        this.overlays = overlays;
    }

    @Column(nullable = true)
    @JsonIgnore
    private boolean hideZeroCoordinates;
    @Column(nullable = true)
    @JsonIgnore
    private boolean hideInvalidLocations;
    @Column(nullable = true)
    @JsonIgnore
    private boolean hideDuplicates;
    @JsonIgnore
    private Double minDistance;
    @JsonIgnore
    private String speedModifier;
    @JsonIgnore
    private Double speedForFilter;

    public boolean isHideZeroCoordinates() {
        return hideZeroCoordinates;
    }

    public void setHideZeroCoordinates(boolean hideZeroCoordinates) {
        this.hideZeroCoordinates = hideZeroCoordinates;
    }

    public boolean isHideInvalidLocations() {
        return hideInvalidLocations;
    }

    public void setHideInvalidLocations(boolean hideInvalidLocations) {
        this.hideInvalidLocations = hideInvalidLocations;
    }

    public boolean isHideDuplicates() {
        return hideDuplicates;
    }

    public void setHideDuplicates(boolean hideDuplicates) {
        this.hideDuplicates = hideDuplicates;
    }

    public Double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(Double minDistance) {
        this.minDistance = minDistance;
    }

    public String getSpeedModifier() {
        return speedModifier;
    }

    public void setSpeedModifier(String speedModifier) {
        this.speedModifier = speedModifier;
    }

    public Double getSpeedForFilter() {
        return speedForFilter;
    }

    public void setSpeedForFilter(Double speedForFilter) {
        this.speedForFilter = speedForFilter;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @JsonIgnore
    private PositionIconType archiveMarkerType;

    public PositionIconType getArchiveMarkerType() {
        return archiveMarkerType;
    }

    public void setArchiveMarkerType(PositionIconType archiveMarkerType) {
        this.archiveMarkerType = archiveMarkerType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<OverlayType> overlays() {
        if (getOverlays() == null) {
            return Collections.emptyList();
        }
        List<OverlayType> overlays = new LinkedList<>();
        for (String s : getOverlays().split(",")) {
            overlays.add(OverlayType.valueOf(s));
        }
        return overlays;
    }

    public void copyFrom(UserSettings userSettings) {
        id = userSettings.id;
        speedUnit = userSettings.speedUnit;
        timePrintInterval = userSettings.timePrintInterval;
        traceInterval = userSettings.traceInterval;
        timeZoneId = userSettings.timeZoneId;
        zoomLevel = userSettings.zoomLevel;
        centerLongitude = userSettings.centerLongitude;
        centerLatitude = userSettings.centerLatitude;
        mapType = userSettings.mapType;
        overlays = userSettings.overlays;
        hideZeroCoordinates = userSettings.hideZeroCoordinates;
        hideInvalidLocations = userSettings.hideInvalidLocations;
        hideDuplicates = userSettings.hideDuplicates;
        minDistance = userSettings.minDistance;
        speedModifier = userSettings.speedModifier;
        speedForFilter = userSettings.speedForFilter;
        archiveMarkerType = userSettings.archiveMarkerType;
    }
}
