package cz.mendelu.argeo.util;

/**
 * @author adamb_000
 * @since 13. 7. 2016
 */
public class ARLog {
    // ========================================================================
    // =====================   V  A  R  I  A  B  L  E  S   ====================
    // ========================================================================

    // Logging identifier
    public static final String LOG_DEBUG_TAG = "argeo-test";
    // ---------------------------------------------------
    //
    // Static defs for the hex dump
    //
    // ---------------------------------------------------
    public static final String EOL = System.getProperty("line.separator");
    // ========================================================================
    // =======================    M  E  T  H  O  D  S   =======================
    // ========================================================================
    private static final char _hexcodes[] = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final int _shifts[] = {
            60, 56, 52, 48, 44, 40, 36, 32, 28, 24, 20, 16, 12, 8, 4, 0};
    private static boolean sIsLoggingEnabled = true;

    public static void v(String msg) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.v(LOG_DEBUG_TAG, msg);
    }

    public static void v(String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.v(LOG_DEBUG_TAG, String.format(msgFormat, args));
    }

    public static void v(Throwable t, String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.v(LOG_DEBUG_TAG, String.format(msgFormat, args), t);
    }

    public static void v(boolean log, String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        if (log) {
            v(msgFormat, args);
        }
    }

    public static void d(String msg) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.d(LOG_DEBUG_TAG, msg);
    }

    public static void d(String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.d(LOG_DEBUG_TAG, String.format(msgFormat, args));
    }

    public static void d(Throwable t, String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.d(LOG_DEBUG_TAG, String.format(msgFormat, args), t);
    }

    public static void i(String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.i(LOG_DEBUG_TAG, String.format(msgFormat, args));
    }

    public static void w(String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.w(LOG_DEBUG_TAG, String.format(msgFormat, args));
    }

    public static void e(String msgFormat) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.e(LOG_DEBUG_TAG, msgFormat);
    }

    public static void e(String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.e(LOG_DEBUG_TAG, String.format(msgFormat, args));
    }

    public static void e(Exception e, String msgFormat) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.e(LOG_DEBUG_TAG, msgFormat, e);
    }

    public static void e(Exception e, String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        android.util.Log.e(LOG_DEBUG_TAG, String.format(msgFormat, args), e);
    }

    public static void e(boolean log, String msgFormat, Object... args) {
        if (!sIsLoggingEnabled) {
            return;
        }
        if (log) {
            e(msgFormat, args);
        }
    }

    // ---------------------------------------------------
    //
    //	dump an array of bytes to a String
    //
    //	@param data the byte array to be dumped
    //	@param offset its offset, whatever that might mean
    //	@param index initial index into the byte array
    //
    //	@exception ArrayIndexOutOfBoundsException if the index is
    //	outside the data array's bounds
    //	@return output string
    //
    // ---------------------------------------------------
    public static void dump(final byte[] data) {
        dump(data, 0, 0);
    }

    public static void dump(final byte[] data, final long offset, final int index) {
        if (!sIsLoggingEnabled) {
            return;
        }

        StringBuffer buffer;
        if ((index < 0) || (index >= data.length)) {
            throw new ArrayIndexOutOfBoundsException("illegal index: " + index + " into array of length " + data.length);
        }
        long display_offset = offset + index;

        buffer = new StringBuffer(74);

        for (int j = index; j < data.length; j += 16) {
            int chars_read = data.length - j;
            if (chars_read > 16) {
                chars_read = 16;
            }

            buffer.append(dump(display_offset)).append(' ');
            for (int k = 0; k < 16; k++) {
                if (k < chars_read) {
                    buffer.append(dump(data[k + j]));
                } else {
                    buffer.append(" ");
                }
                buffer.append(' ');
            }
            for (int k = 0; k < chars_read; k++) {
                if ((data[k + j] >= ' ') && (data[k + j] < 127)) {
                    buffer.append((char) data[k + j]);
                } else {
                    buffer.append('.');
                }
            }

            String s = buffer.toString();
            ARLog.v(s);

            buffer.setLength(0);
            display_offset += chars_read;
        }
    }

    // ---------------------------------------------------
    //
    // Helper functions
    //
    // ---------------------------------------------------
    private static String dump(final long value) {
        StringBuffer buf = new StringBuffer();
        buf.setLength(0);
        for (int j = 0; j < 8; j++) {
            buf.append(_hexcodes[((int) (value >> _shifts[j + _shifts.length - 8])) & 15]);
        }
        return buf.toString();
    }

    private static String dump(final byte value) {
        StringBuffer buf = new StringBuffer();
        buf.setLength(0);
        for (int j = 0; j < 2; j++) {
            buf.append(_hexcodes[(value >> _shifts[j + 6]) & 15]);
        }
        return buf.toString();
    }

    public static void dump(String pattern, String dataOrg) {
        if (!sIsLoggingEnabled) {
            return;
        }
        String data = dataOrg.replaceAll("\\s+", " ");

        int rowDataLng = 80 - pattern.length() + 2;
        int rowCount = data.length() / rowDataLng;
        int rowMod = data.length() % rowDataLng;

        for (int i = 0; i < rowCount; i++) {
            v(pattern, data.substring(i * rowDataLng, (i + 1) * rowDataLng));
        }

        if (rowMod > 0) {
            v(pattern, data.substring(data.length() - rowMod, data.length()));
        }
    }

    public static void setLoggingEnabled(boolean value) {
        sIsLoggingEnabled = value;
    }
}

