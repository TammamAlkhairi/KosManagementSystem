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

-- Data kamar: 12 kamar (4 AC, 4 Non-AC, 4 Ensuite)
INSERT IGNORE INTO kamar(nomor_kamar,tipe,harga_sewa,biaya_utility,biaya_wifi,status) VALUES
('A01','AC',1200000,150000,0,'TERISI'),
('A02','NON_AC',850000,130000,0,'TERISI'),
('B01','ENSUITE',1500000,170000,0,'KOSONG'),
('A03','AC',1200000,150000,0,'TERISI'),
('A04','AC',1200000,150000,0,'TERISI'),
('A05','AC',1200000,150000,0,'TERISI'),
('A06','NON_AC',850000,130000,0,'TERISI'),
('A07','NON_AC',850000,130000,0,'TERISI'),
('A08','NON_AC',850000,130000,0,'KOSONG'),
('B02','ENSUITE',1500000,170000,0,'TERISI'),
('B03','ENSUITE',1500000,170000,0,'TERISI'),
('B04','ENSUITE',1500000,170000,0,'TERISI');

-- Data demo: 10 penghuni (penyewa), sewa, tagihan, dan pembayaran.
-- Tanggal acuan: November 2026. Ada penghuni yang lunas, mencicil, dan menunggak supaya
-- dashboard, Tagihan, dan Laporan Tunggakan punya contoh nyata.
-- Hapus bagian di bawah ini jika ingin mulai dengan data kosong.
INSERT IGNORE INTO penghuni(id_penghuni, nama, no_identitas, no_hp, alamat) VALUES
(1, 'Budi Santoso', '3216010101900001', '0812-3456-7890', 'Jl. Mawar No. 5, Cikarang'),
(2, 'Siti Rahma', '3216024505950002', '0857-1122-3344', 'Bekasi'),
(3, 'Andi Wijaya', '3275031203980003', '0821-9988-7766', 'Bekasi'),
(4, 'Dewi Lestari', '3275044708970004', '0812-5550-0004', 'Jl. Kenanga No. 12, Bekasi'),
(5, 'Rizky Pratama', '3216051509990005', '0813-5550-0005', 'Jl. Melati No. 3, Karawang'),
(6, 'Putri Ayu Ningsih', '3273066302000006', '0821-5550-0006', 'Jl. Dahlia No. 8, Bandung'),
(7, 'Fajar Nugroho', '3374071007960007', '0857-5550-0007', 'Jl. Pemuda No. 21, Semarang'),
(8, 'Maya Sari', '3201082204010008', '0812-5550-0008', 'Jl. Raya Bogor Km 30, Depok'),
(9, 'Hendra Gunawan', '3171090611950009', '0856-5550-0009', 'Jl. Cempaka No. 7, Jakarta Timur'),
(10, 'Nur Aini', '3578104403020010', '0878-5550-0010', 'Jl. Anggrek No. 15, Surabaya');

INSERT IGNORE INTO sewa(id_sewa, id_kamar, id_penghuni, tanggal_mulai, tanggal_selesai, jenis_periode, status_sewa) VALUES
(1, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A02'), 1, '2026-10-01', NULL, 'BULANAN', 'AKTIF'),
(2, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'B01'), 2, '2026-06-01', '2026-08-31', 'BULANAN', 'SELESAI'),
(3, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'B02'), 2, '2026-09-01', NULL, 'BULANAN', 'AKTIF'),
(4, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A01'), 3, '2026-10-01', NULL, 'BULANAN', 'AKTIF'),
(5, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A03'), 4, '2026-08-01', NULL, 'BULANAN', 'AKTIF'),
(6, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A04'), 5, '2026-09-01', NULL, 'BULANAN', 'AKTIF'),
(7, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A05'), 6, '2026-10-01', NULL, 'BULANAN', 'AKTIF'),
(8, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A06'), 7, '2026-07-01', NULL, 'BULANAN', 'AKTIF'),
(9, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'A07'), 8, '2026-10-01', NULL, 'BULANAN', 'AKTIF'),
(10, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'B03'), 9, '2026-09-01', NULL, 'BULANAN', 'AKTIF'),
(11, (SELECT id_kamar FROM kamar WHERE nomor_kamar = 'B04'), 10, '2026-11-01', NULL, 'BULANAN', 'AKTIF');

INSERT IGNORE INTO tagihan(id_tagihan, id_sewa, periode_mulai, periode_akhir, jatuh_tempo, total_tagihan, total_bayar) VALUES
(1, 1, '2026-10-01', '2026-10-31', '2026-10-05', 980000, 980000),
(2, 1, '2026-11-01', '2026-11-30', '2026-11-05', 980000, 500000),
(3, 3, '2026-09-01', '2026-09-30', '2026-09-05', 1770000, 1770000),
(4, 3, '2026-10-01', '2026-10-31', '2026-10-05', 1770000, 1770000),
(5, 3, '2026-11-01', '2026-11-30', '2026-11-05', 1770000, 1770000),
(6, 4, '2026-10-01', '2026-10-31', '2026-10-05', 1350000, 1350000),
(7, 4, '2026-11-01', '2026-11-30', '2026-11-05', 1350000, 0),
(8, 5, '2026-08-01', '2026-08-31', '2026-08-05', 1350000, 1350000),
(9, 5, '2026-09-01', '2026-09-30', '2026-09-05', 1350000, 1350000),
(10, 5, '2026-10-01', '2026-10-31', '2026-10-05', 1350000, 1350000),
(11, 5, '2026-11-01', '2026-11-30', '2026-11-05', 1350000, 1350000),
(12, 6, '2026-09-01', '2026-09-30', '2026-09-05', 1350000, 1350000),
(13, 6, '2026-10-01', '2026-10-31', '2026-10-05', 1350000, 1350000),
(14, 6, '2026-11-01', '2026-11-30', '2026-11-05', 1350000, 700000),
(15, 7, '2026-10-01', '2026-10-31', '2026-10-05', 1350000, 1350000),
(16, 7, '2026-11-01', '2026-11-30', '2026-11-05', 1350000, 1350000),
(17, 8, '2026-07-01', '2026-07-31', '2026-07-05', 980000, 980000),
(18, 8, '2026-08-01', '2026-08-31', '2026-08-05', 980000, 980000),
(19, 8, '2026-09-01', '2026-09-30', '2026-09-05', 980000, 980000),
(20, 8, '2026-10-01', '2026-10-31', '2026-10-05', 980000, 0),
(21, 8, '2026-11-01', '2026-11-30', '2026-11-05', 980000, 0),
(22, 9, '2026-10-01', '2026-10-31', '2026-10-05', 980000, 980000),
(23, 9, '2026-11-01', '2026-11-30', '2026-11-05', 980000, 980000),
(24, 10, '2026-09-01', '2026-09-30', '2026-09-05', 1770000, 1770000),
(25, 10, '2026-10-01', '2026-10-31', '2026-10-05', 1770000, 1770000),
(26, 10, '2026-11-01', '2026-11-30', '2026-11-05', 1770000, 1000000),
(27, 11, '2026-11-01', '2026-11-30', '2026-11-05', 1770000, 1770000);

INSERT IGNORE INTO pembayaran(id_pembayaran, id_tagihan, tanggal_bayar, jumlah_bayar, metode_pembayaran) VALUES
(1, 1, '2026-10-03', 980000, 'Transfer'),
(2, 2, '2026-11-03', 500000, 'Tunai'),
(3, 3, '2026-09-02', 1770000, 'Tunai'),
(4, 4, '2026-10-02', 1770000, 'Tunai'),
(5, 5, '2026-11-02', 1770000, 'Tunai'),
(6, 6, '2026-10-03', 1350000, 'Transfer'),
(7, 8, '2026-08-04', 1350000, 'Tunai'),
(8, 9, '2026-09-04', 1350000, 'Tunai'),
(9, 10, '2026-10-04', 1350000, 'Tunai'),
(10, 11, '2026-11-04', 1350000, 'Tunai'),
(11, 12, '2026-09-02', 1350000, 'Transfer'),
(12, 13, '2026-10-02', 1350000, 'Transfer'),
(13, 14, '2026-11-02', 700000, 'Transfer'),
(14, 15, '2026-10-03', 1350000, 'Tunai'),
(15, 16, '2026-11-03', 1350000, 'Tunai'),
(16, 17, '2026-07-04', 980000, 'Transfer'),
(17, 18, '2026-08-04', 980000, 'Transfer'),
(18, 19, '2026-09-04', 980000, 'Transfer'),
(19, 22, '2026-10-02', 980000, 'Tunai'),
(20, 23, '2026-11-02', 500000, 'Tunai'),
(21, 23, '2026-11-11', 480000, 'Transfer'),
(22, 24, '2026-09-03', 1770000, 'Transfer'),
(23, 25, '2026-10-03', 1770000, 'Transfer'),
(24, 26, '2026-11-03', 1000000, 'Transfer'),
(25, 27, '2026-11-04', 1770000, 'Tunai');

-- Samakan status kamar dengan sewa aktif, termasuk bila database lama sudah berisi data awal.
UPDATE kamar SET status = CASE WHEN EXISTS (
    SELECT 1 FROM sewa WHERE sewa.id_kamar = kamar.id_kamar AND sewa.status_sewa = 'AKTIF'
) THEN 'TERISI' ELSE 'KOSONG' END;
