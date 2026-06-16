#!/bin/bash

echo "===== BUILD FRAMEWORK ====="

mvn clean install

if [ $? -ne 0 ]; then
    echo "Framework build failed"
    exit 1
fi

echo "===== BUILD TEST PROJECT ====="

cd ../test-fra_Me_work || exit
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Test build failed"
    exit 1
fi

echo "===== DONE ====="