package org.sonatype.client;

import org.codehaus.jackson.annotate.JsonValue;

/**
 * @see http://apr.apache.org/versioning.html
 */
public class Version {
    private final int major;
    private final int minor;
    private final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * @param version Must be of the form 1.2.3
     */
    public Version(final String version) {
        final String[] bits = version.split("\\.");
        if (bits.length != 3) {
            throw new IllegalArgumentException(String.format("Passed version, %s, is not of the form MAJOR.MINOR.PATCH",
                    version));
        }
        major = Integer.parseInt(bits[0]);
        minor = Integer.parseInt(bits[1]);
        patch = Integer.parseInt(bits[2]);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        final Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch;

    }

    @Override
    public int hashCode() {
        int result;
        result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }

    /**
     * When used in the form <code>a.isCompatibleWith(a)</code> this method
     * answers the question "If I require version a, will I work against version
     * b?"
     */
    public boolean isCompatibleWith(final Version other) {
        return major == other.major && minor <= other.minor;
    }

    @JsonValue
    @Override
    public String toString() {
        return new StringBuilder().append(major).append(".").append(minor).append(".").append(patch).toString();
    }
}