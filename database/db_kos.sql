CREATE DATABASE IF NOT EXISTS db_kos;
USE db_kos;

CREATE TABLE IF NOT EXISTS kamar (
    id_kamar INT AUTO_INCREMENT PRIMARY KEY,
    nomor_kamar VARCHAR(10) NOT NULL UNIQUE,
    tipe ENUM('AC','NON_AC','ENSUITE') NOT NULL,
    harga_sewa DECIMAL(12,2) NOT NULL,
    biaya_utility DECIMAL(12,2) NOT NULL DEFAULT 0,
    -- Kolom legacy; aplikasi menggabungkan nilainya ke biaya_utility saat membaca data lama.
    biaya_wifi DECIMAL(12,2) NOT NULL DEFAULT 0,
    status ENUM('KOSONG','TERISI') NOT NULL DEFAULT 'KOSONG'
);

CREATE TABLE IF NOT EXISTS penghuni (
    id_penghuni INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    no_identitas VARCHAR(30) UNIQUE,
    no_hp VARCHAR(20),
    alamat TEXT
);

CREATE TABLE IF NOT EXISTS sewa (
    id_sewa INT AUTO_INCREMENT PRIMARY KEY,
    id_kamar INT NOT NULL,
    id_penghuni INT NOT NULL,
    tanggal_mulai DATE NOT NULL,
    tanggal_selesai DATE NULL,
    jenis_periode ENUM('HARIAN','BULANAN') NOT NULL DEFAULT 'BULANAN',
    status_sewa ENUM('AKTIF','SELESAI') NOT NULL DEFAULT 'AKTIF',
    CONSTRAINT fk_sewa_kamar FOREIGN KEY (id_kamar) REFERENCES kamar(id_kamar),
    CONSTRAINT fk_sewa_penghuni FOREIGN KEY (id_penghuni) REFERENCES penghuni(id_penghuni)
);

CREATE TABLE IF NOT EXISTS tagihan (
    id_tagihan INT AUTO_INCREMENT PRIMARY KEY,
    id_sewa INT NOT NULL,
    periode_mulai DATE NOT NULL,
    periode_akhir DATE NOT NULL,
    jatuh_tempo DATE NOT NULL,
    total_tagihan DECIMAL(12,2) NOT NULL,
    total_bayar DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_tagihan_sewa FOREIGN KEY (id_sewa) REFERENCES sewa(id_sewa)
);

CREATE TABLE IF NOT EXISTS pembayaran (
    id_pembayaran INT AUTO_INCREMENT PRIMARY KEY,
    id_tagihan INT NOT NULL,
    tanggal_bayar DATE NOT NULL,
    jumlah_bayar DECIMAL(12,2) NOT NULL,
    metode_pembayaran VARCHAR(30),
    CONSTRAINT fk_pembayaran_tagihan FOREIGN KEY (id_tagihan) REFERENCES tagihan(id_tagihan)
);

-- ============================================================
-- DATA AWAL
-- INSERT IGNORE membuat skrip ini aman dijalankan berulang kali.
-- ============================================================

-- Data kamar
INSERT IGNORE INTO kamar(nomor_kamar,tipe,harga_sewa,biaya_utility,biaya_wifi,status) VALUES
('A01','AC',1200000,150000,0,'KOSONG'),
('A02','NON_AC',850000,130000,0,'TERISI'),
('B01','ENSUITE',1500000,170000,0,'KOSONG');

-- Data demo (contoh penghuni, sewa, tagihan, dan pembayaran)
-- Konsisten dengan status kamar di atas: A02 TERISI (sewa AKTIF), B01 KOSONG (sewa SELESAI).
-- Hapus bagian di bawah ini jika ingin mulai dengan data kosong.
INSERT IGNORE INTO penghuni(id_penghuni, nama, no_identitas, no_hp, alamat) VALUES
(1, 'Budi Santoso', '3216010101900001', '0812-3456-7890', 'Jl. Mawar No. 5, Cikarang'),
(2, 'Siti Rahma',   '3216024505950002', '0857-1122-3344', 'Bekasi'),
(3, 'Andi Wijaya',  '3275031203980003', '0821-9988-7766', 'Bekasi');

INSERT IGNORE INTO sewa(id_sewa, id_kamar, id_penghuni, tanggal_mulai, tanggal_selesai, jenis_periode, status_sewa) VALUES
(1, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A02'), 1, '2026-10-01', NULL,         'BULANAN', 'AKTIF'),
(2, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'B01'), 2, '2026-06-01', '2026-08-31', 'BULANAN', 'SELESAI');

INSERT IGNORE INTO tagihan(id_tagihan, id_sewa, periode_mulai, periode_akhir, jatuh_tempo, total_tagihan, total_bayar) VALUES
(1, 1, '2026-10-01', '2026-10-31', '2026-10-05', 980000, 980000),
(2, 1, '2026-11-01', '2026-11-30', '2026-11-05', 980000, 500000);

INSERT IGNORE INTO pembayaran(id_pembayaran, id_tagihan, tanggal_bayar, jumlah_bayar, metode_pembayaran) VALUES
(1, 1, '2026-10-03', 980000, 'Transfer'),
(2, 2, '2026-11-03', 500000, 'Tunai');
