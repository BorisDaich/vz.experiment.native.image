package biz.daich.vocalzoom.experiment.nativeimage.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.axis.NumberAxis;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import biz.daich.vocalzoom.experiment.nativeimage.common.IGlobals;
import biz.daich.vocalzoom.experiment.nativeimage.common.IHasEventBus;
import biz.daich.vocalzoom.experiment.nativeimage.common.JFrame4Testing;
import lombok.Getter;
import lombok.NonNull;
import net.miginfocom.swing.MigLayout;

/**
 *
 * TODO: Boris need somehow reduce duplication with {@link PanelEsp32ChartRaw}
 * this is tool to show Raw UART DATA Messages stream from a Sensor's
 */
public class PanelBaseRawDataGraph extends JPanel implements IHasEventBus, Consumer<SensorRawDataMsg> {
	private static final Logger l = LogManager.getLogger(PanelBaseRawDataGraph.class.getName());

	protected static final SimpleDateFormat DateFormatWithMilliseconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private PanelSimpleVZChart panelChartV;
	private PanelSimpleVZChart panelChartD;
	private PanelSimpleVZChart panelChartM;
	@Getter
	@NonNull
	protected final EventBus bus;
	@Getter
	@NonNull
	protected String id;

	protected volatile boolean pause = false;

	protected EvictingQueue<SensorRawDataMsg> eq = EvictingQueue.create(10000);

	Timer uiUpdateTimer = new Timer(
			1000,
			e -> SwingUtilities.invokeLater( //
					() -> {
						if (!pause) {
							setData(Lists.newArrayList(eq));
						}
					}//
			)//
	);

	private JButton btnClear;

	private JLabel lblFromLabel;
	private JLabel lblFrom;
	private JLabel lblToLabel;
	private JLabel lblTo;
	private JButton btnLive;
	private JButton btnPause;

	/**
	 * for development. Do not use.
	 */
	public PanelBaseRawDataGraph() {
		this(IGlobals.bus, "id");
		l.warn("DO NOT USE THIS CONSTRUCTOR IN PRODUCTION");
	}

	public PanelBaseRawDataGraph(@NonNull EventBus bus, @NonNull String id) {
		this.bus = bus;
		this.id = id;

		initUI();
		getPanelChartV().clear();
		getPanelChartD().clear();
		getPanelChartM().clear();
		setData(Lists.newArrayList(eq));
		uiUpdateTimer.setInitialDelay(1000);
		uiUpdateTimer.start();
		bus.register(this);
	}

	private void initUI() {
		setBounds(100, 100, 1017, 901);

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(new MigLayout("", "[][][grow][][][][][][grow][grow][grow][grow]", "[][grow][grow][grow]"));
		add(getBtnLive(), "cell 0 0");
		add(getBtnPause(), "cell 1 0");
		add(getBtnClear(), "cell 2 0,alignx right");
		add(getLblFromLabel(), "cell 3 0");
		add(getLblFrom(), "cell 4 0");
		add(getLblToLabel(), "cell 5 0");
		add(getLblTo(), "cell 6 0");
		this.add(getPanelChartV(), "cell 0 1 12 1,grow");
		this.add(getPanelChartD(), "cell 0 2 12 1,grow");
		this.add(getPanelChartM(), "cell 0 3 12 1,grow");
	}

	public PanelSimpleVZChart getPanelChartV() {
		if (panelChartV == null) {
			panelChartV = new PanelSimpleVZChart();
			panelChartV.getChart().setTitle("Velocity");
		}
		return panelChartV;
	}

	public PanelSimpleVZChart getPanelChartD() {
		if (panelChartD == null) {
			panelChartD = new PanelSimpleVZChart();
			panelChartD.getChart().setTitle("Distance");
		}
		return panelChartD;
	}

	public PanelSimpleVZChart getPanelChartM() {
		if (panelChartM == null) {
			panelChartM = new PanelSimpleVZChart();
			panelChartM.getChart().setTitle("Mask");
		}
		return panelChartM;
	}

	public synchronized void setData(Collection<SensorRawDataMsg> eq) {
		int dLen = 0;
		// if the message has interval value - it will be set later
		double interval = 166.667;

		SensorRawDataMsg first = null;
		SensorRawDataMsg last = null;
		for (SensorRawDataMsg x : eq) {
			if (first == null)
				first = x;
			dLen += x.getV().length;
			last = x;
		}

		if (first != null) {
			interval = first.intervalMicro;

			this.getLblFrom().setText(DateFormatWithMilliseconds.format(new Date(first.ts)));
		}
		if (last != null) {
			this.getLblTo().setText(DateFormatWithMilliseconds.format(new Date(last.ts + (long) ((interval * last.d.length) / 1000.0))));
		}

		// l.trace("dLen is {}", dLen);
		double[] v = new double[dLen];
		double[] d = new double[dLen];
		double[] m = new double[dLen];

		int offset = 0;
		for (SensorRawDataMsg x : eq) {
			int xLen = x.getV().length;
			for (int i = 0; i < xLen; i++) {
				v[offset + i] = (double) x.getV()[i];
				d[offset + i] = (double) x.getD()[i];
				m[offset + i] = (double) x.getM()[i];
			}
			offset += xLen;
		}

		// X axis values
		double[] x = new double[dLen];
		for (int i = 0; i < dLen; i++) {
			// x[i] = (double) (currentTimeMilli + i * interval);
			x[i] = (double) i;
		}

		double[][] dataV = new double[2][dLen];
		double[][] dataD = new double[2][dLen];
		double[][] dataM = new double[2][dLen];

		dataV[0] = x;
		dataV[1] = v;
		getPanelChartV().clear();
		getPanelChartV().setData(dataV, "Velocity");
		getPanelChartV().setXAxis(new NumberAxis("sample no."));

		dataD[0] = x;
		dataD[1] = d;
		getPanelChartD().clear();
		getPanelChartD().setData(dataD, "Distance");
		getPanelChartD().setXAxis(new NumberAxis("sample no."));

		dataM[0] = x;
		dataM[1] = m;
		getPanelChartM().clear();
		getPanelChartM().setData(dataM, "Mask");
		getPanelChartM().setXAxis(new NumberAxis("sample no."));
	}

	private class BtnClearActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			eq.clear();
			setData(Lists.newArrayList(eq));
		}
	}

	@Subscribe
	@Override
	public synchronized void accept(SensorRawDataMsg x) {
		eq.add(x);
	}

	public JButton getBtnClear() {
		if (btnClear == null) {
			btnClear = new JButton("clear");
			btnClear.addActionListener(new BtnClearActionListener());
		}
		return btnClear;
	}

	public JLabel getLblFromLabel() {
		if (lblFromLabel == null) {
			lblFromLabel = new JLabel("From:");
			lblFromLabel.setLabelFor(getLblFrom());
		}
		return lblFromLabel;
	}

	public JLabel getLblFrom() {
		if (lblFrom == null) {
			lblFrom = new JLabel("-----------");
		}
		return lblFrom;
	}

	public JLabel getLblToLabel() {
		if (lblToLabel == null) {
			lblToLabel = new JLabel("To:");
			lblToLabel.setLabelFor(getLblTo());
		}
		return lblToLabel;
	}

	public JLabel getLblTo() {
		if (lblTo == null) {
			lblTo = new JLabel("=====");
		}
		return lblTo;
	}

	protected JButton getBtnLive() {
		if (btnLive == null) {
			btnLive = new JButton("Live");
			btnLive.addActionListener((e) -> pause = false);
			btnLive.setMnemonic('l');
		}
		return btnLive;
	}

	protected JButton getBtnPause() {
		if (btnPause == null) {
			btnPause = new JButton("Pause");
			btnPause.addActionListener((e) -> pause = true);
			btnPause.setMnemonic('p');
		}
		return btnPause;
	}

	public static void main(String[] a) {
		PanelBaseRawDataGraph p = new PanelBaseRawDataGraph();
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));
		p.accept(SensorRawDataMsg.genRandomRawMsg(100));
		JFrame4Testing.asFrame(p);
	}
}
