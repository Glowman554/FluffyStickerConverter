package de.toxicfox.foxbot.convert;

import de.glowman554.bot.utils.HttpClient;
import de.glowman554.config.Savable;
import de.glowman554.config.auto.AutoSavable;
import de.glowman554.config.auto.Saved;
import de.glowman554.config.auto.processors.SavableProcessor;
import de.glowman554.config.premade.ArrayListSavable;
import net.shadew.json.Json;
import net.shadew.json.JsonNode;

import java.io.IOException;

public class TelegramStickerPack extends AutoSavable {
    private final String token;

    public TelegramStickerPack(String token) {
        this.token = token;
    }

    @Saved
    public String name;

    @Saved
    public String title;

    @Saved
    public String sticker_type;

    @Saved
    public boolean contains_masks;

    @Saved
    public ArrayListSavable<Sticker> stickers = new ArrayListSavable<>(new SavableProcessor(), Sticker::new);



    public class Sticker extends AutoSavable {
        @Saved
        public int width;

        @Saved
        public int height;

        @Saved
        public String emoji;

        @Saved
        public String set_name;

        @Saved
        public boolean is_animated;

        @Saved
        public boolean is_video;

        @Saved
        public String type;

        @Saved(remap = Savable.class)
        public Thumbnail thumbnail = new Thumbnail();


        @Saved(remap = Savable.class)
        public Thumbnail thumb = new Thumbnail();

        @Saved
        public String file_id;

        @Saved
        public String file_unique_id;

        @Saved
        public int file_size;

        public StickerFile file() {
            JsonNode file = TelegramStickerPack.request(token, "getFile?file_id=" + file_id).get("result");

            StickerFile stickerFile = new StickerFile();
            stickerFile.fromJSON(file);
            return stickerFile;
        }
    }

    public static class Thumbnail extends AutoSavable {
        @Saved
        public String file_id;

        @Saved
        public String file_unique_id;

        @Saved
        public int file_size;

        @Saved
        public int width;

        @Saved
        public int height;

        @Saved
        public String file_path;


    }

    public class StickerFile extends AutoSavable {
        @Saved
        public String file_id;

        @Saved
        public String file_unique_id;

        @Saved
        public int file_size;

        @Saved
        public String file_path;

        public String url() {
            return "https://api.telegram.org/file/bot" + token + "/" + file_path;
        }
    }

    public ArrayListSavable<Sticker> getStickers() {
        return stickers;
    }

    public static JsonNode request(String token, String url) {
        try {
            String result = HttpClient.get("https://api.telegram.org/bot" + token + "/" + url);

            return Json.json().parse(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TelegramStickerPack load(String token, String name) {
        JsonNode data = request(token, "getStickerSet?name=" + name);

        TelegramStickerPack pack = new TelegramStickerPack(token);
        pack.fromJSON(data.get("result"));
        return pack;
    }
}