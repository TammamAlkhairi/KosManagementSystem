package id.kos.model;

public class Penghuni {
    private int idPenghuni;
    private String nama;
    private String noIdentitas;
    private String noHp;
    private String alamat;

    public Penghuni(int idPenghuni, String nama, String noIdentitas, String noHp, String alamat) {
        this.idPenghuni = idPenghuni;
        this.nama = nama;
        this.noIdentitas = noIdentitas;
        this.noHp = noHp;
        this.alamat = alamat;
    }

    public int getIdPenghuni() { return idPenghuni; }
    public void setIdPenghuni(int idPenghuni) { this.idPenghuni = idPenghuni; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getNoIdentitas() { return noIdentitas; }
    public void setNoIdentitas(String noIdentitas) { this.noIdentitas = noIdentitas; }
    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    @Override public String toString() { return nama; }
}
