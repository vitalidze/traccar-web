---
layout: default
title: Eclipse IDE setup
---

Project can be set up in Eclipse.

#### Environment

Eclipse 4.4.2 (Luna) with plugins:

  * m2e (a.k.a. Maven Integration for Eclipse (Juno and newer)) - comes bundled with `Eclipse IDE for Java Developers`
  * m2e-wtp (a.k.a. Maven Integration for Eclipse WTP (Juno))
  * google plugin for eclipse
  * git - comes bundled with `Eclipse IDE for Java Developers`

### Plugin installation

1) Open 'Help >> Eclipse Marketplace...'

![Eclipse marketplace menu](http://i62.tinypic.com/fb9deq.png)

2) Since `m2e` is already bundled we start from installing `m2e-wtp`. Type 'm2e-wtp' in 'Find' field and click 'Go' button to search for a plugin. Once it's found click on 'Install' to bring it to your eclipse.

![Search for m2e-wtp](http://i59.tinypic.com/2mpn0ir.png)

Wait until it checks dependencies and once it finishes click 'Confirm' to confirm installation.

![Confirm m2e-wtp installation](http://i58.tinypic.com/2lscoex.png)

Then some time is needed to initialize installation. After that accept licenses and click 'Finish'.

![Accept licenses for m2e-wtp](http://i57.tinypic.com/riu2w3.png)

Wait until eclipse downloads and installs necessary packages and confirm restarting.

![Confirm eclipse restart](http://i62.tinypic.com/qyibz9.png)

3) Install 'Google plugin for eclipse'. Follow [official instructions for eclipse luna](https://developers.google.com/eclipse/docs/install-eclipse-4.4). Check 'Google plugin for eclipse' and 'GWT Designer for GPE'.

![Google plugin for eclipse selection](http://i58.tinypic.com/359gylx.png)

### Instructions

0) Go to File >> Import project... Select 'Git >> Projects from git' and click 'Next'.

![Import projects from git](http://i59.tinypic.com/2livnzd.png)

Select 'Clone URI' and click 'Next'.

![Git clone type selection](http://i59.tinypic.com/1r92tv.png)

Type 'https://github.com/vitalidze/traccar-web.git' into 'URI' and click 'Next'.

![Enter project's clone URL](http://i62.tinypic.com/502m34.png)

Main branch is 'dev', so select it at least and click 'Next'.

![Branch selection](http://i62.tinypic.com/289ve5c.png)

Confirm git repository location and click 'Next'

![Confirm repository location on disk](http://i60.tinypic.com/2mybz3k.png)

Wait until cloning completes. Then select 'Use the new project wizard' and click 'Finish'

![Finish git repository import](http://i61.tinypic.com/inhnqr.png)

Just close this wizard.

1) Go to File >> Import project... Select Maven >> Existing Maven Projects and click 'Next'.

![Import maven project](http://i58.tinypic.com/21kkl7d.png)

Then browse for a folder containing maven project. It should automatically find pom.xml file. Then click 'Finish'.

![Finish importing of maven project](http://i62.tinypic.com/2a5ljdy.png)

2) Set up GWT for project. In project's context menu select 'Properties'. Select 'Google >> Web Toolkit'. Then check 'Use Google Web Toolkit' and click 'OK' to save changes.

![Set up GWT](http://i61.tinypic.com/hraauf.png)

Wait until GWT project initialization completes.

3) Run/Debug project. In project's context menu select Run as >> Web application (GWT Super Dev Mode).

![Run as webapp](http://i62.tinypic.com/34o23r6.png)

Select `src/main/webapp` folder when prompted for `WAR` file.

### Troubleshooting

* During 'Google plugin for eclipse' installation getting error about 'Egit' dependency - try to add 'Egit' repository as said [here](https://www.eclipse.org/forums/index.php?t=msg&th=1026052&goto=1678778&#msg_1678778).

* If you are getting "Main type not specified" then go to Run/Debug configuration settings (for example via Run >> Run Configurations... menu), select Web Application >> traccar.html and put `com.google.gwt.dev.DevMode` to the `Main class` field.

* If it complains about missing `src/test/java` folder then go to Project prefrences >> Java Build Path >> Source(tab) and remove `src/test/java` entry from source entries.