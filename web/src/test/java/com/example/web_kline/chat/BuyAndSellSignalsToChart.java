/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.web_kline.chat;

import org.example.data.PriceBean;
import org.example.indicators.SuperTrendLowerBandIndicator;
import org.example.indicators.SuperTrendUpperBandIndicator;
import org.example.indicators.SupertrendIndicator;
import org.example.rule.SupertrendRule;
import org.example.util.KlineUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.indicators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.aroon.AroonUpIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;


import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * This class builds a graphical chart showing the buy/sell signals of a
 * strategy.
 */
public class BuyAndSellSignalsToChart {

    /**
     * Builds a JFreeChart time series from a Ta4j bar series and an indicator.
     *
     * @param barSeries the ta4j bar series
     * @param indicator the indicator
     * @param name      the name of the chart time series
     * @return the JFreeChart time series
     */
    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(BarSeries barSeries, Indicator<Num> indicator,
                                                                       String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())),
                    indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    /**
     * Runs a strategy over a bar series and adds the value markers corresponding to
     * buy/sell signals to the plot.
     *
     * @param series   the bar series
     * @param strategy the trading strategy
     * @param plot     the plot
     */

    private static void addBuySellSignals(BarSeries series, Strategy strategy, XYPlot plot) {
        // Running the strategy
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        List<Position> positions = seriesManager.run(strategy).getPositions();
        positions.stream().mapToDouble(e->e.getExit().getNetPrice().minus(e.getEntry().getNetPrice()).doubleValue()).filter(i->i>0).count();

        positions.stream().mapToDouble(e->e.getExit().getNetPrice().minus(e.getEntry().getNetPrice()).doubleValue()).sum();

        for (Position position : positions) {
            // Buy signal
            int buyIndex = position.getEntry().getIndex();
            double buySignalBarTime = new Minute(Date.from(series.getBar(buyIndex).getEndTime().toInstant())).getFirstMillisecond();
            double buyPrice = series.getBar(buyIndex).getClosePrice().doubleValue();
            XYPointerAnnotation buyAnnotation = new XYPointerAnnotation("Buy", buySignalBarTime, buyPrice, 3.0 * Math.PI / 2);
            buyAnnotation.setBaseRadius(15.0);
            buyAnnotation.setTipRadius(10.0);
            buyAnnotation.setPaint(Color.GREEN);
            plot.addAnnotation(buyAnnotation);



            Shape buyShape = new Ellipse2D.Double(-5, -5, 10, 10); // Increase the size of the circle
            XYShapeAnnotation buyShapeAnnotation = new XYShapeAnnotation(
                    buyShape,
                    new BasicStroke(2.0f), // Make the stroke thicker
                    Color.GREEN,           // Set the color of the shape
                    Color.BLACK);          // Set the color of the border if desired
            plot.addAnnotation(buyShapeAnnotation);

            // Sell signal
            int sellIndex = position.getExit().getIndex();
            double sellSignalBarTime = new Minute(Date.from(series.getBar(sellIndex).getEndTime().toInstant())).getFirstMillisecond();
            double sellPrice = series.getBar(sellIndex).getClosePrice().doubleValue();
            XYPointerAnnotation sellAnnotation = new XYPointerAnnotation("Sell", sellSignalBarTime, sellPrice, Math.PI / 2);
            sellAnnotation.setBaseRadius(15.0);
            sellAnnotation.setTipRadius(10.0);
            sellAnnotation.setPaint(Color.RED);
            plot.addAnnotation(sellAnnotation);

            // Optionally, add a shape for sell signal
            Shape sellShape = new Ellipse2D.Double(-3, -3, 6, 6);
            XYShapeAnnotation sellShapeAnnotation = new XYShapeAnnotation(sellShape, new BasicStroke(1.0f), Color.RED);
            plot.addAnnotation(sellShapeAnnotation);
        }
    }

//
//    private static void addBuySellSignals(BarSeries series, Strategy strategy, XYPlot plot) {
//        // Running the strategy
//        BarSeriesManager seriesManager = new BarSeriesManager(series);
//        List<Position> positions = seriesManager.run(strategy).getPositions();
//        // Adding markers to plot
//        for (Position position : positions) {
//            // Buy signal
//            double buySignalBarTime = new Minute(
//                    Date.from(series.getBar(position.getEntry().getIndex()).getEndTime().toInstant()))
//                    .getFirstMillisecond();
//            Marker buyMarker = new ValueMarker(buySignalBarTime);
//            buyMarker.setPaint(Color.GREEN);
//            buyMarker.setLabel("B");
//            plot.addDomainMarker(buyMarker);
//            // Sell signal
//            double sellSignalBarTime = new Minute(
//                    Date.from(series.getBar(position.getExit().getIndex()).getEndTime().toInstant()))
//                    .getFirstMillisecond();
//            Marker sellMarker = new ValueMarker(sellSignalBarTime);
//            sellMarker.setPaint(Color.RED);
//            sellMarker.setLabel("S");
//            plot.addDomainMarker(sellMarker);
//        }
//    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Buy and sell signals to chart");
        frame.setContentPane(panel);
        frame.pack();
        UIUtils.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws InterruptedException {
        BarSeries series =new BaseBarSeries("mySeries", DoubleNum::valueOf);
        long e = System.currentTimeMillis();
        long s = e - (1000*24 * 60 *60 * 1000);
        for (PriceBean priceBean : KlineUtil.getBar2("BTCUSDT", "5m", s, e)) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
                    .build();
            series.addBar(newBar);
        }

        // Getting the bar series

        // Building the trading strategy

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        int atrPeriod = 9;

        // Define the Supertrend indicator using ATR
        Indicator<Num> supertrendUpIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                true// Multiplier
        );

        Indicator<Num> supertrendDnIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                false// Multiplier
        );


        // Create a trading strategy
        Strategy strategy = new BaseStrategy(
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, true),
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, false)
        );

        /*
         * Building chart datasets
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"));

        TimeSeriesCollection dataset1 = new TimeSeriesCollection();
        dataset1.addSeries(buildChartTimeSeries(series, new SuperTrendLowerBandIndicator(series), "SuperTrendLowerBandIndicator (BTC)"));


        TimeSeriesCollection dataset2 = new TimeSeriesCollection();
        dataset2.addSeries(buildChartTimeSeries(series, new SuperTrendUpperBandIndicator(series), "SuperTrendUpperBandIndicator (BTC)"));


        /*
         * Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Bitstamp BTC", // title
                "Date", // x-axis label
                "Price", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();

        addCashFlowAxis(plot,dataset1,Color.green,1);
        addCashFlowAxis(plot,dataset2,Color.blue,2);

        /*
         * Running the strategy and adding the buy and sell signals to plot
         */
        addBuySellSignals(series, strategy(series), plot);

        displayChart(chart);
    }

    private static void addCashFlowAxis(XYPlot plot, TimeSeriesCollection dataset,Color color, int index) {
//        final NumberAxis cashAxis = new NumberAxis("Cash Flow Ratio");
//        cashAxis.setAutoRangeIncludesZero(false);
//        plot.setRangeAxis(index, cashAxis);
        plot.setDataset(index, dataset);
//        plot.mapDatasetToRangeAxis(index, index);

        final StandardXYItemRenderer cashFlowRenderer = new StandardXYItemRenderer();
        cashFlowRenderer.setSeriesPaint(0,color);
        plot.setRenderer(index, cashFlowRenderer);
//        plot.setRenderer(index, new XYLineAndShapeRenderer(true, false));
    }

    private static Strategy strategy(BarSeries series){
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        int atrPeriod = 9;

        // Define the Supertrend indicator using ATR
        Indicator<Num> supertrendUpIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                true// Multiplier
        );

        Indicator<Num> supertrendDnIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                false// Multiplier
        );


        // 初始化Aroon指标
        AroonUpIndicator aroonUp = new AroonUpIndicator(series, 20);  // 使用25周期
        AroonDownIndicator aroonDown = new AroonDownIndicator(series, 20);  // 使用25周期

        // Aroon策略规则
        Rule aroonBullish = new OverIndicatorRule(aroonUp, aroonDown);
        Rule aroonBearish = new UnderIndicatorRule(aroonUp, aroonDown);


        // Create a trading strategy
        Strategy strategy = new BaseStrategy(
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, true).and(aroonBullish),
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, false).or(aroonBearish)
        );
        return strategy;
    }
}
