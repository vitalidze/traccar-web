---
layout: default
---

After a long timeout I am announcing new release v0.8.1. Mostly it contains numerous bug fixes as well as couple of medium features:

* updated translations to [Spanish](/features/spanish.html) language
* added translation to the [Persian](/features/persian.html) language
* added new period drop-down list, which allows to quickly select period for archive generation (like day, week, month, etc.)
* improved snap-to-roads function. Now it runs on client side and what's more important loads track geometry
* URLs to resources are updated to be relative to the deployment path. It allows to deploy service to non-root (i.e. '/') URL
* added new 'display archive' setting to the Settings>>Users screen. It allows to disable archive for a user. In such case the whole archive panel will not be hidden from the user as well as the API call will be restricted.
* updated german translations to remove 'Unknown overlay' strings for layers, which titles don't have appropriate translations
* now trace recording has new setting called 'time interval' in Settings>>Preferences (in minutes). If set then after that time passes the tail of recorded trace will be cut to make sure that only 'X' minutes of trace are shown
* 'Share' dialog both for Devices and GeoFences objects will be hidden if saving completed with success
* implemented parsing of JSON in 'other' column (see [#266](https://github.com/vitalidze/traccar-web/issues/266)) because now it is the default format in traccar since 3.0. Old XML is supported as well
* set up MD5 for all new installations by default. Added global 'salt' that will be now used for all passwords (see [#260](https://github.com/vitalidze/traccar-web/issues/260))
* implemented registration in mobile UI (for [#257](https://github.com/vitalidze/traccar-web/issues/257))
* added new fields from positions according to data model of traccar 3.x: `server time` and `protocol`.
* updated maximum devices settings and access checks according to [#214](https://github.com/vitalidze/traccar-web/issues/214), [#215](https://github.com/vitalidze/traccar-web/issues/215) and discussion in [#252](https://github.com/vitalidze/traccar-web/pull/252).
* fixed [#246](https://github.com/vitalidze/traccar-web/issues/246) - uploaded marker icon cannot be removed when there are devices with selected icons in profile
* fixed [#234](https://github.com/vitalidze/traccar-web/issues/234) - UI crashed when no overlays are selected in user profile
* fixed [#238](https://github.com/vitalidze/traccar-web/issues/238) - always bring latest position of selected device to the front
* fixed [#249](https://github.com/vitalidze/traccar-web/issues/249) - google maps API was changed and broken compatibility with openlayers. Fixed by forcing use of previous version of google maps API as suggested [here](http://stackoverflow.com/questions/32335221/google-maps-api-change)

Hope you will enjoy this new release.

For the brave ones that want to contribute their translations here is the list of strings that were added since previous release (on 14th July 2015, lines starting with '+' were added):

    @@ -6,6 +6,7 @@ name = Name
     error = Error
     confirm = Confirm
     errRemoteCall = Remote procedure call error
    +errAccessDenied = Access denied
     # login dialog
     authentication = Authentication
     language = Language
    @@ -99,6 +100,7 @@ address = Address
     # user dialog
     administrator = Administrator
     errUsernameTaken = Username is already taken
    +errMaxNumOfDevicesExceeded = Maximum number of devices should not exceed {0}
     manager = Manager
     event = Event
     deviceEventType = Unknown event
    @@ -120,6 +122,7 @@ map = Map
     # user settings dialog
     speedUnits = Speed Units
     timePrintInterval = Time stamps print interval
    +traceInterval = Trace recording interval
     defaultMapState = Default map state
     zoom = Zoom
     takeFromMap = Take from map
    @@ -223,4 +226,13 @@ intervals = Intervals
     customIntervals = Custom Intervals
     intervalFrom = Interval from
     text = Text
    +# periodComboBox
    +periodComboBox_today = Today
    +periodComboBox_Yesterday = Yesterday
    +periodComboBox_ThisWeek = This week
    +periodComboBox_PreviousWeek = Previous week
    +periodComboBox_ThisMonth = This month
    +periodComboBox_PreviousMonth = Previous month
    +periodComboBox_Custom = Custom
    +periodComboBox_SelectPeriod = Select period