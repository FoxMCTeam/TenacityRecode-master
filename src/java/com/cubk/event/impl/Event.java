package com.cubk.event.impl;

import lombok.Getter;

/**
 * Marker interface for events.
 */
public interface Event {
    // This interface doesn't contain any methods or fields.
    // It's used to mark classes as events.
    @Getter
    class StateEvent extends CancellableEvent {
        private boolean pre = true;

        public boolean isPost() {
            return !pre;
        }

        public void setPost() {
            pre = false;
        }
    }
}