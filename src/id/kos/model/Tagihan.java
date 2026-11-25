package id.kos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Tagihan {
    public static final String LUNAS = "LUNAS";
    public static final String MENUNGGAK = "MENUNGGAK";
    public static final String BELUM_LUNAS = "BELUM LUNAS";

    private int idTagihan;
    private int idSewa;
    private LocalDate periodeMulai;
    private LocalDate periodeAkhir;
    private LocalDate jatuhTempo;
    private BigDecimal totalTagihan;
    private BigDecimal totalBayar;
    private String nomorKamar;
    private String namaPenghuni;
    private String noHp;

    public Tagihan(int idTagihan, int idSewa, LocalDate periodeMulai, LocalDate periodeAkhir,
        LocalDate jatuhTempo, BigDecimal totalTagihan, BigDecimal totalBayar) {
        this.idTagihan = idTagihan;
        this.idSewa = idSewa;
        this.periodeMulai = periodeMulai;
        this.periodeAkhir = periodeAkhir;
        this.jatuhTempo = jatuhTempo;
        this.totalTagihan = totalTagihan;
        this.totalBayar = totalBayar;
    }

    public BigDecimal hitungSisa() { return totalTagihan.subtract(totalBayar); }
    public boolean isLunas() { return hitungSisa().signum() <= 0; }
    public boolean isMenunggak(LocalDate hariIni) { return !isLunas() && hariIni.isAfter(jatuhTempo); }

    // Jumlah hari keterlambatan terhitung dari jatuh tempo 
    public long hariTerlambat(LocalDate hariIni) {
        return isMenunggak(hariIni) ? ChronoUnit.DAYS.between(jatuhTempo, hariIni) : 0;
    }

    public String getStatus(LocalDate hariIni) {
        if (isLunas()) return LUNAS;
        return isMenunggak(hariIni) ? MENUNGGAK : BELUM_LUNAS;
    }

    public int getIdTagihan() { return idTagihan; }
    public int getIdSewa() { return idSewa; }
    public LocalDate getPeriodeMulai() { return periodeMulai; }
    public LocalDate getPeriodeAkhir() { return periodeAkhir; }
    public LocalDate getJatuhTempo() { return jatuhTempo; }
    public BigDecimal getTotalTagihan() { return totalTagihan; }
    public BigDecimal getTotalBayar() { return totalBayar; }
    public String getNomorKamar() { return nomorKamar; }
    public void setNomorKamar(String nomorKamar) { this.nomorKamar = nomorKamar; }
    public String getNamaPenghuni() { return namaPenghuni; }
    public void setNamaPenghuni(String namaPenghuni) { this.namaPenghuni = namaPenghuni; }
    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }
}
