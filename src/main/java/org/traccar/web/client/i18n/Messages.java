/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client.i18n;

import org.traccar.web.shared.model.*;

public interface Messages extends com.google.gwt.i18n.client.Messages {
    String authentication();

    String user();

    String password();

    String login();

    String register();

    String save();

    String cancel();

    String globalSettings();

    String registration();

    String updateInterval();

    String device();

    String archive();

    String from();

    String to();

    String load();

    String clear();

    String valid();

    String time();

    String latitude();

    String longitude();

    String altitude();

    String speed();

    String course();

    String power();

    String name();

    String uniqueIdentifier();

    String devices();

    String add();

    String edit();

    String remove();

    String settings();

    String account();

    String preferences();

    String users();

    String global();

    String logout();

    String follow();

    String state();

    String attribute();

    String value();

    String address();

    String administrator();

    String map();

    String speedUnits();

    String error();

    String errNoResults();

    String errFillFields();

    String errDeviceExists();

    String errUsernameTaken();

    String confirm();

    String confirmUserRemoval();

    String errUsernameOrPasswordEmpty();

    String errInvalidUsernameOrPassword();

    String confirmDeviceRemoval();

    String errRemoteCall();

    String recordTrace();

    String timePrintInterval();

    String trackerServerLog();

    String refresh();

    String close();

    String logSize();

    String defaultMapState();

    String zoom();

    String takeFromMap();

    String day();

    String hour();

    String minute();

    String second();

    String ago(String dateTimeString);

    String meter();

    String manager();

    String share();

    String deviceTimeout();

    String offline();

    String since(String dateTimeString);

    String idle();

    String disallowDeviceManagementByUsers();

    String idleWhenSpeedIsLE();

    String distance();

    String exportToCSV();

    String exportToGPX();

    String changePassword();

    String enterNewPassword(String p0);

    String importData();

    String fileToImport();

    String log();

    String importingData();

    String defaultHashImplementation();

    String filter();

    String hideZeroCoordinates();

    String hideInvalidLocations();

    String hideDuplicates();

    String ignoreLocationsWithDistanceFromPreviousLT();

    String disableFilter();

    String server();

    String port();

    String secureConnectionType();

    String useAuthorization();

    String test();

    String notifications();

    String notificationSettings();

    String testFailed();

    String testSucceeded();

    String email();

    String invalidEmail();

    String fromAddress();

    String style();

    String fullPalette();

    String smallPalette();

    String standardMarkers();

    String reducedMarkers();

    String zoomToTrack();

    String exportData();

    String errNoDeviceNameOrId();

    String eventRecordingEnabled();

    String language();

    String readOnly();

    String protocol();

    String objects();

    String description();

    String geoFence();

    String type();

    String width();

    String radius();

    String color();

    String errGeoFenceIsEmpty();

    String confirmGeoFenceRemoval();

    String newGeoFence();

    String geoFenceType(@Select GeoFenceType type);

    String errSaveChanges();

    String applyToAllDevices();

    String deviceEventType(@Select DeviceEventType type);

    String event();

    String accessToken();

    String messageTemplates();

    String subject();

    String contentType();

    String placeholderDescription(@Select MessagePlaceholder placeholder);

    String defaultNotificationTemplate(@Select DeviceEventType type,
                                       @Optional String deviceName,
                                       @Optional String geoFenceName,
                                       @Optional String eventTime,
                                       @Optional String positionTime,
                                       @Optional String maintenanceName,
                                       @Optional String positionAddress,
                                       @Optional String positionLat,
                                       @Optional String positionLon,
                                       @Optional String positionAlt,
                                       @Optional String positionSpeed,
                                       @Optional String positionCourse);

    String noMarkers();

    String select();

    String defaultIcon();

    String selectedIcon();

    String offlineIcon();

    String upload();

    String confirmDeviceIconRemoval();

    String odometer();

    String km();

    String auto();

    String maintenance();

    String serviceName();

    String mileageInterval();

    String lastServiceMileage();

    String remaining();

    String overdue();

    String reset();

    String sensors();

    String parameter();

    String visible();

    String copyFrom();

    String intervals();

    String customIntervals();

    String intervalFrom();

    String text();

    String interval();

    String phoneNumber();

    String plateNumber();

    String vehicleBrandModelColor();

    String photo();

    String errUserAccountBlocked();

    String errUserAccountExpired();

    String errMaxNumberDevicesReached(String p0);

    String errUserSessionExpired();

    String errUserDisconnected();

    String firstName();

    String lastName();

    String companyName();

    String expirationDate();

    String maxNumOfDevices();

    String blocked();

    String overlays();

    String overlay();

    String overlayType(@Select UserSettings.OverlayType type);

    String snapToRoads();

    String period(@Select Period period);

    String periodComboBox_SelectPeriod();

    String traceInterval();

    String followedDeviceZoomLevel();

    String followedDeviceZoomLevelToolTip();

    String useCurrentZoomLevel();

    String errAccessDenied();

    String errMaxNumOfDevicesExceeded(int maxNumOfDevices);

    String timeZone();

    String bingMapsKey();

    String reports();

    String report();

    String timePeriod();

    String generate();

    String newReport();

    String reportType(@Select ReportType type);

    String confirmReportRemoval();

    String routeStart();

    String routeEnd();

    String routeLength();

    String moveDuration();

    String stopDuration();

    String topSpeed();

    String averageSpeed();

    String overspeedCount();

    String speedLimit();

    String status();

    String start();

    String end();

    String duration();

    String moving();

    String stopped();

    String stopPosition();

    String overspeedPosition();

    String geoFenceIn();

    String geoFenceOut();

    String geoFenceName();

    String geoFencePosition();

    String eventPosition();

    String totalOffline();

    String totalGeoFenceEnters();

    String totalGeoFenceExits();

    String totalMaintenanceRequired();

    String date();

    String totalMileage();

    String logs();

    String includeMap();

    String minIdleTime();

    String wrapperLog();

    String preview();

    String maximizeOverviewMap();

    String command();

    String send();

    String customMessage();

    String commandType(@Select CommandType commandType);

    String frequency();

    String groups();

    String newGroup();

    String confirmGroupRemoval();

    String group();

    String noGroup();

    String online();

    String message();

    String notificationExpiryPeriod();

    String loadingData();

    String icon();

    String showOnMap();

    String deviceIconMode(@Select DeviceIconMode mode);

    String iconRotation();

    String arrowColorMoving();

    String arrowColorPaused();

    String arrowColorStopped();

    String arrowColorOffline();

    String showName();

    String showProtocol();

    String showOdometer();

    String matchServiceURL();

    String errSnapToRoads(int code, String text);

    String arrowSize();

    String allowCommandsOnlyForAdmins();

    String defaultPreferences();

    String matchServiceType();
}
