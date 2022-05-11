package biz.daich.vocalzoom.experiment.nativeimage.mqtt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;

/**
 * just a wrapper to make sure that the server is stoped when goes out of scope of a test
 */
public class EmbeddedMqttBrokerForTesting extends Server implements AutoCloseable {
	private static final Logger l = LoggerFactory.getLogger(EmbeddedMqttBrokerForTesting.class.getName());

	@Override
	public void close() throws Exception {
		this.stopServer();
	}

	/**
	 * create and start an embedded mqttBroker on
	 * <li>port 1883
	 * <li>host localhost
	 * <li>TLS - false
	 * <li>allows anonymous
	 *
	 *
	 */
	public static EmbeddedMqttBrokerForTesting createAndStartDefaultOpenMqttBroker() throws IOException {
		final EmbeddedMqttBrokerForTesting mqttBroker = new EmbeddedMqttBrokerForTesting();
		mqttBroker.startServer(new MemoryConfig(new Properties()));
		mqttBroker.addInterceptHandler(new PublisherListener());
		return mqttBroker;
	}

	/**
	 * implementation of the {@link AbstractInterceptHandler} for use in {@link TestUtils#createAndStartDefaultOpenMqttBroker()}
	 */
	public static class PublisherListener extends AbstractInterceptHandler {

		@Override
		public String getID() {
			return "EmbeddedTestPublishListener";
		}

		/**
		 * has some issue with the dependency on netty
		 * keep throwing java.lang.UnsupportedOperationException: direct buffer
		 */
		@Override
		public synchronized void onPublish(InterceptPublishMessage msg) {
			// this is the correct way to do it from here
			// https://stackoverflow.com/questions/52658774/netty-io-netty-buffer-bytebuf-array-throws-exception-direct-buffer/52659401
			ByteBuf buf = msg.getPayload();
			final String decodedPayload = buf.toString(StandardCharsets.UTF_8);

			if (l.isTraceEnabled()) {
				l.trace("From moquette.broker on topic: {} content: {}", msg.getTopicName(), decodedPayload); //$NON-NLS-1$
			}
		}
	}

}
