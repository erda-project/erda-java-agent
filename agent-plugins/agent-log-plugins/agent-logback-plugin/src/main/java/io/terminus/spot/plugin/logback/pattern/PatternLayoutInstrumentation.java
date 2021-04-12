package io.terminus.spot.plugin.logback.pattern;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class PatternLayoutInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    private static final String PATTERN_LAYOUT_CLASS = "ch.qos.logback.core.pattern.PatternLayoutBase";
    private static final String PATTERN_LAYOUT_INTERCEPT_CLASS = "io.terminus.spot.plugin.logback.pattern.PatternLayoutInterceptor";

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("getEffectiveConverterMap");
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return PATTERN_LAYOUT_INTERCEPT_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }

    @Override
    protected ClassMatch enhanceClass() {
        return NameMatch.byName(PATTERN_LAYOUT_CLASS);
    }
}
