package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.shared.model.DeviceIconDTO;

import java.util.List;

public interface PicturesServiceAsync {
    void getMarkerPictures(AsyncCallback<List<DeviceIconDTO>> async);

    void addMarkerPicture(DeviceIconDTO marker, AsyncCallback<DeviceIconDTO> async);

    void updateMarkerPicture(DeviceIconDTO marker, AsyncCallback<DeviceIconDTO> async);

    void removeMarkerPicture(DeviceIconDTO marker, AsyncCallback<Void> async);
}
