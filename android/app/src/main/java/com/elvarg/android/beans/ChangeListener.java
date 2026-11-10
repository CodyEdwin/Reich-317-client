package com.elvarg.android.beans;

/** Android-neutral stand-in for JavaFX's {@code ChangeListener}. */
public interface ChangeListener<T> {
    void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);
}
