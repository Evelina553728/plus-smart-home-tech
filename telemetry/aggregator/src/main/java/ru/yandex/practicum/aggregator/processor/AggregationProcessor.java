package ru.yandex.practicum.aggregator.processor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.config.AggregatorKafkaProperties;
import ru.yandex.practicum.aggregator.kafka.AvroDeserializer;
import ru.yandex.practicum.aggregator.kafka.AvroSerializer;
import ru.yandex.practicum.aggregator.service.SnapshotService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Component
public class AggregationProcessor {
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);

    private final AggregatorKafkaProperties kafkaProperties;
    private final SnapshotService snapshotService;

    private final AvroDeserializer<SensorEventAvro> deserializer =
            new AvroDeserializer<>(SensorEventAvro.class);

    private final AvroSerializer<SensorsSnapshotAvro> serializer =
            new AvroSerializer<>();

    public AggregationProcessor(AggregatorKafkaProperties kafkaProperties,
                                SnapshotService snapshotService) {
        this.kafkaProperties = kafkaProperties;
        this.snapshotService = snapshotService;
    }

    public void start() {
        Properties consumerProperties = createConsumerProperties();
        Properties producerProperties = createProducerProperties();

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerProperties);
             KafkaProducer<String, byte[]> producer = new KafkaProducer<>(producerProperties)) {

            consumer.subscribe(List.of(kafkaProperties.getSensorsTopic()));

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                try {
                    for (ConsumerRecord<String, byte[]> record : consumer.poll(POLL_TIMEOUT)) {
                        SensorEventAvro event = deserializer.deserialize(record.value());

                        Optional<SensorsSnapshotAvro> snapshot = snapshotService.updateState(event);

                        snapshot.ifPresent(value -> sendSnapshot(producer, value));
                    }

                    consumer.commitSync();
                } catch (WakeupException ignored) {
                    break;
                } catch (Exception e) {
                    throw new IllegalStateException("Error while aggregating sensor events", e);
                }
            }
        }
    }

    private void sendSnapshot(KafkaProducer<String, byte[]> producer,
                              SensorsSnapshotAvro snapshot) {
        byte[] data = serializer.serialize(snapshot);

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                kafkaProperties.getSnapshotsTopic(),
                snapshot.getHubId(),
                data
        );

        producer.send(record);
        producer.flush();
    }

    private Properties createConsumerProperties() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return properties;
    }

    private Properties createProducerProperties() {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return properties;
    }
}