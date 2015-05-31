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
package org.traccar.web.shared.model;

import com.google.gson.annotations.Expose;
import com.google.gwt.user.client.rpc.IsSerializable;

public class UserSettingsDTO implements IsSerializable {
    public static final short DEFAULT_TIME_PRINT_INTERVAL = 10;

    public static final int DEFAULT_ZOOM_LEVEL = 1;
    public static final double DEFAULT_CENTER_LONGITUDE = 12.5;
    public static final double DEFAULT_CENTER_LATITUDE = 41.9;

    private long id;

    @Expose
    private SpeedUnit speedUnit;
    @Expose
    private Short timePrintInterval;
    @Expose
    private Integer zoomLevel;
    @Expose
    private Double centerLongitude;
    @Expose
    private Double centerLatitude;
    @Expose
    private MapType mapType;

    private PositionIconType archiveMarkerType;

    private boolean hideZeroCoordinates;
    private boolean hideInvalidLocations;
    private boolean hideDuplicates;
    private Double minDistance;
    private String speedModifier;
    private Double speedForFilter;

    public UserSettingsDTO() {
        speedUnit = SpeedUnit.knots;
        timePrintInterval = DEFAULT_TIME_PRINT_INTERVAL;
        zoomLevel = DEFAULT_ZOOM_LEVEL;
        centerLongitude = DEFAULT_CENTER_LONGITUDE;
        centerLatitude = DEFAULT_CENTER_LATITUDE;
        mapType = MapType.OSM;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SpeedUnit getSpeedUnit() {
        return speedUnit;
    }

    public void setSpeedUnit(SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
    }

    public MapType getMapType() {
        return mapType;
    }

    public void setMapType(MapType mapType) {
        this.mapType = mapType;
    }

    public Short getTimePrintInterval() {
        return timePrintInterval;
    }

    public void setTimePrintInterval(Short timePrintInterval) {
        this.timePrintInterval = timePrintInterval;
    }

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

    public PositionIconType getArchiveMarkerType() {
        return archiveMarkerType;
    }

    public void setArchiveMarkerType(PositionIconType archiveMarkerType) {
        this.archiveMarkerType = archiveMarkerType;
    }
}
