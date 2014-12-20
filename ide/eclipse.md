---
layout: default
title: Eclipse IDE setup
---

Project can be set up in Eclipse.

#### Environment

Eclipse 4.3 with plugins:

  - m2e (a.k.a. Maven Integration for Eclipse (Juno and newer))
  - m2e-wtp (a.k.a. Maven Integration for Eclipse WTP (Juno))
  - google plugin for eclipse

#### Instructions

0) Clone repository:

    git clone https://github.com/vitalidze/traccar-web.git

1) Go to File >> Import project... Select Maven >> Existing Maven Projects and click 'Next'.
Then browse for a folder containing maven project. It should automatically find pom.xml file. Then click 'Next' to check maven goals or click finish.

2) In project's context menu select Google >> GWT Compile and unfold 'Advanced' section. Put '-war src/main/webapp' In 'Additional compiler arguments' section (this setting will is remembered for further compilations). Then hit 'Compile' button and wait until GWT compilation completes.

3) Run/Debug project. In project's context menu select Run as >> Web application.

#### Troubleshooting

* If you are getting "Main type not specified" then go to Run/Debug configuration settings (for example via Run >> Run Configurations... menu), select Web Application >> traccar.html and put `com.google.gwt.dev.DevMode` to the `Main class` field.

* If it complains about missing `src/test/java` folder then go to Project prefrences >> Java Build Path >> Source(tab) and remove `src/test/java` entry from source entries.