package biz.daich.vocalzoom.experiment.nativeimage.mqtt;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import biz.daich.vocalzoom.experiment.nativeimage.common.CommonUtils;
import lombok.Data;
import lombok.NonNull;

public interface UtilsMqtt {
	static final Logger l = LoggerFactory.getLogger(UtilsMqtt.class.getName());

	public static class BadTopicNameException extends Exception {
		public BadTopicNameException(String s) {
			super(s);
		}
	}

	public static String getHostName() {
		String hostName = "UNKNOWN";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		return hostName;
	}

	/**
	 * generates a unique clientId in form of "<hostName>_<DateTime>" for example "Boris-xps_2022-04-19-21-00-45-815"
	 * 
	 */
	public static String uniqueMqttClientId() {
		String hostName = getHostName();
		String strDateTime = CommonUtils.getFormattedDateTimeFileNameCompat(System.currentTimeMillis());
		String clientId = hostName + "_" + strDateTime;
		return clientId;
	}

	/**
	 * this is an abstraction of a topic structure of the VzDevice
	 * {projectId}/{unitId}/{algorithmName}/{"in"|"out"|"data"}
	 */
	@Data
	public static class VzTopic implements Serializable {
		public VzTopic(@NonNull String topic) throws BadTopicNameException {
			this.topic = topic;
			String[] parts = topic.split("/");
			if (parts.length != 4)
				throw new BadTopicNameException("The topic [" + topic
						+ "] is of a the bad form. must have 4 parts like this {projectId}/{unitId}/{algorithmName}/{\"in\"|\"out\"|\"data\"}");
			projectId = parts[0];
			unitId = parts[1];
			algorithmName = parts[2];
			channel = parts[3];
		}

		public final String topic;

		public final String projectId;
		public final String unitId;
		public final String algorithmName;
		/**
		 * last element of the topic name should be "in"|"out"|"data"
		 */
		public final String channel;

	}

	/**
	 * Get {<b>deviceId</b>} from the mqtt topic of the form
	 * {projectId}/{unitId}/{algorithmName}/{"in"|"out"|"data"}
	 */
	public static final Function<String, String> getDeviceId = (String t) -> {
		String p = null;
		String[] parts = t.split("/");
		if (parts.length > 1)
			p = parts[1];
		p = (Strings.isNullOrEmpty(p)) ? null : p;
		return p;
	};

	/**
	 * Get {<b>algorithmName</b>} from the mqtt topic of the form
	 * {projectId}/{unitId}/{algorithmName}/{"in"|"out"|"data"}
	 */
	public static final Function<String, String> getAlgorithmName = (String t) -> {
		String p = null;
		String[] parts = t.split("/");
		if (parts.length > 2)
			p = parts[2];
		p = (Strings.isNullOrEmpty(p)) ? null : p;
		return p;
	};

	/**
	 * Get {<b>projectId</b>} from the mqtt topic of the form
	 * {projectId}/{unitId}/{algorithmName}/{"in"|"out"|"data"}
	 */

	public static final Function<String, String> getProjectId = (String t) -> {
		String p = null;
		Iterator<String> iterator = Splitter.on('/').split(t).iterator();
		if (iterator.hasNext())
			p = iterator.next();
		p = (Strings.isNullOrEmpty(p)) ? null : p;
		return p;
	};

	/**
	 * extract the host as string from the string returned by the paho
	 * mqttClient.getCurrentServerURI()
	 */
	public static String getHostFromCurrentServerURI(String currentServerURI) {
		if (l.isInfoEnabled()) {
			l.info("getHostFromCurrentServerURI(String) - String currentServerURI={}", currentServerURI); //$NON-NLS-1$
		}

		throw new RuntimeException("TO BE IMPLEMENTED!!!");

	}

	/**
	 * extract the port as int from the string returned by the paho
	 * mqttClient.getCurrentServerURI()
	 */
	public static int getPortFromCurrentServerURI(String currentServerURI) {
		if (l.isInfoEnabled()) {
			l.info("getPortFromCurrentServerURI(String) - String currentServerURI={}", currentServerURI); //$NON-NLS-1$
		}

		throw new RuntimeException("TO BE IMPLEMENTED!!!");

	}
}
