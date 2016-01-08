package com.palantir.stash.codesearch.elasticsearch;

import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.DisposableBean;

public class EmbeddedEsServiceImpl implements EmbeddedEsService, DisposableBean {
    private final ApplicationPropertiesService applicationPropertiesService;
    private final ElasticSearchSettings elasticSearchSettings;
    private final EventPublisher eventPublisher;

    private Node node;
    private Client client;

    public EmbeddedEsServiceImpl(ApplicationPropertiesService applicationPropertiesService, ElasticSearchSettings elasticSearchSettings, EventPublisher eventPublisher) {
        this.applicationPropertiesService = applicationPropertiesService;
        this.elasticSearchSettings = elasticSearchSettings;
        this.eventPublisher = eventPublisher;
        this.eventPublisher.register(this);
    }

    private String getIndexFolder() {
        return applicationPropertiesService.getHomeDir().getAbsolutePath() + elasticSearchSettings.getIndexFolder();
    }

    private Node getNode() {
        if (node==null) {
            Settings settings = ImmutableSettings.settingsBuilder()
                    .classLoader(getClass().getClassLoader())
                    .put("path.data", getIndexFolder())
                    .build();
            node = NodeBuilder.nodeBuilder()
                    .settings(settings)
                    .clusterName(elasticSearchSettings.getClusterName())
                    .data(true)
                    .local(true)
                    .node();
        }
        return node;
    }

    @Override
    public Client getClient() {
        if (client==null) {
            client = getNode().client();
        }
        return client;
    }

    private void reset() {
        if (client!=null) {
            client.close();
            client = null;
        }
        if (node!=null) {
            node.close();
            node = null;
        }
    }

    @Override
    public void destroy() throws Exception {
        reset();
        eventPublisher.unregister(this);
    }

    @EventListener
    public void settingsUpdatedEvent(ElasticSearchSettingsUpdatedEvent event) {
        reset();
    }
}
