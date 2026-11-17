package model;

import java.math.BigDecimal;

public abstract class Kamar {
    private int idKamar;
    private String nomorKamar;
    private BigDecimal hargaSewa;
    private BigDecimal biayaUtility;
    private BigDecimal biayaWifi;
    private String status;

    public Kamar(int idKamar, String nomorKamar, BigDecimal hargaSewa,
                 BigDecimal biayaUtility, BigDecimal biayaWifi, String status) {
        this.idKamar = idKamar;
        this.nomorKamar = nomorKamar;
        this.hargaSewa = hargaSewa;
        this.biayaUtility = biayaUtility;
        this.biayaWifi = biayaWifi;
        this.status = status;
    }

    public abstract BigDecimal hitungBiayaKhusus();

    public BigDecimal hitungTotalBulanan() {
        return hargaSewa.add(biayaUtility).add(biayaWifi)
                .add(hitungBiayaKhusus());
    }

    public int getIdKamar() { return idKamar; }
    public void setIdKamar(int idKamar) { this.idKamar = idKamar; }
    public String getNomorKamar() { return nomorKamar; }
    public void setNomorKamar(String nomorKamar) { this.nomorKamar = nomorKamar; }
    public BigDecimal getHargaSewa() { return hargaSewa; }
    public void setHargaSewa(BigDecimal hargaSewa) { this.hargaSewa = hargaSewa; }
    public BigDecimal getBiayaUtility() { return biayaUtility; }
    public void setBiayaUtility(BigDecimal biayaUtility) { this.biayaUtility = biayaUtility; }
    public BigDecimal getBiayaWifi() { return biayaWifi; }
    public void setBiayaWifi(BigDecimal biayaWifi) { this.biayaWifi = biayaWifi; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public abstract String getTipe();
}
