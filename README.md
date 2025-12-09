# 🧩 Mini Sosmed

Aplikasi **Mini Sosmed** adalah aplikasi media sosial sederhana berbasis **Firebase** yang memungkinkan pengguna untuk membuat akun, berbagi postingan, memperbarui profil, mencari pengguna lain, berkomentar pada postingan, dan melakukan chat antar pengguna.  
Dibangun menggunakan **Jetpack Compose**, **Firebase (Auth & Firestore)**, dan **Hilt (Dependency Injection)**.

---

## ✨ Fitur Utama

| Fitur | Deskripsi |
|-------|------------|
| 🔐 **Login & Register** | Autentikasi pengguna menggunakan **Firebase Authentication** (Email & Password). |
| 👤 **Profile User** | Menampilkan dan memperbarui data profil pengguna yang tersimpan di **Firestore**. |
| 🏠 **Dashboard (Home Feed)** | Menampilkan daftar postingan dari seluruh pengguna. |
| 📝 **Add / Update Post** | Pengguna dapat menambahkan dan memperbarui posting mereka sendiri. |
| 💬 **Komentar Post** | Pengguna dapat memberikan komentar pada postingan lain. |
| 🔍 **Search User** | Mencari pengguna lain berdasarkan nama atau username. |
| 💭 **Chat antar Pengguna** | Fitur chatting sederhana antar pengguna tanpa notifikasi realtime. |
| ⚙️ **State Management** | Menggunakan pendekatan berbasis **ViewModel + StateFlow** untuk mengelola UI state. |
| 🧩 **Dependency Injection (Hilt)** | Menggunakan **Hilt** untuk pengelolaan dependency yang efisien dan modular. |

---

## 🛠️ Teknologi yang Digunakan

- **Kotlin**
- **Jetpack Compose** (UI Declarative)
- **Firebase Authentication**
- **Firebase Firestore**
- **Hilt (Dependency Injection)**
- **Coroutines & Flow**
- **MVVM Architecture**
- **Navigation Component**

---

## 🧱 Arsitektur Proyek

Struktur proyek mengikuti pola **MVVM (Model - ViewModel - View)**:

