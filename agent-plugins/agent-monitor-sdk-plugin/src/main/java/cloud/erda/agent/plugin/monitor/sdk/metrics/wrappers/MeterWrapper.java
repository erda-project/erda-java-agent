/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.monitor.sdk.metrics.wrappers;

import io.terminus.dice.monitor.sdk.metrics.Meter;
import cloud.erda.agent.plugin.monitor.sdk.metrics.ExponentialMovingAveragesMeter;

/**
 * @author liuhaoyang 2020/3/22 11:34
 */
public class MeterWrapper extends BaseWrapper<ExponentialMovingAveragesMeter> implements Meter {

    public MeterWrapper(ExponentialMovingAveragesMeter metric) {
        super(metric);
    }

    @Override
    public void mark(double value) {
        this.metric.mark((long) value);
    }

    @Override
    public void mark(double value, String[] args) {
        this.metric.labels(args).mark((long) value);
    }
}