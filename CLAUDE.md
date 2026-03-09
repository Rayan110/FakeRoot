# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules
./gradlew assembleDebug

# Build specific module
./gradlew :manager:assembleDebug
./gradlew :server:assembleDebug

# Clean build
./gradlew clean
```

## Architecture Overview

FakeRoot is a root permission management solution for MIUI devices that leverages the hidden `miui.mqsas.IMQSNative` system service, which runs with root privileges.

### Module Structure

- **core** (`com.fakeroot.core`) - Core IMQSNative executor with no external dependencies
  - `IMQSNativeExecutor.java` - Low-level service call wrapper
  - `RootExecutor.java` - High-level singleton API with convenience methods
  - `RootCommand.java` - Result model

- **server** (`com.fakeroot.server`) - FakeRoot service (Shizuku-compatible API)
  - `Service.java` - Base service extending `IShizukuService.Stub`, overrides `newProcess` to use IMQSNative
  - `FakeRootService.java` - Main service entry point (`main()` method)
  - `IMQSNativeProcessHolder.java` - Process wrapper for remote execution

- **manager** (`com.fakeroot.manager`) - Android manager app (Kotlin)
  - `Starter.kt` - Service startup logic via IMQSNative
  - `MainActivity.kt` - Main UI with navigation

- **cli** - Shell script wrapper (`su` command in assets)
- **bootstarter** - Auto-start on boot via `BootReceiver`

### IMQSNative Service Call

The core mechanism uses MIUI's hidden service:

```
service call miui.mqsas.IMQSNative 21 i32 1 s16 "command" i32 1 s16 "args" s16 "outputPath" i32 timeout
```

- Service name: `miui.mqsas.IMQSNative`
- Transaction code `21` = execute command
- Output written to specified file path

### Key Dependencies

- `dev.rikka.hidden:stub:4.2.0` - Hidden API stubs (compileOnly)
- `dev.rikka.shizuku:api:13.1.5` - Shizuku API compatibility (manager only)

### Build Configuration

- minSdk: 24, targetSdk: 34
- Java 17, Kotlin 1.9.22
- AGP 8.2.2

## Important Notes

- **MIUI-only**: Requires `miui.mqsas.IMQSNative` service (MIUI 12+)
- Server entry point: `com.fakeroot.server.FakeRootService.main()`
- Permission declared: `com.fakeroot.permission.API`
- The `Shizuku/` directory contains reference implementation, not modified
