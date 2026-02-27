package de.toxicfox.foxbot.convert.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WebmToGif {
    public static void convert(InputStream webm, OutputStream gif) throws IOException, InterruptedException {
        Process ffmpeg1 = new ProcessBuilder(
                "/tmp/foxbot-ffmpeg",
                "-y",
                "-loglevel", "error",
                "-c:v", "libvpx-vp9",
                "-i", "pipe:0",
                "-pix_fmt", "rgba",
                "-f", "apng",
                "pipe:1"
        ).start();

        Process ffmpeg2 = new ProcessBuilder(
                "/tmp/foxbot-ffmpeg",
                "-y",
                "-loglevel", "error",
                "-i", "pipe:0",
                "-vf",
                "fps=30,scale=512:-1:flags=lanczos,split[s0][s1];" +
                "[s0]palettegen=reserve_transparent=1[p];" +
                "[s1][p]paletteuse=alpha_threshold=128",
                "-f", "gif",
                "pipe:1"
        ).start();

        // webm → ffmpeg1
        Thread pump1 = new Thread(() -> {
            try (OutputStream stdin = ffmpeg1.getOutputStream()) {
                webm.transferTo(stdin);
            } catch (IOException ignored) {}
        });

        // ffmpeg1 → ffmpeg2
        Thread pump2 = new Thread(() -> {
            try (
                    InputStream out1 = ffmpeg1.getInputStream();
                    OutputStream in2 = ffmpeg2.getOutputStream()
            ) {
                out1.transferTo(in2);
            } catch (IOException ignored) {}
        });

        // ffmpeg2 → final gif
        Thread pump3 = new Thread(() -> {
            try (InputStream out2 = ffmpeg2.getInputStream()) {
                out2.transferTo(gif);
            } catch (IOException ignored) {}
        });

        pump1.start();
        pump2.start();
        pump3.start();

        int exit1 = ffmpeg1.waitFor();
        int exit2 = ffmpeg2.waitFor();

        pump1.join();
        pump2.join();
        pump3.join();

        if (exit1 != 0 || exit2 != 0) {
            throw new IOException("ffmpeg pipeline failed");
        }
    }
}
