---
layout: feature
title: Snap to roads
---

Archive toolbar now contains new check box named 'Snap to roads'.
 
![Snap to roads check box on archive toolbar](http://i57.tinypic.com/15pqwx5.png) 
 
Once checked when archive is being loaded a query to the [project OSRM](http://project-osrm.org/) server is executed to load points snapped to roads. Coordinates are updated for all matched points, so the resulting track will be closer to roads. 

There is a possibility to draw more precise track aligned to road using response from the server, which is not supported for now, but will be supported in future.