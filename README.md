# FakeRoot

[English](README_EN.md) | 中文

FakeRoot 是一个专为 MIUI 设备设计的 Root 权限管理解决方案，利用 MIUI 系统隐藏的 `miui.mqsas.IMQSNative` 服务以 root 权限执行命令。

## ✨ 功能特性

- 🚀 **无需解锁 Bootloader** - 利用 MIUI 系统自带服务实现 root 权限
- 🔐 **权限管理系统** - 控制哪些应用可以使用 root 权限
- 🔄 **开机自启动** - 可选的开机自动启动服务
- 📱 **图形化管理界面** - 提供友好的管理应用
- 💻 **命令行支持** - 提供 su 脚本，支持其他应用调用
- 🔗 **Shizuku API 兼容** - 与 Shizuku 应用配合使用

## ⚙️ 系统要求

- MIUI 设备（需要 IMQSNative 服务，大多数 MIUI 12+ 设备支持）
- Android 7.0+ (API 24+)
- Shizuku 应用

## 📦 项目结构

```
FakeRoot/
├── core/                    # 核心 IMQSNative 执行器
│   └── src/main/java/com/fakeroot/core/
│       ├── IMQSNativeExecutor.java  # 服务调用封装
│       ├── RootCommand.java         # 命令结果模型
│       └── RootExecutor.java        # 高层 API
│
├── cli/                     # 命令行工具 (su 脚本)
│   └── src/main/assets/su   # Shell 脚本包装器
│
├── server/                  # FakeRoot 服务端
│   └── src/main/java/com/fakeroot/server/
│       ├── FakeRootService.java     # 主服务
│       └── Service.java             # 基础服务类
│
├── manager/                 # 管理器应用
│   └── src/main/java/com/fakeroot/manager/
│       ├── ui/                      # 用户界面
│       └── util/                    # 工具类
│
├── bootstarter/             # 开机自启动模块
│
└── Shizuku/                 # Shizuku 参考实现
```

## 🔧 编译

```bash
# 克隆项目
git clone https://github.com/your-repo/FakeRoot.git
cd FakeRoot

# 编译 Debug 版本
./gradlew assembleDebug

# 编译 Release 版本
./gradlew assembleRelease

# 编译指定模块
./gradlew :manager:assembleDebug
```

编译完成后，APK 文件位于：
- Debug: `manager/build/outputs/apk/debug/manager-debug.apk`
- Release: `manager/build/outputs/apk/release/manager-release.apk`

## 📖 使用指南

### 安装步骤

1. 安装 [Shizuku](https://github.com/RikkaApps/Shizuku) 应用
2. 启动 Shizuku 服务（通过无线调试或 root）
3. 安装 FakeRoot 管理器
4. 授予 Shizuku 权限
5. 点击"安装 su"按钮

### 使用 su 脚本

安装完成后，可以通过以下方式使用：

```bash
# 执行单条命令
/data/local/tmp/su -c 'id'

# 查看系统文件
/data/local/tmp/su -c 'cat /data/system/packages.xml'

# 列出应用数据目录
/data/local/tmp/su -c 'ls /data/data/'
```

### IMQSNative 服务调用格式

```
service call miui.mqsas.IMQSNative 21 i32 1 s16 "sh" i32 1 s16 "脚本路径" s16 "输出文件" i32 超时秒数
```

参数说明：
- `21` - Transaction Code（执行命令）
- `i32 1` - 用户 ID
- `s16 "sh"` - 执行器
- `i32 1` - 参数数量
- `s16 "脚本路径"` - 要执行的脚本文件路径
- `s16 "输出文件"` - 输出结果保存路径
- `i32 超时` - 超时时间（秒）

## 🔒 安全说明

- IMQSNative 服务具有 root 权限，请谨慎使用
- 权限请求会向用户显示，用户可以选择允许或拒绝
- 应用需要声明 `com.fakeroot.permission.API` 权限才能使用 API
- 建议仅在个人设备上使用

## ⚠️ 免责声明

本工具仅供合法用途，如系统管理、开发和安全研究。请负责任地使用，风险自负。作者不对任何滥用或损害负责。

## 🙏 致谢

- [Shizuku](https://github.com/RikkaApps/Shizuku) - 提供了 ADB 权限提升方案
- 酷安的各位大佬

## 📄 许可证

本项目遵循MIT开源协议

## 👤 作者

- **紫檀**
- QQ: 732275573
