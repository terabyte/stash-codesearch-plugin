/**
 * Provides the elasticsearch Node and Client objects used by all other classes.
 */

package com.palantir.stash.codesearch.elasticsearch;

import org.elasticsearch.client.Client;

public interface ElasticSearch {

    String ES_UPDATEALIAS = "scs-update";

    String ES_SEARCHALIAS = "scs-search";

    Client getClient();

}
