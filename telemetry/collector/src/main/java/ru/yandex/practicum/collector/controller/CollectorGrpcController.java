package ru.yandex.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.grpc.HubEventProtoMapper;
import ru.yandex.practicum.collector.grpc.SensorEventProtoMapper;
import ru.yandex.practicum.collector.kafka.TelemetryEventProducer;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@GrpcService
public class CollectorGrpcController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final SensorEventProtoMapper sensorEventProtoMapper;
    private final HubEventProtoMapper hubEventProtoMapper;
    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;
    private final TelemetryEventProducer producer;

    public CollectorGrpcController(SensorEventProtoMapper sensorEventProtoMapper,
                                   HubEventProtoMapper hubEventProtoMapper,
                                   SensorEventMapper sensorEventMapper,
                                   HubEventMapper hubEventMapper,
                                   TelemetryEventProducer producer) {
        this.sensorEventProtoMapper = sensorEventProtoMapper;
        this.hubEventProtoMapper = hubEventProtoMapper;
        this.sensorEventMapper = sensorEventMapper;
        this.hubEventMapper = hubEventMapper;
        this.producer = producer;
    }

    @Override
    public void collectHubEvent(HubEventProto request,
                                StreamObserver<Empty> responseObserver) {
        try {
            producer.send(hubEventMapper.toAvro(hubEventProtoMapper.toDomain(request)));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            responseObserver.onError(e);
        }
    }

    @Override
    public void collectSensorEvent(SensorEventProto request,
                                   StreamObserver<Empty> responseObserver) {
        try {
            producer.send(sensorEventMapper.toAvro(sensorEventProtoMapper.toDomain(request)));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            responseObserver.onError(e);
        }
    }
}