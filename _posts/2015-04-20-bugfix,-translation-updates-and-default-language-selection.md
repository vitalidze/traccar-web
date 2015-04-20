---
layout: default
---

Fixed issue [#149](https://github.com/vitalidze/traccar-web/issues/149) database migration was failing from old versions. Also:

* updated [german](/features/german.html) translation
* added default language selection to the `Settings >> Global` menu. If no `?locale` URL parameter is set then this language will be selected by default. Previously this could be also changed by setting up special value in `<meta>` tag (see [this issue](https://github.com/vitalidze/traccar-web/issues/141#issuecomment-91893508) for more info). Now it's set up in special cookie.