package ru.yandex.practicum.collector.grpc;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.hub.*;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;

import java.time.Instant;
import java.util.List;

@Component
public class HubEventProtoMapper {

    public HubEvent toDomain(HubEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> toDeviceAddedEvent(proto);
            case DEVICE_REMOVED -> toDeviceRemovedEvent(proto);
            case SCENARIO_ADDED -> toScenarioAddedEvent(proto);
            case SCENARIO_REMOVED -> toScenarioRemovedEvent(proto);
            default -> throw new IllegalArgumentException(
                    "Unsupported hub event payload: " + proto.getPayloadCase()
            );
        };
    }

    private DeviceAddedEvent toDeviceAddedEvent(HubEventProto proto) {
        DeviceAddedEvent event = new DeviceAddedEvent();
        fillBaseFields(event, proto);

        event.setId(proto.getDeviceAdded().getId());
        event.setDeviceType(DeviceType.valueOf(proto.getDeviceAdded().getType().name()));

        return event;
    }

    private DeviceRemovedEvent toDeviceRemovedEvent(HubEventProto proto) {
        DeviceRemovedEvent event = new DeviceRemovedEvent();
        fillBaseFields(event, proto);

        event.setId(proto.getDeviceRemoved().getId());

        return event;
    }

    private ScenarioAddedEvent toScenarioAddedEvent(HubEventProto proto) {
        ScenarioAddedEvent event = new ScenarioAddedEvent();
        fillBaseFields(event, proto);

        event.setName(proto.getScenarioAdded().getName());
        event.setConditions(toConditions(proto.getScenarioAdded().getConditionList()));
        event.setActions(toActions(proto.getScenarioAdded().getActionList()));

        return event;
    }

    private ScenarioRemovedEvent toScenarioRemovedEvent(HubEventProto proto) {
        ScenarioRemovedEvent event = new ScenarioRemovedEvent();
        fillBaseFields(event, proto);

        event.setName(proto.getScenarioRemoved().getName());

        return event;
    }

    private List<ScenarioCondition> toConditions(List<ScenarioConditionProto> protoConditions) {
        return protoConditions.stream()
                .map(this::toCondition)
                .toList();
    }

    private ScenarioCondition toCondition(ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();

        condition.setSensorId(proto.getSensorId());
        condition.setType(ConditionType.valueOf(proto.getType().name()));
        condition.setOperation(ConditionOperation.valueOf(proto.getOperation().name()));
        condition.setValue(getConditionValue(proto));

        return condition;
    }

    private Object getConditionValue(ScenarioConditionProto condition) {
        return switch (condition.getValueCase()) {
            case BOOL_VALUE -> condition.getBoolValue();
            case INT_VALUE -> condition.getIntValue();
            default -> null;
        };
    }

    private List<DeviceAction> toActions(List<DeviceActionProto> protoActions) {
        return protoActions.stream()
                .map(this::toAction)
                .toList();
    }

    private DeviceAction toAction(DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();

        action.setSensorId(proto.getSensorId());
        action.setType(ActionType.valueOf(proto.getType().name()));
        action.setValue(proto.hasValue() ? proto.getValue() : null);

        return action;
    }

    private void fillBaseFields(HubEvent event, HubEventProto proto) {
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