package ru.yandex.practicum.aggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aggregator.kafka")
public class AggregatorKafkaProperties {
    private String bootstrapServers;
    private String sensorsTopic;
    private String snapshotsTopic;
    private String groupId;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getSensorsTopic() {
        return sensorsTopic;
    }

    public String getSnapshotsTopic() {
        return snapshotsTopic;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setSensorsTopic(String sensorsTopic) {
        this.sensorsTopic = sensorsTopic;
    }

    public void setSnapshotsTopic(String snapshotsTopic) {
        this.snapshotsTopic = snapshotsTopic;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}