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


package org.apache.skywalking.apm.agent.core.boot;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader;

import java.util.*;

/**
 * The <code>ServiceManager</code> bases on {@link ServiceLoader},
 * load all {@link BootService} implementations.
 *
 * @author wusheng
 */
public enum ServiceManager {
    INSTANCE;

    private static final ILog logger = LogManager.getLogger(ServiceManager.class);
    private Map<Class, BootService> bootedServices = Collections.emptyMap();

    public void boot() {
        bootedServices = loadAllServices();

        for (BootService service : bootedServices.values()) {
            try {
                service.beforeBoot();
            } catch (Throwable e) {
                logger.error(e, "ServiceManager try to pre-start [{}] fail.", service.getClass().getName());
                continue;
            }
            try {
                service.boot();
            } catch (Throwable e) {
                logger.error(e, "ServiceManager try to start [{}] fail.", service.getClass().getName());
                continue;
            }
            try {
                service.afterBoot();
            } catch (Throwable e) {
                logger.error(e, "ServiceManager try to post-start [{}] fail.", service.getClass().getName());
            }
        }
    }

    public void shutdown() {
        for (BootService service : bootedServices.values()) {
            try {
                service.shutdown();
            } catch (Throwable e) {
                logger.error(e, "ServiceManager try to shutdown [{}] fail.", service.getClass().getName());
            }
        }
    }

    private Map<Class, BootService> loadAllServices() {
        Map<Class, BootService> bootedServices = new HashMap<>();
        for (BootService bootService : load()) {
            bootedServices.put(bootService.getClass(), bootService);
        }
        Map<Class, BootService> dependencies = new LinkedHashMap<>();
        for (Map.Entry<Class, BootService> service : bootedServices.entrySet()) {
            DependsOn dependsOn = (DependsOn) service.getKey().getAnnotation(DependsOn.class);
            if (dependsOn != null) {
                for (Class depend : dependsOn.value()) {
                    BootService dependsOnInstance = bootedServices.get(depend);
                    if (dependsOnInstance == null) {
                        continue;
                    }
                    if (dependencies.containsKey(depend)) {
                        continue;
                    }
                    dependencies.put(depend, dependsOnInstance);
                }
            }
            if (dependencies.containsKey(service.getKey())) {
                continue;
            }
            dependencies.put(service.getKey(), service.getValue());
        }
        return bootedServices;
    }

    private void beforeBoot() {
        for (BootService service : bootedServices.values()) {
            try {
                service.beforeBoot();
            } catch (Throwable e) {
                logger.error(e, "ServiceManager try to pre-start [{}] fail.", service.getClass().getName());
            }
        }
    }

    private void startup() {
        for (BootService service : bootedServices.values()) {
            try {
                service.boot();
                logger.info("ServiceManager start service [{}]", service.getClass().getName());
            } catch (Throwable e) {
                logger.error(e, "ServiceManager try to start [{}] fail.", service.getClass().getName());
            }
        }
    }

    private void afterBoot() {
        for (BootService service : bootedServices.values()) {
            try {
                service.afterBoot();
            } catch (Throwable e) {
                logger.error(e, "Service [{}] AfterBoot process fails.", service.getClass().getName());
            }
        }
    }

    /**
     * Find a {@link BootService} implementation, which is already started.
     *
     * @param serviceClass class name.
     * @param <T>          {@link BootService} implementation class.
     * @return {@link BootService} instance
     */
    public <T extends BootService> T findService(Class<T> serviceClass) {
        return (T) bootedServices.get(serviceClass);
    }

    ServiceLoader<BootService> load() {
        return ServiceLoader.load(BootService.class, AgentClassLoader.getDefault());
    }
}
