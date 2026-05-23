package ru.yandex.practicum.analyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.model.Sensor;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.util.List;

@Service
public class HubEventService {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    public HubEventService(SensorRepository sensorRepository,
                           ScenarioRepository scenarioRepository) {
        this.sensorRepository = sensorRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Transactional
    public void handle(HubEventAvro event) {
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro deviceAddedEvent) {
            addSensor(event.getHubId(), deviceAddedEvent);
            return;
        }

        if (payload instanceof DeviceRemovedEventAvro deviceRemovedEvent) {
            removeSensor(event.getHubId(), deviceRemovedEvent);
            return;
        }

        if (payload instanceof ScenarioAddedEventAvro scenarioAddedEvent) {
            addScenario(event.getHubId(), scenarioAddedEvent);
            return;
        }

        if (payload instanceof ScenarioRemovedEventAvro scenarioRemovedEvent) {
            removeScenario(event.getHubId(), scenarioRemovedEvent);
        }
    }

    private void addSensor(String hubId, DeviceAddedEventAvro event) {
        if (sensorRepository.existsById(event.getId())) {
            return;
        }

        Sensor sensor = new Sensor(event.getId(), hubId);
        sensorRepository.save(sensor);
    }

    private void removeSensor(String hubId, DeviceRemovedEventAvro event) {
        sensorRepository.findByIdAndHubId(event.getId(), hubId)
                .ifPresent(sensorRepository::delete);
    }

    private void addScenario(String hubId, ScenarioAddedEventAvro event) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenarioRepository::delete);

        Scenario scenario = new Scenario(hubId, event.getName());

        addConditions(scenario, event.getConditions());
        addActions(scenario, event.getActions());

        scenarioRepository.save(scenario);
    }

    private void addConditions(Scenario scenario, List<ScenarioConditionAvro> conditions) {
        for (ScenarioConditionAvro conditionAvro : conditions) {
            Sensor sensor = sensorRepository.findByIdAndHubId(
                    conditionAvro.getSensorId(),
                    scenario.getHubId()
            ).orElseGet(() -> sensorRepository.save(
                    new Sensor(conditionAvro.getSensorId(), scenario.getHubId())
            ));

            Condition condition = new Condition(
                    conditionAvro.getType().name(),
                    conditionAvro.getOperation().name(),
                    toIntegerValue(conditionAvro.getValue())
            );

            ScenarioCondition scenarioCondition = new ScenarioCondition(scenario, sensor, condition);

            scenario.getConditions().add(scenarioCondition);
        }
    }

    private void addActions(Scenario scenario, List<DeviceActionAvro> actions) {
        for (DeviceActionAvro actionAvro : actions) {
            Sensor sensor = sensorRepository.findByIdAndHubId(
                    actionAvro.getSensorId(),
                    scenario.getHubId()
            ).orElseGet(() -> sensorRepository.save(
                    new Sensor(actionAvro.getSensorId(), scenario.getHubId())
            ));

            Action action = new Action(
                    actionAvro.getType().name(),
                    actionAvro.getValue()
            );

            ScenarioAction scenarioAction = new ScenarioAction(scenario, sensor, action);

            scenario.getActions().add(scenarioAction);
        }
    }

    private void removeScenario(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepository.deleteByHubIdAndName(hubId, event.getName());
    }

    private Integer toIntegerValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }

        throw new IllegalArgumentException("Unsupported condition value type: " + value.getClass());
    }
}