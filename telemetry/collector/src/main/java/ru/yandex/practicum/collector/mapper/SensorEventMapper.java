package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        Object payload = toPayload(event);

        System.err.println("SENSOR EVENT DOMAIN: " + event.getClass().getName());
        System.err.println("SENSOR EVENT AVRO PAYLOAD: " + payload.getClass().getName());

        return new SensorEventAvro(
                event.getId(),
                event.getHubId(),
                event.getTimestamp(),
                payload
        );
    }

    private Object toPayload(SensorEvent event) {
        if (event instanceof ClimateSensorEvent e) {
            return new ClimateSensorAvro(
                    e.getTemperatureC(),
                    e.getHumidity(),
                    e.getCo2Level()
            );
        }

        if (event instanceof LightSensorEvent e) {
            return new LightSensorAvro(
                    e.getLinkQuality(),
                    e.getLuminosity()
            );
        }

        if (event instanceof MotionSensorEvent e) {
            return new MotionSensorAvro(
                    e.getLinkQuality(),
                    e.isMotion(),
                    e.getVoltage()
            );
        }

        if (event instanceof SwitchSensorEvent e) {
            return new SwitchSensorAvro(
                    e.isState()
            );
        }

        if (event instanceof TemperatureSensorEvent e) {
            return new TemperatureSensorAvro(
                    e.getId(),
                    e.getHubId(),
                    e.getTimestamp(),
                    e.getTemperatureC(),
                    e.getTemperatureF()
            );
        }

        throw new IllegalArgumentException(
                "Unsupported sensor event type: " + event.getClass().getName()
        );
    }
}