package com.palantir.stash.codesearch.elasticsearch;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

public class ElasticSearchSettingsImpl implements ElasticSearchSettings {
    private static final String INDEX_FOLDER_KEY = "codesearch.elasticsearch.index.folder";
    private static final String DEFAULT_INDEX_FOLDER = "/index";
    private static final String CLUSTER_NAME_KEY = "codesearch.elasticsearch.cluster.name";
    private static final String DEFAULT_CLUSTER_NAME = "stash-codesearch";
    private static final String HOST_NAME_KEY = "codesearch.elasticsearch.host.name";
    private static final String PORT_RANGE_KEY = "codesearch.elasticsearch.port.range";
    private static final String EMBEDDED_ES_KEY = "codesearch.elasticsearch.embedded";

    private final Settings DEFAULT_SETTINGS = ImmutableSettings.settingsBuilder()
            .classLoader(getClass().getClassLoader())
            .loadFromClasspath("elasticsearch.yml")
            .build();

    private final PluginSettingsFactory pluginSettingsFactory;
    private final EventPublisher eventPublisher;

    public ElasticSearchSettingsImpl(PluginSettingsFactory pluginSettingsFactory, EventPublisher eventPublisher) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.eventPublisher = eventPublisher;
    }

    private void settingsUpdated() {
        eventPublisher.publish(new ElasticSearchSettingsUpdatedEvent());
    }

    @Override
    public String getIndexFolder() {
        Object data = pluginSettingsFactory.createGlobalSettings().get(INDEX_FOLDER_KEY);
        if (data instanceof String) {
            return (String) data;
        }
        return DEFAULT_INDEX_FOLDER;
    }

    @Override
    public void setIndexFolder(String indexfolder) {
        pluginSettingsFactory.createGlobalSettings().put(INDEX_FOLDER_KEY,indexfolder);
        settingsUpdated();
    }

    @Override
    public String getClusterName() {
        Object data = pluginSettingsFactory.createGlobalSettings().get(CLUSTER_NAME_KEY);
        if (data instanceof String) {
            return (String) data;
        }
        return DEFAULT_CLUSTER_NAME;
    }

    @Override
    public void setClusterName(String clusterName) {
        pluginSettingsFactory.createGlobalSettings().put(CLUSTER_NAME_KEY,clusterName);
        settingsUpdated();
    }

    @Override
    public String getHostName() {
        Object data = pluginSettingsFactory.createGlobalSettings().get(HOST_NAME_KEY);
        if (data instanceof String) {
            return (String) data;
        }
        return DEFAULT_SETTINGS.get("client.transport.cluster.host");
    }

    @Override
    public void setHostName(String hostName) {
        pluginSettingsFactory.createGlobalSettings().put(HOST_NAME_KEY,hostName);
        settingsUpdated();
    }

    @Override
    public String getPortRange() {
        Object data = pluginSettingsFactory.createGlobalSettings().get(PORT_RANGE_KEY);
        if (data instanceof String) {
            return (String) data;
        }
        return DEFAULT_SETTINGS.get("client.transport.cluster.port");
    }

    @Override
    public void setPortRange(String portRange) {
        pluginSettingsFactory.createGlobalSettings().put(PORT_RANGE_KEY,portRange);
        settingsUpdated();
    }

    @Override
    public boolean useEmbeddedES() {
        return Boolean.TRUE.equals(pluginSettingsFactory.createGlobalSettings().get(EMBEDDED_ES_KEY));
    }

    @Override
    public void setUseEmbeddedES(boolean state) {
        pluginSettingsFactory.createGlobalSettings().put(EMBEDDED_ES_KEY,state);
        settingsUpdated();
    }

    @Override
    public Settings getDefaultSettings() {
        return DEFAULT_SETTINGS;
    }
}
