package com.franciscogiraldo.grability;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class Main {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(3, "com.franciscogiraldo.fcog.grability.db");

        Entity app = schema.addEntity("App");
        app.addIdProperty();
        app.addStringProperty("title");
        app.addStringProperty("image");
        app.addStringProperty("description");
        app.addStringProperty("category");
        app.addStringProperty("link");
        app.addIntProperty("price");
        app.addIntProperty("favorite");
        app.addIntProperty("newSync");
        app.addContentProvider();

        new DaoGenerator().generateAll(schema, "./GrabilityTest/src/main/java");
    }
}
