package biz.daich.vocalzoom.experiment.nativeimage.mqtt;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.cert.Certificate;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class DialogAcceptProblemCertificates extends JDialog {

	protected static DialogAcceptProblemCertificates dialogAcceptProblemCertificates;
	private final JPanel contentPanel = new JPanel();
	private JScrollPane scrollPane;
	private JTextArea taCertificates;
	protected boolean isOk = false;
	private JButton btnOk;
	private JButton btnCancel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DialogAcceptProblemCertificates dialog = new DialogAcceptProblemCertificates();
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DialogAcceptProblemCertificates() {
		setBounds(200, 200, 1169, 925);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		contentPanel.add(getScrollPane());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.add(getBtnOk());
			buttonPane.add(getBtnCancel());
		}
		setModal(true);
	}

	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton("OK");
			btnOk.addActionListener(new OkButtonActionListener());
			btnOk.setActionCommand("OK");
			getRootPane().setDefaultButton(btnOk);
		}
		return btnOk;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new CancelButtonActionListener());
			btnCancel.setActionCommand("Cancel");
		}
		return btnCancel;
	}

	public JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTaCertificates());
		}
		return scrollPane;
	}

	public JTextArea getTaCertificates() {
		if (taCertificates == null) {
			taCertificates = new JTextArea();
		}
		return taCertificates;
	}

	public static boolean asDialog(Certificate[] certs) {
		if (dialogAcceptProblemCertificates == null) {
			dialogAcceptProblemCertificates = new DialogAcceptProblemCertificates();
		}
		dialogAcceptProblemCertificates.getTaCertificates().setText("");
		for (Certificate cert : certs) {
			dialogAcceptProblemCertificates.getTaCertificates().append("-----------------------------------\n" + cert.toString() + "\n========================================\n");
		}
		dialogAcceptProblemCertificates.setVisible(true);
		return dialogAcceptProblemCertificates.isOk;
	}

	private class OkButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			isOk = true;
			DialogAcceptProblemCertificates.this.setVisible(false);
		}
	}

	private class CancelButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			isOk = false;
			DialogAcceptProblemCertificates.this.setVisible(false);
		}
	}
}
