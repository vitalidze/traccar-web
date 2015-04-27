---
layout: default
---

Several found bugs were fixed:

* geo-fence enter/exit notifications could not be sent to the ordinary users. Only administartor received such notifications ([issue #150](https://github.com/vitalidze/traccar-web/issues/150))
* added `version` attribute to the root node of GPX XML file so it conform to the official GPX 1.1 schema
* devices list wasn't loaded for 'manager' user if his users has same device shared
* minor UI issues in russian language ([issue #156](https://github.com/vitalidze/traccar-web/issues/156))
* when device with assigned device-specific geo-fence was removed relation between device and geo-fence was not cleared ([issue #155](https://github.com/vitalidze/traccar-web/issues/155))