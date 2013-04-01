/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package ec.app.gui;

import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ec.EvolutionState;
import ec.display.chart.ChartableStatistics;
import ec.util.Parameter;

/**
 * @author spaus
 */
public abstract class TimeSeriesChartStatistics extends ChartableStatistics {

	public TimeSeriesCollection seriesCollection;
	
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		seriesCollection = new TimeSeriesCollection();
	}

	public JFreeChart makeChart() {
		JFreeChart chart = ChartFactory.createTimeSeriesChart(this.title,
				this.xlabel, this.ylabel, this.seriesCollection,
				true, false, false);
		
		//JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(this.title, "Date", "Price",, true, true, false);

		return chart;
	}

	public int addSeries(String name) {
		seriesCollection.addSeries(new TimeSeries(name));
		return seriesCollection.getSeriesCount() - 1;
	}

	public void addDataPoint(int seriesID, Date date, double value) {
		seriesCollection.getSeries(seriesID).add(new Day(date), value);
	}
}
