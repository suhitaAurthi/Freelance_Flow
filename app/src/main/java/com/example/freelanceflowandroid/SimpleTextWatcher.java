package com.example.freelanceflowandroid;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.function.Consumer;

/** Lightweight TextWatcher helper for afterTextChanged callbacks. */
public abstract class SimpleTextWatcher implements TextWatcher {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) {}

    public static TextWatcher after(Consumer<CharSequence> consumer) {
        return new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                consumer.accept(s);
            }
        };
    }
}

