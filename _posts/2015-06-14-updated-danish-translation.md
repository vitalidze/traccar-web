---
layout: default
---

Jens-Jørgen Rask kindly updated [danish translation](/features/danish.html) and also translated the mobile UI.

For the brave ones that want to contribute their translations here is the list of strings that were added since previous release (on 15th May 2015, lines starting with '+' were added):

     register = Register
     errUsernameOrPasswordEmpty = User name and password must not be empty
     errInvalidUsernameOrPassword = User name or password is invalid
    +errUserAccountBlocked = User account is blocked
    +errUserAccountExpired = User account expired
     # application settings dialog
     globalSettings = Global Settings
     registration = Registration
     @@ -51,6 +53,21 @@ power = Power
     uniqueIdentifier = Unique Identifier
     deviceTimeout = Timeout
     idleWhenSpeedIsLE = Idle when speed is <=
    +select = Select
    +odometer = Odometer
    +km = km
    +auto = Auto
    +copyFrom = Copy from
    +phoneNumber = Phone number
    +plateNumber = Plate number
    +vehicleBrandModelColor = Vehicle brand / model / color
    +photo = Photo
    +# device markers dialog
    +defaultIcon = Default
    +selectedIcon = Selected
    +offlineIcon = Offline
    +upload = Upload
    +confirmDeviceIconRemoval = Are you sure you want to remove marker icon?
     # device view
     objects = Objects
     devices = Devices
     @@ -70,6 +87,7 @@ logout = Logout
     follow = Follow
     recordTrace = Rec. trace
     errDeviceExists = Device with this Unique ID already exists
    +errMaxNumberDevicesReached = Limit on number of devices is reached (max {0})
     confirmDeviceRemoval = Are you sure you want remove device?
     # state view
     state = State
     @@ -85,6 +103,7 @@ deviceEventType = Unknown event
     deviceEventType[OFFLINE] = Went offline
     deviceEventType[GEO_FENCE_ENTER] = Enter geo-fence
     deviceEventType[GEO_FENCE_EXIT] = Exit geo-fence
    +deviceEventType[MAINTENANCE_REQUIRED] = Maintenance required
     # users dialog
     confirmUserRemoval = Are you sure you want remove user?
     readOnly = Read only
     @@ -142,10 +161,12 @@ placeholderDescription[deviceName] = Name of device
     placeholderDescription[geoFenceName] = Name of geo-fence
     placeholderDescription[eventTime] = Date and time when event was recorded
     placeholderDescription[positionTime] = Date and time of associated position
    +placeholderDescription[maintenanceName] = Name of maintenance service
     defaultNotificationTemplate = Unknown message
     defaultNotificationTemplate[OFFLINE] = Device ''{1}'' went offline at {3}
     defaultNotificationTemplate[GEO_FENCE_ENTER] = Device ''{1}'' entered geo-fence ''{2}'' at {4}
     defaultNotificationTemplate[GEO_FENCE_EXIT] = Device ''{1}'' exited geo-fence ''{2}'' at {4}
    +defaultNotificationTemplate[MAINTENANCE_REQUIRED] = ''{1}'' requires maintenance ''{5}''
     # Archive style menu
     style = Style
     fullPalette = Full palette
     @@ -170,4 +191,22 @@ geoFenceType[LINE] = Line
     geoFenceType[CIRCLE] = Circle
     geoFenceType[POLYGON] = Polygon
     errSaveChanges = Please save changes
    -applyToAllDevices = Apply to all devices 
    +applyToAllDevices = Apply to all devices
    +# Maintenance
    +maintenance = Maintenance
    +serviceName = Service name
    +mileageInterval = Mileage interval
    +lastServiceMileage = Last service
    +remaining = Remaining
    +overdue = Overdue
    +reset = Reset
    +# Sensors
    +sensors = Sensors
    +parameter = Parameter
    +visible = Visible
    +intervals = Intervals
    +# Sensor intervals dialog
    +customIntervals = Custom Intervals
    +intervalFrom = Interval from
    +text = Text
    +interval = Interval 
