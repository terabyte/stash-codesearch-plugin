package com.palantir.stash.codesearch.elasticsearch;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;

public interface EsService {
    Client getClient();
    void resetClient();
    void testClient() throws ElasticsearchException;
}
