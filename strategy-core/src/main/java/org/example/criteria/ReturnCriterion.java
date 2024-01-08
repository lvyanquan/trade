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

package org.example.criteria;


import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.AbstractAnalysisCriterion;
import org.ta4j.core.num.Num;

/**
 * Return (in percentage) criterion (includes trading costs), returned in
 * decimal format.
 *
 * <p>
 * The return of the provided {@link Position position(s)} over the provided
 * {@link BarSeries series}.
 */
public class ReturnCriterion extends AbstractAnalysisCriterion {

    /**
     * If true, then the base percentage of {@code 1} (equivalent to 100%) is added
     * to the criterion value.
     */
    private final boolean addBase;

    /**
     * Constructor with {@link #addBase} == true.
     */
    public ReturnCriterion() {
        this.addBase = true;
    }

    /**
     * Constructor.
     *
     * @param addBase the {@link #addBase}
     */
    public ReturnCriterion(boolean addBase) {
        this.addBase = addBase;
    }


    public Num calculate(BarSeries series, Position position) {
        return calculateProfit(series, position);
    }


    public Num calculate(BarSeries series, TradingRecord tradingRecord) {
        return tradingRecord.getPositions()
                .stream()
                .map(position -> calculateProfit(series, position))
                .reduce(series.numOf(1), Num::multipliedBy)
                .minus(addBase ? series.numOf(0) : series.numOf(1));
    }

    /** The higher the criterion value, the better. */

    public boolean betterThan(Num criterionValue1, Num criterionValue2) {
        return criterionValue1.isGreaterThan(criterionValue2);
    }

    /**
     * Calculates the gross return of a position (Buy and sell).
     *
     * @param series   a bar series
     * @param position a position
     * @return the gross return of the position
     */
    private Num calculateProfit(BarSeries series, Position position) {
        if (position.isClosed()) {
            return position.getGrossReturn(series);
        }
        return addBase ? series.numOf(1) : series.numOf(0);
    }
}
