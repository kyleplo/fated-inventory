package com.kyleplo.fatedinventory;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;

public class Config {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static String currentVersion = "1.1.1";

    //Config Default Values
    public String CONFIG_VERSION_DO_NOT_TOUCH_PLS = currentVersion;

    public String COMMENT = "Further customization is possible through datapacks - see Modrinth page";
    public boolean fateStoresXp = true;
    public boolean fatedAltarRequiresCharges = false;
    public boolean showMessageOnRespawn = true;
    public boolean generateAltarBuildingsInVillages = true;
    public int villageAltarBuildingWeight = 2;
    public boolean anyNonstackableAllowsModifiedComponents = false;
    public boolean anyDurabilityItemAllowsModifiedComponents = true;
    public boolean experimentalFlattenContainerItems = false;

    public static Config init() {
        Config config = null;

        try {
            Path configPath = Paths.get("", "config", "fated_inventory.json");

            if (Files.exists(configPath)) {
                config = gson.fromJson(
                    new FileReader(configPath.toFile()),
                    Config.class
                );

                if (!config.CONFIG_VERSION_DO_NOT_TOUCH_PLS.equals(currentVersion)) {
                    config.CONFIG_VERSION_DO_NOT_TOUCH_PLS = currentVersion;

                    BufferedWriter writer = new BufferedWriter(
                        new FileWriter(configPath.toFile())
                    );

                    writer.write(gson.toJson(config));
                    writer.close();
                }

            } else {
                config = new Config();
                Paths.get("", "config").toFile().mkdirs();

                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(configPath.toFile())
                );

                writer.write(gson.toJson(config));
                writer.close();
            }


        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return config;
    }
}