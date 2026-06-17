# GhostWidget 👻

🌐 Languages
[English](README.md) |
[繁體中文](README_zh-TW.md) |
[简体中文](README_zh-CN.md) 

**GhostWidget** is an “invisible” Android home-screen widget designed for people who want to place a vehicle barcode, e-ticket, Instagram profile QR code, or similar content on their home screen or lock screen without blocking their wallpaper. It appears only when needed. By default, it is completely transparent, and only shows your image when you tap once or twice.

This tool is especially suitable for users who want a clean home screen, or who need to quickly display a barcode on the lock screen (with Samsung Good Lock LockStar).

---

## ✨ Features

- **Completely transparent**: Normally hidden entirely against the home screen background, without ruining your carefully arranged layout.
- **Single-/double-tap dual image setup**: Supports assigning different images for single tap and double tap, offering great flexibility.
- **Adjustable response speed**: Optimized tap-detection logic with a customizable response delay from **150 ms to 1000 ms**.
- **Keep screen awake support**: When enabled, the screen will not turn off automatically while the image is displayed, making barcode scanning easier. (Not supported when placed on the lock screen via Samsung Good Lock LockStar.)
- **Material You dynamic theming**: The interface theme automatically adapts to your system wallpaper colors (Android 12+).
- **High-quality display**: Your original images are shown without scaling or compression, ensuring barcode details remain clear and easy to scan.

## 🚀 Usage Guide

1. **Set your images**: Open the app and choose the content you want to display for both “single-tap image” and “double-tap image.”
2. **Add the widget**: Return to the home screen, long-press an empty area -> choose “Widgets” -> find **GhostWidget** and drag it onto the home screen.
3. **Adjust response speed**: Use the slider in the app to set the tap timing that works best for you (200 ms is recommended; 150 ms is the fastest).
4. **Use the widget**:
   - **Single tap**: Show the first image / tap again to hide it.
   - **Double tap**: Show the second image / double-tap again to hide it.

## 📱 Advanced Use: Samsung Good Lock (LockStar)

For Samsung users, you can add GhostWidget to the **lock screen** through the **LockStar** plugin in **Good Lock**.  
This lets you display a vehicle barcode instantly by tapping on the lock screen without unlocking your phone, making it a perfect solution for both efficiency and people who care about keeping things neat.

## 🛠 Technical Specifications

- **Language**: Kotlin
- **UI framework**: Android XML (Material Design 3)
- **Minimum supported version**: Android 7.0 (API 24)
- **Core technologies**:
  - `AppWidgetProvider` for widget state handling.
  - `Static Handler` for precise double-tap interception.
  - `WakeLock Service` to keep the screen awake while displayed.
  - `Material Design` & `Dynamic Colors` for the interface.

## 👤 Author

**yc35723-dot**
- GitHub: [@yc35723-dot](https://github.com/yc35723-dot)

## 📄 License

This project is open source under the [MIT License](LICENSE).