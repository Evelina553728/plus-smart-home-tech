package ru.yandex.practicum.collector.grpc;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.sensor.*;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;

@Component
public class SensorEventProtoMapper {

    public SensorEvent toDomain(SensorEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case MOTION_SENSOR_EVENT -> toMotionSensorEvent(proto);
            case TEMPERATURE_SENSOR_EVENT -> toTemperatureSensorEvent(proto);
            case LIGHT_SENSOR_EVENT -> toLightSensorEvent(proto);
            case CLIMATE_SENSOR_EVENT -> toClimateSensorEvent(proto);
            case SWITCH_SENSOR_EVENT -> toSwitchSensorEvent(proto);
            default -> throw new IllegalArgumentException(
                    "Unsupported sensor event payload: " + proto.getPayloadCase()
            );
        };
    }

    private MotionSensorEvent toMotionSensorEvent(SensorEventProto proto) {
        MotionSensorEvent event = new MotionSensorEvent();
        fillBaseFields(event, proto);

        event.setLinkQuality(proto.getMotionSensorEvent().getLinkQuality());
        event.setMotion(proto.getMotionSensorEvent().getMotion());
        event.setVoltage(proto.getMotionSensorEvent().getVoltage());

        return event;
    }

    private TemperatureSensorEvent toTemperatureSensorEvent(SensorEventProto proto) {
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        fillBaseFields(event, proto);

        event.setTemperatureC(proto.getTemperatureSensorEvent().getTemperatureC());
        event.setTemperatureF(proto.getTemperatureSensorEvent().getTemperatureF());

        return event;
    }

    private LightSensorEvent toLightSensorEvent(SensorEventProto proto) {
        LightSensorEvent event = new LightSensorEvent();
        fillBaseFields(event, proto);

        event.setLinkQuality(proto.getLightSensorEvent().getLinkQuality());
        event.setLuminosity(proto.getLightSensorEvent().getLuminosity());

        return event;
    }

    private ClimateSensorEvent toClimateSensorEvent(SensorEventProto proto) {
        ClimateSensorEvent event = new ClimateSensorEvent();
        fillBaseFields(event, proto);

        event.setTemperatureC(proto.getClimateSensorEvent().getTemperatureC());
        event.setHumidity(proto.getClimateSensorEvent().getHumidity());
        event.setCo2Level(proto.getClimateSensorEvent().getCo2Level());

        return event;
    }

    private SwitchSensorEvent toSwitchSensorEvent(SensorEventProto proto) {
        SwitchSensorEvent event = new SwitchSensorEvent();
        fillBaseFields(event, proto);

        event.setState(proto.getSwitchSensorEvent().getState());

        return event;
    }

    private void fillBaseFields(SensorEvent event, SensorEventProto proto) {
        event.setId(proto.getId());
        event.setHubId(proto.getHubId());
        event.setTimestamp(toInstant(proto.getTimestamp()));
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos()
        );
    }
}