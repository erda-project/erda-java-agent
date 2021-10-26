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

package cloud.erda.agent.plugin.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;

public class RequestIdPatternConverter extends ClassicConverter {

    static final RequestIdPatternConverter INSTANCE = new RequestIdPatternConverter();

    @Override
    public String convert(ILoggingEvent event) {
        Scope scope = TracerManager.tracer().active();
        return scope == null ? "" : scope.span().getContext().getTraceId();
    }
}
