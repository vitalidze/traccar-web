---
layout: default
---

I am pleased to announce new release, which contains multiple new features:

* [snap-to-roads](/features/snap-to-roads.html) function in archive loading (ticket [#193](https://github.com/vitalidze/traccar-web/issues/193)).
* two new mapping providers were added [MapQuest](http://www.mapquest.com/) and [Stamen toner](http://maps.stamen.com/toner/) (ticket [#203](https://github.com/vitalidze/traccar-web/issues/203))
* [OpenSeaMap](http://openseamap.org/) seamarks layer added (ticket [#203](https://github.com/vitalidze/traccar-web/issues/203))
* possibility to save selected layers in user's preferences
* [serbian translation](/features/serbian.html) (thanks to [mpele](https://github.com/mpele) github user)
* [hebrew translation](/features/hebrew.html) (LTR, thanks to Alon Ben-Zvi)

Also this release contains bug fixes:
 
* for RESTful API (fixed processing of JSON objects in request body), updated [documentation](/features/rest-api.html) to contain example on how to add device
* for password resetting by 'manager' user (ticket [#202](https://github.com/vitalidze/traccar-web/issues/202))

For the brave ones that want to contribute their translations here is the list of strings that were added since previous release (on 24th June 2015, lines starting with '+' were added):

    @@ -30,6 +30,7 @@ defaultHashImplementation = Default password hash
     archive = Archive
     from = From
     to = To
    +snapToRoads = Snap to roads
     load = Load
     exportToCSV = Export to CSV
     exportToGPX = Export to GPX
    @@ -73,7 +74,6 @@ confirmDeviceIconRemoval = Are you sure you want to remove marker icon?
     # device view
     objects = Objects
     devices = Devices
    -geoFences = Geo-fences
     add = Add
     edit = Edit
     share = Share
    @@ -123,7 +123,13 @@ timePrintInterval = Time stamps print interval
     defaultMapState = Default map state
     zoom = Zoom
     takeFromMap = Take from map
    -seamark = Seamark
    +overlays = Overlays
    +overlay = Overlay
    +overlayType = Unknown overlay
    +overlayType[GEO_FENCES] = Geo-fences
    +overlayType[VECTOR] = Vector
    +overlayType[MARKERS] = Markers
    +overlayType[SEAMARK] = Seamark
     # tracker server log view dialog
     refresh = Refresh
     close = Close
    @@ -180,7 +186,6 @@ defaultNotificationTemplate[MAINTENANCE_REQUIRED] = ''{1}'' requires maintenance
     style = Style
     fullPalette = Full palette
     smallPalette = Small palette
    -markers = Markers
     noMarkers = No markers
     standardMarkers = Standard markers
     reducedMarkers = Reduced markers