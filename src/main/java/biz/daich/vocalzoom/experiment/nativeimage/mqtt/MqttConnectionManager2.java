package biz.daich.vocalzoom.experiment.nativeimage.mqtt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.collections4.map.MultiKeyMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.common.base.Preconditions;

import biz.daich.vocalzoom.experiment.nativeimage.common.CommonUtils;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Getter;
import lombok.Setter;

public class MqttConnectionManager2 {
	private static final Logger l = LoggerFactory.getLogger(MqttConnectionManager2.class.getName());

	protected static MqttConnectionManager2 singletonInstance = null;
	protected static MemoryPersistence persistence = null;
	@Setter
	@Getter
	protected static int connectionTimeoutSec = 3;
	private static MqttClient DUMMYclient;

	public static MqttConnectionManager2 getInstance() {
		if (singletonInstance == null) {
			persistence = new MemoryPersistence();
			singletonInstance = new MqttConnectionManager2();
		}
		return singletonInstance;
	}

	public static MqttClient DUMMY() {
		if (DUMMYclient == null) {
			try {
				DUMMYclient = new VzMqttClient("tcp://localhost:3883", "clientId");
			} catch (MqttException e) {
			}
		}
		return DUMMYclient;
	}

	/**
	 * instance should be acquired only through a static factory method {@link #getInstance()}
	 */
	protected MqttConnectionManager2() {
		super();
	}

	protected final MultiKeyMap<String, MqttClient> mqttClientsCache = new MultiKeyMap<>();
	protected final MultiKeyMap<String, MqttTopicsProvider> mqttTopicProvidersCache = new MultiKeyMap<>();

	/**
	 * used to communicate TLS with hosts that has certificates issues but user accepted it.
	 */
	protected static final HostnameVerifier allwaysTrueHostnameVerifier = new HostnameVerifier() {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			l.debug("hostname = {} SSLSession = {}", hostname, session);
			return true;
		}
	};

	public MqttTopicsProvider getMqttTopicsProviderByMqttClient(MqttClient mqttClient) throws Exception {
		Preconditions.checkArgument(mqttClient != null, "mqtt Client must not be null");
		Preconditions.checkArgument(mqttClient.isConnected(), "mqtt Client must be connected");

		MqttTopicsProvider mqttTopicsProvider = null;

		URI uri = new URI(mqttClient.getCurrentServerURI());
		String host = uri.getHost();
		int port = uri.getPort();
		l.trace("request for mqttTopicsProvider for {}:{}", host, port);
		mqttTopicsProvider = mqttTopicProvidersCache.get(host, "" + port);
		if (mqttTopicsProvider == null) {
			l.trace("request for mqttTopicsProvider {}:{}  => Cache MISS!", host, port);
			mqttTopicsProvider = new MqttTopicsProvider(mqttClient);
			mqttTopicProvidersCache.put(host, "" + port, mqttTopicsProvider);
		}
		return mqttTopicsProvider;
	}

	public MqttTopicsProvider getMqttTopicsProviderByConfig(MqttBaseConfig c) throws Exception {
		MqttTopicsProvider mqttTopicsProvider = null;
		if (c != null) {
			l.trace("request for mqttTopicsProvider for {}:{}", c.host, c.port);
			mqttTopicsProvider = mqttTopicProvidersCache.get(c.host, "" + c.port);
			if (mqttTopicsProvider == null) {
				l.trace("request for mqttTopicsProvider {}:{}  => Cache MISS!", c.host, c.port);
				mqttTopicsProvider = new MqttTopicsProvider(getConnectedClientByConfig(c));
				mqttTopicProvidersCache.put(c.host, "" + c.port, mqttTopicsProvider);
			}
		} else {
			l.info("called with MqttBaseConfig parameter = null");
		}
		return mqttTopicsProvider;
	}

	public MqttClient getConnectedClientByConfig(MqttBaseConfig c) throws Exception {
		MqttClient mqttClient = null;

		if (c != null) {
			// l.trace("request for mqttClient {}:{}:{} ", c.host, c.port, c.clientId);
			mqttClient = mqttClientsCache.get(c.host, "" + c.port, c.clientId);
			if (mqttClient == null) {
				l.trace("request for mqttClient {}:{}:{}  => Cache MISS!", c.host, c.port, c.clientId);
				// if TLS - do pre flight check
				if (c.useTLS) {
					if (!isFlightCheckOK(c.host, c.port)) { // we got SSL Handshake issue
						Certificate[] problematicCertificates = getCertificatesFromProblematicConnection(c.host, c.port);
						if (!c.ignoreCertificateIssues) // User might want to look at the certificate issues...
						{
							if (!DialogAcceptProblemCertificates.asDialog(problematicCertificates)) {
								return null;
							}
						}
						// user does not want to know about the issues - just handle it

						// add certificates to the key store
						KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
						keyStore.load(null, null);
						int i = 0;
						for (Certificate cert : problematicCertificates) {
							keyStore.setCertificateEntry("s" + i++, cert);
						}
						TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
						trustManagerFactory.init(keyStore);
						c.trustManagerFactory = trustManagerFactory;
						c.hostnameVerifier = allwaysTrueHostnameVerifier;
					}
				} // end of useTLS == true

				// create the paho mqttClient

				String brokerURI = ((c.useTLS) ? "ssl://" : "tcp://") + c.host + ":" + c.port;

				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setCleanSession(true);
				if (c.password != null && c.user != null) {
					connOpts.setPassword(c.password.toCharArray());
					connOpts.setUserName(c.user);
				}
				connOpts.setAutomaticReconnect(true);
				connOpts.setConnectionTimeout(connectionTimeoutSec);
				connOpts.setSSLHostnameVerifier(c.hostnameVerifier);
				connOpts.setMaxInflight(100);
				l.trace("connecting to brokerURI {} with MqttConnectOptions = {} and persistence = {}", brokerURI, CommonUtils.toPrettyJson(connOpts), persistence);
				mqttClient = new VzMqttClient(brokerURI, c.clientId, persistence);
				mqttClient.connect(connOpts);
				l.debug("connected to brokerURI {}");

				mqttClientsCache.put(c.host, "" + c.port, c.clientId, mqttClient);
				l.debug("created and cached for {}:{}:{} to hash {}", c.host, c.port, c.clientId, mqttClient.hashCode());
			} else {
				l.debug("request for mqttClient {}:{}:{} to hash {} Cache HIT!", c.host, c.port, c.clientId, mqttClient.hashCode());
			}
		} else {
			l.info("called with MqttBaseConfig parameter = null");
		}
		return mqttClient;

	}

	protected Certificate[] getCertificatesFromProblematicConnection(String host, int port) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		// get the certificates without checking them
		SSLContext sslInsecureContext = SSLContext.getInstance("TLS");
		sslInsecureContext.init(null, InsecureTrustManagerFactory.INSTANCE.getTrustManagers(), null);

		HttpsURLConnection inSecureConnection = (HttpsURLConnection) (new URL("https://" + host + ":" + port)).openConnection();
		inSecureConnection.setSSLSocketFactory(sslInsecureContext.getSocketFactory());

		inSecureConnection.connect();
		Certificate[] certs = inSecureConnection.getServerCertificates();
		l.debug("retrived {} Certificates", certs.length);
		for (Certificate cert : certs)
			l.trace("Certificate:\n {}", cert);
		return certs;
	}

	public void removeClient(MqttClient mqttClient) throws URISyntaxException, MqttException, IOException {

		if (mqttClient != null) {
			URI uri = new URI(mqttClient.getCurrentServerURI());
			String host = uri.getHost();
			int port = uri.getPort();
			String clientId = mqttClient.getClientId();
			mqttClientsCache.removeAll(host, "" + port, clientId);
			MqttTopicsProvider mqttTopicsProvider = mqttTopicProvidersCache.get(host, "" + port);
			if (mqttTopicsProvider != null) {
				mqttTopicsProvider.close();
				mqttTopicProvidersCache.removeAll(host, "" + port);
				l.trace("closed and removed mqttTopicProvider for {}:{}", host, "" + port);
			}

			mqttClient.disconnect();
			l.debug("killed MqttClient {}:{} as {}", host, port, clientId);
		} else {
			l.warn("mqttClient is null");
		}
	}

	/**
	 * try to connect to the TLS required destination and if all good return true
	 * if any certificate issue - return false
	 *
	 * @return true - if connection successful - i.e. certificates are good etc.
	 *         false - if connection failed on ssl handshake - so something is not right with certificates
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	protected boolean isFlightCheckOK(String host, int port) throws IOException {
		try {
			((HttpsURLConnection) new URL("https://" + host + ":" + port).openConnection()).connect();
			return true;
		} catch (SSLHandshakeException e) {
			l.error("we got SSLHandshakeException exception: {}", e.getMessage());
			return false;
		}
	}

}
