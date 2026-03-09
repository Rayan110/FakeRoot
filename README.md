# FakeRoot

FakeRoot is a root permission management solution for MIUI devices that leverages the hidden `miui.mqsas.IMQSNative` system service.

## Features

- Execute commands with root privileges on MIUI devices
- Permission management system for controlling app access
- Auto-start capability on boot
- Compatible with Shizuku API

## Requirements

- MIUI device with IMQSNative service (most MIUI 12+ devices)
- Android 7.0+ (API 24+)

## Project Structure

```
FakeRoot/
├── core/                    # Core IMQSNative executor
├── cli/                     # Command-line su tool
├── server/                  # FakeRoot server (based on Shizuku)
├── manager/                 # Manager application
├── bootstarter/             # Auto-start module
└── Shizuku/                 # Reference Shizuku implementation
```

## Building

```bash
# Clone the project
cd FakeRoot

# Build
./gradlew assembleDebug
```

## Usage

### Using the su script

```bash
# Interactive root shell
su

# Execute command as root
su -c "id"
su -c "ls /data/data"
```

### Starting the service via IMQSNative

The service can be started automatically via the manager app or manually:

```bash
# Using IMQSNative directly
service call miui.mqsas.IMQSNative 21 i32 1 s16 "id" i32 1 s16 "" s16 "/sdcard/test.txt" i32 60
```

## Security Considerations

- IMQSNative service has root privileges
- Permission requests are shown to the user before granting access
- Apps must declare the `com.fakeroot.permission.API` permission

## License

This project is based on Shizuku and follows similar licensing terms.

## Disclaimer

This tool is intended for legitimate purposes only, such as system administration, development, and security research on your own devices. Use responsibly and at your own risk.
