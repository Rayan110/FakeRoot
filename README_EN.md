# FakeRoot

English | [中文](README.md)

FakeRoot is a root permission management solution for MIUI devices that leverages the hidden `miui.mqsas.IMQSNative` system service to execute commands with root privileges.

## ✨ Features

- 🚀 **No Bootloader Unlock Required** - Uses built-in MIUI system service
- 🔐 **Permission Management** - Control which apps can use root access
- 🔄 **Auto-start on Boot** - Optional automatic service startup
- 📱 **GUI Manager** - User-friendly management application
- 💻 **Command Line Support** - Provides su script for other apps
- 🔗 **Shizuku Compatible** - Works with Shizuku app

## ⚙️ Requirements

- MIUI device with IMQSNative service (most MIUI 12+ devices)
- Android 7.0+ (API 24+)
- Shizuku app

## 📦 Project Structure

```
FakeRoot/
├── core/                    # Core IMQSNative executor
├── cli/                     # Command-line su tool
├── server/                  # FakeRoot server
├── manager/                 # Manager application
├── bootstarter/             # Auto-start module
└── Shizuku/                 # Reference Shizuku implementation
```

## 🔧 Building

```bash
# Clone the project
git clone https://github.com/your-repo/FakeRoot.git
cd FakeRoot

# Build Debug version
./gradlew assembleDebug

# Build Release version
./gradlew assembleRelease
```

## 📖 Usage

### Installation

1. Install [Shizuku](https://github.com/RikkaApps/Shizuku) app
2. Start Shizuku service (via wireless debugging or root)
3. Install FakeRoot Manager
4. Grant Shizuku permission
5. Click "Install su" button

### Using su Script

```bash
# Execute single command
/data/local/tmp/su -c 'id'

# View system files
/data/local/tmp/su -c 'cat /data/system/packages.xml'

# List app data directories
/data/local/tmp/su -c 'ls /data/data/'
```

### IMQSNative Service Call Format

```
service call miui.mqsas.IMQSNative 21 i32 1 s16 "sh" i32 1 s16 "script_path" s16 "output_file" i32 timeout
```

## 🔒 Security

- IMQSNative service has root privileges - use with caution
- Permission requests are shown to users
- Apps must declare `com.fakeroot.permission.API` permission

## ⚠️ Disclaimer

This tool is intended for legitimate purposes only, such as system administration, development, and security research. Use responsibly and at your own risk.

## 🙏 Credits

- [Shizuku](https://github.com/RikkaApps/Shizuku) - ADB privilege escalation
- The users worked on it in KuAn

## 📄 License

This project uses MIT licensing.

## 👤 Author

- **紫檀**
- QQ: 732275573
