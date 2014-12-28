---
layout: default
---

Finally [feature #41](https://github.com/vitalidze/traccar-web/issues/41) - possibility to [change device icon](/features/change-device-icon.html) finished. For now, device icons are hard coded and work both for desktop and mobile versions.

Fixed bug in mobile version, which was not working for user that never changed their preferences. For now all users will have default preferences stored in database.

Another improvement was done to the [automatic login](/features/automatic-login.html) ([feature #40](https://github.com/vitalidze/traccar-web/issues/40)) by [Antonio Fernandes](https://github.com/antoniopaisfernandes). Now either password hash or it's actual text may be specified to log in.