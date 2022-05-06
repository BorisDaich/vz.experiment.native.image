package biz.daich.vocalzoom.experiment.nativeimage;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import biz.daich.vocalzoom.experiment.nativeimage.cmd.RawDataRecorderConfig;
import biz.daich.vocalzoom.experiment.nativeimage.common.CommonUtils;
import picocli.CommandLine;

import javax.swing.border.TitledBorder;

public class JFrameMainNativeImageExperiment extends JFrame {
	private static final Logger l = LogManager.getLogger(JFrameMainNativeImageExperiment.class.getName());

	private JPanel contentPane;
	private PanelMainNativeImageExperiment panelMainNativeImageExperiment;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		String[] a = new String[] {
				"--stopAfterSec", "34", "--filePrefix", "aaaaa", "--split", "3421", "--qos", "2"
		};
		RawDataRecorderConfig config = new RawDataRecorderConfig();
		CommandLine processCmd = CommonUtils.processCmd(a, config, true);
		l.trace("processCmd = {}", processCmd);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrameMainNativeImageExperiment frame = new JFrameMainNativeImageExperiment();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JFrameMainNativeImageExperiment() {
		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1128, 793);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		contentPane.add(getPanelMainNativeImageExperiment(), BorderLayout.CENTER);
	}

	protected PanelMainNativeImageExperiment getPanelMainNativeImageExperiment() {
		if (panelMainNativeImageExperiment == null) {
			panelMainNativeImageExperiment = new PanelMainNativeImageExperiment();
			panelMainNativeImageExperiment.setBorder(new TitledBorder(null, "PanelMainNativeImageExperiment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		}
		return panelMainNativeImageExperiment;
	}
}
