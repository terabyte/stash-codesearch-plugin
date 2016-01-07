/**
 * Contains the elasticsearch Node and Client objects used by all other classes.
 *
 * Config is loaded from elasticsearch.yml in the resources path.
 */

package com.palantir.stash.codesearch.elasticsearch;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.client.transport.TransportClient;

public class ElasticSearchImpl implements ElasticSearch, LifecycleAware {

    private final TransportClient client;

    public ElasticSearchImpl () throws SettingsException {
        client = new TransportClient();
    }

    @Override
    public TransportClient getClient () {
        return client;
    }

    @Override
    public void onStart() {
        // HACK: get hostname from elasticsearch.yml todo: use elasticsearch.yaml as the source of the default
        String host = client.settings().get("client.transport.cluster.host");
        if (host == null) {
            throw new SettingsException(
                    "client.transport.cluster.host must be set to a cluster data node address");
        }

        // HACK: get port/port range from elasticsearch.yml (format for ranges is 1234-2345)
        int fromPort = 0, toPort = 0;
        String portSetting = client.settings().get("client.transport.cluster.port");
        if (portSetting == null) {
            throw new SettingsException(
                    "client.transport.cluster.port mulst be set to a valid port range.");
        }
        String[] toks = portSetting.split("-");
        if (toks.length == 1) {
            try {
                fromPort = toPort = Integer.parseInt(toks[0]);
            } catch (NumberFormatException e) {
                throw new SettingsException("Error parsing client.transport.cluster.port", e);
            }
        } else if (toks.length == 2) {
            try {
                fromPort = Integer.parseInt(toks[0]);
                toPort = Integer.parseInt(toks[1]);
            } catch (NumberFormatException e) {
                throw new SettingsException("Error parsing client.transport.cluster.port", e);
            }
        } else {
            throw new SettingsException(
                    "Error parsing client.transport.cluster.port (too may dashes)");
        }

        // Create a new transport address for reach port in the port range
        for (int port = fromPort; port <= toPort; ++port) {
            client.addTransportAddress(new InetSocketTransportAddress(host, port));
        }
    }

    @Override
    public void onStop() {
        client.close();
    }
}
