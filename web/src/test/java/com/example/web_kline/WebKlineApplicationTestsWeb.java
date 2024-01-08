package com.example.web_kline;

import org.example.data.PriceBean;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


class WebKlineApplicationTestsWeb extends BaseTest {

    public WebKlineApplicationTestsWeb() {
        super();
    }

    public static void main(String[] args) throws Exception {
        WebKlineApplicationTestsWeb webKlineApplicationTestsWeb = new WebKlineApplicationTestsWeb();
        webKlineApplicationTestsWeb.test("BTCUSDT", "1m");
    }


    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, 12);

        // Signals
        // Buy when SMA goes over close price
        // Sell when close price goes over SMA
        Strategy buySellSignals = new BaseStrategy(new OverIndicatorRule(sma, closePrice),
                new UnderIndicatorRule(sma, closePrice), 10);
        return buySellSignals;
    }


    @Override
    public void loadHistory() {
        long e = System.currentTimeMillis();
        long s = e - (600 * 60 * 1000);
        for (PriceBean priceBean : KlineUtil.getBar("BTCUSDT", "1m", s, e)) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
                    .build();
            handler(newBar);
        }
    }

}
