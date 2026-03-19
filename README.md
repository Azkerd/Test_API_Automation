⚙️ Setup: OpenWeatherMap API Testing di Katalon Studio

STEP 1 — Install Katalon Studio

Download di katalon.com/download → pilih versi Windows
Extract / install, lalu buka Katalon Studio
Login dengan akun Katalon (gratis)


STEP 2 — Buat Project Baru

Klik File → New → Project
Isi:

Name: openweather_katalon
Type: API/Web Service
Location: pilih folder sesukamu


Klik OK


STEP 3 — Dapat API Key OpenWeatherMap

Buka openweathermap.org → Sign Up (gratis)
Setelah login → klik foto profil → My API Keys
Copy API key yang sudah ada (default key)
⚠️ Tunggu ±10 menit sebelum key aktif pertama kali


STEP 4 — Set Global Variables

Di panel kiri Tests Explorer → expand Profiles
Double-click default
Klik tombol Add (+) lalu isi satu per satu:

NameValueAPI_KEY'isi_api_key_kamu_disini'BASE_URL'https://api.openweathermap.org/data/2.5'LAT_JAKARTA_SELATAN'-6.2615'LON_JAKARTA_SELATAN'106.8106'UNITS'metric'

Klik Save (Ctrl+S)


STEP 5 — Buat Folder Test Cases

Klik kanan folder Test Cases → New → Folder
Nama folder: API
Klik OK


STEP 6 — Buat & Paste 4 Test Cases
Untuk setiap test case, lakukan langkah ini:

Klik kanan folder API → New → Test Case
Isi nama sesuai tabel di bawah → klik OK
Di editor yang muncul, klik tab Script (bukan Manual!)
Ctrl+A → Delete semua kode default
Paste isi script .groovy dari file yang sudah dibuat
Ctrl+S untuk Save
Ulangi untuk TC berikutnya

Nama Test CaseScript FileTC_001_5Day_Forecast_JakartaSelatanTC_001_...groovyTC_002_Current_Air_Pollution_JakartaSelatanTC_002_...groovyTC_003_Negative_InvalidAPIKeyTC_003_...groovyTC_004_Negative_InvalidCoordinatesTC_004_...groovy

STEP 7 — Buat Test Suite

Klik kanan Test Suites → New → Test Suite
Nama: TS_OpenWeatherMap_JakartaSelatan → OK
Di editor Test Suite, klik tombol Add (+)
Cari dan tambahkan keempat TC satu per satu dengan urutan:

TC_001 → TC_002 → TC_003 → TC_004


Pastikan semua ✅ checked di kolom Run
Ctrl+S Save


STEP 8 — Run Test Suite

Klik kanan TS_OpenWeatherMap_JakartaSelatan → Run
Pastikan Profile = default → klik OK
Monitor output di panel Log Viewer dan Console bawah
Setiap assertion pass akan tampil ✅ di log


STEP 9 — Lihat Report

Setelah run selesai → klik notifikasi "Open Report" yang muncul otomatis
Atau: Tests Explorer → Reports → expand folder timestamp terbaru → double-click file .html
Report terbuka di browser, berisi:

✅ / ❌ per test case
Full request URL & response body
Detail setiap assertion



