package de.toxicfox.foxbot.convert.commands;

import de.glowman554.bot.command.*;
import de.glowman554.bot.command.Schema.Argument.Type;
import de.glowman554.bot.logging.Logger;
import de.glowman554.bot.utils.StreamedFile;
import de.toxicfox.foxbot.convert.MatrixStickerConverter;
import de.toxicfox.foxbot.convert.Store;
import de.toxicfox.foxbot.convert.filething.FileThingApi;
import de.toxicfox.foxbot.convert.filething.UploadResult;

public class ConvertCommand extends SchemaCommand {
    private final FileThingApi fileThingApi;
    private final Store store;
    
    public ConvertCommand(FileThingApi fileThingApi, Store store) {
        super("Convert a telegram sticker pack for usage with FluffyChat.", "Usage: <command> [name]", null, Group.TOOLS);
        this.fileThingApi = fileThingApi;
        this.store = store;
    }

    private void doSend(IReply reply, String name) throws Exception {
        String strip = "https://t.me/addstickers/";
        if (name.startsWith(strip)) {
            name = name.substring(strip.length());
        }

        if (store.contains(name)) {
            String url = store.get(name);
            reply.reply("This sticker pack has already been converted: " + url);
            return;
        }

        reply.reply("Converting please wait...");

        try (StreamedFile file = MatrixStickerConverter.convert(name)) {
            UploadResult uploadResult = fileThingApi.prepareUpload(file.getName());
            fileThingApi.upload(file, uploadResult);
            store.create(name, uploadResult.getUrl());

            reply.reply("Here is your converted sticker pack: " + uploadResult.getUrl());
        } catch (Exception e) {
            reply.reply("Failed to convert sticker pack: " + e.getMessage());
            Logger.exception(e);
            e.printStackTrace();
        }
    }

    @Override
    public void loadSchema(Schema schema) {
        schema.addArgument(Type.STRING, "name", "Name of the sticker pack", false).register();
    }

    @Override
    public void execute(SchemaCommandContext commandContext) throws Exception {
        doSend(commandContext, commandContext.get("name").asString());
    }

    @Override
    public void execute(LegacyCommandContext commandContext, String[] arguments) throws Exception {
        if (arguments.length != 1) {
            commandContext.reply("Missing or invalid name argument");
        } else {
            doSend(commandContext, arguments[0]);
        }
    }

}