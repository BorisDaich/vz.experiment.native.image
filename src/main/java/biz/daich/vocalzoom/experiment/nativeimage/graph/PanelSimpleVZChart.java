package biz.daich.vocalzoom.experiment.nativeimage.graph;

import java.awt.Color;
import java.util.Random;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

/**
 * simple VocalZoom XY chart
 *
 * assumes that user has a 2D double array of data
 * data[0] - X - values of the chart
 * data[1] - Y - values of the chart
 *
 * OR
 *
 * a 1D array of data and the interval that will be used to calc the X coordinate for the data
 *
 *
 */
public class PanelSimpleVZChart extends JPanel {

	private static final long serialVersionUID = 2758348052087109563L;

	protected ChartPanel chartPanel;
	@Getter
	protected DefaultXYDataset dataset;
	protected JFreeChart chart;
	static final Random r = new Random();
	protected XYPlot plot;

	@Getter
	private ValueAxis xAxis = new DateAxis("Time Axis");

	public void setData(double[][] data, String name) {
		dataset.addSeries(name, data);
	}

	public void setData(double[] data, String name, int interval) {

		double[][] d = new double[2][data.length];
		double[] x = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			x[i] = i * interval;
		}
		d[0] = x;
		d[1] = data;
		dataset.addSeries(name, d);
	}

	/**
	 * just generates data to show for debug and development
	 */
	protected double[] genData(int samplesCount) {
		double[] x = new double[samplesCount];

		for (int i = 0; i < x.length; i++)
			x[i] = 10 * r.nextDouble();
		return x;
	}

	/**
	 * remove all data
	 */
	public void clear() {
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			dataset.removeSeries(dataset.getSeriesKey(i));
		}
	}

	/**
	 * Create the panel.
	 */
	public PanelSimpleVZChart() {

		dataset = new DefaultXYDataset();

		setData(genData(5000), "A", 10);
		setData(genData(5000), "B", 10);

		setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));
		add(getChartPanel(), "cell 0 0,grow");
	}

	public ChartPanel getChartPanel() {
		if (chartPanel == null) {
			chartPanel = new ChartPanel(getChart());
			// mouse zoom in / zoom out
			chartPanel.setMouseWheelEnabled(true);
			chartPanel.setDomainZoomable(true);
			chartPanel.setRangeZoomable(false);
		}
		return chartPanel;
	}

	public JFreeChart getChart() {
		if (chart == null) {
			chart = ChartFactory.createXYLineChart(
					"Sample Chart",
					"Time",
					"Value",
					dataset,
					PlotOrientation.VERTICAL,
					true,
					true,
					false);

			final XYPlot plot = chart.getXYPlot();

			plot.setBackgroundPaint(new Color(0xffffe0));
			plot.setDomainGridlinesVisible(true);
			plot.setDomainGridlinePaint(Color.lightGray);
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.lightGray);

			plot.setDomainAxis(0, xAxis);
			ValueAxis xaxis = plot.getDomainAxis();

			xaxis.setAutoRange(true);
			xaxis.setVerticalTickLabels(true);

			ValueAxis yaxis = plot.getRangeAxis();
			yaxis.setAutoRange(true);

		}
		return chart;
	}

	public void setXAxis(ValueAxis axis) {
		this.xAxis = axis;
		this.getChart().getXYPlot().setDomainAxis(axis);
	}
}
