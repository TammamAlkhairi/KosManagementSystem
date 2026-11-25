package id.kos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Kamar {
    // INHERITANCE DAN ABSTRAKSI

    // Jumlah hari yang dipakai untuk mengonversi total bulanan menjadi tarif harian. 
    public static final int HARI_PER_BULAN = 30;

    // ENCAPSULATION
    // Atribut dibuat private agar data hanya diubah melalui constructor, getter, dan setter.
    private int idKamar;
    private String nomorKamar;
    private BigDecimal hargaSewa;
    private BigDecimal biayaUtility;
    private String status;

    public Kamar(int idKamar, String nomorKamar, BigDecimal hargaSewa, BigDecimal biayaUtility, String status) {
        this.idKamar = idKamar;
        this.nomorKamar = nomorKamar;
        this.hargaSewa = hargaSewa;
        this.biayaUtility = biayaUtility;
        this.status = status;
    }

    // POLYMORPHISM
    // abstrak
    public abstract BigDecimal hitungBiayaKhusus();

    // Nama foto tipe kamar.
    public abstract String getNamaGambar();

    // Nama tipe kamar.
    public abstract String getNamaTipe();

    // Fasilitas khas tipe kamar.
    public abstract String[] getFasilitas();

    // Harga sewa standar tipe kamar, terisi otomatis di form Data Kamar. 
    public abstract BigDecimal getHargaStandar();

    // Biaya utility standar tipe kamar, listrik, air, wifi, terisi otomatis di form Data Kamar.
    public abstract BigDecimal getUtilityStandar();

    public BigDecimal hitungTotalBulanan() {
        return hargaSewa.add(biayaUtility).add(hitungBiayaKhusus());
    }

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

    @Override public String toString() { return nomorKamar + " - " + getNamaTipe(); }
}
