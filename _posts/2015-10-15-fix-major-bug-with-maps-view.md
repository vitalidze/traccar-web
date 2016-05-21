---
layout: news
---

This release contains fix for a major bug [#284](https://github.com/vitalidze/traccar-web/issues/284), which was originally posted [here](https://github.com/openlayers/openlayers/issues/1461). The reason is that the hard coded key for the Bing Maps was expired, which break the whole [OpenLayers library](http://openlayers.org/), which is used for mapping in Traccar Web UI. To overcome this issue:

* hard coded Bing Maps key was removed both from desktop and mobile versions
* added new setting "Bing Maps key" to the Settings>>Global menu, where one can set up his own Bing Maps Key.    Key may be obtained from the Bing Maps account on the [official page](https://www.bingmapsportal.com/). To remove the Bing Maps Key leave the "Bing Maps key" field in Settings>>Global menu empty.
* if no Bing Maps key present then Bing Maps won't be available
* same key will be used for mobile UI
* if user had Bing Maps selected as the default layer, but there are no Bing Maps key, then system will fall back to OpenStreetMap. This works both in mobile in desktop version.

Also this release contains:

* updated [Russian](/features/russian.html) translation
* updated [Portuguese (Brazillian)](/features/portuguese-brazilian.html) translation
* updated [French](/features/french.html) translation
* new method to detect if device is offline by the `serverTime` field (see [#256](https://github.com/vitalidze/traccar-web/issues/256)). This is useful when device does not update position when there are no GPS signal, but it still has GSM connection and thus may be considered online.
* new 'Time zone' setting in Settings>>Preferences. For now the timezone is used to format dates in notification messages so they will have the time local to the user's time zone. Time zone is detected automatically when user logs in to the desktop version.
* 'Loading application...' block showing until GWT javascript is loaded