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

package cloud.erda.agent.plugin.okhttp.common;

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * {@link RealCallInterceptor} intercept the synchronous http calls by the discovery of okhttp.
 *
 * @author peng-yongsheng
 */
public class RealCallInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {

    private static ILog log = LogManager.getLogger(RealCallInterceptor.class);

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        objInst.setDynamicField(allArguments[1]);
    }

    /**
     * Get the {@link Request} from {@link EnhancedInstance}, then create {@link Span} and set host, port, kind,
     * component, url from {@link Request}. Through the reflection of the way, set the http header of context
     * data into {@link Request#headers}.
     *
     * @param context
     * @param result  change this result, if you want to truncate the method.
     * @throws Throwable
     */
    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Request request = (Request) context.getInstance().getDynamicField();

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan("HTTP " + request.method()).childOf(spanContext).startActive().span();

        CallInterceptorUtils.wrapRequestSpan(span, request);
        CallInterceptorUtils.injectRequestHeader(request, span);

        log.info("StartSpan");

        TransactionMetricBuilder transactionMetricBuilder = CallInterceptorUtils.createRequestAppMetric(request);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);

        log.info("StartMetric");
    }

    /**
     * Get the status code from {@link Response}, when status code greater than 400, it means there was some errors in
     * the server. Finish the {@link Span}.
     *
     * @param context
     * @param ret     the method's original return value.
     * @return
     * @throws Throwable
     */
    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Response response = (Response) ret;
        log.info("context.getAttachment " + context.getArguments().length);
        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        transactionMetricBuilder = CallInterceptorUtils.wrapResponseAppMetric(transactionMetricBuilder, response);
        if (transactionMetricBuilder != null) {
            log.info("CloseMetric");
            MetricReporter.report(transactionMetricBuilder);
        }

        Scope scope = TracerManager.tracer().active();
        CallInterceptorUtils.wrapResponseSpan(scope.span(), response);
        scope.close();
        log.info("CloseSpan");
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
