/**
 * 
 */
package biz.daich.vocalzoom.experiment.nativeimage.mqtt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This is a wrapper for the MqttClient to provide better insight into the state of the MqttClient
 * 
 * @author boris
 *
 */
public class VzMqttClient extends MqttClient {
	private static final Logger l = LoggerFactory.getLogger(VzMqttClient.class.getName());

	protected MqttConnectOptions mqttConnectOptions = null;
	@Getter
	protected ReSubscribeOnReConnect reSubscribeOnReConnect = new ReSubscribeOnReConnect();

	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	static class Subscription {
		public int qos = 0;
		public IMqttMessageListener iMqttMessageListener;
	}

	@Getter
	protected HashMap<String, Subscription> subscriptions = new HashMap<>();

	/**
	 * this comes to replace this {@link org.eclipse.paho.client.mqttv3.MqttAsyncClient.MqttReconnectCallback} so it has to do what it does
	 * and then what we need
	 */
	class ReSubscribeOnReConnect implements MqttCallbackExtended {

		public void connectionLost(Throwable cause) {
			l.trace("connectionLost to {} cause = {}", getServerURI(), cause.getMessage(), cause);
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
		}

		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			l.trace("connectComplete to {} reconnect = {}", serverURI, reconnect);
			if (reconnect && !subscriptions.isEmpty()) {
				try {
					// restore the subscriptions

					String[] topicFilters = new String[subscriptions.size()];
					int[] qos = new int[subscriptions.size()];
					IMqttMessageListener[] messageListeners = new IMqttMessageListener[subscriptions.size()];
					int i = 0;
					for (Map.Entry<String, Subscription> e : subscriptions.entrySet()) {
						topicFilters[i] = e.getKey();
						qos[i] = e.getValue().qos;
						messageListeners[i] = e.getValue().iMqttMessageListener;
						i++;
					}
					subscribe(topicFilters, qos, messageListeners);
					l.trace("reSubscribed on {} to topics {}", serverURI, Arrays.toString(topicFilters));
				} catch (MqttException e1) {
					l.error("connectComplete({}, {})", reconnect, serverURI, e1); //$NON-NLS-1$
				}
			}
		}

	}

	public void refreshSubscriptions() {
		this.reSubscribeOnReConnect.connectComplete(true, this.getServerURI());
	}

	/**
	 * @param serverURI
	 * @param clientId
	 * @throws MqttException
	 */
	public VzMqttClient(String serverURI, String clientId) throws MqttException {
		super(serverURI, clientId);
		setCallback(reSubscribeOnReConnect);
	}

	/**
	 * @param serverURI
	 * @param clientId
	 * @param persistence
	 * @throws MqttException
	 */
	public VzMqttClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
		super(serverURI, clientId, persistence);
		setCallback(reSubscribeOnReConnect);
	}

	/**
	 * @param serverURI
	 * @param clientId
	 * @param persistence
	 * @param executorService
	 * @throws MqttException
	 */
	public VzMqttClient(String serverURI, String clientId, MqttClientPersistence persistence, ScheduledExecutorService executorService) throws MqttException {
		super(serverURI, clientId, persistence, executorService);
		setCallback(reSubscribeOnReConnect);
	}

	@Override
	public synchronized void subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException {
		super.subscribe(topicFilters, qos, messageListeners);
		for (int i = 0; i < topicFilters.length; i++) {
			subscriptions.put(topicFilters[i], new Subscription(qos[i], messageListeners[i]));
		}
		dumpSubscriptions();
	}

	@Override
	public synchronized void unsubscribe(String[] topicFilters) throws MqttException {
		super.unsubscribe(topicFilters);
		for (int i = 0; i < topicFilters.length; i++) {
			subscriptions.remove(topicFilters[i]);
		}
		dumpSubscriptions();
	}

	public synchronized void dumpSubscriptions() {
		subscriptions.entrySet().forEach((s) -> l.trace("subscription to {} with {}", s.getKey(), s.getValue()));
	}

	@Override
	public void connect(MqttConnectOptions options) throws MqttSecurityException, MqttException {
		mqttConnectOptions = options;
		super.connect(options);
	}

	@Override
	public IMqttToken connectWithResult(MqttConnectOptions options) throws MqttSecurityException, MqttException {
		mqttConnectOptions = options;
		return super.connectWithResult(options);
	}

}
