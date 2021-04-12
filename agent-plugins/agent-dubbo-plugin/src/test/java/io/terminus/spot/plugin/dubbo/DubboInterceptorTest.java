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


//package io.terminus.spot.plugin.dubbo;
//
//import com.alibaba.dubbo.common.URL;
//import com.alibaba.dubbo.rpc.Invocation;
//import com.alibaba.dubbo.rpc.Invoker;
//import com.alibaba.dubbo.rpc.Result;
//import com.alibaba.dubbo.rpc.RpcContext;
//import io.terminus.spot.agent.core.conf.Config;
//import io.terminus.spot.agent.core.plugin.interceptor.enhance.EnhancedInstance;
//import io.terminus.spot.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
//import io.terminus.spot.agent.test.helper.SegmentHelper;
//import io.terminus.spot.agent.test.helper.SegmentRefHelper;
//import io.terminus.spot.agent.test.helper.SpanHelper;
//import io.terminus.spot.agent.test.tools.AgentServiceRule;
//import io.terminus.spot.agent.test.tools.SegmentStorage;
//import io.terminus.spot.agent.test.tools.SegmentStoragePoint;
//import io.terminus.spot.agent.test.tools.TracingSegmentRunner;
//import org.hamcrest.CoreMatchers;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.modules.junit4.PowerMockRunnerDelegate;
//
//import java.util.List;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.*;
//import static org.powermock.api.mockito.PowerMockito.when;
//
//@RunWith(PowerMockRunner.class)
//@PowerMockRunnerDelegate(TracingSegmentRunner.class)
//@PrepareForTest({RpcContext.class})
//public class DubboInterceptorTest {
//
//    @SegmentStoragePoint
//    private SegmentStorage segmentStorage;
//
//    @Rule
//    public AgentServiceRule agentServiceRule = new AgentServiceRule();
//
//    @Mock
//    private EnhancedInstance enhancedInstance;
//
//    private DubboInterceptor dubboInterceptor;
//
//    @Mock
//    private RpcContext rpcContext;
//    @Mock
//    private Invoker invoker;
//    @Mock
//    private Invocation invocation;
//    @Mock
//    private MethodInterceptResult methodInterceptResult;
//    @Mock
//    private Result result;
//
//    private Object[] allArguments;
//    private Class[] argumentTypes;
//
//    @Before
//    public void setUp() {
//        dubboInterceptor = new DubboInterceptor();
//
//        PowerMockito.mockStatic(RpcContext.class);
//
//        when(invoker.getUrl()).thenReturn(URL.valueOf("dubbo://127.0.0.1:20880/org.apache.skywalking.apm.test.TestDubboService"));
//        when(invocation.getMethodName()).thenReturn("test");
//        when(invocation.getParameterTypes()).thenReturn(new Class[] {String.class});
//        when(invocation.getArguments()).thenReturn(new Object[] {"abc"});
//        PowerMockito.when(RpcContext.getContext()).thenReturn(rpcContext);
//        when(rpcContext.isConsumerSide()).thenReturn(true);
//        allArguments = new Object[] {invoker, invocation};
//        argumentTypes = new Class[] {invoker.getClass(), invocation.getClass()};
//        Config.Agent.APPLICATION_CODE = "DubboTestCases-APP";
//    }
//
//    @Test
//    public void testConsumerWithAttachment() {
//        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
//        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);
//
//        assertThat(segmentStorage.getTraceSegments().size(), is(1));
//        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
//        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);
//        assertThat(spans.size(), is(1));
//        assertConsumerSpan(spans.get(0));
//    }
//
//    @Test
//    public void testConsumerWithException() {
//        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
//        dubboInterceptor.handleMethodException(enhancedInstance, null, allArguments, argumentTypes, new RuntimeException());
//        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);
//        assertThat(segmentStorage.getTraceSegments().size(), is(1));
//        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
//        assertConsumerTraceSegmentInErrorCase(traceSegment);
//    }
//
//    @Test
//    public void testConsumerWithResultHasException() {
//        when(result.getException()).thenReturn(new RuntimeException());
//
//        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
//        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);
//
//        assertThat(segmentStorage.getTraceSegments().size(), is(1));
//        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
//        assertConsumerTraceSegmentInErrorCase(traceSegment);
//    }
//
//    @Test
//    public void testProviderWithAttachment() {
//        when(rpcContext.isConsumerSide()).thenReturn(false);
//        when(rpcContext.getAttachment(SW3CarrierItem.HEADER_NAME)).thenReturn("1.323.4433|3|1|1|#192.168.1.8 :18002|#/portal/|#/testEntrySpan|#AQA*#AQA*Et0We0tQNQA*");
//
//        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
//        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);
//        assertProvider();
//    }
//
//    private void assertConsumerTraceSegmentInErrorCase(
//        TraceSegment traceSegment) {
//        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);
//        assertThat(spans.size(), is(1));
//        assertConsumerSpan(spans.get(0));
//        AbstractTracingSpan span = spans.get(0);
//        assertThat(SpanHelper.getLogs(span).size(), is(1));
//        assertErrorLog(SpanHelper.getLogs(span).get(0));
//    }
//
//    private void assertErrorLog(LogDataEntity logData) {
//        assertThat(logData.getLogs().size(), is(4));
//        assertThat(logData.getLogs().get(0).getValue(), CoreMatchers.<Object>is("error"));
//        assertThat(logData.getLogs().get(1).getValue(), CoreMatchers.<Object>is(RuntimeException.class.getName()));
//        assertNull(logData.getLogs().get(2).getValue());
//    }
//
//    private void assertProvider() {
//        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
//        assertThat(SegmentHelper.getSpans(traceSegment).size(), is(1));
//        assertProviderSpan(SegmentHelper.getSpans(traceSegment).get(0));
//        assertTraceSegmentRef(traceSegment.getRefs().get(0));
//    }
//
//    private void assertTraceSegmentRef(TraceSegmentRef actual) {
//        assertThat(SegmentRefHelper.getSpanId(actual), is(3));
//        assertThat(SegmentRefHelper.getEntryApplicationInstanceId(actual), is(1));
//        assertThat(SegmentRefHelper.getTraceSegmentId(actual).toString(), is("1.323.4433"));
//    }
//
//    private void assertProviderSpan(AbstractTracingSpan span) {
//        assertCommonsAttribute(span);
//        assertTrue(span.isEntry());
//    }
//
//    private void assertConsumerSpan(AbstractTracingSpan span) {
//        assertCommonsAttribute(span);
//        assertTrue(span.isExit());
//    }
//
//    private void assertCommonsAttribute(AbstractTracingSpan span) {
//        List<KeyValuePair> tags = SpanHelper.getTags(span);
//        assertThat(tags.size(), is(1));
//        assertThat(SpanHelper.getLayer(span), CoreMatchers.is(SpanLayer.RPC_FRAMEWORK));
//        assertThat(SpanHelper.getComponentId(span), is(3));
//        assertThat(tags.get(0).getValue(), is("dubbo://127.0.0.1:20880/org.apache.skywalking.apm.test.TestDubboService.test(String)"));
//        assertThat(span.getOperationName(), is("org.apache.skywalking.apm.test.TestDubboService.test(String)"));
//    }
//}
