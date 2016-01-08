package com.palantir.stash.codesearch.elasticsearch;

import org.elasticsearch.client.Client;

public interface EmbeddedEsService {
    Client getClient();
    void settingsUpdatedEvent(ElasticSearchSettingsUpdatedEvent event);
}
