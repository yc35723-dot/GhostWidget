# GhostWidget 👻

🌐 Languages
[English](README.md) |
[繁體中文](README_zh-TW.md) |
[简体中文](README_zh-CN.md) 

🔗 下載連結 
https://github.com/yc35723-dot/GhostWidget/releases/download/GhostWidget-v1.1/GhostWidget-v1.1-release.apk

**GhostWidget** 是一款專為 Android 設計的「隱形」桌面小工具 。旨在為想要在桌面或鎖定畫面上想放載具條碼/電子票券/IG 的個人檔案 QR-Code ...等等，但又不想擋到桌布的人們設計，只在需要時出現! 它在預設情況完全透明，只有在您單擊或雙擊時才會顯示您的圖片。

這款工具特別適合追求桌面簡潔、或是需要在鎖定螢幕（配合 Samsung Goodlock LockStar）快速出示條碼的使用者。

---

## ✨ 特色功能

- **完全透明**：平時完全隱藏在桌面背景，不破壞您的精心排版。
- **單/雙擊雙圖設定**：支援為「單擊」與「雙擊」分別設定不同的圖片，靈活度高。
- **可調整響應速度**：經過最佳化的點擊判定邏輯，支援自訂 **150ms - 1000ms** 的反應延遲。
- **螢幕常亮支援**：開啟功能後，只要圖片正在顯示，螢幕就不會自動關閉，方便掃描條碼。(在Samsung Goodlock LockStar放在鎖定螢幕時不支援）
- **Material You 動態取色**：介面主題會自動跟隨系統桌布顏色進行變化（ Android 12+）。
- **高品質顯示**：不縮放、不壓縮您的原始圖片，確保條碼細節清晰可見。

## 🚀 使用指南

1. **設定圖片**：開啟 App，分別為「單擊圖片」與「雙擊圖片」選擇您想顯示的內容。
2. **加入小工具**：回到桌面，長按空白處 -> 選擇「小工具」 -> 找到 **GhostWidget** 並拖曳至桌面。
3. **調整反應速度**：在 App 內使用滑桿調整最適合您的點擊節奏（建議設定為 200ms，150ms最快）。
4. **操作小工具**：
   - **單擊**：顯示第一張圖片 / 再次單擊隱藏。
   - **雙擊**：顯示第二張圖片 / 再次雙擊隱藏。

## 📱 進階應用：Samsung Good Lock (LockStar)

對於三星使用者，您可以透過 **Good Lock** 中的 **LockStar** 插件，將 GhostWidget 新增至「鎖定螢幕」。
這讓您無需解鎖手機，只需在鎖定畫面點擊，即可立即顯示載具條碼，是強迫症與效率追求者的完美方案！

## 🛠 技術規格

- **開發語言**：Kotlin
- **UI 框架**：Android XML (Material Design 3)
- **最低支援版本**：Android 7.0 (API 24)
- **核心技術**：
  - `AppWidgetProvider` 處理小工具狀態。
  - `Static Handler` 確保精確的雙擊攔截。
  - `WakeLock Service` 實作顯示期間螢幕常亮。
  - `Material Design` & `Dynamic Colors` 介面。

## 👤 作者

**yc35723-dot**
- GitHub: [@yc35723-dot](https://github.com/yc35723-dot)

## 📄 開源協議

本專案採用 [MIT License](LICENSE) 協議開源。
