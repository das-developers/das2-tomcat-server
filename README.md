# das2-tomcat-server
Das2Server implementation intended for use with Apache Tomcat

# Build Instructions
To build the "war file" for the application, take the following steps:
```
git clone git@github.com:das-developers/das2-tomcat-server.git
cd das2-tomcat-server/lib
# copy the large library file needed for time processing.
wget -N https://jfaden.net/jenkins/job/autoplot-jar-all/lastSuccessfulBuild/artifact/autoplot/Autoplot/dist/AutoplotAll.jar
cd ..
ant -Dj2ee.server.home=/usr/local/apache-tomcat-8.0.27/ dist
```
This will make "das2serverTomcat.war" which can be deployed on an
Apache Tomcat server.

# Notes
* This is a simple server, intended to demonstrate that a Tomcat web application
would work fine to replace the functionality of the old Perl-based server.
* Caching and HAPI server functions of the Python-based server have not been 
implemented, and there are no plans to do this.
* This server relies on the Autoplot jumbo jar file to provide some functions.
* The header found on autoplot.jar prevents this version's use, unless the
header is removed.

