package de.toxicfox.foxbot.convert.converter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class WebpToPng {
    public static void convert(InputStream webp, OutputStream png) throws IOException, InterruptedException {
        BufferedImage image = ImageIO.read(webp);
        if (image == null) {
            throw new IOException("Invalid or unsupported WebP image");
        }

        ImageIO.write(image, "PNG", png);
    }
}
