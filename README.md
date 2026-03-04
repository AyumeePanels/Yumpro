# 📱 YumPRO - Server Monitoring App

Professional server & website monitoring tool for Android with Telegram Bot integration.

![Status](https://img.shields.io/badge/Status-Ready%20to%20Build-00FF88)
![Android](https://img.shields.io/badge/Android-8%2B-00FF88)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)

---

## ✨ FITUR

| Fitur | Keterangan |
|-------|-----------|
| 🌐 HTTP/Website Monitor | Cek status code, response time |
| 🔍 Keyword Monitor | Cek apakah keyword ada di halaman |
| 🏓 Ping Monitor | Ping IP/domain, latency |
| 🔌 Port Monitor | Cek port terbuka/tertutup |
| ⏱ Cron/Heartbeat | Monitor jadwal heartbeat |
| 🔤 DNS Monitor | Verifikasi resolusi DNS |
| 📡 API Monitor | Validasi JSON response |
| 📶 UDP Monitor | Monitor service UDP |
| 🤖 Telegram Bot | Alert realtime + laporan 10 menit |
| 🔔 Push Notification | Notifikasi lokal saat status berubah |
| 📊 Realtime Clock | Jam realtime HH:mm:ss |
| 📈 Response Chart | Grafik response time |
| 📝 Log History | Riwayat lengkap dengan timestamp |
| 🚀 Boot Auto-start | Otomatis jalan saat HP restart |

---

## 📲 CARA BUILD (HANYA PAKAI HP!)

### Metode 1: GitHub Actions (TERMUDAH ✅)

1. **Buat akun GitHub** di https://github.com (gratis)
2. **Buat repository baru** bernama `yumpro`
3. **Upload semua file** dari folder ini ke repository
   - Gunakan aplikasi **GitMobile** atau **Spck Editor** di HP
   - Atau upload manual via browser HP di github.com
4. **GitHub Actions otomatis build!**
   - Pergi ke tab **Actions** di repository kamu
   - Tunggu ~5-10 menit
   - Klik **Build YumPRO APK**
   - Download APK dari bagian **Artifacts** ⬇️

### Metode 2: Termux di HP (Tingkat Lanjut)

```bash
# Install Termux dari F-Droid (bukan Play Store!)
# Lalu jalankan:

pkg update && pkg upgrade
pkg install openjdk-17
pkg install gradle

# Clone/copy project ke HP
cd /sdcard/yumpro

# Build APK
./gradlew assembleDebug

# APK ada di:
# app/build/outputs/apk/debug/app-debug.apk
```

### Metode 3: Online Build (Alternatif)

1. Upload ke **Replit.com**
2. Buat project Android baru
3. Copy semua file
4. Build via terminal Replit

---

## 🤖 SETUP TELEGRAM BOT

1. Buka Telegram → cari **@BotFather**
2. Ketik `/newbot` → ikuti instruksi
3. Copy **Bot Token** yang diberikan
4. Tambahkan bot ke grup kamu
5. Dapatkan **Chat ID** dari **@userinfobot**
6. Di app → tab **Telegram** → masukkan Token & Chat ID
7. Tekan **Test Notification**

---

## 📱 TAMPILAN LAYAR

```
┌─────────────────────────┐
│ YumPRO   ●  14:23:05 │  ← Jam Realtime
├─────────────────────────┤
│  4    2    1    1        │
│ Total Up  Down Err       │  ← Summary
├─────────────────────────┤
│ ● example.com  HEALTHY  │
│   https://...   245ms   │  ← Monitor Cards
├─────────────────────────┤
│ ● api.service  DOWN     │
│   https://...   0ms     │
└─────────────────────────┘
│ Dashboard Logs Tg  ⚙️  │  ← Bottom Nav
```

---

## 🎨 UI / TEMA

- Background: **Dark Navy** `#060D16`
- Accent: **Neon Green** `#00FF88`
- Cards: **Dark Blue** `#0D1F35`
- Status Healthy: **Green** `#00FF88`
- Status Down: **Red** `#FF4444`
- Status Error: **Yellow** `#FFB800`

---

## 📁 STRUKTUR PROJECT

```
app/
├── data/           → Room Database (Monitor, MonitorLog)
├── monitor/        → Logika pengecekan (HTTP, Ping, Port, dll)
├── service/        → Background Services (Foreground + Telegram)
├── receiver/       → Boot Receiver
├── notification/   → Alert notifications
├── telegram/       → Telegram Bot integration
├── ui/             → Semua layar (Dashboard, Detail, Settings, dll)
└── util/           → Helper classes
```

---

## ⚙️ PERSYARATAN TEKNIS

- **Min SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 14 (API 34)
- **Language:** Kotlin
- **Database:** Room
- **Background:** Foreground Service + WorkManager
- **Network:** OkHttp4
- **Charts:** MPAndroidChart

---

## 🔧 TROUBLESHOOTING

**App mati saat layar off?**
→ Settings → Disable Battery Optimization untuk UPRO

**Bot Telegram tidak kirim pesan?**
→ Pastikan bot sudah di-add ke grup dan Chat ID benar

**Build gagal di GitHub Actions?**
→ Cek tab Actions untuk error detail

---

Made with ❤️ - YumPRO v1.0.0
