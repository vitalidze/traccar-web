---
layout: default
---

In recently released ['read-only' users](/features/read-only-users.html) feature were a major security bug - check was not performed on server side (aspect code wasn't properly invoked). Now it's fixed and covered by unit tests. Upgrade is highly recommended for all users of that function.

Then some ORM mappings were redesigned to reduce workload on the underlying database. Also there were an issue in web UI that produced additional load right after application start, which was fixed too. So for users that have many devices may find both performance improvement and lighter overall server workload.

Next, for [ticket #113](https://github.com/vitalidze/traccar-web/issues/113) all links to scripts were made to either relative or protocol-agnostic. All scripts are now loaded from [CloudFlare](https://www.cloudflare.com/) instead of OpenLayers.

Last change brought to us by github user [j5boot](https://github.com/j5boot) - update to [spanish translation](/features/spanish.html) both in desktop and mobile versions of UI. Thank you!