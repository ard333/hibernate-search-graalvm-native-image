mvn clean package

native-image \
--verbose \
--no-server \
--no-fallback \
--report-unsupported-elements-at-runtime \
--initialize-at-build-time=net.bytebuddy.description.type,net.bytebuddy.implementation.bind.annotation,net.bytebuddy.ClassFileVersion \
-jar target/hibernate-search-graalvm-native-image-0.0.1-SNAPSHOT-jar-with-dependencies.jar


