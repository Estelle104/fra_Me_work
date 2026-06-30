#!/bin/bash

echo "=== CLEAN FRAMEWORK ==="
mvn clean install

echo "=== PACKAGE FRAMEWORK ==="
mvn clean package

echo "== OK =="