package de.toxicfox.foxbot.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.glowman554.bot.Main;
import de.glowman554.bot.logging.Logger;
import de.glowman554.bot.utils.HttpClient;
import de.glowman554.bot.utils.StreamedFile;
import de.glowman554.config.auto.AutoSavable;
import de.glowman554.config.auto.Saved;
import de.glowman554.config.auto.processors.SavableProcessor;
import de.glowman554.config.premade.HashMapSavable;
import de.toxicfox.foxbot.convert.converter.TgsToGif;
import de.toxicfox.foxbot.convert.converter.WebmToGif;
import de.toxicfox.foxbot.convert.converter.WebpToPng;
import net.shadew.json.Json;

public class MatrixStickerConverter {
    public static StreamedFile convert(String name) {
        TelegramStickerPack sticker = TelegramStickerPack.load(Main.config.getTelegramToken(), name);

        StickerMetadata metadata = new StickerMetadata();
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipBuffer)) {
            for (TelegramStickerPack.Sticker s : sticker.getStickers()) {
                String url = s.file().url();

                try (StreamedFile file = HttpClient.download(url)) {
                    String zipPath = null;
                    if (s.is_animated) {
                        zipPath = "stickers/" + s.file_unique_id + ".gif";
                        zos.putNextEntry(new ZipEntry(zipPath));
                        TgsToGif.convert(file.getStream(), zos);
                        zos.closeEntry();
                    } else if (s.is_video) {
                        zipPath = "stickers/" + s.file_unique_id + ".gif";
                        zos.putNextEntry(new ZipEntry(zipPath));
                        WebmToGif.convert(file.getStream(), zos);
                        zos.closeEntry();
                    } else {
                        zipPath = "stickers/" + s.file_unique_id + ".png";
                        zos.putNextEntry(new ZipEntry(zipPath));
                        WebpToPng.convert(file.getStream(), zos);
                        zos.closeEntry();
                    }

                    if (zipPath != null) {
                        Logger.log("Adding converted sticker %s to metadata", zipPath);

                        StickerMetadataEntry entry = new StickerMetadataEntry();
                        entry.body = s.emoji;
                        entry.url = zipPath;
                        metadata.stickers.put(s.file_unique_id, entry);
                    }
                } 
            }

            metadata.name = sticker.name;
            metadata.title = sticker.title;
            metadata.author = "Telegram";

            zos.putNextEntry(new ZipEntry("meta.json"));
            zos.write(Json.json().serialize(metadata.toJSON()).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.finish();

            Logger.log("ZIP file for sticker pack %s created, size: %d bytes", name, zipBuffer.size());
            return new StreamedFile(new ByteArrayInputStream(zipBuffer.toByteArray()), name + ".zip");
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert sticker pack", e);
        }
    }

    private static class StickerMetadata extends AutoSavable {
        @Saved
        public String name;

        @Saved
        public String title;

        @Saved
        public String author;

        @Saved
        public HashMapSavable<StickerMetadataEntry> stickers = new HashMapSavable<>(new SavableProcessor(), StickerMetadataEntry::new);
    }

    private static class StickerMetadataEntry extends AutoSavable {
        @Saved
        public String body;

        @Saved
        public String url;
    } 
}
