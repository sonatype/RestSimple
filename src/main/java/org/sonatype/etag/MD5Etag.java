package org.sonatype.etag;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class MD5Etag implements ETag {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private final AtomicLong etag;
    private final ConcurrentLinkedQueue<ETagChangeListener> listeners = new ConcurrentLinkedQueue<ETagChangeListener>();

    public MD5Etag() {
        this.etag = new AtomicLong(Math.abs(new Random().nextLong()));
    }

    public final void regenerateEtag() {
        announce(etag.getAndIncrement());
    }

    @Override
    public String getEtag() {
        try {
            MessageDigest messageDigestMd5 = MessageDigest.getInstance("MD5");
            messageDigestMd5.update(String.valueOf(etag).getBytes());

            return new String(encodeHex(messageDigestMd5.digest(), DIGITS_LOWER));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 digest algorithm available");
        }
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    @Override
    public void addChangeListener(ETagChangeListener listener) {
        listeners.offer(listener);
    }

    @Override
    public void removeChangeListener(ETagChangeListener listener) {
        listeners.remove(listener);
    }

    void announce(long prevValue) {
        Iterator<ETagChangeListener> i = listeners.iterator();
        ETagChangeListenerEvent e = new ETagChangeListenerEvent(etag.get(), prevValue);
        while (i.hasNext()) {
            i.next().onChange(e);
            if (e.isCancelled()) {
                i.remove();
            }
        }
    }

}
