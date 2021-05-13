package com.amazon.opendistroforelasticsearch.security.modules;

import com.amazon.opendistroforelasticsearch.security.DefaultObjectMapper;
import com.amazon.opendistroforelasticsearch.security.securityconf.DynamicConfigFactory;
import com.amazon.opendistroforelasticsearch.security.securityconf.impl.SecurityDynamicConfiguration;
import com.amazon.opendistroforelasticsearch.security.validation.ConfigValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionResponse;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.common.settings.ClusterSettings;
import org.opensearch.common.settings.IndexScopedSettings;
import org.opensearch.common.settings.Setting;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.SettingsFilter;
import org.opensearch.plugins.ActionPlugin;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestHandler;
import org.opensearch.script.ScriptContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OpenDistroSecurityModulesRegistry {
    public static final OpenDistroSecurityModulesRegistry INSTANCE = new OpenDistroSecurityModulesRegistry();

    private static final Logger log = LogManager.getLogger(OpenDistroSecurityModulesRegistry.class);

    private List<OpenDistroSecurityModule<?>> subModules = new ArrayList<>();

    private OpenDistroSecurityModulesRegistry() {

    }

    public void add(String... classes) {
        for (String clazz : classes) {
            try {
                Object object = Class.forName(clazz).getDeclaredConstructor().newInstance();

                if (object instanceof OpenDistroSecurityModule) {
                    subModules.add((OpenDistroSecurityModule<?>) object);
                } else {
                    log.error(object + " does not implement OpenDistroSecuritySubModule");
                }

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException e) {
                log.error("Error while instantiating " + clazz, e);
            }
        }
    }

    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings,
                                             IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        List<RestHandler> result = new ArrayList<>();

        for (OpenDistroSecurityModule<?> module : subModules) {
            result.addAll(module.getRestHandlers(settings, restController, clusterSettings, indexScopedSettings, settingsFilter,
                indexNameExpressionResolver, nodesInCluster));
        }

        return result;
    }

    public List<ActionPlugin.ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        List<ActionPlugin.ActionHandler<? extends ActionRequest, ? extends ActionResponse>> result = new ArrayList<>();

        for (OpenDistroSecurityModule<?> module : subModules) {
            result.addAll(module.getActions());
        }

        return result;
    }

    public List<ScriptContext<?>> getContexts() {
        List<ScriptContext<?>> result = new ArrayList<>();

        for (OpenDistroSecurityModule<?> module : subModules) {
            result.addAll(module.getContexts());
        }

        return result;
    }

    public Collection<Object> createComponents(OpenDistroSecurityModule.BaseDependencies baseDependencies) {
        List<Object> result = new ArrayList<>();

        for (OpenDistroSecurityModule<?> module : subModules) {
            result.addAll(module.createComponents(baseDependencies));

            registerConfigChangeListener(module, baseDependencies.getDynamicConfigFactory());
        }

        return result;
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> result = new ArrayList<>();

        for (OpenDistroSecurityModule<?> module : subModules) {
            result.addAll(module.getSettings());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void registerConfigChangeListener(OpenDistroSecurityModule<?> module, DynamicConfigFactory dynamicConfigFactory) {
        OpenDistroSecurityModule.SecurityConfigMetadata<?> configMetadata = module.getSecurityConfigMetadata();

        if (configMetadata == null) {
            return;
        }

        dynamicConfigFactory.addConfigChangeListener(configMetadata.getSecurityConfigType(), (config) -> {
            Object convertedConfig = convert(configMetadata, config);

            @SuppressWarnings("rawtypes")
            Consumer consumer = configMetadata.getConfigConsumer();

            consumer.accept(convertedConfig);
        });
    }

    private <T> T convert(OpenDistroSecurityModule.SecurityConfigMetadata<T> configMetadata, SecurityDynamicConfiguration<?> value) {
        if (value == null) {
            return null;
        }

        Object entry = value.getCEntry(configMetadata.getEntry());

        if (entry == null) {
            return null;
        }

        JsonNode subNode = DefaultObjectMapper.objectMapper.valueToTree(entry).at(configMetadata.getJsonPointer());

        if (subNode == null || subNode.isMissingNode()) {
            return null;
        }

        try {
            return configMetadata.getConfigParser().parse(subNode);
        } catch (ConfigValidationException e) {
            log.error("Error while parsing configuration in " + this, e);
            return null;
        }
    }


} 
