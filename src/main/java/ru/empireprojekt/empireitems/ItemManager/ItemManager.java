package ru.empireprojekt.empireitems.ItemManager;

import com.google.gson.*;
import org.bukkit.ChatColor;
import org.json.simple.JSONValue;
import ru.empireprojekt.empireitems.CustomUISettings;
import ru.empireprojekt.empireitems.EmpireItems;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ItemManager {
    public class mItem {
        String texture_path = null;
        String model_path = null;
        String item_name = null;
        String material = null;
        String namespace = "empire_items";
        int custom_model_data = 1;
        boolean isBlock = false;

        mItem(String namespace, String texture_path, String model_path, String item_name, String material, int custom_model_data, boolean isBlock) {
            this.namespace = namespace;
            this.texture_path = texture_path;
            this.model_path = model_path;
            this.item_name = item_name;
            this.isBlock = isBlock;
            this.material = material;
            this.custom_model_data = custom_model_data;
        }
    }

    private List<mItem> items;
    private EmpireItems plugin;

    public List<String> GetNames() {
        List<String> mArgs = new ArrayList<String>();
        for (mItem item : items)
            mArgs.add(item.item_name);
        return mArgs;
    }

    public ItemManager(EmpireItems plugin) {
        this.plugin = plugin;
        items = new ArrayList<mItem>();
    }

    public void AddItem(String namespace, String texture_path, String model_path, String item_name, String material, int custom_model_data, boolean isBlock) {
        items.add(new mItem(namespace, texture_path, model_path, item_name, material, custom_model_data, isBlock));
    }


    private JsonObject GetBowObject(int pulling, String model, String namespace, int custom_model_data) {
        JsonObject bow;
        JsonObject predicate;
        predicate = new JsonObject();
        bow = new JsonObject();
        predicate.addProperty("pulling", pulling);
        if (model.contains("_1"))
            predicate.addProperty("pull", 0.65);
        else if (model.contains("_2"))
            predicate.addProperty("pull", 0.9);
        if (custom_model_data > 0)
            predicate.addProperty("custom_model_data", custom_model_data);
        bow.add("predicate", predicate);
        if (namespace.length() > 0)
            bow.addProperty("model", namespace + ":auto_generated/" + model);
        else
            bow.addProperty("model", model);
        return bow;
    }

    private JsonObject GetShieldObject(int blocking, String model, int customModelData) {
        JsonObject shield;
        JsonObject predicate;
        predicate = new JsonObject();
        shield = new JsonObject();
        shield.addProperty("model", model);
        predicate.addProperty("blocking", blocking);
        if (customModelData > 0)
            predicate.addProperty("custom_model_data", customModelData);
        shield.add("predicate", predicate);
        return shield;
    }

    private JsonObject GetGenericObject(String modelPath, int customModelData) {
        JsonObject itemObj;
        JsonObject predicate;
        predicate = new JsonObject();
        itemObj = new JsonObject();
        predicate.addProperty("custom_model_data", customModelData);
        itemObj.addProperty("model", modelPath);
        itemObj.add("predicate", predicate);
        return itemObj;
    }

    private JsonObject auto_generate(String parent, String namespace, String layerPath, String modelName) throws IOException {
        JsonObject itemObj = new JsonObject();
        itemObj.addProperty("parent", parent);
        JsonObject layer = new JsonObject();
        layer.addProperty("layer0", namespace + ":" + layerPath.replace(".png", ""));
        if (parent.contains("block_real")) {

            layer.addProperty("all", namespace + ":" + layerPath.replace(".png", ""));
            layer.addProperty("particle", namespace + ":" + layerPath.replace(".png", ""));

        }
        itemObj.add("textures", layer);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter file = new FileWriter(plugin.getDataFolder() + File.separator + "pack" + File.separator + "assets" + File.separator + namespace + File.separator + "models" + File.separator + "auto_generated" + File.separator + modelName + ".json");
        file.write(gson.toJson(itemObj));
        file.close();

        return itemObj;
    }

    private JsonObject GetUIJson( CustomUISettings.InterfaceItem interfaceItem){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("file", interfaceItem.namespace + ":" + interfaceItem.path);
        JsonArray tempJSArray = new JsonArray();
        tempJSArray.add(String.valueOf(interfaceItem.chars));
        jsonObject.add("chars", tempJSArray);

        jsonObject.addProperty("height", interfaceItem.size);
        jsonObject.addProperty("ascent", interfaceItem.offset);
        jsonObject.addProperty("type", "bitmap");
        return jsonObject;
    }

    public void GenerateInterfaceJson() throws IOException {
        System.out.println(plugin.CONSTANTS.PLUGIN_MESSAGE + "Generating interface Json");
        String defaultJsonPath = plugin.getDataFolder() + File.separator + "pack" + File.separator + "assets" + File.separator + "minecraft" + File.separator + "font" + File.separator + "default" + ".json";
        FileReader reader = new FileReader(defaultJsonPath);

        JsonObject jsonFile = new JsonParser().parse(reader).getAsJsonObject();
        reader.close();
        JsonArray providers = new JsonArray();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "ttf");
        jsonObject.addProperty("file", "minecraft:negative_spaces.ttf");
        JsonArray tempJSArray = new JsonArray();
        tempJSArray.add(0.0);
        tempJSArray.add(0.0);
        jsonObject.add("shift", tempJSArray);
        jsonObject.addProperty("size", 10.0);
        jsonObject.addProperty("oversample", 1);
        jsonFile.remove("providers");

        providers.add(jsonObject);
        for (CustomUISettings.InterfaceItem interfaceItem : plugin.getCustomUISettings().getGuiItems()) {
            if (interfaceItem.hasNull())
                continue;
            jsonObject = GetUIJson(interfaceItem);
            providers.add(jsonObject);
        }
        for (CustomUISettings.InterfaceItem interfaceItem : plugin.getCustomUISettings().getEmojiItems()) {
            if (interfaceItem.hasNull())
                continue;
            jsonObject = GetUIJson(interfaceItem);
            providers.add(jsonObject);
        }

        jsonFile.add("providers", providers);
        FileWriter file = new FileWriter(defaultJsonPath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String result = gson.toJson(jsonFile);
        result = result.replace("\\\\","\\");
        file.write(result);
        file.close();
    }

    public void GenerateMinecraftModels() {
        JsonParser jsonParser = new JsonParser();
        //Чистим файыл
        for (mItem item : items) {
            try {
                FileReader reader = new FileReader(plugin.getDataFolder() + File.separator + "pack" + File.separator + "assets" + File.separator + "minecraft" + File.separator + "models" + File.separator + "item" + File.separator + item.material.toLowerCase() + ".json");
                JsonObject jsonFile = (JsonObject) jsonParser.parse(reader);
                JsonArray overrides = new JsonArray();

                if (jsonFile.has("overrides"))
                    jsonFile.remove("overrides");

                if (item.material.toLowerCase().equals("shield")) {
                    GetShieldObject(0, "item/shield", 0);
                    GetShieldObject(1, "item/shield_blocking", 0);
                    jsonFile.add("overrides", overrides);
                }
                if (item.material.toLowerCase().equals("bow")) {
                    overrides.add(GetBowObject(0, "item/bow", "", 0));
                    overrides.add(GetBowObject(1, "item/bow_pulling_0", "", 0));
                    overrides.add(GetBowObject(1, "item/bow_pulling_1", "", 0));
                    overrides.add(GetBowObject(1, "item/bow_pulling_2", "", 0));
                    jsonFile.add("overrides", overrides);
                }


                FileWriter file = new FileWriter(plugin.getDataFolder() + File.separator + "pack" + File.separator + "assets" + File.separator + "minecraft" + File.separator + "models" + File.separator + "item" + File.separator + item.material.toLowerCase() + ".json");

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                file.write(gson.toJson(jsonFile));
                file.close();
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.RED + "Не удалось очистить файл: " + item.material + ".json");
            }
        }

        System.out.println(plugin.CONSTANTS.PLUGIN_MESSAGE + ChatColor.YELLOW + "Generating Interface");
        try {
            GenerateInterfaceJson();
        } catch (IOException e) {
            System.out.println(plugin.CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + "Error while Generating Interface");
            e.printStackTrace();
        }

        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Generating Models");
        for (mItem item : items) {
            try {
                //Generating minecraft models
                //System.out.println(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                //Открыли существующий файл
                FileReader reader = new FileReader(plugin.getDataFolder() + File.separator + "pack" + File.separator + "assets" + File.separator + "minecraft" + File.separator + "models" + File.separator + "item" + File.separator + item.material.toLowerCase() + ".json");
                JsonObject jsonFile = (JsonObject) jsonParser.parse(reader);
                reader.close();
                if (item.material.toLowerCase().equals("bow")) {
                    JsonArray overrides = jsonFile.getAsJsonArray("overrides");
                    overrides.add(GetBowObject(0, item.item_name, item.namespace, item.custom_model_data));
                    overrides.add(GetBowObject(1, item.item_name + "_0", item.namespace, item.custom_model_data));
                    overrides.add(GetBowObject(1, item.item_name + "_1", item.namespace, item.custom_model_data));
                    overrides.add(GetBowObject(1, item.item_name + "_2", item.namespace, item.custom_model_data));
                    jsonFile.add("overrides", overrides);

                    //auto_generate
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", ""),
                            item.item_name
                    );
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", "") + "_0",
                            item.item_name + "_0"
                    );
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", "") + "_1",
                            item.item_name + "_1"
                    );
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", "") + "_2",
                            item.item_name + "_2"
                    );

                } else if (item.material.toLowerCase().equals("shield")) {
                    JsonArray overrides = jsonFile.getAsJsonArray("overrides");
                    overrides.add(GetShieldObject(0, item.namespace + ":item/" + item.item_name, item.custom_model_data));
                    overrides.add(GetShieldObject(1, item.namespace + ":item/" + item.item_name, item.custom_model_data));
                    jsonFile.add("overrides", overrides);
                } else {
                    JsonArray overrides;
                    if (jsonFile.has("overrides"))
                        overrides = jsonFile.getAsJsonArray("overrides");
                    else
                        overrides = new JsonArray();
                    //System.out.println(item.material + ";" + item.item_name);
                    if (item.model_path != null)
                        overrides.add(GetGenericObject(item.namespace + ":" + item.model_path, item.custom_model_data));
                    else
                        overrides.add(GetGenericObject(item.namespace + ":auto_generated/" + item.item_name, item.custom_model_data));
                    jsonFile.add("overrides", overrides);
                    if (item.model_path == null || item.model_path.length() == 0) {
                        if (item.isBlock)
                            auto_generate("block/base/block_real", item.namespace, item.texture_path, item.item_name);
                        else
                            auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""), item.namespace, item.texture_path, item.item_name);
                    }
                }

                FileWriter file = new FileWriter(plugin.getDataFolder() + File.separator + "pack" + File.separator + "assets" + File.separator + "minecraft" + File.separator + "models" + File.separator + "item" + File.separator + item.material.toLowerCase() + ".json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                file.write(gson.toJson(jsonFile));
                file.close();


            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Не найден файл: " + item.material + ".json");
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Возникла ошибка при выполении файла: " + item.material + ".json");
            }
        }
    }


    public void print() {
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.GREEN + "--------------------------ItemManager--------------------------");
        for (mItem item : items) {

            System.out.println("namespace: " + item.namespace);
            System.out.println("texture_path: " + item.texture_path);
            System.out.println("model_path: " + item.model_path);
            System.out.println("item_name: " + item.item_name);
            System.out.println("material: " + item.material);
            System.out.println("custom_model_data: " + item.custom_model_data);
            System.out.println(ChatColor.GREEN + "---------------------------------------------------------------");
        }
    }
}
