---
layout: default
---

Today I am pleased to announce support for the version 3.3 of traccar. There is a slight change in the [documentation](/installation.html) as well.

Also this release includes following bug fixes:

* seamark layer prevented original layers from working ([#429](https://github.com/vitalidze/traccar-web/issues/429))
* vietnamese language was not working on mobile version ([#430](https://github.com/vitalidze/traccar-web/issues/430))
* commands sending was not working on v3.3, `CUSTOM` commands were not working at all ([#440](https://github.com/vitalidze/traccar-web/issues/440))
* it was possible for a user that does not have access to the device to delete it from database through the API ([#507](https://github.com/vitalidze/traccar-web/issues/507))
* unable to add user through API because password could not be serialized ([#509](https://github.com/vitalidze/traccar-web/issues/509))

Also it contains latest translation updates from [transifex project site](https://www.transifex.com/traccar-web-ui-mod/traccar-web/) along with the new 'Romanian' language.