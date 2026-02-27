package de.toxicfox.foxbot.convert.commands;

import java.util.List;

import de.glowman554.bot.command.IReply;
import de.glowman554.bot.command.LegacyCommandContext;
import de.glowman554.bot.command.Schema;
import de.glowman554.bot.command.SchemaCommand;
import de.glowman554.bot.command.SchemaCommandContext;
import de.toxicfox.foxbot.convert.Store;
import de.toxicfox.foxbot.convert.filething.FileThingApi;

public class ConvertCleanCommand extends SchemaCommand {
    
    private final FileThingApi fileThingApi;
    private final Store store;
    
    public ConvertCleanCommand(FileThingApi fileThingApi, Store store) {
        super("Clean convertions cache for FluffyChat sticker packs.", "Usage: <command> [name]", "convert-clean", Group.TOOLS);
        this.fileThingApi = fileThingApi;
        this.store = store;
    }

    private void doExecute(IReply reply) throws Exception {
        List<String> names = store.getAll();
        for (String name : names) {
        
            String url = store.get(name);
            fileThingApi.deleteFile(fileThingApi.idFromUrl(url));

            store.delete(name);
        }



        reply.reply("Cleared " + names.size() + " entries from the cache");
    }

    @Override
    public void loadSchema(Schema schema) {
    }

    @Override
    public void execute(SchemaCommandContext commandContext) throws Exception {
        doExecute(commandContext);
    }

    @Override
    public void execute(LegacyCommandContext commandContext, String[] arguments) throws Exception {
        doExecute(commandContext);
    }

}
