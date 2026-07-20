# 🔧 Mindustry 模组生成器

在 Android 手机上快速生成 Mindustry 模组文件的工具，无需电脑！

## ✨ 功能特性

- 📦 **生成模组文件** — 自动生成 `mod.hjson` 和 `content/*.json`
- 📦 **打包 ZIP** — 一键打包成可导入游戏的 ZIP 文件
- 👁️ **JSON 预览** — 语法高亮显示生成的 JSON 内容
- 📂 **模组管理** — 查看、加载、删除已生成的模组
- 📖 **内置帮助** — 包含常用字段说明和示例
- ⚙️ **完全自定义** — 支持任意 JSON 字段，自由扩展

## 📱 适用平台

- Android 5.0+
- 支持手机、电视（Android TV）

## 🚀 快速开始

1. 填写模组基本信息（名称、作者、版本等）
2. 选择内容类型（炮塔/物品/方块/单位等）
3. 添加自定义字段
4. 点击 **"生成模组文件"**
5. 可选：点击 **"打包 ZIP"** 导入游戏

## 📂 文件位置

生成的文件保存在：/storage/emulated/0/MindustryMods/

## 📷 截图

![主界面](https://github.com/user-attachments/assets/bed7b3fb-7b61-4582-bc13-b3d6c21fd53d)

![预览 JSON](https://github.com/user-attachments/assets/7cd3b37a-7b68-4b76-936b-9d7554c33cdd)

![模组列表](https://github.com/user-attachments/assets/96ca73f2-f95a-4094-a430-4a67dc43f80b)

## ⚙️ 内容类型支持

| 类型 | 存放目录 |
|------|----------|
| 🎯 炮塔 | `content/blocks/` |
| 📦 物品 | `content/items/` |
| 🧪 材料 | `content/items/` |
| 🏗️ 方块 | `content/blocks/` |
| 🔫 单位 | `content/units/` |
| ⚡ 技术 | `content/tech/` |

## 🛠️ 开发环境

- AIDE Pro
- JDK 7/8
- Android SDK 26

## ⚠️ 注意事项

### 编译说明
- 使用 AIDE Pro 编译时请选择 **"正式"** 变体
- **不要使用 "调试aide" 变体**，会导致 APK 无法运行

### 权限说明
- 读写存储权限：用于生成和保存模组文件

## 📄 开源协议

本项目采用 [MIT License](LICENSE)

## 🙏 致谢

- [Mindustry](https://github.com/Anuken/Mindustry) — 游戏本体
- AIDE Pro — Android 开发工具

## 👤 作者

wanfenxu199

---

⭐ 如果这个项目对你有帮助，请点个 Star 支持一下！


