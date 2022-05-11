package biz.daich.vocalzoom.experiment.nativeimage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jdesktop.swingx.JXPanel;

import com.fazecast.jSerialComm.SerialPort;
import com.jidesoft.swing.JideTabbedPane;

import biz.daich.vocalzoom.experiment.nativeimage.common.IGlobals;
import biz.daich.vocalzoom.experiment.nativeimage.common.SerializationTools;
import biz.daich.vocalzoom.experiment.nativeimage.graph.PanelBaseRawDataGraph;
import biz.daich.vocalzoom.experiment.nativeimage.graph.SensorRawDataMsg;
import biz.daich.vocalzoom.experiment.nativeimage.mqtt.EmbeddedMqttBrokerForTesting;
import biz.daich.vocalzoom.experiment.nativeimage.mqtt.MqttBaseConfig;
import biz.daich.vocalzoom.experiment.nativeimage.mqtt.MqttConnectionManager2;
import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PanelMainNativeImageExperiment extends JXPanel {
	private static final Logger l = LoggerFactory.getLogger(PanelMainNativeImageExperiment.class.getName());

	private JButton btnAddTab;
	private JButton btnExit;
	private JRadioButton rdA;
	private JCheckBox chBoxB;
	private JButton btnListComPorts;
	private JideTabbedPane tabs;
	private JScrollPane spTxt;
	private JTextArea taText;
	private JButton btnMqttTest;

	/**
	 * Create the panel.
	 */
	public PanelMainNativeImageExperiment() {

		initGUI();
		IGlobals.executor.submit(() -> {
			SwingUtilities.invokeLater(() -> {
				listPorts();
			});
		});
		IGlobals.executor.submit(() -> {
			SwingUtilities.invokeLater(() -> {
				try {
					mqttTest();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		});
		IGlobals.executor.submit(() -> {
			SwingUtilities.invokeLater(() -> {
				addTab();
			});
		});
	}

	private void initGUI() {
		setLayout(new MigLayout("", "[][][grow][grow][][][][][][][][][][][][][]", "[][][][][][][][][][][]"));
		add(getTabs(), "cell 2 1 13 5,grow");
		add(getBtnAddTab(), "cell 16 1");
		add(getBtnMqttTest(), "cell 0 3");
		add(getSpTxt(), "cell 3 6 12 3,grow");
		add(getBtnListComPorts(), "cell 16 6");
		add(getRdA(), "cell 4 9");
		add(getChBoxB(), "cell 6 9");
		add(getBtnExit(), "cell 16 10");
	}

	private void listPorts() {
		SerialPort[] commPorts = SerialPort.getCommPorts();
		for (SerialPort sp : commPorts) {
			try {
				getTaText().append(SerializationTools.toPrettyJson(sp));
			} catch (Exception e1) {
				l.error("$ActionListener.actionPerformed(ActionEvent)", e1); //$NON-NLS-1$

				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	void mqttTest() throws Exception {
		// start mqttBroker;
		if (openMqttBroker == null) {
			openMqttBroker = EmbeddedMqttBrokerForTesting.createAndStartDefaultOpenMqttBroker();
		}
		if (mqttClient == null) {
			mqttClient = MqttConnectionManager2.getInstance().getConnectedClientByConfig(mqttBaseConfig);
			mqttClient.subscribe("#", new IMqttMessageListener() {

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String s = new String(message.getPayload(), StandardCharsets.UTF_8);
					l.trace("on topic {} got {}", topic, s);
				}
			});
		}
		mqttClient.publish("/some/topic", new MqttMessage(("some strange message " + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)));

		// subscribe
		// send

	}

	private void addTab() {
		if (l.isDebugEnabled()) {
			l.debug("$ActionListener.actionPerformed(ActionEvent) - start"); //$NON-NLS-1$
		}

		PanelBaseRawDataGraph p = new PanelBaseRawDataGraph();
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));

		getTabs().addTab("TAB_" + Math.random(), null, p, null);
		if (l.isDebugEnabled()) {
			l.debug("$ActionListener.actionPerformed(ActionEvent) - end"); //$NON-NLS-1$
		}
	}

	protected JButton getBtnAddTab() {
		if (btnAddTab == null) {
			btnAddTab = new JButton("Add tab");
			btnAddTab.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addTab();
				}

			});
		}
		return btnAddTab;
	}

	protected JButton getBtnExit() {
		if (btnExit == null) {
			btnExit = new JButton("Exit");
			btnExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (l.isDebugEnabled()) {
						l.debug("$ActionListener.actionPerformed(ActionEvent) - start"); //$NON-NLS-1$
					}

					System.exit(0);

					if (l.isDebugEnabled()) {
						l.debug("$ActionListener.actionPerformed(ActionEvent) - end"); //$NON-NLS-1$
					}
				}
			});
		}
		return btnExit;
	}

	protected JRadioButton getRdA() {
		if (rdA == null) {
			rdA = new JRadioButton("radio button A");
		}
		return rdA;
	}

	protected JCheckBox getChBoxB() {
		if (chBoxB == null) {
			chBoxB = new JCheckBox("check box B");
		}
		return chBoxB;
	}

	protected JButton getBtnListComPorts() {
		if (btnListComPorts == null) {
			btnListComPorts = new JButton("List Com Ports");
			btnListComPorts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					listPorts();
				}

			});
		}
		return btnListComPorts;
	}

	protected JideTabbedPane getTabs() {
		if (tabs == null) {
			tabs = new JideTabbedPane();
			tabs.setBorder(new TitledBorder(null, "Tabs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		}
		return tabs;
	}

	protected JScrollPane getSpTxt() {
		if (spTxt == null) {
			spTxt = new JScrollPane();
			spTxt.setViewportView(getTaText());
		}
		return spTxt;
	}

	protected JTextArea getTaText() {
		if (taText == null) {
			taText = new JTextArea();
		}
		return taText;
	}

	protected JButton getBtnMqttTest() {
		if (btnMqttTest == null) {
			btnMqttTest = new JButton("Mqtt Test");
			btnMqttTest.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						mqttTest();
					} catch (Exception e1) {
						l.error("$ActionListener.actionPerformed(ActionEvent)", e1); //$NON-NLS-1$

						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}
		return btnMqttTest;
	}

	private EmbeddedMqttBrokerForTesting openMqttBroker = null;
	final private MqttBaseConfig mqttBaseConfig = MqttBaseConfig.builder().build();
	private MqttClient mqttClient = null;

}
