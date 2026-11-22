package id.kos.model;
import java.math.BigDecimal;

public class KamarEnsuite extends Kamar {
    public KamarEnsuite(int id, String nomor, BigDecimal harga, BigDecimal utility,
                        String status) {
        super(id, nomor, harga, utility, status);
    }
    @Override public BigDecimal hitungBiayaKhusus() {
        return new BigDecimal("100000");
    }
    @Override public BigDecimal getHargaStandar() { return new BigDecimal("1500000"); }
    @Override public BigDecimal getUtilityStandar() { return new BigDecimal("170000"); }
    @Override public String getTipe() { return "ENSUITE"; }
    @Override public String getNamaTipe() { return "Kamar Ensuite"; }
    @Override public String getNamaGambar() { return "kamar_ensuite.jpg"; }
    @Override public String[] getFasilitas() {
        return new String[]{"Kamar mandi dalam", "Kasur besar", "Lemari built-in", "Wastafel"};
    }
}
