---
layout: default
---

Today's release fixed several issues quickly discovered by the users in yesterday's major release:

* selected track color was not visible on archive toolbar, instead of this there were a weird button ([#330](https://github.com/vitalidze/traccar-web/issues/330))
* users which have device limit set were not able to log in to the mobile version ([#331](https://github.com/vitalidze/traccar-web/issues/331))
* in previous version library used for the mobile was updated, but CSS files weren't. This was corrected.
* in mobile UI when invalid username/password is entered the warning appeared twice
* fixed glitch in the devices list on mobile UI
* new 'speed limit' setting was not saving to the database ([#332](https://github.com/vitalidze/traccar-web/issues/332))
* when manager's limit on number of devices is reached the underlying user was not getting the error message when trying to add device([#335](https://github.com/vitalidze/traccar-web/issues/335))
* google maps stopped working due to the JS API version deprecation ([#336](https://github.com/vitalidze/traccar-web/issues/336) [#337](https://github.com/vitalidze/traccar-web/issues/337))
* after redesign of event recording there appeared a bug, which prevented most events from recording ([#338](https://github.com/vitalidze/traccar-web/issues/338))
* user accounts were updated incorrectly from Settings>>Users screen: changing of 'read only' flag can reset the expiration date and 'blocked' flag

Also this release contains new feature - print geolocation of the current device on map. For now it works only in mobile version. Just click/tap the button with circle right below the '>' button (that opens sidebar), grant the geo-location access and after some time there will be a point with accuracy circle right on map.

This release also contains updates of the following languages:

* [bulgarian](/features/bulgarian.html)
* [czech](/features/czech.html)
* [greek](/features/greek.html)
* [spanish](/features/spanish.html)
* [portuguese brazilian](/features/portuguese-brazilian.html)
* [serbian translation](/features/serbian.html)

The translations are now reside on [transifex](https://www.transifex.com/traccar-web-ui-mod/traccar-web/) project. Everyone interested in updating existing/submitting new translations are welcome to the web site. Of course, it is still possible to send translations as the text files by email. But the project on transifex is the preferred and I believe simpler way to help with translation.

Since translations makes compilation time much longer and many of them are outdated I decided to start removing translations, which have less than 50% strings translated. If starting from this moment noone will update the following translations within three weeks they will be removed:

* german
* polish
* italian
* lithuanian
* tagalog

Also I have updated the [installation instructions](installation.html) for the recently released version 3.2 of original traccar project