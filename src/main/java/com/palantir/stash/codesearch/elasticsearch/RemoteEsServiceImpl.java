package com.palantir.stash.codesearch.elasticsearch;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.DisposableBean;

public class RemoteEsServiceImpl implements RemoteEsService, DisposableBean {
    private final Logger log = Logger.getLogger(getClass());

    private final ElasticSearchSettings elasticSearchSettings;
    private final EventPublisher eventPublisher;

    private TransportClient client;

    public RemoteEsServiceImpl(ElasticSearchSettings elasticSearchSettings, EventPublisher eventPublisher) {
        this.elasticSearchSettings = elasticSearchSettings;
        this.eventPublisher = eventPublisher;
        this.eventPublisher.register(this);
    }

    private Tuple<Integer,Integer> splitPortRange(String portSetting) {
        Tuple<Integer,Integer> portRange = null;
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

    @Override
    public Client getClient() {
        if (client==null) {
            client = new TransportClient(elasticSearchSettings.getDefaultSettings());
            String host = elasticSearchSettings.getHostName();
            Tuple<Integer,Integer> portRange = splitPortRange(elasticSearchSettings.getPortRange());

            // Create a new transport address for reach port in the port range
            for (int port = portRange.v1(); port <= portRange.v2(); ++port) {
                client.addTransportAddress(new InetSocketTransportAddress(host, port));
            }
        }
        return client;
    }

    private void reset() {
        if (client!=null) {
            client.close();
            client = null;
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
