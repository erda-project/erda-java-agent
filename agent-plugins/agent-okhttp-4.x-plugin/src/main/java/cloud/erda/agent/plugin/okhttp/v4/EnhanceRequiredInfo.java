/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.erda.agent.plugin.okhttp.v4;

import cloud.erda.agent.core.tracing.TracerSnapshot;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;

/**
 * {@link EnhanceRequiredInfo} storage the `ContextSnapshot` and `RealCall` instances for support the async function of
 * okhttp client.
 *
 * @author zhangxin
 */
public class EnhanceRequiredInfo {
    private TracerSnapshot tracerSnapshot;
    private EnhancedInstance realCallEnhance;

    public EnhanceRequiredInfo(EnhancedInstance realCallEnhance, TracerSnapshot tracerSnapshot) {
        this.tracerSnapshot = tracerSnapshot;
        this.realCallEnhance = realCallEnhance;
    }

    public TracerSnapshot getTracerSnapshot() {
        return tracerSnapshot;
    }

    public EnhancedInstance getRealCallEnhance() {
        return realCallEnhance;
    }
}