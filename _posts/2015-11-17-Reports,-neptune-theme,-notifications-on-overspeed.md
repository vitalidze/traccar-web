---
layout: news
---

I am pleased to announce a new major release. The main big features here:

* [reports](/features/reports.html)
* neptune theme (looks like the current theme of original traccar web UI 3.x)
* redesign of the application menu - added navigation bar at the top
* new [notification](/features/notifications.html) event type 'Overspeed'

Also there were several minor features added:

* possibility to display contents of `wrapper.log` file. Previously it was only possible to view contents of "backend" server log only (file named `tracker-server.log`)
* now polygon geo-fence is selected by default. This is to avoid confustion between the 'line' and 'polygon' types, which produced lots of questions and misunderstandings because 'line' was the default selection.
* offline devices detection now should be much more lightweight and scalable since it will not scan all posted offline events
* redesigned 'Import' screen, which now contains the drop-down list to select destination device. Previously it took device selection from the 'Archive' toolbar. I believe now Import is more convenient.
* other events (geo-fence enter/exit, maintenace, overspeed) calculation was slightly rewritten, which should make it both more lightweight and extensible in future
* updated [spanish](/features/spanish.html) translation (thanks to Miguel Abad)
* updated [french](/features/french.html) translation (thanks to [toshdi](https://github.com/toshdi) github user and Igor)
* updated [hungarian](/features/hungarian.html) translation (thanks to Balogh Zoltán)
* added [greek](/features/greek.html) translation (thanks to [gsiotas](https://github.com/gsiotas) github user)
* added new settings to the device profile: 
    * 'Min. idle time' - used to calculate device status both in live data and on reports. Device will not be considered idle until it's speed is less than the defined value for the specified duration
    * 'Speed limit' - used to produce 'Overspeed' events/notifications and for reports calculation
* skip logging of error when unauthenticated user opens web UI ([#289](https://github.com/vitalidze/traccar-web/issues/289))

Bugs fixed:

* [#324](https://github.com/vitalidze/traccar-web/issues/324) - when multiple devices tracks are drawn on map the style of the last loaded track was overriding styles of all existing tracks
* snap to roads function was not working when current user locale uses number format with comma as the decimal separator

Also there are two more major things related to the project:

1) I have updated [NetBeans](/ide/netbeans.html) project setup instructions. Now anyone should be able to run project from that IDE.

2) The translations are now reside on [transifex](https://www.transifex.com/traccar-web-ui-mod/traccar-web/) project. Everyone interested in updating existing/submitting new translations are welcome to the web site. Of course, it is still possible to send translations as the text files by email. But the project on transifex is the preferred and I believe simpler way to help with translation.