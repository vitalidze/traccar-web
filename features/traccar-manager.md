---
layout: feature
title: Traccar Managed mobile apps
---

For the original version of traccar [there are couple of mobile apps called "Manager"](https://www.traccar.org/manager/). To make the working with the installed Traccar Web UI Mod there are several:

1) Backend version must be 3.8, 3.9 or higher. Follow installation instructions carefully, pay attention to the updated queries. The goal is to adapt database schema used by Traccar Web UI Mod to the database schema used on backend.

2) `Default password hash` must be set to `pbkdf2withhmacsha1` in the Settings >> Global. After this to update passwords every **existing** user is required to log off and then log on. All newly created users will have properly hashed password stored in the database.

3) Email field **MUST** be filled in. Backend uses `email` for the authentication and not `login` as used in the Traccar Web UI Mod.

**Important note**: devices list is synchronized with database not simultaneously, but with some delay. This means that after adding/updating device on the front end (i.e. add new device from Traccar Web UI Mod) it will take some time to see it in mobile application. By default the delay is set to 5 minutes and can be changed in configuration file with `database.refreshDelay` parameter, value is in seconds.