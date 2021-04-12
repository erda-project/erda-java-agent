package io.terminus.spot.plugin.log.error;

import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.GsonUtils;

import java.util.List;
import java.util.Map;

public class ErrorEvent {

    private String eventId;

    private long timestamp;

    private String requestId;

    private List<StackElement> stacks;

    private Map<String, String> tags;

    private Map<String, String> metaDatas;

    private Map<String, String> requestContext;

    private Map<String, String> requestHeaders;

    public List<StackElement> getStacks() {
        return stacks;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRequestId() {
        return requestId;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Map<String, String> getMetaDatas() {
        return metaDatas;
    }

    public Map<String, String> getRequestContext() {
        return requestContext;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setStacks(List<StackElement> stacks) {
        this.stacks = stacks;
    }

    public void setMetaDatas(Map<String, String> metaDatas) {
        this.metaDatas = metaDatas;
    }

    public void setRequestContext(Map<String, String> requestContext) {
        this.requestContext = requestContext;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Metric toMetric() {
        Metric metric = Metric.New("error", timestamp);
        metric.addTag("request_id", requestId);
        metric.addTag("event_id", eventId);
        metric.addField("count", 1);
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            metric.addTag("tag#" + tag.getKey(), tag.getValue());
        }
        for (Map.Entry<String, String> metaData : metaDatas.entrySet()) {
            metric.addTag("metadata#" + metaData.getKey(), metaData.getValue());
        }
        for (Map.Entry<String, String> context : requestContext.entrySet()) {
            metric.addTag("context#" + context.getKey(), context.getValue());
        }
        for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
            metric.addTag("header#" + header.getKey(), header.getValue());
        }
        for (StackElement element : stacks) {
            metric.addTag("stack#" + element.getIndex(), GsonUtils.toJson(element));
        }
        return metric;
    }
}
