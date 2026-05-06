package ru.yandex.practicum.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collector.kafka")
public class KafkaProperties {
    private String bootstrapServers;
    private String sensorsTopic;
    private String hubsTopic;

    public String getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
    public String getSensorsTopic() { return sensorsTopic; }
    public void setSensorsTopic(String sensorsTopic) { this.sensorsTopic = sensorsTopic; }
    public String getHubsTopic() { return hubsTopic; }
    public void setHubsTopic(String hubsTopic) { this.hubsTopic = hubsTopic; }
}