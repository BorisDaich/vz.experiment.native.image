package biz.daich.vocalzoom.experiment.nativeimage.common;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JFrame4Testing extends JFrame {
	private static final Logger l = LogManager.getLogger(JFrame4Testing.class.getName());

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame4Testing frame = new JFrame4Testing();
					frame.setVisible(true);
				} catch (Exception e) {
					l.error("$Runnable.run()", e); //$NON-NLS-1$
				}
			}
		});
	}

	/**
	 * sets the given JPanel as contentPane of the new instance of JFrame4Testing and does {@code setVisible(true)}
	 * returns {@code JFrame4Testing } instance;
	 *
	 * <br>
	 *
	 * <b>USAGE EXAMPLE:</b>
	 *
	 * <br>
	 * {@code JFrame4Testing.asFrame(new JPanel());}
	 *
	 *
	 * @param JPanel
	 *            panel - a panel to show in the JFrame as {@code contentPane}
	 */
	public static JFrame4Testing asFrame(JPanel panel) {
		JFrame4Testing x = new JFrame4Testing();
		x.setPanel(panel);
		x.pack();
		EventQueue.invokeLater(() -> x.setVisible(true));
		x.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (panel instanceof AutoCloseable) {
					AutoCloseable ac = (AutoCloseable) panel;
					try {
						ac.close();
					} catch (Exception e1) {
						l.error("$WindowAdapter.windowClosing(WindowEvent)", e1); //$NON-NLS-1$
					}
				}

			}
		});
		return x;
	}

	/**
	 * Create the frame.
	 */
	public JFrame4Testing() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 800);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
	}

	public void setPanel(JPanel panel) {
		this.contentPane.add(panel, BorderLayout.CENTER);
	}

}
