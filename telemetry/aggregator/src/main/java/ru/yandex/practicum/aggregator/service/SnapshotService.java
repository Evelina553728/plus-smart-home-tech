package ru.yandex.practicum.aggregator.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SnapshotService {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {

        SensorsSnapshotAvro currentSnapshot = snapshots.computeIfAbsent(
                event.getHubId(),
                hubId -> new SensorsSnapshotAvro(
                        hubId,
                        event.getTimestamp(),
                        new HashMap<>()
                )
        );

        SensorStateAvro newState = new SensorStateAvro(
                event.getTimestamp(),
                event.getPayload()
        );

        SensorStateAvro oldState =
                currentSnapshot.getSensorsState().get(event.getId());

        if (oldState != null &&
                oldState.getData().equals(newState.getData())) {

            return Optional.empty();
        }

        currentSnapshot.getSensorsState()
                .put(event.getId(), newState);

        currentSnapshot.setTimestamp(event.getTimestamp());

        SensorsSnapshotAvro snapshotCopy = copySnapshot(currentSnapshot);

        return Optional.of(snapshotCopy);
    }

    private SensorsSnapshotAvro copySnapshot(SensorsSnapshotAvro snapshot) {

        Map<String, SensorStateAvro> copiedStates = new HashMap<>();

        for (Map.Entry<String, SensorStateAvro> entry :
                snapshot.getSensorsState().entrySet()) {

            SensorStateAvro state = entry.getValue();

            SensorStateAvro copiedState = new SensorStateAvro(
                    state.getTimestamp(),
                    state.getData()
            );

            copiedStates.put(entry.getKey(), copiedState);
        }

        return new SensorsSnapshotAvro(
                snapshot.getHubId(),
                snapshot.getTimestamp(),
                copiedStates
        );
    }
}