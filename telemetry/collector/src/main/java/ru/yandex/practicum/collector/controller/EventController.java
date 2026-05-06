package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.hub.HubEvent;
import ru.yandex.practicum.collector.kafka.TelemetryEventProducer;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.collector.sensor.SensorEvent;

@RestController
@RequestMapping("/events")
public class EventController {
    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;
    private final TelemetryEventProducer producer;

    public EventController(SensorEventMapper sensorEventMapper,
                           HubEventMapper hubEventMapper,
                           TelemetryEventProducer producer) {
        this.sensorEventMapper = sensorEventMapper;
        this.hubEventMapper = hubEventMapper;
        this.producer = producer;
    }

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.CREATED)
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        producer.send(sensorEventMapper.toAvro(event));
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.CREATED)
    public void collectHubEvent(@Valid @RequestBody HubEvent event) {
        producer.send(hubEventMapper.toAvro(event));
    }
}