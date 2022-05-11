
echo on 
native-image ^
-jar target/experiment-native-image.jar ^
--no-fallback ^
-H:ConfigurationFileDirectories=./src/main/resources/native-image/ ^
-H:ConfigurationResourceRoots=./src/main/resources/native-image/ ^
--language:js ^
--trace-object-instantiation=^
ch.qos.logback.classic.Logger,^
org.apache.logging.slf4j.Log4jLogger,^
org.apache.logging.slf4j.Log4jMarkerFactory,^
org.apache.logging.log4j.core.Logger,^
org.apache.logging.log4j.core.config.xml.XmlConfiguration,^
org.apache.logging.log4j.core.config.LoggerConfig,^
org.apache.logging.log4j.core.config.AppenderControlArraySet,^
org.apache.logging.log4j.core.lookup.RuntimeStrSubstitutor,^
org.apache.logging.log4j.core.appender.ConsoleAppender ^

-H:IncludeResourceBundles=javax.servlet.http.LocalStrings ^
--initialize-at-build-time=org.apache.logging.slf4j.Log4jLogger,^
org.apache.logging.log4j.core.Logger,^
org.apache.logging.log4j.core.config.xml.XmlConfiguration,^
org.apache.logging.log4j.core.config.LoggerConfig,^
org.apache.logging.log4j.core.config.AppenderControlArraySet,^
org.apache.logging.log4j.core.lookup.RuntimeStrSubstitutor,^
org.apache.logging.log4j.core.appender.ConsoleAppender ^
 --initialize-at-run-time=io.netty.channel.epoll.EpollEventArray,^
 io.netty.channel.epoll.Native ^
 
 pause
  --trace-object-instantiation=ch.qos.logback.classic.Logger