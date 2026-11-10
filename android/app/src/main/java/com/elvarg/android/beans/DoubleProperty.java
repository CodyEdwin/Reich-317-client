package com.elvarg.android.beans;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A minimal observable {@code double} property, standing in for JavaFX's
 * {@code DoubleProperty}. Only the operations the login screen uses are
 * implemented: {@link #get()}, {@link #set(double)}, {@link #addListener} and
 * {@link #removeListener}.
 */
public class DoubleProperty implements ObservableValue<Number> {

    private final CopyOnWriteArrayList<ChangeListener<? super Number>> listeners = new CopyOnWriteArrayList<>();
    private double value;

    public DoubleProperty() {
    }

    public DoubleProperty(double initialValue) {
        this.value = initialValue;
    }

    public double get() {
        return value;
    }

    public void set(double newValue) {
        double oldValue = this.value;
        this.value = newValue;
        if (oldValue != newValue) {
            for (ChangeListener<? super Number> listener : listeners) {
                listener.changed(this, oldValue, newValue);
            }
        }
    }

    @Override
    public Number getValue() {
        return value;
    }

    public void setValue(Number newValue) {
        set(newValue.doubleValue());
    }

    public void addListener(ChangeListener<? super Number> listener) {
        listeners.add(listener);
    }

    public void removeListener(ChangeListener<? super Number> listener) {
        listeners.remove(listener);
    }
}
