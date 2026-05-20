package ru.yandex.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@GrpcService
public class CollectorGrpcController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private static final String HUB_TOPIC = "telemetry.hubs.v1";
    private static final String SENSOR_TOPIC = "telemetry.sensors.v1";

    private final Producer<String, byte[]> kafkaProducer;

    public CollectorGrpcController(Producer<String, byte[]> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void collectHubEvent(HubEventProto request,
                                StreamObserver<Empty> responseObserver) {

        kafkaProducer.send(
                new ProducerRecord<>(
                        HUB_TOPIC,
                        request.getHubId(),
                        request.toByteArray()
                )
        );

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void collectSensorEvent(SensorEventProto request,
                                   StreamObserver<Empty> responseObserver) {

        kafkaProducer.send(
                new ProducerRecord<>(
                        SENSOR_TOPIC,
                        request.getId(),
                        request.toByteArray()
                )
        );

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}