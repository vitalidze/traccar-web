---
layout: default
title: Building
---

You can build it yourself easily without installing IDE (eclipse, idea, netbeans, etc.). The only requirement is installed Java Development Kit (JDK) v. 6 or greater:

1) Clone my repository

    git clone https://github.com/vitalidze/traccar-web.git

2) Change to the cloned repo folder

    cd traccar-web

3) Run build via maven wrapper

On Linux/Unix:

    ./mvnw package

On Windows:

    mvnw package

Build can take several minutes. Once it completes the 'war' file will be under 'target' folder.