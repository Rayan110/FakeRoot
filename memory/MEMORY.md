# FakeRoot 项目记忆

## 用户偏好

- **语言**: 默认使用简体中文回复，除非用户明确要求使用其他语言

## 项目概述

FakeRoot 是一个基于 MIUI IMQSNative 服务的 Root 权限管理解决方案。利用 MIUI 系统隐藏的 `miui.mqsas.IMQSNative` 服务（具有 root 权限）来执行任意命令。

## 核心技术

### IMQSNative 服务调用格式
```bash
service call miui.mqsas.IMQSNative 21 i32 1 s16 "命令" i32 1 s16 "参数" s16 "输出路径" i32 超时
```

### 关键参数
- Transaction Code: `21` (执行命令)
- 服务名: `miui.mqsas.IMQSNative`
- 输出通过指定文件路径返回

## 模块架构

```
FakeRoot/
├── core/                    # 核心 IMQSNative 执行器 (无外部依赖)
│   └── IMQSNativeExecutor.java  # 底层服务调用封装
│   └── RootExecutor.java        # 高层单例 API
│   └── RootCommand.java         # 命令结果模型
│
├── server/                  # FakeRoot 服务端 (Shizuku 兼容 API)
│   └── Service.java             # 基础服务，重写 newProcess 使用 IMQSNative
│   └── FakeRootService.java     # 主服务入口 (main 方法)
│   └── api/IMQSNativeProcessHolder.java  # 进程封装
│
├── manager/                 # 管理器应用 (Kotlin)
│   └── starter/Starter.kt       # 服务启动逻辑
│   └── MainActivity.kt          # 主界面
│
├── cli/                     # 命令行工具 (su 脚本)
│   └── src/main/assets/su       # Shell 脚本包装器
│
├── bootstarter/             # 开机自启动模块
│
└── Shizuku/                 # 参考的 Shizuku 源码 (不修改)
```

## 关键依赖

- `dev.rikka.hidden:stub:4.2.0` - Hidden API 访问 (compileOnly)
- `dev.rikka.shizuku:api:13.1.5` - Shizuku API 兼容 (仅 manager)

## 构建配置

- minSdk: 24 (Android 7.0)
- targetSdk: 34
- Java: 17
- Kotlin: 1.9.22
- Gradle: 8.5
- Android Gradle Plugin: 8.2.2

## 包名规范

- 核心模块: `com.fakeroot.core`
- 服务端: `com.fakeroot.server`
- 管理器: `com.fakeroot.manager`
- 权限: `com.fakeroot.permission.API`

## 构建命令

```bash
# 构建所有模块
./gradlew assembleDebug

# 构建特定模块
./gradlew :manager:assembleDebug
./gradlew :server:assembleDebug

# 清理构建
./gradlew clean
```

## 注意事项

1. **仅支持 MIUI 设备** - 需要 IMQSNative 服务 (MIUI 12+)
2. 服务入口点: `com.fakeroot.server.FakeRootService.main()`
3. Shizuku/ 目录为参考实现，不修改
4. IMQSNative 可能因 MIUI 版本不同而有差异
