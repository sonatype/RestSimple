package org.sonatype.etag;

public class ETagChangeListenerEvent {

    private boolean cancel = false;

    private final long value;

    private final long prevValue;

    public ETagChangeListenerEvent(long value, long prevValue) {
        this.value = value;
        this.prevValue = prevValue;
    }

    public void cancel() {
        cancel = true;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public long newValue() {
        return value;
    }

    public long previousValue() {
        return prevValue;
    }
}
