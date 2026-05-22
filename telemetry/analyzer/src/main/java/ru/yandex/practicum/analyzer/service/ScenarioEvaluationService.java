package ru.yandex.practicum.analyzer.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioEvaluationService {
    private final ScenarioRepository scenarioRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public ScenarioEvaluationService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());

        for (Scenario scenario : scenarios) {
            if (matchesScenario(snapshot, scenario)) {
                executeScenario(snapshot, scenario);
            }
        }
    }

    private boolean matchesScenario(SensorsSnapshotAvro snapshot, Scenario scenario) {
        return scenario.getConditions().stream()
                .allMatch(condition -> checkCondition(snapshot, condition));
    }

    private boolean checkCondition(SensorsSnapshotAvro snapshot,
                                   ScenarioCondition scenarioCondition) {

        String sensorId = scenarioCondition.getSensor().getId();

        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        if (!states.containsKey(sensorId)) {
            return false;
        }

        SensorStateAvro sensorState = states.get(sensorId);

        Object data = sensorState.getData();

        Integer actualValue = extractValue(
                data,
                scenarioCondition.getCondition().getType()
        );

        if (actualValue == null) {
            return false;
        }

        return compare(
                actualValue,
                scenarioCondition.getCondition()
        );
    }

    private Integer extractValue(Object data, String type) {
        return switch (type) {
            case "TEMPERATURE" -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield climate.getTemperatureC();
                }

                if (data instanceof TemperatureSensorAvro temperature) {
                    yield temperature.getTemperatureC();
                }

                yield null;
            }

            case "HUMIDITY" -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield climate.getHumidity();
                }

                yield null;
            }

            case "CO2LEVEL" -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield climate.getCo2Level();
                }

                yield null;
            }

            case "LUMINOSITY" -> {
                if (data instanceof LightSensorAvro light) {
                    yield light.getLuminosity();
                }

                yield null;
            }

            case "MOTION" -> {
                if (data instanceof MotionSensorAvro motion) {
                    yield motion.getMotion() ? 1 : 0;
                }

                yield null;
            }

            case "SWITCH" -> {
                if (data instanceof SwitchSensorAvro sw) {
                    yield sw.getState() ? 1 : 0;
                }

                yield null;
            }

            default -> null;
        };
    }

    private boolean compare(Integer actualValue, Condition condition) {
        Integer expectedValue = condition.getValue();

        return switch (condition.getOperation()) {
            case "EQUALS" -> actualValue.equals(expectedValue);

            case "GREATER_THAN" -> actualValue > expectedValue;

            case "LOWER_THAN" -> actualValue < expectedValue;

            default -> false;
        };
    }

    private void executeScenario(SensorsSnapshotAvro snapshot,
                                 Scenario scenario) {

        for (ScenarioAction scenarioAction : scenario.getActions()) {
            sendAction(snapshot, scenario, scenarioAction);
        }
    }

    private void sendAction(SensorsSnapshotAvro snapshot,
                            Scenario scenario,
                            ScenarioAction scenarioAction) {

        Action action = scenarioAction.getAction();

        DeviceActionProto protoAction = DeviceActionProto.newBuilder()
                .setSensorId(scenarioAction.getSensor().getId())
                .setType(ActionTypeProto.valueOf(action.getType()))
                .setValue(action.getValue())
                .build();

        Instant now = Instant.now();

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(snapshot.getHubId())
                .setScenarioName(scenario.getName())
                .setAction(protoAction)
                .setTimestamp(timestamp)
                .build();

        hubRouterClient.handleDeviceAction(request);
    }
}