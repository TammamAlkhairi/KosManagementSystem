package id.kos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Kamar {
    /** Jumlah hari yang dipakai untuk mengonversi total bulanan menjadi tarif harian. */
    public static final int HARI_PER_BULAN = 30;

    private int idKamar;
    private String nomorKamar;
    private BigDecimal hargaSewa;
    private BigDecimal biayaUtility;
    private String status;

    public Kamar(int idKamar, String nomorKamar, BigDecimal hargaSewa,
                 BigDecimal biayaUtility, String status) {
        this.idKamar = idKamar;
        this.nomorKamar = nomorKamar;
        this.hargaSewa = hargaSewa;
        this.biayaUtility = biayaUtility;
        this.status = status;
    }

    public abstract BigDecimal hitungBiayaKhusus();

    /** Nama berkas foto tipe kamar (ada di folder images/). Setiap subclass menentukan fotonya sendiri. */
    public abstract String getNamaGambar();

    /** Nama tipe yang enak dibaca di layar, mis. "Kamar AC". */
    public abstract String getNamaTipe();

    /** Fasilitas khas tipe kamar (nilai awal; silakan sesuaikan dengan kondisi kos sebenarnya). */
    public abstract String[] getFasilitas();

    /** Harga sewa standar tipe ini, terisi otomatis di form Data Kamar. Ubah di tiap subclass bila tarif kos berubah. */
    public abstract BigDecimal getHargaStandar();

    /** Biaya utility standar tipe ini (listrik, air, wifi), terisi otomatis di form Data Kamar. */
    public abstract BigDecimal getUtilityStandar();

    public BigDecimal hitungTotalBulanan() {
        return hargaSewa.add(biayaUtility).add(hitungBiayaKhusus());
    }

    /**
     * Tarif per hari untuk sewa HARIAN = total bulanan / 30, dibulatkan ke atas ke kelipatan Rp1.000.
     * (Aturan simulasi; ubah di sini bila kos memakai tarif harian yang berbeda.)
     */
    public BigDecimal hitungTarifHarian() {
        BigDecimal perHari = hitungTotalBulanan().divide(BigDecimal.valueOf(HARI_PER_BULAN), 0, RoundingMode.CEILING);
        BigDecimal seribu = BigDecimal.valueOf(1000);
        return perHari.divide(seribu, 0, RoundingMode.CEILING).multiply(seribu);
    }

    public int getIdKamar() { return idKamar; }
    public void setIdKamar(int idKamar) { this.idKamar = idKamar; }
    public String getNomorKamar() { return nomorKamar; }
    public void setNomorKamar(String nomorKamar) { this.nomorKamar = nomorKamar; }
    public BigDecimal getHargaSewa() { return hargaSewa; }
    public void setHargaSewa(BigDecimal hargaSewa) { this.hargaSewa = hargaSewa; }
    public BigDecimal getBiayaUtility() { return biayaUtility; }
    public void setBiayaUtility(BigDecimal biayaUtility) { this.biayaUtility = biayaUtility; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public abstract String getTipe();

    /** Dipakai JComboBox (Sewa & Laporan) agar yang tampil "A01 - Kamar AC", bukan alamat objek. */
    @Override public String toString() { return nomorKamar + " - " + getNamaTipe(); }
}
