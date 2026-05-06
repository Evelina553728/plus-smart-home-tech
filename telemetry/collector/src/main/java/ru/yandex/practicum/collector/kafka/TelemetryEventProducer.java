package ru.yandex.practicum.collector.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Component
public class TelemetryEventProducer {
    private final KafkaProducer<String, byte[]> kafkaProducer;
    private final KafkaProperties kafkaProperties;
    private final ru.yandex.practicum.collector.kafka.AvroSerializer avroSerializer;

    public TelemetryEventProducer(KafkaProducer<String, byte[]> kafkaProducer,
                                  KafkaProperties kafkaProperties,
                                  ru.yandex.practicum.collector.kafka.AvroSerializer avroSerializer) {
        this.kafkaProducer = kafkaProducer;
        this.kafkaProperties = kafkaProperties;
        this.avroSerializer = avroSerializer;
    }

    public void send(SensorEventAvro event) {
        send(kafkaProperties.getSensorsTopic(), event.getHubId(), event);
    }

    public void send(HubEventAvro event) {
        send(kafkaProperties.getHubsTopic(), event.getHubId(), event);
    }

    private void send(String topic, String key, SpecificRecordBase event) {
        kafkaProducer.send(new ProducerRecord<>(topic, key, avroSerializer.serialize(event)));
    }
}