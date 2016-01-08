package com.palantir.stash.codesearch.elasticsearch;

import org.elasticsearch.client.Client;

public interface RemoteEsService {
    Client getClient();
    void settingsUpdatedEvent(ElasticSearchSettingsUpdatedEvent event);
}
