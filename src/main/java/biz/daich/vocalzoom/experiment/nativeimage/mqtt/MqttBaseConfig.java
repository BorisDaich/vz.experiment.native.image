package biz.daich.vocalzoom.experiment.nativeimage.mqtt;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.TrustManagerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * the simplest use {@code final MqttBaseConfig c = MqttBaseConfig.builder().build();}
 * creates a config for {@code localhost} on port 1883 no authentication no TLS {@code clientId = UUID.randomUUID().toString()}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class MqttBaseConfig implements Serializable {

	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String CLIENT_ID = "clientId";
	public static final String QUALITY_OF_SERVICE = "qos";
	public static final String USE_TLS = "useTLS";
	public static final String IGNORE_CERTIFICATE_ISSUES = "ignoreCertificateIssues";
	public static final String HOSTNAME_VERIFIER = "hostnameVerifier";
	public static final String TRUST_MANAGER_FACTORY = "trustManagerFactory";

	public static final int DEFAULT_PORT = 1883;

	protected final transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(propertyName, listener);
	}

	@Getter
	// @Setter
	@Builder.Default
	public String user = null;

	@Getter
	// @Setter
	@Builder.Default
	public String password = null;

	@Getter
	@Setter
	@Builder.Default
	public String host = "localhost";

	@Getter
	// @Setter
	@Builder.Default
	public int port = DEFAULT_PORT;

	@Getter
	// @Setter
	@Builder.Default
	public String clientId = UUID.randomUUID().toString();

	@Getter
	// @Setter
	@Builder.Default
	public int qos = QOS.at_most_once.ordinal();

	@Getter
	// @Setter
	@Builder.Default
	public boolean useTLS = false;

	@Getter
	// @Setter
	@Builder.Default
	public boolean ignoreCertificateIssues = false;

	@JsonIgnore
	@Getter
	// @Setter
	@Builder.Default
	public transient HostnameVerifier hostnameVerifier = null;

	@JsonIgnore
	@Getter
	// @Setter
	@Builder.Default
	public transient TrustManagerFactory trustManagerFactory = null;

	/***
	 * copy constructor
	 *
	 * @param c
	 *            - MqttBaseConfig an object to copy from
	 */
	public MqttBaseConfig(MqttBaseConfig c) {
		super();
		this.host = c.host;
		this.user = c.user;
		this.password = c.password;
		this.port = c.port;
		this.qos = c.qos;
		this.clientId = c.clientId;
		this.useTLS = c.useTLS;
		this.ignoreCertificateIssues = c.ignoreCertificateIssues;
		this.hostnameVerifier = c.hostnameVerifier;
		this.trustManagerFactory = c.trustManagerFactory;

	}

	public void setHost(String host) {
		pcs.firePropertyChange(HOST, this.host, this.host = host);
	}

	public void setPort(int port) {
		pcs.firePropertyChange(PORT, this.port, this.port = port);
	}

	public void setQos(int qos) {
		pcs.firePropertyChange(QUALITY_OF_SERVICE, this.qos, this.qos = qos);
	}

	public void setUser(String user) {
		pcs.firePropertyChange(USER, this.user, this.user = user);
	}

	public void setPassword(String password) {
		pcs.firePropertyChange(PASSWORD, this.password, this.password = password);
	}

	public void setClientId(String clientId) {
		pcs.firePropertyChange(CLIENT_ID, this.clientId, this.clientId = clientId);
	}

	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		pcs.firePropertyChange(HOSTNAME_VERIFIER, this.hostnameVerifier, this.hostnameVerifier = hostnameVerifier);
	}

	public void setIgnoreCertificateIssues(boolean ignoreCertificateIssues) {
		pcs.firePropertyChange(IGNORE_CERTIFICATE_ISSUES, this.ignoreCertificateIssues, this.ignoreCertificateIssues = ignoreCertificateIssues);
	}

	public void setTrustManagerFactory(TrustManagerFactory trustManagerFactory) {
		pcs.firePropertyChange(TRUST_MANAGER_FACTORY, this.trustManagerFactory, this.trustManagerFactory = trustManagerFactory);
	}

	public void setUseTLS(boolean useTLS) {
		pcs.firePropertyChange(USE_TLS, this.useTLS, this.useTLS = useTLS);
	}

	// public static void validate(MqttBaseConfig c) {
	// if (Strings.isNullOrEmpty(c.topic))
	// throw new IllegalArgumentException("topic must not be empty or null");
	// }
}