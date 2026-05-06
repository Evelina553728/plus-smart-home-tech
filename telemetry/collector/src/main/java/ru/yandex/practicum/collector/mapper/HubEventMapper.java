package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

@Component
public class HubEventMapper {
    public HubEventAvro toAvro(HubEvent event) {
        return new HubEventAvro(
                event.getHubId(),
                event.getTimestamp(),
                toPayload(event)
        );
    }

    private Object toPayload(HubEvent event) {
        if (event instanceof DeviceAddedEvent e) {
            return new DeviceAddedEventAvro(e.getId(), DeviceTypeAvro.valueOf(e.getDeviceType().name()));
        }
        if (event instanceof DeviceRemovedEvent e) {
            return new DeviceRemovedEventAvro(e.getId());
        }
        if (event instanceof ScenarioAddedEvent e) {
            return new ScenarioAddedEventAvro(
                    e.getName(),
                    toConditionAvroList(e.getConditions()),
                    toActionAvroList(e.getActions())
            );
        }
        if (event instanceof ScenarioRemovedEvent e) {
            return new ScenarioRemovedEventAvro(e.getName());
        }
        throw new IllegalArgumentException("Unsupported hub event type: " + event.getClass().getName());
    }

    private List<ScenarioConditionAvro> toConditionAvroList(List<ScenarioCondition> conditions) {
        return conditions.stream()
                .map(condition -> new ScenarioConditionAvro(
                        condition.getSensorId(),
                        ConditionTypeAvro.valueOf(condition.getType().name()),
                        ConditionOperationAvro.valueOf(condition.getOperation().name()),
                        normalizeConditionValue(condition.getValue())
                ))
                .toList();
    }

    private List<DeviceActionAvro> toActionAvroList(List<DeviceAction> actions) {
        return actions.stream()
                .map(action -> new DeviceActionAvro(
                        action.getSensorId(),
                        ActionTypeAvro.valueOf(action.getType().name()),
                        action.getValue()
                ))
                .toList();
    }

    private Object normalizeConditionValue(Object value) {
        if (value == null || value instanceof Boolean || value instanceof Integer) {
            return value;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Condition value must be null, integer or boolean");
    }
}