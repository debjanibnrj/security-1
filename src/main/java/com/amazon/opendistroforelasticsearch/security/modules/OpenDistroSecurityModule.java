package com.amazon.opendistroforelasticsearch.security.modules;

import com.amazon.opendistroforelasticsearch.security.configuration.ConfigurationRepository;
import com.amazon.opendistroforelasticsearch.security.securityconf.DynamicConfigFactory;
import com.amazon.opendistroforelasticsearch.security.validation.JsonNodeParser;
import com.fasterxml.jackson.core.JsonPointer;

import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionResponse;
import org.opensearch.client.Client;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.settings.ClusterSettings;
import org.opensearch.common.settings.IndexScopedSettings;
import org.opensearch.common.settings.Setting;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.SettingsFilter;
import org.opensearch.common.xcontent.NamedXContentRegistry;
import org.opensearch.env.Environment;
import org.opensearch.plugins.ActionPlugin;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestHandler;
import org.opensearch.script.ScriptContext;
import org.opensearch.script.ScriptService;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.watcher.ResourceWatcherService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface OpenDistroSecurityModule<T> {
    default List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings,
                                              IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver,
                                              Supplier<DiscoveryNodes> nodesInCluster) {
        return Collections.emptyList();
    }

    default List<ActionPlugin.ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Collections.emptyList();
    }

    default List<ScriptContext<?>> getContexts() {
        return Collections.emptyList();
    }

    default Collection<Object> createComponents(BaseDependencies baseDependencies) {
        return Collections.emptyList();
    }

    default public List<Setting<?>> getSettings() {
        return Collections.emptyList();
    }

    default public SecurityConfigMetadata<T> getSecurityConfigMetadata() {
        return null;
    }

    public class SecurityConfigMetadata<T> {
        private final Class<?> securityConfigType;
        private final String entry;
        private final JsonPointer jsonPointer;
        private final JsonNodeParser<T> configParser;
        private final Consumer<T> configConsumer;

        public SecurityConfigMetadata(Class<?> securityConfigType, String entry, JsonPointer jsonPointer, JsonNodeParser<T> configParser,
                                Consumer<T> configConsumer) {
            super();
            this.securityConfigType = securityConfigType;
            this.entry = entry;
            this.jsonPointer = jsonPointer;
            this.configParser = configParser;
            this.configConsumer = configConsumer;
        }

        public Class<?> getSecurityConfigType() {
            return securityConfigType;
        }

        public String getEntry() {
            return entry;
        }

        public JsonPointer getJsonPointer() {
            return jsonPointer;
        }

        public JsonNodeParser<T> getConfigParser() {
            return configParser;
        }

        public Consumer<T> getConfigConsumer() {
            return configConsumer;
        }

    }

    public class BaseDependencies {

        private Settings settings;
        private Client localClient;
        private ClusterService clusterService;
        private ThreadPool threadPool;
        private ResourceWatcherService resourceWatcherService;
        private ScriptService scriptService;
        private NamedXContentRegistry xContentRegistry;
        private Environment environment;
        private IndexNameExpressionResolver indexNameExpressionResolver;
        private DynamicConfigFactory dynamicConfigFactory;
        private ConfigurationRepository configurationRepository;
        private ProtectedIndices protectedIndices;

        public BaseDependencies(Settings settings, Client localClient, ClusterService clusterService, ThreadPool threadPool,
                                ResourceWatcherService resourceWatcherService, ScriptService scriptService, NamedXContentRegistry xContentRegistry,
                                Environment environment, IndexNameExpressionResolver indexNameExpressionResolver, DynamicConfigFactory dynamicConfigFactory,
                                ConfigurationRepository configurationRepository, ProtectedIndices protectedIndices) {
            super();
            this.settings = settings;
            this.localClient = localClient;
            this.clusterService = clusterService;
            this.threadPool = threadPool;
            this.resourceWatcherService = resourceWatcherService;
            this.scriptService = scriptService;
            this.xContentRegistry = xContentRegistry;
            this.environment = environment;
            this.indexNameExpressionResolver = indexNameExpressionResolver;
            this.dynamicConfigFactory = dynamicConfigFactory;
            this.configurationRepository = configurationRepository;
            this.protectedIndices = protectedIndices;
        }

        public Settings getSettings() {
            return settings;
        }

        public void setSettings(Settings settings) {
            this.settings = settings;
        }

        public Client getLocalClient() {
            return localClient;
        }

        public void setLocalClient(Client localClient) {
            this.localClient = localClient;
        }

        public ClusterService getClusterService() {
            return clusterService;
        }

        public void setClusterService(ClusterService clusterService) {
            this.clusterService = clusterService;
        }

        public ThreadPool getThreadPool() {
            return threadPool;
        }

        public void setThreadPool(ThreadPool threadPool) {
            this.threadPool = threadPool;
        }

        public ResourceWatcherService getResourceWatcherService() {
            return resourceWatcherService;
        }

        public void setResourceWatcherService(ResourceWatcherService resourceWatcherService) {
            this.resourceWatcherService = resourceWatcherService;
        }

        public ScriptService getScriptService() {
            return scriptService;
        }

        public void setScriptService(ScriptService scriptService) {
            this.scriptService = scriptService;
        }

        public NamedXContentRegistry getxContentRegistry() {
            return xContentRegistry;
        }

        public void setxContentRegistry(NamedXContentRegistry xContentRegistry) {
            this.xContentRegistry = xContentRegistry;
        }

        public Environment getEnvironment() {
            return environment;
        }

        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        public IndexNameExpressionResolver getIndexNameExpressionResolver() {
            return indexNameExpressionResolver;
        }

        public void setIndexNameExpressionResolver(IndexNameExpressionResolver indexNameExpressionResolver) {
            this.indexNameExpressionResolver = indexNameExpressionResolver;
        }

        public DynamicConfigFactory getDynamicConfigFactory() {
            return dynamicConfigFactory;
        }

        public void setDynamicConfigFactory(DynamicConfigFactory dynamicConfigFactory) {
            this.dynamicConfigFactory = dynamicConfigFactory;
        }

        public ConfigurationRepository getConfigurationRepository() {
            return configurationRepository;
        }

        public void setConfigurationRepository(ConfigurationRepository configurationRepository) {
            this.configurationRepository = configurationRepository;
        }

        public ProtectedIndices getProtectedIndices() {
            return protectedIndices;
        }

        public void setProtectedIndices(ProtectedIndices protectedIndices) {
            this.protectedIndices = protectedIndices;
        }
    }

}
