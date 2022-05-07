echo running native image agent
java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image/ -jar target/experiment-native-image.jar