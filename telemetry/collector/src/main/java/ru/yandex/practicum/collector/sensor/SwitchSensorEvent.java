package ru.yandex.practicum.collector.sensor;

public class SwitchSensorEvent extends SensorEvent {
    private boolean state;

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
    @Override public SensorEventType getType() { return SensorEventType.SWITCH_SENSOR_EVENT; }
}