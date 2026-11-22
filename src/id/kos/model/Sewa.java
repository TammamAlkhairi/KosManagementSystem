package id.kos.model;

import java.time.LocalDate;

public class Sewa {
    public static final String HARIAN = "HARIAN";
    public static final String BULANAN = "BULANAN";
    public static final String AKTIF = "AKTIF";
    public static final String SELESAI = "SELESAI";

    private int idSewa;
    private int idKamar;
    private int idPenghuni;
    private LocalDate tanggalMulai;
    private LocalDate tanggalSelesai;   // null selama sewa berjalan
    private String jenisPeriode;
    private String statusSewa;
    // data tampilan hasil JOIN
    private String nomorKamar;
    private String namaPenghuni;

    public Sewa(int idSewa, int idKamar, int idPenghuni, LocalDate tanggalMulai,
            LocalDate tanggalSelesai, String jenisPeriode, String statusSewa) {
        this.idSewa = idSewa;
        this.idKamar = idKamar;
        this.idPenghuni = idPenghuni;
        this.tanggalMulai = tanggalMulai;
        this.tanggalSelesai = tanggalSelesai;
        this.jenisPeriode = jenisPeriode;
        this.statusSewa = statusSewa;
    }

    public boolean isAktif() { return AKTIF.equals(statusSewa); }

    public int getIdSewa() { return idSewa; }
    public void setIdSewa(int idSewa) { this.idSewa = idSewa; }
    public int getIdKamar() { return idKamar; }
    public int getIdPenghuni() { return idPenghuni; }
    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public LocalDate getTanggalSelesai() { return tanggalSelesai; }
    public String getJenisPeriode() { return jenisPeriode; }
    public String getStatusSewa() { return statusSewa; }
    public String getNomorKamar() { return nomorKamar; }
    public void setNomorKamar(String nomorKamar) { this.nomorKamar = nomorKamar; }
    public String getNamaPenghuni() { return namaPenghuni; }
    public void setNamaPenghuni(String namaPenghuni) { this.namaPenghuni = namaPenghuni; }

    @Override public String toString() { return nomorKamar + " - " + namaPenghuni + " (" + jenisPeriode + ")"; }
}
