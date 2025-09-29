# Tugas PPB 2 Login dengan Google menggunakan Firebase

Aplikasi Android untuk autentikasi login menggunakan akun Google dengan bantuan Firebase Authentication.

---

## Fitur

- Login menggunakan akun Google.
- Integrasi Firebase Authentication.
- Menampilkan pesan sukses atau gagal saat login.

---

## Persiapan

1. Buat project di [Firebase Console](https://console.firebase.google.com/).
2. Tambahkan aplikasi Android pada Firebase, dan download `google-services.json`.
3. Masukkan `google-services.json` ke folder `app/` di project Android Studio.
4. Aktifkan Google Sign-In di menu **Authentication > Sign-in method** di Firebase Console.
5. Dapatkan **Web client ID** dari Credentials di Google Cloud Console, yang akan dipakai sebagai `serverClientId`.

---

## Cara Instalasi

1. Clone repo ini:

```bash
git clone https://github.com/Waahyuuu/loginFirebase.git

