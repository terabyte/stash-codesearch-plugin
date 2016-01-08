package com.palantir.stash.codesearch.elasticsearch;

import org.elasticsearch.common.settings.Settings;

public interface ElasticSearchSettings {
    String getIndexFolder();
    void setIndexFolder(String indexfolder);

    String getClusterName();
    void setClusterName(String clusterName);

    String getHostName();
    void setHostName(String hostName);

    String getPortRange();
    void setPortRange(String portRange);

    boolean useEmbeddedES();
    void setUseEmbeddedES(boolean state);

    Settings getDefaultSettings();
}
