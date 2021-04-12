package io.terminus.spot.plugin.okhttp.v3;

import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.util.Strings;
import cloud.erda.plugin.app.insight.AppMetricBuilder;
import cloud.erda.plugin.app.insight.AppMetricContext;
import cloud.erda.plugin.app.insight.AppMetricUtils;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static cloud.erda.agent.core.utils.Constants.Tags.*;

/**
 * @author randomnil
 */
public class CallInterceptorUtils {

    static AppMetricBuilder createRequestAppMetric(Request request) {
        if (request == null) {
            return null;
        }

        URI uri = request.url().uri();
        String hostname = uri.getHost();
        if (uri.getPort() > 0) {
            hostname += ":" + uri.getPort();
        }
        return AppMetricUtils.createHttpMetric(hostname);
    }

    static AppMetricBuilder wrapResponseAppMetric(AppMetricBuilder appMetricBuilder, Response response) {
        if (response == null) {
            return appMetricBuilder;
        }

        String terminusKey = response.header(Constants.Carriers.RESPONSE_TERMINUS_KEY);
        if (!Strings.isEmpty(terminusKey)) {
            return null;
        }

        AppMetricUtils.handleStatusCode(appMetricBuilder, response.code());
        return appMetricBuilder;
    }

    static void wrapRequestSpan(Span span, Request request) {
        if (request == null) {
            return;
        }

        URI uri = request.url().uri();
        String hostname = uri.getHost() + ":" + uri.getPort();
        span.tag(COMPONENT, COMPONENT_OKHTTP);
        span.tag(SPAN_KIND, SPAN_KIND_CLIENT);
        span.tag(SPAN_LAYER, SPAN_LAYER_HTTP);
        span.tag(PEER_ADDRESS, uri.getScheme() + "://" + hostname);
        span.tag(PEER_HOSTNAME, hostname);
        span.tag(PEER_PORT, String.valueOf(uri.getPort()));
        span.tag(HTTP_URL, uri.toString());
        span.tag(HTTP_METHOD, request.method());
    }

    static void wrapResponseSpan(Span span, Response response) {
        if (response == null) {
            return;
        }

        int statusCode = response.code();
        if (statusCode >= 400) {
            span.tag(ERROR, ERROR_TRUE);
        }
        span.tag(HTTP_STATUS, String.valueOf(statusCode));
    }

    static void injectRequestHeader(Request request, Span span) throws Throwable {
        Tracer tracer = TracerManager.tracer();
        tracer.context().put(AppMetricContext.instance);
        Map<String, String> map = new HashMap<String, String>(16);
        TextMapCarrier carrier = new TextMapCarrier(map);
        tracer.inject(span.getContext(), carrier);

        Field headersField = Request.class.getDeclaredField("headers");
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(headersField, headersField.getModifiers() & ~Modifier.FINAL);

        headersField.setAccessible(true);
        Headers.Builder headerBuilder = request.headers().newBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            if (Strings.isEmpty(entry.getValue())) {
                headerBuilder.removeAll(entry.getKey());
                continue;
            }
            headerBuilder.add(entry.getKey(), entry.getValue());
        }
        headersField.set(request, headerBuilder.build());
    }
}
