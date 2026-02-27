package de.toxicfox.foxbot.convert;

import de.glowman554.config.ConfigFile;
import de.glowman554.config.auto.Saved;

import java.io.File;

public class Config extends ConfigFile {
    @Saved
    private String uploadServer = "https://filething.toxicfox.de";

    @Saved
    private String uploadToken = "";

    public Config(File configFile) {
        super(configFile);
        load();
    }

    public String getUploadServer() {
        return uploadServer;
    }

    public String getUploadToken() {
        return uploadToken;
    }
}