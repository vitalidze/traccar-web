---
layout: default
---

Added two features to the mobile UI:

* following device/vehicle like in 'desktop' version - use button below 'select on map' (see [#161](https://github.com/vitalidze/traccar-web/issues/161)). Only one device can be followed at time. Now same applies to desktop version as well.

* quick way to send position information or link to the OpenStreetMap from mobile UI (see [#152](https://github.com/vitalidze/traccar-web/issues/152)). On iOS devices this works only for sending by Email, on others there are both Email and SMS links.

Then because [GXT library](http://www.sencha.com/products/gxt/) used for UI development does not include bulgarian translation of basic strings (like sorting order, default error messages, etc.). Boko Kid kindly translated all missing strings that are used in traccar, which is now included into latest release.