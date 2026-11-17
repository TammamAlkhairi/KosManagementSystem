# Kos Management System - Java Swing + MySQL

Struktur project ini sengaja dibuat sama dengan struktur folder yang dijelaskan pada laporan:

```text
KosManagementSystem/
|
|-- src/
|   |-- model/
|   |   |-- Kamar.java
|   |   |-- KamarAC.java
|   |   |-- KamarNonAC.java
|   |   `-- KamarEnsuite.java
|   |
|   |-- collection/
|   |   `-- KamarManager.java
|   |
|   |-- database/
|   |   `-- KoneksiDatabase.java
|   |
|   |-- dao/
|   |   `-- KamarDAO.java
|   |
|   |-- view/
|   |   `-- KamarFrame.java
|   |
|   `-- Main.java
|
|-- database/
|   `-- db_kos.sql
|
|-- lib/
|   `-- README.txt
|
`-- README.md
```

## Fitur yang sudah diimplementasikan
- Menampilkan data kamar dari MySQL ke JTable.
- Pencarian kamar berdasarkan nomor kamar.
- Tambah, update, dan hapus data kamar melalui GUI.
- Jenis kamar AC, NON_AC, dan ENSUITE.
- Model PBO: Encapsulation, Inheritance, Abstraction, dan Polymorphism.
- ArrayList untuk menampung daftar kamar dan HashMap untuk indeks nomor kamar.

## Persiapan
1. Install JDK 17 atau lebih baru.
2. Install XAMPP, lalu jalankan MySQL.
3. Import `database/db_kos.sql` melalui phpMyAdmin atau MySQL client.
4. Unduh MySQL Connector/J dari https://dev.mysql.com/downloads/connector/j/
5. Simpan file JAR Connector/J ke folder `lib`.
6. Periksa URL, username, dan password di `src/database/KoneksiDatabase.java`.
   Konfigurasi default adalah `root` tanpa password pada MySQL lokal XAMPP.

## Menjalankan dari Windows Command Prompt
Contoh di bawah mengasumsikan nama file driver `mysql-connector-j-9.x.x.jar`.
Ganti nama tersebut sesuai JAR yang benar-benar Anda unduh.

```bat
javac -cp "lib\mysql-connector-j-9.x.x.jar" -d out src\model\*.java src\collection\*.java src\database\*.java src\dao\*.java src\view\*.java src\Main.java
java -cp "out;lib\mysql-connector-j-9.x.x.jar" Main
```

## Menjalankan melalui IDE
Buat/open Java project pada folder ini, tambahkan seluruh folder `src` sebagai source folder, lalu tambahkan JAR MySQL Connector/J dari folder `lib` sebagai library/dependency. Jalankan class `Main.java`.

## Catatan ruang lingkup
ZIP ini adalah fondasi tugas P10-P13 dan sudah mengimplementasikan CRUD data kamar yang terkoneksi ke MySQL. Tabel penghuni, sewa, tagihan, dan pembayaran telah disiapkan di SQL, tetapi GUI/modul operasional untuk reservasi, penghuni, pembayaran, dan laporan tunggakan belum diimplementasikan. Fitur tersebut merupakan pengembangan lanjutan.

Nilai biaya khusus kamar AC dan ensuite di source code merupakan nilai simulasi dan dapat disesuaikan.
