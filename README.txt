FF PROFILE CHECKER - ANDROID WEBVIEW NATIVE (tanpa Flutter)

PERINTAH BUILD (tidak ada flag --android-skip-build-dependency-validation):
  export ANDROID_HOME=/path/ke/android-sdk
  bash build.sh
atau manual:
  echo "sdk.dir=$ANDROID_HOME" > local.properties
  gradle assembleRelease --no-daemon

HASIL APK:
  app/build/outputs/apk/release/app-release.apk   (juga disalin ke FFChecker.apk oleh build.sh)

PERSYARATAN: JDK 17, Android SDK platform android-34 + build-tools, Gradle 8.7+.

INSTALL DI HP: kirim APK ke HP, buka file, izinkan "Instal dari sumber tidak dikenal", lalu Instal.
Jika gagal "App not installed": hapus versi lama aplikasi (tanda tangan beda) lalu instal ulang.

Aset web ada di app/src/main/assets/index.html (HTML+CSS+JS dalam satu file, tanpa path eksternal).

BUILD TANPA INSTAL APA PUN (GitHub Actions):
  1. Buat repository baru di github.com, upload SEMUA isi folder ini (termasuk folder .github).
  2. Buka tab Actions > "Build APK" > Run workflow.
  3. Setelah hijau, buka run tersebut, unduh "FFChecker-apk" di bagian Artifacts, ekstrak, hasilnya app-release.apk.
