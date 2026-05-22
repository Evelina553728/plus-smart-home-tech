package ru.yandex.practicum.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer.kafka")
public class AnalyzerKafkaProperties {
    private String bootstrapServers;
    private String snapshotsTopic;
    private String hubsTopic;
    private String snapshotsGroupId;
    private String hubsGroupId;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getSnapshotsTopic() {
        return snapshotsTopic;
    }

    public String getHubsTopic() {
        return hubsTopic;
    }

    public String getSnapshotsGroupId() {
        return snapshotsGroupId;
    }

    public String getHubsGroupId() {
        return hubsGroupId;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setSnapshotsTopic(String snapshotsTopic) {
        this.snapshotsTopic = snapshotsTopic;
    }

    public void setHubsTopic(String hubsTopic) {
        this.hubsTopic = hubsTopic;
    }

    public void setSnapshotsGroupId(String snapshotsGroupId) {
        this.snapshotsGroupId = snapshotsGroupId;
    }

    public void setHubsGroupId(String hubsGroupId) {
        this.hubsGroupId = hubsGroupId;
    }
}