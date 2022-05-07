echo build maven 
set JAVA_HOME=c:\Java\graalvm\
set PATH=c:\Java\graalvm\bin;%PATH%
echo %PATH%
echo %JAVA_HOME%

java -version
call "C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools\VC\Auxiliary\Build\vcvars64.bat"

rem mvn -T 1C -DskipTests  clean package 
native-image -jar target/experiment-native-image.jar --no-fallback -H:ConfigurationFileDirectories=./src/main/resources/native-image/ -H:ConfigurationResourceRoots=./src/main/resources/native-image/
