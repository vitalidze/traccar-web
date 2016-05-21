---
layout: news
---

Today's release does not contain any new function except updates to the several translations.

There are multiple bugs fixed:

* [#380](https://github.com/vitalidze/traccar-web/issues/380) - I have decided to abandon idea to use traccar backend API for commands sending because it requires modification of the source code of backend. Thus sending through API most probably will work only since 3.4 version of traccar backend (3.3 released today will not support it). Instead of API for versions 3.1 and 3.2 (3.3 is not yet tested) the commands will be sent by calling appropriate methods in the core of traccar backend. This is done on the server side of the web UI.
* import of files was not supporting `other` field in JSON format. Now it is supported, moreover even if the input file may contain XML there the JSON will be saved as a result.
* [#400](https://github.com/vitalidze/traccar-web/issues/400) - 'Import' button will be hidden for 'read only' users and when 'disallow device management by users' is selected in the Settings >> Global menu.
* [#406](https://github.com/vitalidze/traccar-web/issues/406) - devices view became blank sometimes when users deletes group that have assigned devices
* [#422](https://github.com/vitalidze/traccar-web/issues/422) - when devices view was filtered the popup shown information about some different device and not, which is below the mouse at the moment

Added translations:

* [Arabic](/features/arabic.html)
* [Vietnamese](/features/vietnamese.html)

Updated translations:

* [Bulgarian](/features/bulgarian.html)
* [Czech](/features/czech.html)
* [Greek](/features/greek.html)
* [French](/features/french.html)
* [Italian](/features/italian.html)
* [Hungarian](/features/hungarian.html)
* [Polish](/features/polish.html)
* [Portuguese (Brazil)](/features/portuguese-brazilian.html)
* [Russian](/features/russian.html)
* [Serbian](/features/serbian.html)
* [Serbian (latin)](/features/serbian_latin.html)

This version also contains major changes in a way "desktop" version uses translations. Previously it was using so calling "static" approach, in which every translation resulted in a separate set of resulting javascript files with "hard-coded" strings in it. Each new translation increased compilation time dramatically and resulting `traccar-web.war` file size too. Now all translations will be loaded dynamically in runtime and compilation will be done only once. This approach makes adding new translations much more scalable. However, there may be some issues in how translations work, please report them in [official issue tracker](https://github.com/vitalidze/traccar-web/issues). This also means that the size of resulting file will be much smaller (~24MB instead of ~85MB at the moment).

I wish everyone Merry Christmas, Happy New Year. Let all your dreams come true!