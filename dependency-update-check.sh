#!/usr/bin/env bash

echo "###### checking for property updates ######"
mvn -f pom.xml versions:display-property-updates
echo "###### checking for plugin updates ######"
mvn -f pom.xml versions:display-plugin-updates
echo "###### checking for dependency updates ######"
mvn -f pom.xml versions:display-dependency-updates | grep -v "for updates from" | grep -v Download