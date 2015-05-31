/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.entity;

import org.traccar.web.shared.model.MapType;
import org.traccar.web.shared.model.PositionIconType;
import org.traccar.web.shared.model.SpeedUnit;
import org.traccar.web.shared.model.UserSettingsDTO;

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
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public static UserSettings defaults() {
        return new UserSettings().from(new UserSettingsDTO());
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
    @Enumerated(EnumType.STRING)
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

    @Column(nullable = true)
    private boolean hideZeroCoordinates;
    @Column(nullable = true)
    private boolean hideInvalidLocations;
    @Column(nullable = true)
    private boolean hideDuplicates;
    private Double minDistance;
    private String speedModifier;
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

    public UserSettingsDTO dto() {
        UserSettingsDTO dto = new UserSettingsDTO();
        dto.setId(getId());
        dto.setSpeedUnit(getSpeedUnit());
        dto.setMapType(getMapType());
        dto.setCenterLongitude(getCenterLongitude());
        dto.setCenterLatitude(getCenterLatitude());
        dto.setZoomLevel(getZoomLevel());
        dto.setTimePrintInterval(getTimePrintInterval());
        dto.setHideZeroCoordinates(isHideZeroCoordinates());
        dto.setHideInvalidLocations(isHideInvalidLocations());
        dto.setHideDuplicates(isHideDuplicates());
        dto.setMinDistance(getMinDistance());
        dto.setSpeedModifier(getSpeedModifier());
        dto.setSpeedForFilter(getSpeedForFilter());
        dto.setArchiveMarkerType(getArchiveMarkerType());
        return dto;
    }

    public UserSettings from(UserSettingsDTO dto) {
        setSpeedUnit(dto.getSpeedUnit());
        setMapType(dto.getMapType());
        setCenterLongitude(dto.getCenterLongitude());
        setCenterLatitude(dto.getCenterLatitude());
        setZoomLevel(dto.getZoomLevel());
        setTimePrintInterval(dto.getTimePrintInterval());
        setHideZeroCoordinates(dto.isHideZeroCoordinates());
        setHideInvalidLocations(dto.isHideInvalidLocations());
        setHideDuplicates(dto.isHideDuplicates());
        setMinDistance(dto.getMinDistance());
        setSpeedModifier(dto.getSpeedModifier());
        setSpeedForFilter(dto.getSpeedForFilter());
        setArchiveMarkerType(dto.getArchiveMarkerType());
        return this;
    }
}
