package biz.daich.vocalzoom.experiment.nativeimage.mqtt;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.common.base.Strings;

import lombok.Getter;

/**
 * listens to all messages on all topics and builds list of all topics
 * <br/>
 * extracts the projectIDs into the {@code projectsList} and devices IDs into the {@code devicesList}
 */
public class MqttTopicsProvider implements Closeable {
	private static final Logger l = LogManager.getLogger(MqttTopicsProvider.class.getName());

	/**
	 * as long as mqttClient is connected it keeps updating
	 */
	@Getter
	protected final DefaultComboBoxModel<String> topicsList = new DefaultComboBoxModel<>();
	/**
	 * as long as mqttClient is connected it keeps updating
	 */

	@Getter
	protected final DefaultComboBoxModel<String> projectsList = new DefaultComboBoxModel<>();
	/**
	 * as long as mqttClient is connected it keeps updating
	 */

	@Getter
	protected final DefaultComboBoxModel<String> devicesList = new DefaultComboBoxModel<>();
	/**
	 * as long as mqttClient is connected it keeps updating
	 */

	@Getter
	protected final Set<String> topicsSet = new HashSet<>();
	/**
	 * as long as mqttClient is connected it keeps updating
	 */

	@Getter
	protected final Set<String> projectsSet = new HashSet<>();
	/**
	 * as long as mqttClient is connected it keeps updating
	 */
	@Getter
	protected final Set<String> devicesSet = new HashSet<>();
	protected final MqttClient mqttClient;

	public MqttTopicsProvider(MqttClient mqttClient) throws MqttException {
		super();
		this.mqttClient = mqttClient;
		final IMqttMessageListener mqttMessageListener = (String topic, MqttMessage message) -> {
			if (!topicsSet.contains(topic)) {
				topicsSet.add(topic);
				topicsList.addElement(topic);
				l.debug("new topic {} at {}", topic, mqttClient.getServerURI());
				String projectId = UtilsMqtt.getProjectId.apply(topic);
				if (projectId != null && !projectsSet.contains(projectId)) {
					projectsList.addElement(projectId);
					projectsSet.add(projectId);
					l.debug("new project {} at {}", topic, mqttClient.getServerURI());
				}

				String deviceId = UtilsMqtt.getDeviceId.apply(topic);
				if (deviceId != null && !devicesSet.contains(deviceId)) {
					devicesList.addElement(deviceId);
					devicesSet.add(deviceId);
					l.debug("new device {} at {}", topic, mqttClient.getServerURI());
				}

			} else {
				// l.trace("topic {} already seen - ignoring", topic);
			}
		};
		mqttClient.subscribe("#", 0, mqttMessageListener);
		l.trace("mqttTopicProvider for {} subscribed to #", mqttClient.getServerURI());
	}

	@Override
	public void close() throws IOException {
		try {
			mqttClient.unsubscribe("#");
			l.trace("mqttTopicProvider for {} unsubscribed", mqttClient.getServerURI());
		} catch (MqttException e) {
			l.error("", e);
		}
	}

	/**
	 * this is a snapshot of the current list
	 */
	public List<String> getProjectIds() {
		return topicsSet.stream().map(UtilsMqtt.getProjectId).filter(t -> (!Strings.isNullOrEmpty(t))).collect(Collectors.toList());
	}

	/**
	 * this is a snapshot of the current list
	 */
	public List<String> getDeviceIds() {
		return topicsSet.stream().map(UtilsMqtt.getDeviceId).filter(t -> (!Strings.isNullOrEmpty(t))).collect(Collectors.toList());
	}
}