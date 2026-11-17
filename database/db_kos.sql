CREATE DATABASE IF NOT EXISTS db_kos;
USE db_kos;

CREATE TABLE IF NOT EXISTS kamar (
    id_kamar INT AUTO_INCREMENT PRIMARY KEY,
    nomor_kamar VARCHAR(10) NOT NULL UNIQUE,
    tipe ENUM('AC','NON_AC','ENSUITE') NOT NULL,
    harga_sewa DECIMAL(12,2) NOT NULL,
    biaya_utility DECIMAL(12,2) NOT NULL DEFAULT 0,
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

INSERT INTO kamar(nomor_kamar,tipe,harga_sewa,biaya_utility,biaya_wifi,status) VALUES
('A01','AC',1200000,100000,50000,'KOSONG'),
('A02','NON_AC',850000,80000,50000,'TERISI'),
('B01','ENSUITE',1500000,120000,50000,'KOSONG');
