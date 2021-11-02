# das2-tomcat-server
Das2Server implementation intended for use with Apache Tomcat

# Build Instructions
```
git clone git@github.com:das-developers/das2-tomcat-server.git
cd das2-tomcat-server/lib
# copy the large library file needed for time processing.
wget -N https://jfaden.net/jenkins/job/autoplot-jar-all/lastSuccessfulBuild/artifact/autoplot/Autoplot/dist/AutoplotAll.jar
cd ..
ant dist
```

