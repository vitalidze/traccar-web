---
layout: default
---

New version with bug-fixes related to new [geo-fences functionality](/features/geofences.html) was released. Following issues/improvements were done:

* if geo-fence add/edit window is opened then no other action with geo-fences may be performed
* radius/width field is now disabled for 'Polygon' geo-fence since it does not make sense for such zones
* if 'disallow device management by users' is set up in Settings >> Global menu then geo-fence management should still be possible
* by default width of database column that contains geo-fence points was 255, which limits number of points to 7. Now it will be 2048, which allows to hold about 60 points. All existing databases must be updated manually. See [this issue for more details](https://github.com/vitalidze/traccar-web/issues/100#issuecomment-87578241).

The functionality is still in beta and any feedback is welcome in [issue tracker](https://github.com/vitalidze/traccar-web/issues/100).