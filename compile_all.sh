#/bin/sh

cd joern
gradle build -x test
cd ..

cd phpjoern
gradle build
cd ..

