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

package com.example.web_kline.test2;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.example.data.PriceBean;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DoubleNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoxTest {
    public static void main(String[] args) {
        ArrayList<Double> doubles = new ArrayList<>();
        ArrayList<ZonedDateTime> time = new ArrayList<>();
        for (PriceBean priceBean : KlineUtil.getBar2("BTCUSDT", "1h", System.currentTimeMillis() - 20 * 24 * 60 * 60 * 1000, System.currentTimeMillis())) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
                    .build();
            doubles.add(newBar.getClosePrice().doubleValue());
            time.add(newBar.getEndTime());
        }


        double[] doubles2 = new double[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) {
            doubles2[i] = doubles.get(i);
        }

        // Example usage
        List<Box> allBoxes = findBoxes(doubles2, 0, doubles2.length - 1);
        allBoxes = merge(allBoxes);
        if (!allBoxes.isEmpty()) {
            for (Box allBox : allBoxes) {
                int startIndex = allBox.startIndex;
                int endIndex = allBox.endIndex;
                ZonedDateTime startTime = time.get(startIndex);
                ZonedDateTime endTime = time.get(endIndex);
                System.out.println("发现一个箱体： 时间区间为 [" + startTime + "," + endTime + "]价格区间为: [" + allBox.lowerBound + "," + allBox.upperBound + "]");
            }
        }
    }


    public static double cacciteLower(double[] data, boolean lower) {
        DescriptiveStatistics priceStats = new DescriptiveStatistics();
        for (int i = 0; i < data.length; i++) {
            priceStats.addValue(data[i]);
        }

        if (lower) {
            return priceStats.getPercentile(5);
        } else {
            return priceStats.getPercentile(95);
        }
    }


    public static List<Box> findBoxes(double[] data, int start, int end) {
        List<Box> boxes = new ArrayList<>();

        for (int i = start ; i <= end; i++) {
            if (i + 100 > end) {
                break;
            }

            for (int j = i ; j <= end; j++) {
                if (isBox(data, i, j)) {
                    Box box = new Box(i, j, cacciteLower(Arrays.copyOfRange(data, start, end), false), cacciteLower(Arrays.copyOfRange(data, start, end), true));
                    boxes.add(box);
                    if (boxes.size() > 1000) {
                        boxes = removeContainedBoxes(boxes);
                    }
                }
            }
        }

        // Remove contained boxes
        return removeContainedBoxes(boxes);
    }

    public static boolean isBox(double[] data, int start, int end) {
        // Implement the logic to check if the data range forms a box
        // based on the given criteria (8% amplitude, 48 data points, 1% outliers)
        DescriptiveStatistics priceStats = new DescriptiveStatistics();
        for (double v : Arrays.copyOfRange(data, start, end)) {
            priceStats.addValue(v);
        }

        double upperBound = priceStats.getPercentile(95);
        double lowerBound = priceStats.getPercentile(5);

        double v = lowerBound * 1.08;

        // 判断箱体大小是否合理
        boolean b = end - start > 40 && upperBound <= v;

        if (b && isCycle(data, 0, data.length - 1, upperBound, lowerBound, 2)) {
            double mean = priceStats.getMean();
            double stdDev = priceStats.getStandardDeviation();

            double meanlowerBound = mean - stdDev;
            double meanupperBound = mean + stdDev;
            return calculatePercentilePosition(priceStats, meanlowerBound) < 15 && calculatePercentilePosition(priceStats, meanupperBound) < 85;
        }

        return b;

//// 判断持续时间是否足够长
//        boolean isDurationValid = ;
//
//// 分析关键支撑位的交易量是否足够大
//        boolean isVolumeAtBoundsHigh =
//
//// 综合判断是否为有效箱体
//        boolean isBoxValid = isBoxSizeValid ;


    }

    public static boolean isCycle(double[] data, int startIndex, int endIndex, double upperBound, double lowerBound, int minCycles) {
        int cycles = 0;
        boolean touchedUpper = false;
        boolean touchedLower = false;

        for (int i = startIndex; i <= endIndex; i++) {
            // 如果触及上限，标记touchedUpper为true
            if (data[i] >= upperBound) {
                // 如果之前已经触及过下限，增加一次往返计数，并重置touchedLower
                if (touchedLower) {
                    cycles++;
                    touchedLower = false; // 重置，准备下一次往返
                }
                touchedUpper = true;
            }
            // 如果触及下限，标记touchedLower为true
            if (data[i] <= lowerBound) {
                // 如果之前已经触及过上限，增加一次往返计数，并重置touchedUpper
                if (touchedUpper) {
                    cycles++;
                    touchedUpper = false; // 重置，准备下一次往返
                }
                touchedLower = true;
            }

            // 如果达到最小往返次数，返回true
            if (cycles >= minCycles) {
                return true;
            }
        }
        return false;
    }


    public static List<Box> removeContainedBoxes(List<Box> boxes) {
        // 创建一个新列表来存储没有被包含的箱体
        List<Box> filteredBoxes = new ArrayList<>();

        for (Box outerBox : boxes) {
            boolean isContained = false;
            for (Box innerBox : boxes) {
                // 检查 outerBox 是否被 innerBox 包含
                if (outerBox != innerBox && innerBox.contains(outerBox)) {
                    isContained = true;
                    break;
                }
            }
            // 如果 outerBox 没有被任何其他箱体包含，则添加到 filteredBoxes
            if (!isContained) {
                filteredBoxes.add(outerBox);
            }
        }

//        filteredBoxes = merge(filteredBoxes);
        return filteredBoxes;
    }


    public static List<Box> merge(List<Box> list) {
        List<Box> boxes = new ArrayList<>();
        if (!list.isEmpty() && list.size() > 1) {
            for (int i = 0; i < list.size() - 1; i++) {
                if (list.get(i).canMerge(list.get(i + 1))) {
                    Box merge = list.get(i).merge(list.get(i + 1));
                    boxes.add(merge);
                    i = i + 1;
                } else {
                    boxes.add(list.get(i));
                }
            }
        }
        return boxes;
    }

    public static double calculatePercentilePosition(DescriptiveStatistics stats, double value) {
        for (int p = 1; p <= 100; p++) {
            if (stats.getPercentile(p) >= value) {
                return p;
            }
        }
        return 100;
    }


    public static class Box {
        int startIndex;
        int endIndex;
        double upperBound;
        double lowerBound;

        Box(int startIndex, int endIndex, double upperBound, double lowerBound) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
        }

        boolean contains(Box other) {
            return this.startIndex <= other.startIndex && this.endIndex >= other.endIndex;
        }

        boolean canMerge(Box other) {
            if (other.startIndex == this.startIndex + 1 && other.endIndex == this.endIndex + 1) {
                return Math.min(lowerBound, other.lowerBound) * 1.08 > Math.max(upperBound, other.upperBound);
            }
            return false;
        }

        Box merge(Box other) {
            return new Box(this.startIndex, other.endIndex, Math.max(upperBound, other.upperBound), Math.min(lowerBound, other.lowerBound));
        }
    }
}
