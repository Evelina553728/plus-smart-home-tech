package ru.yandex.practicum.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.analyzer.kafka.AvroDeserializer;
import ru.yandex.practicum.analyzer.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class HubEventProcessor implements Runnable {
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);

    private final AnalyzerKafkaProperties kafkaProperties;
    private final HubEventService hubEventService;
    private final AvroDeserializer<HubEventAvro> deserializer =
            new AvroDeserializer<>(HubEventAvro.class);

    public HubEventProcessor(AnalyzerKafkaProperties kafkaProperties,
                             HubEventService hubEventService) {
        this.kafkaProperties = kafkaProperties;
        this.hubEventService = hubEventService;
    }

    @Override
    public void run() {
        Properties properties = createConsumerProperties();

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(List.of(kafkaProperties.getHubsTopic()));

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                try {
                    for (ConsumerRecord<String, byte[]> record : consumer.poll(POLL_TIMEOUT)) {
                        HubEventAvro event = deserializer.deserialize(record.value());
                        hubEventService.handle(event);
                    }

                    consumer.commitSync();
                } catch (WakeupException ignored) {
                    break;
                } catch (Exception e) {
                    throw new IllegalStateException("Error while processing hub events", e);
                }
            }
        }
    }

    private Properties createConsumerProperties() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getHubsGroupId());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return properties;
    }
}