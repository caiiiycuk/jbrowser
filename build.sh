rm -rf assembled
mkdir assembled

rm -rf target
mvn package -e -P generic
cp target/jbrowser-1.9-jar-with-dependencies.jar assembled/jbrowser-1.9-generic.jar

rm -rf target
mvn package -e -P linux
cp target/jbrowser-1.9-jar-with-dependencies.jar assembled/jbrowser-1.9-linux.jar

rm -rf target
mvn package -e -P solaris
cp target/jbrowser-1.9-jar-with-dependencies.jar assembled/jbrowser-1.9-solaris.jar

rm -rf target
mvn package -e -P macosx
cp target/jbrowser-1.9-jar-with-dependencies.jar assembled/jbrowser-1.9-macosx.jar

rm -rf target
mvn package -e -P windows
cp target/jbrowser-1.9-jar-with-dependencies.jar assembled/jbrowser-1.9-windows.jar
