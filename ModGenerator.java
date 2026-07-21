package com.mindustry.modgen;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import android.net.Uri;

public class ModGenerator {

    private Context context;

    // 模组配置
    public static class ModConfig {
        public String name;
        public String displayName;
        public String author;
        public String description;
        public String version;
        public String minGameVersion;
        public String contentType;
        public Map<String, String> customFields = new HashMap<>();
        public String iconPath;  // 新增：图标路径
        public ModConfig() {
            // 默认值
            customFields.put("type", "ItemTurret");
            customFields.put("name", "super-turret");
            customFields.put("health", "800");
        }
    }

    public ModGenerator(Context context) {
        this.context = context;
    }

    public File getModsDirectory() {
        File dir = new File(Environment.getExternalStorageDirectory(), "MindustryMods");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public File getModDirectory(String modName) {
        File dir = new File(getModsDirectory(), modName);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public boolean generateMod(ModConfig config) {
        try {
            if (config.name == null || config.name.trim().isEmpty()) {
                showToast("模组名称不能为空");
                return false;
            }

            // 清理名称
            config.name = cleanFileName(config.name);

            File dir = getModDirectory(config.name);

            // 生成 mod.hjson
            String modHjson = buildModHjson(config);
            writeFile(new File(dir, "mod.hjson"), modHjson);

            // 创建 content 目录
            File contentDir = new File(dir, "content");
            contentDir.mkdirs();

            // 根据类型创建子目录
            String subDir = getContentSubDir(config.contentType);
            File typeDir = new File(contentDir, subDir);
            typeDir.mkdirs();
            
            // 生成sprites目录
            File spritesDir = new File(dir, "sprites");
            spritesDir.mkdirs();
            new File(spritesDir, "blocks").mkdirs();
            new File(spritesDir, "items").mkdirs();
            new File(spritesDir, "units").mkdirs();
            new File(spritesDir, "liquids").mkdirs();
            new File(spritesDir, "tech").mkdirs();
            
            // 生成内容 JSON
            String contentJson = buildContentJson(config);
            String fileName = config.customFields.getOrDefault("name", "custom-object");
            writeFile(new File(typeDir, fileName + ".json"), contentJson);

            showToast("✅ 模组已生成: " + config.name);
            return true;

        } catch (Exception e) {
            showToast("❌ 生成失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean packToZip(String modName) {
        try {
            if (modName == null || modName.trim().isEmpty()) {
                showToast("模组名称不能为空");
                return false;
            }

            modName = cleanFileName(modName);
            File dir = getModDirectory(modName);

            if (!dir.exists()) {
                showToast("模组目录不存在");
                return false;
            }

            File zipFile = new File(getModsDirectory(), modName + ".zip");
            if (zipFile.exists()) zipFile.delete();

            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zipDirectory(dir, modName, zos);
            zos.close();

            showToast("✅ ZIP 已保存: " + zipFile.getName());
            return true;

        } catch (Exception e) {
            showToast("❌ 打包失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<File> getExistingMods() {
        List<File> mods = new ArrayList<>();
        File dir = getModsDirectory();
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    File modHjson = new File(f, "mod.hjson");
                    if (modHjson.exists()) {
                        mods.add(f);
                    }
                }
            }
        }
        return mods;
    }
    // 复制图标到模组目录
    public void copyIconToMod(String modName, String iconPath) {
        try {
            if (iconPath == null || iconPath.isEmpty()) return;

            File dir = getModDirectory(modName);
            File iconFile = new File(dir, "icon.png");

            // 从 Uri 复制文件
            InputStream is = context.getContentResolver().openInputStream(Uri.parse(iconPath));
            FileOutputStream fos = new FileOutputStream(iconFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();

        } catch (Exception e) {
            // 忽略错误，不影响模组生成
        }
    }
    private String buildModHjson(ModConfig config) {
        String iconLine = "";
        if (config.iconPath != null && !config.iconPath.isEmpty()) {
            iconLine = "  \"icon\": \"icon.png\",\n";
        }
        return String.format(
            "{\n" +
            "  \"name\": \"%s\",\n" +
            "  \"displayName\": \"%s\",\n" +
            "  \"author\": \"%s\",\n" +
            "  \"description\": \"%s\",\n" +
            "  \"version\": \"%s\",\n" +
            "  \"minGameVersion\": %s\n" +
            "}",
            config.name,
            config.displayName != null ? config.displayName : config.name,
            config.author != null ? config.author : "匿名",
            config.description != null ? config.description : "",
            config.version != null ? config.version : "1.0",
            config.minGameVersion != null ? config.minGameVersion : "146"
        );
    }

    private String buildContentJson(ModConfig config) {
        StringBuilder sb = new StringBuilder("{\n");
        int size = config.customFields.size();
        int i = 0;
        for (Map.Entry<String, String> entry : config.customFields.entrySet()) {
            String key = entry.getKey().trim();
            String value = entry.getValue().trim();
            if (key.isEmpty()) continue;

            boolean isNumber = value.matches("-?\\d+(\\.\\d+)?");
            boolean isBoolean = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
            boolean isArray = value.startsWith("[") && value.endsWith("]");

            sb.append("  \"").append(key).append("\": ");
            if (isNumber || isBoolean || isArray) {
                sb.append(value);
            } else {
                sb.append("\"").append(escapeJson(value)).append("\"");
            }
            if (i < size - 1) sb.append(",");
            sb.append("\n");
            i++;
        }
        return sb.append("}").toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private String getContentSubDir(String contentType) {
        if (contentType == null) return "custom";
        if (contentType.contains("炮塔") || contentType.contains("Turret")) return "blocks";
        if (contentType.contains("物品") || contentType.contains("Item")) return "items";
        if (contentType.contains("材料") || contentType.contains("material")) return "items";
        if (contentType.contains("方块") || contentType.contains("Block")) return "blocks";
        if (contentType.contains("单位") || contentType.contains("Unit")) return "units";
        if (contentType.contains("技术") || contentType.contains("Tech")) return "tech";
        return "custom";
    }

    private String cleanFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }

    private void writeFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private void zipDirectory(File dir, String parentPath, ZipOutputStream zos) throws IOException {
        for (File file : dir.listFiles()) {
            String path = parentPath + "/" + file.getName();
            if (file.isDirectory()) {
                zipDirectory(file, path, zos);
            } else {
                zos.putNextEntry(new ZipEntry(path));
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
