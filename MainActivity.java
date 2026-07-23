package com.mindustry.modgen;
import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.content.DialogInterface;
import java.io.*;
import java.util.*;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.Spanned;
import android.content.Intent;
/**
 * Mindustry 模组生成器 - 主界面
 * 
 * 功能：
 * - 配置模组基本信息（名称、作者、版本等）
 * - 选择内容类型（炮塔/物品/方块/单位等）
 * - 自定义 JSON 字段（增删改）
 * - 预览生成的 JSON 内容（语法高亮）
 * - 管理已有模组（列表/加载/删除）
 * - 打包成 ZIP 文件
 * - 内置帮助文档
 * 
 * @author wanfenxu199
 * @version 2.0
 */

public class MainActivity extends Activity {
    private String selectedImagePath = null;
    private String selectedImageName = null;
    // 常用字段列表（字段名 → 中文解释）
    private final String[][] COMMON_FIELDS = {
        {"type", "类型"},
        {"name", "名称"},
        {"health", "血量"},
        {"size", "尺寸"},
        {"range", "射程"},
        {"reload", "装填时间"},
        {"shots", "弹丸数"},
        {"shootCone", "射击角度"},
        {"ammoType", "弹药类型"},
        {"buildCost", "建造费用"},
        {"flammability", "可燃性"},
        {"explosiveness", "爆炸性"},
        {"radioactivity", "放射性"},
        {"hardness", "硬度"},
        {"powerProduction", "发电量"},
        {"powerCapacity", "储电量"},
        {"speed", "速度"},
        {"damage", "伤害"},
        {"bulletSpeed", "弹丸速度"},
        {"range", "射程"}
    };
    // UI 组件
    private EditText modName, displayName, author, description, version, minGameVersion;
    private Spinner contentType;
    private Button btnGenerate, btnPack, btnLoad, btnClear;
    private LinearLayout customFieldsContainer;
    private List<EditText[]> customFields = new ArrayList<>();
    private LinearLayout modListContainer;
    private boolean isGenerating = false;
    private static final int REQUEST_SELECT_ICON = 100;
    private String selectedIconPath = null;
    // ==================== 颜色方案 ====================
// 暗色主题，适配长时间使用
    private final int COLOR_BG = 0xFF0f0f1a;              // 背景色 - 深空色
    private final int COLOR_CARD = 0xFF1a1a2e;            // 卡片背景 - 深蓝灰
    private final int COLOR_CARD_LIGHT = 0xFF2a2a44;      // 卡片高亮 - 亮蓝灰
    private final int COLOR_ACCENT = 0xFFe67e22;          // 强调色 - 橙色
    private final int COLOR_ACCENT_DARK = 0xFFc0392b;     // 危险色 - 红色
    private final int COLOR_SUCCESS = 0xFF27ae60;         // 成功色 - 绿色
    private final int COLOR_TEXT = 0xFFe0e0e0;            // 主文字 - 灰白
    private final int COLOR_TEXT_SECONDARY = 0xFF888888;  // 次要文字 - 灰色

    private ModGenerator modGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        //Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
        modGenerator = new ModGenerator(this);
        //Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
        ScrollView scroll = createMainScrollView();
        LinearLayout root = createMainLayout();
        // Toast.makeText(this, "3", Toast.LENGTH_SHORT).show();
        // 标题
        root.addView(createTitle());
        //Toast.makeText(this, "4", Toast.LENGTH_SHORT).show();
        // 基本信息
        root.addView(createSectionHeader("📦 模组基本信息"));
        root.addView(createBasicInfoCard());
        //Toast.makeText(this, "5", Toast.LENGTH_SHORT).show();
        // 内容类型
        root.addView(createSectionHeader("🎯 内容类型"));
        root.addView(createContentTypeCard());
         //Toast.makeText(this, "6", Toast.LENGTH_SHORT).show();
        // 自定义字段
        root.addView(createSectionHeader("⚙️ 自定义字段"));
        root.addView(createCustomFieldsCard());
        //Toast.makeText(this, "7", Toast.LENGTH_SHORT).show();
        // 按钮行
        root.addView(createButtonRow());
         //Toast.makeText(this, "8", Toast.LENGTH_SHORT).show();
        // 已有模组
        root.addView(createSectionHeader("📁 已有模组"));
        root.addView(createModListCard());
         //Toast.makeText(this, "9", Toast.LENGTH_SHORT).show();
        // 底部信息
        root.addView(createFooter());
        // Toast.makeText(this, "10", Toast.LENGTH_SHORT).show();
        scroll.addView(root);
        setContentView(scroll);
        //Toast.makeText(this, "11", Toast.LENGTH_SHORT).show();
        // 加载默认字段
        addDefaultFields();
        //Toast.makeText(this, "12", Toast.LENGTH_SHORT).show();
        // 刷新模组列表
        refreshModList();
         //Toast.makeText(this, "13", Toast.LENGTH_SHORT).show();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                       android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                       android.Manifest.permission.READ_EXTERNAL_STORAGE
                                   }, 1);
            }
        }
    }

    private ScrollView createMainScrollView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(COLOR_BG);
        return scroll;
    }

    private LinearLayout createMainLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 48);
        return root;
    }

    private TextView createTitle() {
        TextView title = new TextView(this);
        title.setText("🔧 Mindustry 模组生成器");
        title.setTextSize(26);
        title.setTextColor(COLOR_ACCENT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        return title;
    }

    private LinearLayout createBasicInfoCard() {
        LinearLayout card = createCard();

        card.addView(createLabel("模组名称 (name)"));
        modName = createEditText("my-mod", "仅限字母、数字、下划线");
        card.addView(modName);

        card.addView(createLabel("显示名称 (displayName)"));
        displayName = createEditText("我的模组", "");
        card.addView(displayName);

        card.addView(createLabel("作者"));
        author = createEditText("匿名", "");
        card.addView(author);

        card.addView(createLabel("描述"));
        description = createEditText("一个自动生成的模组", "");
        card.addView(description);

        card.addView(createLabel("版本"));
        version = createEditText("1.0", "如: 1.0, 1.0.1");
        card.addView(version);

        card.addView(createLabel("最低游戏版本 (minGameVersion)"));
        minGameVersion = createEditText("146", "Mindustry 版本号");
        card.addView(minGameVersion);
        // 选择图标
        card.addView(createLabel("模组图标"));
        Button btnSelectIcon = createStyledButton("🖼️ 选择图标", 0xFF8e44ad);
        btnSelectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectIcon();
                }
            });
        card.addView(btnSelectIcon);
        return card;
    }

    private LinearLayout createContentTypeCard() {
        LinearLayout card = createCard();

        card.addView(createLabel("选择模组类型"));

        contentType = new Spinner(this);
        String[] types = {
            "🎯 炮塔 (ItemTurret)",
            "📦 物品 (Item)", 
            "🧪 材料 (material)",
            "🏗️ 方块 (Block)",
            "🔫 单位 (Unit)",
            "⚡ 技术 (Tech)",
            "🎨 完全自定义"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, types
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contentType.setAdapter(adapter);
        contentType.setBackgroundColor(COLOR_CARD_LIGHT);
        contentType.setPadding(16, 12, 16, 12);
        card.addView(contentType);

        return card;
        
    } 
    private void addCustomField(String key, String value) {
        final LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 4, 0, 4);

        // 字段名输入框
        final EditText etKey = new EditText(this);
        etKey.setHint("字段名");
        etKey.setText(key);
        etKey.setTextSize(13);
        etKey.setTextColor(COLOR_TEXT);
        etKey.setHintTextColor(COLOR_TEXT_SECONDARY);
        etKey.setBackgroundColor(COLOR_CARD_LIGHT);
        etKey.setPadding(12, 10, 12, 10);

        // 下拉按钮（▼）
        Button btnDropdown = new Button(this);
        btnDropdown.setText("▼");
        btnDropdown.setTextColor(COLOR_TEXT);
        btnDropdown.setBackgroundColor(COLOR_CARD_LIGHT);
        btnDropdown.setPadding(8, 8, 8, 8);
        btnDropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFieldDropdown(etKey);
                }
            });

        // 值输入框
        final EditText etValue = new EditText(this);
        etValue.setHint("值");
        etValue.setText(value);
        etValue.setTextSize(13);
        etValue.setTextColor(COLOR_TEXT);
        etValue.setHintTextColor(COLOR_TEXT_SECONDARY);
        etValue.setBackgroundColor(COLOR_CARD_LIGHT);
        etValue.setPadding(12, 10, 12, 10);

        // 选图按钮（🖼️），默认隐藏
        final Button btnImage = new Button(this);
        btnImage.setText("🖼️");
        btnImage.setTextColor(COLOR_TEXT);
        btnImage.setBackgroundColor(COLOR_CARD_LIGHT);
        btnImage.setPadding(8, 8, 8, 8);
        btnImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectImageForField(etValue, etKey);
                }
            });
        btnImage.setVisibility(View.GONE);

        // 监听字段名变化，只有字段名是 "name" 时才显示选图按钮
        etKey.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (s.toString().trim().equals("name")) {
                        btnImage.setVisibility(View.VISIBLE);
                    } else {
                        btnImage.setVisibility(View.GONE);
                    }
                }
            });

        // 删除按钮（✕）
        Button btnDel = new Button(this);
        btnDel.setText("✕");
        btnDel.setTextColor(Color.WHITE);
        btnDel.setTextSize(16);
        btnDel.setBackgroundColor(COLOR_ACCENT_DARK);
        btnDel.setPadding(16, 8, 16, 8);
        btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customFieldsContainer.removeView(row);
                    customFields.remove(new EditText[]{etKey, etValue});
                }
            });

        // 布局参数
        LinearLayout.LayoutParams dropdownLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dropdownLp.setMargins(0, 0, 4, 0);

        LinearLayout.LayoutParams keyLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f);
        keyLp.setMargins(0, 0, 8, 0);

        LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f);
        valueLp.setMargins(0, 0, 4, 0);

        LinearLayout.LayoutParams imageLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        imageLp.setMargins(0, 0, 4, 0);

        LinearLayout.LayoutParams delLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );

        row.addView(btnDropdown, dropdownLp);
        row.addView(etKey, keyLp);
        row.addView(etValue, valueLp);
        row.addView(btnImage, imageLp);
        row.addView(btnDel, delLp);

        customFieldsContainer.addView(row);
        customFields.add(new EditText[]{etKey, etValue});
    }
   /**
     * 创建自定义字段卡片
     * 包含：添加字段、清空字段、预览 JSON 三个操作按钮
     * 以及动态字段列表
     */
    private LinearLayout createCustomFieldsCard() {
        LinearLayout card = createCard();
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, 0, 0, 16);

        Button btnAddField = createStyledButton("➕ 添加字段", COLOR_SUCCESS);
        btnAddField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCustomField("", "");
                }
            });
        buttonRow.addView(btnAddField);

        Button btnClearFields = createStyledButton("🗑️ 清空字段", COLOR_ACCENT_DARK);
        btnClearFields.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearAllFields();
                }
            });
        buttonRow.addView(btnClearFields);

        Button btnPreview = createStyledButton("👁️ 预览", 0xFF8e44ad);
        btnPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPreview();
                }
            });
        buttonRow.addView(btnPreview);

        // 布局参数
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(4, 0, 4, 0);
        btnAddField.setLayoutParams(lp);
        btnClearFields.setLayoutParams(lp);
        btnPreview.setLayoutParams(lp);

        card.addView(buttonRow);

        customFieldsContainer = new LinearLayout(this);
        customFieldsContainer.setOrientation(LinearLayout.VERTICAL);
        card.addView(customFieldsContainer);

        return card;
    }
    private LinearLayout createButtonRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 24, 0, 16);

        btnGenerate = createStyledButton("📄 生成模组", COLOR_ACCENT);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    generateMod();
                }
            });

        btnPack = createStyledButton("📦 打包 ZIP", 0xFF2980b9);
        btnPack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    packToZip();
                }
            });

        btnLoad = createStyledButton("📂 加载模组", 0xFF8e44ad);
        btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadSelectedMod();
                }
            });

        Button btnHelp = createStyledButton("📖 帮助", 0xFF2ecc71);
        btnHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHelp();
                }
            });

        // 统一设置布局参数
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(6, 0, 6, 0);
        btnGenerate.setLayoutParams(lp);
        btnPack.setLayoutParams(lp);
        btnLoad.setLayoutParams(lp);
        btnHelp.setLayoutParams(lp);  

        row.addView(btnGenerate);
        row.addView(btnPack);
        row.addView(btnLoad);
        row.addView(btnHelp);  

        return row;
    }

    private LinearLayout createModListCard() {
        LinearLayout card = createCard();
        card.setOrientation(LinearLayout.VERTICAL);

        modListContainer = new LinearLayout(this);
        modListContainer.setOrientation(LinearLayout.VERTICAL);
        card.addView(modListContainer);

        return card;
    }

    private TextView createFooter() {
        TextView footer = new TextView(this);
        footer.setText("📁 文件保存在 /storage/emulated/0/MindustryMods/");
        footer.setTextSize(13);
        footer.setTextColor(COLOR_TEXT_SECONDARY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 32, 0, 0);
        return footer;
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(COLOR_CARD);
        card.setPadding(24, 20, 24, 20);
        return card;
    }

    private TextView createSectionHeader(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(18);
        tv.setTextColor(COLOR_ACCENT);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(0, 32, 0, 16);
        return tv;
    }

    private TextView createLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(COLOR_TEXT);
        tv.setPadding(0, 12, 0, 4);
        return tv;
    }

    private EditText createEditText(String hint, String detail) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(hint);
        et.setTextSize(15);
        et.setTextColor(COLOR_TEXT);
        et.setHintTextColor(COLOR_TEXT_SECONDARY);
        et.setBackgroundColor(COLOR_CARD_LIGHT);
        et.setPadding(20, 16, 20, 16);

        if (!detail.isEmpty()) {
            et.setContentDescription(detail);
        }
        return et;
    }

    private Button createStyledButton(String text, int color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14);
        btn.setBackgroundColor(color);
        btn.setPadding(20, 14, 20, 14);
        btn.setAllCaps(false);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        return btn;
    }
   /**

    * 添加一行自定义字段

    * @param key   字段名（如 "type"）
    * @param value 字段值（如 "ItemTurret")
    *
  
 
    * 布局： [字段名输入框] [值输入框] [删除按钮]

    */
    

    private void selectIcon() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_SELECT_ICON);
    }
    private void clearAllFields() {
        customFieldsContainer.removeAllViews();
        customFields.clear();
        addDefaultFields();
    }

    private void addDefaultFields() {
        addCustomField("type", "ItemTurret");
        addCustomField("name", "super-turret");
        addCustomField("health", "800");
        addCustomField("size", "2");
        addCustomField("range", "16");
    }
    /**
     * 预览 JSON 内容
     * 使用 SpannableStringBuilder 实现语法高亮：
     * - 键名：金色 (#FFD700)
     * - 值：白色
     */
    
    
    private void showPreview() {
        String json = buildContentJson();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📄 预览 JSON");

        // 使用 ScrollView 包裹
        ScrollView scroll = new ScrollView(this);

        // 使用 TextView 显示带颜色的 JSON
        TextView tv = new TextView(this);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(12);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(32, 32, 32, 32);
        tv.setBackgroundColor(COLOR_BG);

        // 简单高亮处理
        SpannableStringBuilder builder2 = new SpannableStringBuilder();
        String[] lines = json.split("\n");
        for (String line : lines) {
            if (line.contains(":")) {
                // 键名高亮（黄色）
                int colonIndex = line.indexOf(":");
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex + 1);
                    String value = line.substring(colonIndex + 1);

                    builder2.append(key, new ForegroundColorSpan(0xFFFFD700), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder2.append(value + "\n", new ForegroundColorSpan(Color.WHITE), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                builder2.append(line + "\n");
            }
        }

        tv.setText(builder2);
        scroll.addView(tv);

        builder.setView(scroll);
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        builder.show();
    }
    /**
     * 构建 JSON 字符串
     * 自动识别数据类型：
     * - 数字：不加引号 (800)
     * - 布尔值：不加引号 (true/false)
     * - 数组：不加引号 (["a", "b"])
     * - 字符串：自动加引号 ("文本")
     */
    private String buildContentJson() {
        StringBuilder sb = new StringBuilder("{\n");
        int size = customFields.size();
        for (int i = 0; i < size; i++) {
            String key = customFields.get(i)[0].getText().toString().trim();
            String value = customFields.get(i)[1].getText().toString().trim();
            if (key.isEmpty()) continue;

            boolean isNumber = value.matches("-?\\d+(\\.\\d+)?");
            boolean isBoolean = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
            boolean isArray = value.startsWith("[") && value.endsWith("]");

            sb.append("  \"").append(key).append("\": ");
            if (isNumber || isBoolean || isArray) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
            if (i < size - 1) sb.append(",");
            sb.append("\n");
        }
        return sb.append("}").toString();
    }

    private void refreshModList() {
        modListContainer.removeAllViews();

        List<File> mods = modGenerator.getExistingMods();
        if (mods.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无已生成的模组");
            empty.setTextColor(COLOR_TEXT_SECONDARY);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 20, 0, 20);
            modListContainer.addView(empty);
            return;
        }

        for (File mod : mods) {
            LinearLayout item = createModListItem(mod);
            modListContainer.addView(item);
        }
    }

    private LinearLayout createModListItem(final File mod) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setBackgroundColor(COLOR_CARD_LIGHT);
        item.setPadding(16, 12, 16, 12);
        item.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = new TextView(this);
        name.setText(mod.getName());
        name.setTextColor(COLOR_TEXT);
        name.setTextSize(14);
        name.setTypeface(Typeface.DEFAULT_BOLD);

        Button btnLoad = new Button(this);
        btnLoad.setText("加载");
        btnLoad.setTextColor(Color.WHITE);
        btnLoad.setTextSize(12);
        btnLoad.setBackgroundColor(0xFF8e44ad);
        btnLoad.setPadding(16, 8, 16, 8);
        btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadModData(mod.getName());
                }
            });

        Button btnDelete = new Button(this);
        btnDelete.setText("删除");
        btnDelete.setTextColor(Color.WHITE);
        btnDelete.setTextSize(12);
        btnDelete.setBackgroundColor(COLOR_ACCENT_DARK);
        btnDelete.setPadding(16, 8, 16, 8);
        btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteMod(mod.getName());
                }
            });

        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnLp.setMargins(8, 0, 0, 0);

        item.addView(name, nameLp);
        item.addView(btnLoad, btnLp);
        item.addView(btnDelete, btnLp);

        return item;
    }
    // 显示常用字段下拉列表
    private void showFieldDropdown(final EditText targetField) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📋 选择常用字段");

        // 构建带解释的列表
        String[] fieldNames = new String[COMMON_FIELDS.length];
        for (int i = 0; i < COMMON_FIELDS.length; i++) {
            fieldNames[i] = COMMON_FIELDS[i][0] + "  (" + COMMON_FIELDS[i][1] + ")";
        }

        builder.setItems(fieldNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 只填入字段名
                    targetField.setText(COMMON_FIELDS[which][0]);
                }
            });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
    private EditText pendingImageValue = null;
    private EditText pendingImageKey = null;

            private void selectImageForField(final EditText valueField, final EditText keyField) {
            pendingImageValue = valueField;
            pendingImageKey = keyField;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SELECT_ICON);
            }
    private void copyImageToSprites(String modName, String subDir, String imagePath, String imageName) {
        try {
            File dir = modGenerator.getModDirectory(modName);
            File spritesDir = new File(dir, "sprites");
            spritesDir.mkdirs();

            File targetDir = new File(spritesDir, subDir);
            targetDir.mkdirs();

            File targetFile = new File(targetDir, imageName);

            InputStream is = getContentResolver().openInputStream(Uri.parse(imagePath));
            FileOutputStream fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();

        } catch (Exception e) {
            Toast.makeText(this, "⚠️ 图片复制失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void generateMod() {
        // 防重复点击
        if (isGenerating) {
            Toast.makeText(this, "⏳ 正在生成中，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }

        // 输入校验
        String name = modName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "❌ 模组名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!name.matches("^[a-zA-Z0-9\\-_]+$")) {
            Toast.makeText(this, "❌ 模组名称只能包含字母、数字、下划线、连字符", Toast.LENGTH_SHORT).show();
            return;
        }

        String display = displayName.getText().toString().trim();
        if (display.isEmpty()) {
            Toast.makeText(this, "❌ 显示名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String ver = version.getText().toString().trim();
        if (ver.isEmpty()) {
            Toast.makeText(this, "❌ 版本号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String minVer = minGameVersion.getText().toString().trim();
        if (minVer.isEmpty()) {
            Toast.makeText(this, "❌ 最低游戏版本不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 开始生成
        isGenerating = true;

        ModGenerator.ModConfig config = new ModGenerator.ModConfig();
        config.name = name;
        config.displayName = display;
        config.author = author.getText().toString().trim();
        config.description = description.getText().toString().trim();
        config.version = ver;
        config.minGameVersion = minVer;
        config.contentType = contentType.getSelectedItem().toString();

        // 收集自定义字段
        config.customFields.clear();
        for (EditText[] pair : customFields) {
            String key = pair[0].getText().toString().trim();
            String value = pair[1].getText().toString().trim();
            if (!key.isEmpty()) {
                config.customFields.put(key, value);
            }
        }

        // 如果没有任何自定义字段，提示用户
        if (config.customFields.isEmpty()) {
            Toast.makeText(this, "⚠️ 请至少添加一个字段", Toast.LENGTH_SHORT).show();
            isGenerating = false;
            return;
        }

        if (modGenerator.generateMod(config)) {
            // 复制图标
            if (selectedIconPath != null && !selectedIconPath.isEmpty()) {
                modGenerator.copyIconToMod(config.name, selectedIconPath);
            }

            // 复制选中的图片到 sprites/
            if (selectedImagePath != null && !selectedImagePath.isEmpty() && selectedImageName != null) {
                String subDir = modGenerator.getContentSubDir(config.contentType);
                copyImageToSprites(config.name, subDir, selectedImagePath, selectedImageName);
            }

            refreshModList();
            Toast.makeText(this, "✅ 模组已生成！\n图片已放入 sprites/ 对应目录", Toast.LENGTH_LONG).show();
        }
        isGenerating = false;
    }

    private void packToZip() {
        String name = modName.getText().toString().trim();
        modGenerator.packToZip(name);
    }

    private void loadModData(String modName) {
        this.modName.setText(modName);
        Toast.makeText(this, "已加载: " + modName, Toast.LENGTH_SHORT).show();
    }

    private void loadSelectedMod() {
        String name = modName.getText().toString().trim();
        if (!name.isEmpty()) {
            loadModData(name);
        } else {
            Toast.makeText(this, "请输入或选择模组名称", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMod(String modName) {
        // 删除模组目录
        File dir = new File(modGenerator.getModsDirectory(), modName);
        if (dir.exists()) {
            deleteDirectory(dir);
            refreshModList();
            Toast.makeText(this, "已删除: " + modName, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectory(child);
            }
        }
        dir.delete();
    }
    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📖 模组制作帮助");

        android.webkit.WebView wv = new android.webkit.WebView(this);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);

        try {
            InputStream is = getAssets().open("help.html");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String html = new String(buffer, "UTF-8");
            wv.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            wv.loadData("<html><body><h1>加载失败</h1></body></html>", "text/html", "UTF-8");
        }

        builder.setView(wv);
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_ICON && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();

                if (pendingImageValue != null && pendingImageKey != null) {
                    // 获取 key 字段的值作为文件名
                    String keyName = pendingImageKey.getText().toString().trim();
                    if (keyName.isEmpty()) {
                        keyName = "image";
                    }
                    String fileName = keyName + ".png";

                    // 填入值字段
                    pendingImageValue.setText(fileName);

                    // 保存路径供生成时复制
                    selectedImagePath = imageUri.toString();
                    selectedImageName = fileName;

                    pendingImageValue = null;
                    pendingImageKey = null;
                    Toast.makeText(this, "✅ 图片已选择: " + fileName, Toast.LENGTH_SHORT).show();

                } else {
                    // 模组图标
                    selectedIconPath = imageUri.toString();
                    Toast.makeText(this, "✅ 图标已选择", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        // 隐藏键盘
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        super.onBackPressed();
    }
}
