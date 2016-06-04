---
layout: news
---

This small minor release contains following features:

* possibility to set up color for a text of sensor interval. This way it can be highlighted.
* possibility to set up icon for a sensor interval. May be useful to display battery value.
* read-only user can now update his preferences (speed units, default map position, etc., see [#693](https://github.com/vitalidze/traccar-web/issues/693))
* possibility to define default preferences (in Settings >> Default Preferences menu, see [#693](https://github.com/vitalidze/traccar-web/issues/693))
* performance improvement for the latest positions loading. Now it is made more lightweight - the icons won't be re-created on the map, instead of this their position and style is updated. Previously they were always first removed and then drawn again.

Bugs fixed:

* sometimes devices may be duplicated in a list ([#696](https://github.com/vitalidze/traccar-web/issues/696))
* devices may be duplicated on map ([#696](https://github.com/vitalidze/traccar-web/issues/696)). I couldn't find the real reason, but improvements in "live" data rendering might fix the issue
* when report is opened from archive toolbar and "CUSTOM" period is selected it wasn't preset in the opened report (feedback on [#402](https://github.com/vitalidze/traccar-web/issues/402))
* idle/movement detector background task was working with database incorrectly, which could lead to incorrect results in "Idle" status calculation ([#708](https://github.com/vitalidze/traccar-web/issues/708))

Updated Spanish translation from [transifex web site](https://www.transifex.com/traccar-web-ui-mod/traccar-web/dashboard/).