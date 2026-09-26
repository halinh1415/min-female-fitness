package mint.woman.female.workout.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XORDecryptionInputStream extends FilterInputStream {
    private static final byte XOR_KEY = (byte) 0xAB;

    public XORDecryptionInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int data = super.read();
        return (data == -1) ? -1 : (data ^ XOR_KEY); // Giải mã từng byte
    }

    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        int bytesRead = super.read(buffer, offset, count);
        if (bytesRead != -1) {
            for (int i = offset; i < offset + bytesRead; i++) {
                buffer[i] ^= XOR_KEY;
            }
        }
        return bytesRead;
    }
}
