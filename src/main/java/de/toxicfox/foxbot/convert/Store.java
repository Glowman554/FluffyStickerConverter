package de.toxicfox.foxbot.convert;

import java.sql.SQLException;
import java.util.List;

import de.glowman554.bot.database.IDatabaseInterface;
import de.glowman554.bot.database.Migration;

public class Store {
    private final IDatabaseInterface database;

    public Store(IDatabaseInterface database) {
        this.database = database;

        if (!database.getFlavour().equals("sqlite")) {
            throw new RuntimeException("Unsupported database " + database.getFlavour());
        }

        database.applyMigration(new Migration("initial_sticker_converter", db -> {
            if (db.getFlavour().equals("sqlite")) {
                db.update("CREATE TABLE IF NOT EXISTS sticker_converter (name text NOT NULL, url text NOT NULL, PRIMARY KEY (name))");
            } else {
                throw new RuntimeException("Unsupported database " + db.getFlavour());
            }
        }));
    }



    public void create(String name, String url) {
        database.update("insert into sticker_converter (name, url) values (?, ?)", name, url);
    }

    public boolean contains(String name) {
        return database.query("select count(*) from sticker_converter where name = ?", rs -> {
            try {
                return rs.getInt(1) > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, name).get(0);
    }

    public String get(String name) {
        return database.query("select url from sticker_converter where name = ?", rs -> {
            try {
                return rs.getString("url");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, name).get(0);
    }

    public List<String> getAll() {
        return database.query("select name from sticker_converter", rs -> {
            try {
                return rs.getString("name");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void delete(String name) {
        database.update("delete from sticker_converter where name = ?", name);
    }
}
