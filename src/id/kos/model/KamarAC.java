package id.kos.model;
import java.math.BigDecimal;

public class KamarAC extends Kamar {
    public KamarAC(int id, String nomor, BigDecimal harga, BigDecimal utility,
                   String status) {
        super(id, nomor, harga, utility, status);
    }
    @Override public BigDecimal hitungBiayaKhusus() {
        return new BigDecimal("75000");
    }
    @Override public BigDecimal getHargaStandar() { return new BigDecimal("1200000"); }
    @Override public BigDecimal getUtilityStandar() { return new BigDecimal("150000"); }
    @Override public String getTipe() { return "AC"; }
    @Override public String getNamaTipe() { return "Kamar AC"; }
    @Override public String getNamaGambar() { return "kamar_ac.jpg"; }
    @Override public String[] getFasilitas() {
        return new String[]{"AC", "Kasur double", "Meja", "Gorden", "Jendela besar"};
    }
}
