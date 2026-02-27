package de.toxicfox.foxbot.convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.spi.IIORegistry;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import de.glowman554.bot.Main;
import de.glowman554.bot.command.PermissionProvider;
import de.glowman554.bot.logging.Logger;
import de.glowman554.bot.registry.Registries;
import de.glowman554.bot.utils.Pair;
import de.glowman554.config.ConfigManager;
import de.toxicfox.foxbot.convert.commands.ConvertCleanCommand;
import de.toxicfox.foxbot.convert.commands.ConvertCommand;
import de.toxicfox.foxbot.convert.filething.FileThingApi;

public class Entrypoint {
    public static void main(String[] args) throws Exception {
        Main.registerTestPlugin(new Entrypoint());
        Main.main(args);
    }

    @de.glowman554.bot.plugin.Entrypoint
    public void entrypoint() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new WebPImageReaderSpi());

        try {
            extractFfmpeg();
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract ffmpeg", e);
        }

        addPermissions();

        Config config = new Config(new File(ConfigManager.BASE_FOLDER, "converter.json"));
        Store store = new Store(Registries.DATABASE.get());

        FileThingApi fileThingApi = new FileThingApi(config.getUploadServer(), config.getUploadToken());

        Registries.COMMANDS.register("convert", new ConvertCommand(fileThingApi, store));
        Registries.COMMANDS.register("convert-clean", new ConvertCleanCommand(fileThingApi, store));
    }

    private void extractFfmpeg() throws FileNotFoundException, IOException {
        String ffmpeg = "/ffmpeg." + System.getProperty("os.arch");
        Logger.log("Extracting %s from resources...", ffmpeg);

        File output = new File("/tmp/foxbot-ffmpeg");

        try (InputStream inputStream = Entrypoint.class.getResourceAsStream(ffmpeg)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + ffmpeg);
            }

            try (FileOutputStream outputStream = new FileOutputStream(output)) {
                inputStream.transferTo(outputStream);
            }
        }

        output.setExecutable(true);
    }

    private void addPermissions() {
        PermissionProvider provider = Registries.PERMISSION_PROVIDER.get();

        List<Pair<String, List<String>>> roles = provider.getRoles();
        for (Pair<String, List<String>> role : roles) {
            if (role.t1().equals("admin")) {
                boolean foundConvertCleanPermission = false;
                for (String permission : role.t2()) {
                    if (permission.equals("convert-clean")) {
                        foundConvertCleanPermission = true;
                        break;
                    }
                }
                if (!foundConvertCleanPermission) {
                    Logger.log("Adding convert-clean permission to admin role");
                    provider.addPermission("admin", "convert-clean");
                }
            }
        }
    }
}