package ru.yandex.practicum.analyzer.kafka;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.IOException;

public class AvroDeserializer<T extends SpecificRecordBase> {
    private final DatumReader<T> reader;

    public AvroDeserializer(Class<T> targetType) {
        this.reader = new SpecificDatumReader<>(targetType);
    }

    public T deserialize(byte[] data) {
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot deserialize Avro event", e);
        }
    }
}