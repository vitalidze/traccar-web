---
layout: news
---

I am happy to announce next major release. 

It contains one of the most requested feature (based on likes from [roadmap](/roadmap.html)) - the device direction on map. Also it contains the following improvements:

* show idle (red square), paused (two blue rectangles, like on any video player) and movement icons (no overlay icon)
* possibility to rotate icon based on current direction. Basic icon should be faced to the north, otherwise results may be weird.
* new mode for displaying device on map: arrow. When selected in device profile icon will appear only in popup, but on the map there will be an arrow pointing to the current direction of the device. There is a major difference in how the arrow is displayed comparing to the icon: the actual position is in the geometrical center of an arrow, while for icon the actual position is in the bottom of icon vertically and in the middle of it horizontally (see default marker icon for better understanding, it's bottom tail points to the actual position). You can also configure fill color of the arrow to display based on the current status of device. This works both in Desktop and Mobile versions.
* pause and stop signs will appear on the points from the loaded track from archive
* direction arrows will be shown in the archive track for every point which have a time-stamp printed above it. This is configurable in "Preferences" with the `Time stamps print interval` setting (in minutes)
* added option to hide device name from the map ([#209](https://github.com/vitalidze/traccar-web/issues/209))
* added option to hide 'protocol' and 'odometer' from the popup ([#591](https://github.com/vitalidze/traccar-web/issues/591), [#318](https://github.com/vitalidze/traccar-web/issues/318))

Performance improvements and bug fixes:

* there is a major performance improvement in loading of latest positions to the map. Previously right after logging in there were a big resource consuming query to load latest positions, where device was moving. This was scaling badly. Now such query was removed at all and idle/movement status calculation for the latest position was moved to the server side. Server just tracks a last moving positions by a separate background thread.
* sorting of devices was moved to the server side. Previously after introducing the new groups hierarchy functionality devices weren't sorted at all. Now it was fixed and actually should perform even better.
* show more descriptive error for the command sending ([#659](https://github.com/vitalidze/traccar-web/issues/659))
* when user tried to remove the 'automatic zoom' functionality by leaving the field empty in 'Preferences' screen it would be restored to default value after server restart. This was removed, but it affects migration from old databases, so now only newly added users should have the default automatic zoom setting.

Updated translations from transifex web site:

* Bulgarian
* German
* Greek
* Spanish
* Finish
* French
* Hungarian 
* Portuguese (brasil)

Added new Indonesian translation.