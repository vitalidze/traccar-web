package org.traccar.web.client;

import org.traccar.web.shared.model.*;

import java.util.*;

public class ApplicationContext {

    private static final ApplicationContext context = new ApplicationContext();

    public static ApplicationContext getInstance() {
        return context;
    }

    private FormatterUtil formatterUtil;

    public void setFormatterUtil(FormatterUtil formatterUtil) {
        this.formatterUtil = formatterUtil;
    }

    public FormatterUtil getFormatterUtil() {
        if (formatterUtil == null) {
            formatterUtil = new FormatterUtil();
        }
        return formatterUtil;
    }

    private ApplicationSettings applicationSettings;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public ApplicationSettings getApplicationSettings() {
        if (applicationSettings != null) {
            return applicationSettings;
        } else {
            return new ApplicationSettings(); // default settings
        }
    }

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUserSettings(UserSettings userSettings) {
        if (user != null) {
            user.setUserSettings(userSettings);
        }
    }

    public UserSettings getUserSettings() {
        if (user != null && user.getUserSettings() != null) {
            return user.getUserSettings();
        } else {
            return new UserSettings(); // default settings
        }
    }

    private Map<Long, List<Position>> deviceTracks;
    public void trackDevice(Device device) {
        if (deviceTracks == null) {
            deviceTracks = new HashMap<Long, List<Position>>();
        }
        deviceTracks.put(device.getId(), new ArrayList<Position>());
    }

    public void stopTracking(Device device) {
        if (deviceTracks != null) {
            deviceTracks.remove(device.getId());
        }
    }

    public boolean isTracking(Device device) {
        return deviceTracks != null && deviceTracks.containsKey(device.getId());
    }

    public void addTrackingPosition(Position position) {
        deviceTracks.get(position.getDevice().getId()).add(position);
    }

    public List<Position> getTrackedPositions(Device device) {
        return deviceTracks.get(device.getId());
    }
}
