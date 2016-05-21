---
layout: default
---

I am happy to announce new minor release. It contains following new features:

* context menu in devices list. It is possible to click right mouse button over some device and edit/delete/share/generate report ([#402](https://github.com/vitalidze/traccar-web/issues/402), [#665](https://github.com/vitalidze/traccar-web/issues/665))
* quick report menu in archive. Open report with selected device and period from the archive toolbar ([#402](https://github.com/vitalidze/traccar-web/issues/402))
* changed "Reports" menu to be a drop-down, which quickly opens new/existing reports.
* scroll to selected device in the main devices list (i.e. after selection on map)
* possibility to define size of the arrow used to display device on the map.
* added possibility to define URL for the OSRM service, used to query for the points from archive with the "snap to roads" function enabled. Default server has a restriction for 100 points per track in query. The URL can be changed in Settings >> Global menu. Also when there is some issue with OSRM server then an error will be displayed (previously it was just not showing anything on the map)
* hide "Register" button from the login screen instead of greying it out ([#649](https://github.com/vitalidze/traccar-web/issues/649))
* possibility to restrict sending of commands to administrator users only. There is a setting in Settings >> Global

Bug fixes:

* in some cases with large number of devices the visibility setting may not fit into default width of column in database. It was increased four times.
* if device has a group that is not available for current user, then there was a display issue of devices list - this was fixed
* fixed inability to save changes in the "notification expiry period" setting in Settings >> Global
* fixed working of OpenStreetMap over HTTPS. Now there should be no errors about "mixed content" in the web browser ([#672](https://github.com/vitalidze/traccar-web/issues/672))
* fixed "manager" user removal when he has added some underlying users. Now these users will be mapped to the manager's manager user if any
* fixed missing icons in mobile version for sending current position over email/sms ([#680](https://github.com/vitalidze/traccar-web/issues/680))
* when searching for device in devices list groups which don't have any matched device will be hidden

Updated following translations from [transifex](https://www.transifex.com/traccar-web-ui-mod/traccar-web/):

* Greek
* Spanish
* French
* Hungarian
* Portuguese (Brazil)

Added new translation: Kazakh