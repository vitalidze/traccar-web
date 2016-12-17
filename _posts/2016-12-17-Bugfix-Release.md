---
layout: news
---

Hello everybody. 

I was very busy with other tasks so the updates stopped comment as frequent as they were. You can find some info about it [here](https://github.com/vitalidze/traccar-web/issues/971). Anyway, the main thing is that the project is not dead, though it will be moving slowly.

Today I am releasing a BugFix release with fixes found for the past half of year:

* updating of devices through REST API was working incorrectly ([#753](https://github.com/vitalidze/traccar-web/issues/753))
* "double-login" issue when Google Maps are used ([#822](https://github.com/vitalidze/traccar-web/issues/753))
* dropped support for MapQuest mapping layer because they don't provide public tiles anymore ([#832](https://github.com/vitalidze/traccar-web/issues/832) [#817](https://github.com/vitalidze/traccar-web/issues/817))
* mobile version wasn't using non-OSM map ([#873](https://github.com/vitalidze/traccar-web/issues/873))
* map controls were missing in reports ([#929](https://github.com/vitalidze/traccar-web/issues/929))
* custom commands sending wasn't working ([#879](https://github.com/vitalidze/traccar-web/issues/879))

There are several of new useful functions added:

* support for the new version of the OSRM API (v5) used for the "Snap-to-roads" functionality. Demo server has already switched to the new version so it wasn't working with previous release of Traccar Web UI Mod. ([#830](https://github.com/vitalidze/traccar-web/issues/830))
* added 'Search' field to 'Share' dialog
* added OrdnanceSurvey Bing Layer for UK users ([#891](https://github.com/vitalidze/traccar-web/issues/891))
* added "experimental" basic support for the Traccar Manager application developed for the original version of Traccar ([#759](https://github.com/vitalidze/traccar-web/issues/759)). See [this article](http://traccar.litvak.su/features/traccar-manager.html) for additional information.

As usual updated translations:

* bulgarian
* german
* greek
* croatian
* polish
* serbian
* seriban (latin)
* danish
* spanish
* persian
* portuguese (brazil) 
* vietnamese
* finnish

Added new translations:

- Hindi (India)

Wish you all a Merry Christmas, Happy New year and great holidays!