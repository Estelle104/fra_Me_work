#!/bin/bash

echo "=== CLEAN FRAMEWORK ==="
mvn clean

echo "=== PACKAGE FRAMEWORK ==="
mvn package

echo "=== INSTALL INTO MAVEN LOCAL REPO ==="
mvn install

echo "=== DONE ==="
echo "Framework installé dans ~/.m2/repository"