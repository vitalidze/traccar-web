---
layout: default
title: Netbeans IDE setup
---

0) Clone repository.

Open Team >> Git >> Clone... menu.

![NB - Team - Git - Clone](http://i61.tinypic.com/2cf8pqu.png)

Fill 'Repository URL' with https://github.com/vitalidze/traccar-web.git and leave username/password fields empty

![NB - Git repository URL](http://i60.tinypic.com/11jlu9g.png)

Select remote branches. At least `master`, but I recommend to select `dev` too.

![NB - Git branch selection](http://i61.tinypic.com/20i6g0n.png)

Leave all values untouched on last page

![NB - Git clone last page](http://i60.tinypic.com/20ihnxy.png)

Wait until cloning completes. This usually takes 3-5 minutes and depends on internet connection speed.

![NB - Git wait until clone completes](http://i57.tinypic.com/2ibjio3.png)

1) Open cloned project

Open File >> Open Project...

![NB - Open project](http://i57.tinypic.com/5yeq9y.png)

Select cloned project

![NB - Open project window](http://i57.tinypic.com/9qxztd.png)

2) Debug application

Open Debug >> GWT Dev Mode w/o a JEE server

![NB - GWT Dev Mode w/o a JEE server](http://i61.tinypic.com/2i6f1bm.png)

Wait until project is compiled and debug session is started. When it's done there should be a message:

    Listening for transport dt_socket at address: 8000

![NB - GWT debug session startup](http://i57.tinypic.com/66zyjd.png)

Open Debug >> Attach Debugger...

![NB - Attach debugger](http://i57.tinypic.com/5wxyr9.png)

Just fill port number (8000) and leave all other values with defaults.

![NB - Set up debugger attach](http://i62.tinypic.com/2iuytzd.png)

A new window named 'GWT Development Mode' should pop up. Hit 'Launch default browser' button and to open traccar web UI.

![NB - GWT Development Mode](http://i59.tinypic.com/14keheb.png)