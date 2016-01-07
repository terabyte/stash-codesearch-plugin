/**
 * Contains the elasticsearch Node and Client objects used by all other classes.
 *
 * Config is loaded from elasticsearch.yml in the resources path.
 */

package com.palantir.stash.codesearch.elasticsearch;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.client.transport.TransportClient;

public class ElasticSearchImpl implements ElasticSearch, LifecycleAware {
    private final Logger log = Logger.getLogger(getClass());

    private final Settings DEFAULT_SETTINGS = ImmutableSettings.settingsBuilder()
            .classLoader(getClass().getClassLoader())
            .loadFromClasspath("elasticsearch.yml")
            .build();

    private TransportClient client;

    @Override
    public TransportClient getClient () {
        if (client==null) {
            initClient();
        }
        return client;
    }

    private String getHostname() {
        // todo: use elasticsearch.yaml as the source of the default
        String hostname = null;
        // get hostname from elasticsearch.yml
        hostname = DEFAULT_SETTINGS.get("client.transport.cluster.host");

        if (hostname==null) {
            log.error("Unable to determine hostname");
            hostname = ""; // avoid NPE's
        }

        return hostname;
    }

    private Tuple<Integer,Integer> getPortRange() {
        // todo: use elasticsearch.yaml as the source of the default
        Tuple<Integer,Integer> portRange = null;

        // get port range from elasticsearch.yml
        String portSetting = DEFAULT_SETTINGS.get("client.transport.cluster.port");
        String[] toks = portSetting.split("-");
        if (toks.length == 1) {
            try {
                int port = Integer.parseInt(toks[0]);
                portRange = new Tuple<>(port,port);
            } catch (NumberFormatException e) {
                log.error("Error parsing client.transport.cluster.port", e);
            }
        } else if (toks.length == 2) {
            try {
                int fromPort = Integer.parseInt(toks[0]);
                int toPort = Integer.parseInt(toks[1]);
                portRange = new Tuple<>(fromPort,toPort);
            } catch (NumberFormatException e) {
                log.error("Error parsing client.transport.cluster.port", e);
            }
        }

        if (portRange==null) {
            log.error("Unable to determine port range");
            portRange = new Tuple<>(0,0);// fail in a non-destructive way
        }

        return portRange;
    }

    private void initClient() {
        if (client!=null) {
            client.close();
        }

        client = new TransportClient(DEFAULT_SETTINGS);
        String host = getHostname();
        Tuple<Integer,Integer> portRange = getPortRange();

        // Create a new transport address for reach port in the port range
        for (int port = portRange.v1(); port <= portRange.v2(); ++port) {
            client.addTransportAddress(new InetSocketTransportAddress(host, port));
        }
    }

    @Override
    public void onStart() {
        initClient();
    }

    @Override
    public void onStop() {
        if (client!=null) {
            client.close();
        }
    }
}
