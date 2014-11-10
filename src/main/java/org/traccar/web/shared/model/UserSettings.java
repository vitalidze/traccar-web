package org.traccar.web.shared.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
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
    @SequenceGenerator(name = "usersettings_id_seq", sequenceName = "usersettings_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usersettings_id_seq")
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

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

    private Integer zoomLevel;
    private Double centerLongitude;
    private Double centerLatitude;

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

    // Added hashCode() and equals() to conform to JPA.

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserSettings)) {
            return false;
        }
        UserSettings other = (UserSettings) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}
