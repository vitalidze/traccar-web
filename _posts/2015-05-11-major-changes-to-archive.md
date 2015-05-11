---
layout: default
---

Archive UI and logic was slightly rewritten:
 
* to improve scalability now there is a new marker type option - 'No markers', which becomes the default one. In such case only track line with time-stamps is shown on map and when mouse comes close enough to the track it will show closest position along with details in popup.
 
* added possibility to draw multiple device tracks at the same time (feature request [#47](https://github.com/vitalidze/traccar-web/issues/47)). Table for each device resides in it's own tab. When tab is closed track disappears from map as well. 'Clear' button on archive toolbar closes all loaded tracks.

* marker type selection is now saved to the database in user's settings

Other changes include:

* [bulgarian translation](/features/bulgarian.html) - kindly provided by Boko Kid
* change in MIME type of GPX export to `application/gpx+xml`, which allows to open exported file directly in associated application (like garmin or JOSM)