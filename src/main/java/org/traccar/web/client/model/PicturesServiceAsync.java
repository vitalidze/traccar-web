package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.shared.model.DeviceIcon;

import java.util.List;

public interface PicturesServiceAsync {
    void getMarkerPictures(AsyncCallback<List<DeviceIcon>> async);

    void addMarkerPicture(DeviceIcon marker, AsyncCallback<DeviceIcon> async);
}
