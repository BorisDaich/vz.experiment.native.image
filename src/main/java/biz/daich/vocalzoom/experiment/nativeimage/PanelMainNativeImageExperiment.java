package biz.daich.vocalzoom.experiment.nativeimage;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jdesktop.swingx.JXPanel;

import com.fazecast.jSerialComm.SerialPort;
import com.jidesoft.swing.JideTabbedPane;

import biz.daich.vocalzoom.experiment.nativeimage.common.SerializationTools;
import net.miginfocom.swing.MigLayout;

public class PanelMainNativeImageExperiment extends JXPanel {
	private static final Logger l = LogManager.getLogger(PanelMainNativeImageExperiment.class.getName());

	private JButton btnAddTab;
	private JButton btnExit;
	private JRadioButton rdA;
	private JCheckBox chBoxB;
	private JButton btnListComPorts;
	private JideTabbedPane tabs;
	private JScrollPane spTxt;
	private JTextArea taText;

	/**
	 * Create the panel.
	 */
	public PanelMainNativeImageExperiment() {

		initGUI();
	}

	private void initGUI() {
		setLayout(new MigLayout("", "[][][grow][grow][][][][][][][][][][][][][]", "[][grow][][][][][grow][][][][]"));
		add(getTabs(), "cell 2 1 13 5,grow");
		add(getBtnListComPorts(), "cell 16 2");
		add(getSpTxt(), "cell 3 6 12 3,grow");
		add(getBtnAddTab(), "cell 16 8");
		add(getRdA(), "cell 4 9");
		add(getChBoxB(), "cell 6 9");
		add(getBtnExit(), "cell 16 10");
	}

	protected JButton getBtnAddTab() {
		if (btnAddTab == null) {
			btnAddTab = new JButton("Add tab");
			btnAddTab.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (l.isDebugEnabled()) {
						l.debug("$ActionListener.actionPerformed(ActionEvent) - start"); //$NON-NLS-1$
					}
					getTabs().addTab("TAB_" + Math.random(), null, new JXPanel(), null);
					if (l.isDebugEnabled()) {
						l.debug("$ActionListener.actionPerformed(ActionEvent) - end"); //$NON-NLS-1$
					}
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
			});
		}
		return btnListComPorts;
	}

	protected JideTabbedPane getTabs() {
		if (tabs == null) {
			tabs = new JideTabbedPane();
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
}
