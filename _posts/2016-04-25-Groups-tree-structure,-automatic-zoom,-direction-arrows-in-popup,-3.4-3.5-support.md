---
layout: news
---

I am pleased to announce a major release. It contains several new functions:

* tree structure for groups. Now every group may have a parent group. It is set up with drag and drop. Due to restrictions of the tree-grid component sorting was disabled.
* support new command types from backend
* allow text selection in archive. Now it is possible to copy values from columns in archive grid
* automatically zoom to selected position. Zoom level parameter is controlled in "Preferences" menu. By default it is enabled and zoom level set to 16. If you find this function annoying then just disable it by leaving value blank in "Preferences" menu ([#607](https://github.com/vitalidze/traccar-web/issues/607))
* arrow icon in popup menu based on `course` value from the position. Works both in archive and for the current position. (thanks to [mpele](https://github.com/mpele) github user)
* new setting - notification expiration period in Settings>>Global. If for some reason notification wasn't sent within that period then it is marked as "expired" and won't be sent anymore. Enabled by default, value is 12 hours (720 minutes).
* show progress window when loading data from archive ([#362](https://github.com/vitalidze/traccar-web/issues/362))

Bug fixes:

* REST API for managing user settings ([#511](https://github.com/vitalidze/traccar-web/issues/511) thanks to [franiis](https://github.com/franiis) github user)
* REST API for adding geo-fences ([#512](https://github.com/vitalidze/traccar-web/issues/512) thanks to [franiis](https://github.com/franiis) github user)
* Dutch language was missing ([#569](https://github.com/vitalidze/traccar-web/issues/569))
* added new fields for compatibility with 3.4/3.5 ([#439](https://github.com/vitalidze/traccar-web/issues/439))
* offline status detection was working incorrectly with the `serverTime` value ([#601](https://github.com/vitalidze/traccar-web/issues/601))
* "Command" button always greyed out on 3.5 ([#639](https://github.com/vitalidze/traccar-web/issues/639))
* "Open desktop version" from mobile version was not working with default locale ([#546](https://github.com/vitalidze/traccar-web/issues/546), [#449](https://github.com/vitalidze/traccar-web/issues/449))
* user account cannot be removed when there are notification settings with the non-default notification template ([#551](https://github.com/vitalidze/traccar-web/issues/551))

Also this release contains update for the following translations:

* Spanish
* Serbian
* Serbian (latin)
* Vietnamese

Added new translations:

* Croatian
* Finnish
* Turkish

There were several improvements in performance: in notificaiton sending, in loading of devices with groups, loading latest positions.

Updated instructions for the 3.5 version of traccar backend: [http://traccar.litvak.su/installation.html](http://traccar.litvak.su/installation.html)