/**
 * Contains the elasticsearch Node and Client objects used by all other classes.
 *
 * Config is loaded from elasticsearch.yml in the resources path.
 */

package com.palantir.stash.codesearch.elasticsearch;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;

public class ElasticSearchImpl implements ElasticSearch {
    private final ElasticSearchSettings elasticSearchSettings;
    private final EmbeddedEsService embeddedEsService;
    private final RemoteEsService remoteEsService;

    public ElasticSearchImpl(ElasticSearchSettings elasticSearchSettings, EmbeddedEsService embeddedEsService, RemoteEsService remoteEsService) {
        this.elasticSearchSettings = elasticSearchSettings;
        this.embeddedEsService = embeddedEsService;
        this.remoteEsService = remoteEsService;
    }

    private EsService getEsService() {
        if (elasticSearchSettings.useEmbeddedES()) {
            return embeddedEsService;
        }
        return remoteEsService;
    }

    @Override
    public Client getClient () {
        return getEsService().getClient();
    }

    @Override
    public void resetClient() {
        getEsService().resetClient();
    }

    @Override
    public void testClient() throws ElasticsearchException {
        getEsService().testClient();
    }
}
