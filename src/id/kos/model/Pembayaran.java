package id.kos.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pembayaran {
    private int idPembayaran;
    private int idTagihan;
    private LocalDate tanggalBayar;
    private BigDecimal jumlahBayar;
    private String metodePembayaran;

    public Pembayaran(int idPembayaran, int idTagihan, LocalDate tanggalBayar, BigDecimal jumlahBayar, String metodePembayaran) {
        this.idPembayaran = idPembayaran;
        this.idTagihan = idTagihan;
        this.tanggalBayar = tanggalBayar;
        this.jumlahBayar = jumlahBayar;
        this.metodePembayaran = metodePembayaran;
    }

    public int getIdPembayaran() { return idPembayaran; }
    public int getIdTagihan() { return idTagihan; }
    public LocalDate getTanggalBayar() { return tanggalBayar; }
    public BigDecimal getJumlahBayar() { return jumlahBayar; }
    public String getMetodePembayaran() { return metodePembayaran; }
}
