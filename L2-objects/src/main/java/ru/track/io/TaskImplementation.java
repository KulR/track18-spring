package ru.track.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.track.io.vendor.Bootstrapper;
import ru.track.io.vendor.FileEncoder;
import ru.track.io.vendor.ReferenceTaskImplementation;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Arrays;

public final class TaskImplementation implements FileEncoder {

    /**
     * @param finPath  where to read binary data from
     * @param foutPath where to write encoded data. if null, please create and use temporary file.
     * @return file to read encoded data from
     * @throws IOException is case of input/output errors
     */
    @NotNull
    public File encodeFile(@NotNull String finPath, @Nullable String foutPath) throws IOException {
        /* XXX: https://docs.oracle.com/javase/8/docs/api/java/io/File.html#deleteOnExit-- */
        int[] buffer_in = new int[768];
        char[] buffer_out = new char[1024];
        FileInputStream fs = new FileInputStream(finPath);
        File file;
        if (foutPath == null) {
            file = File.createTempFile("base64", null);
        } else {
            file = new File(foutPath);
            if (file.exists())
                if (!file.delete())
                    throw new IOException("file already exist and can't be deleted");
            if (!file.createNewFile()) {
                throw new IOException("can't create file");
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        int i = -1, j = 0;
        while ((i = fs.read()) != -1) {
            if (j < buffer_in.length) {
                buffer_in[j] = i;
                j++;
            } else {
                code_buf(buffer_in, buffer_out);
                bw.write(buffer_out);
                bw.flush();
                buffer_in[0] = i;
                j = 1;
            }
        }
        int[] appendix = Arrays.copyOfRange(buffer_in, 0, j);
        char[] appendix_image = new char[4 * ((j + 2) / 3)];
        code_buf(appendix, appendix_image);
        bw.write(appendix_image);
        bw.flush();
        return file; // TODO: implement
    }

    private static void code_buf(int[] buf_in, char[] buf_out) {
        for (int i = 0; i < buf_in.length / 3; i++) {
            buf_out[4 * i] = toBase64[(buf_in[3 * i] / 4)];
            buf_out[4 * i + 1] = toBase64[(16 * ((buf_in[3 * i]) % 4) + (buf_in[3 * i + 1]) / 16)];
            buf_out[4 * i + 2] = toBase64[(4 * (buf_in[3 * i + 1] % 16) + buf_in[3 * i + 2] / 64)];
            buf_out[4 * i + 3] = toBase64[(buf_in[3 * i + 2] % 64)];
        }
        if (buf_in.length % 3 == 0)
            return;
        int[] appendix = Arrays.copyOfRange(buf_in, 3 * (buf_in.length / 3), buf_in.length);
        char[] temp = new char[4];
        code_appendix(appendix, temp);
        for (int i = 0; i < 4; i++) {
            buf_out[4 * (buf_in.length / 3) + i] = temp[i];
        }
    }

    private static void code_appendix(int[] buf_in, char[] buf_out) {
        switch (buf_in.length % 3) {
            case 1:
                buf_out[0] = toBase64[(buf_in[0] / 4)];
                buf_out[1] = toBase64[16 * (buf_in[0] % 4)];
                buf_out[2] = '=';
                buf_out[3] = '=';
                break;
            case 2:
                buf_out[0] = toBase64[(buf_in[0] >> 2)];
                buf_out[1] = toBase64[(16 * (buf_in[0] % 4) + buf_in[1] / 16)];
                buf_out[2] = toBase64[4 * (buf_in[1] % 16)];
                buf_out[3] = '=';
                break;
        }
    }


    private static final char[] toBase64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static void main(String[] args) throws Exception {
        final FileEncoder encoder = new TaskImplementation();
        // NOTE: open http://localhost:9000/ in your web browser
        (new Bootstrapper(args, encoder))
                .bootstrap("", new InetSocketAddress("127.0.0.1", 9000));
    }
}