package id.kos.model;
import java.math.BigDecimal;

public class KamarNonAC extends Kamar {
    public KamarNonAC(int id, String nomor, BigDecimal harga, BigDecimal utility,String status) {
        // INHERITANCE 
        super(id, nomor, harga, utility, status);
    }

    // OVERRIDE DAN POLYMORPHISM
    @Override public BigDecimal hitungBiayaKhusus() {
        return BigDecimal.ZERO;
    }
    @Override public BigDecimal getHargaStandar() { return new BigDecimal("850000"); }
    @Override public BigDecimal getUtilityStandar() { return new BigDecimal("130000"); }
    @Override public String getTipe() { return "NON_AC"; }
    @Override public String getNamaTipe() { return "Kamar Non-AC"; }
    @Override public String getNamaGambar() { return "kamar_non_ac.jpg"; }
    @Override public String[] getFasilitas() {
        return new String[]{"Ventilasi udara", "Kasur single", "Meja & kursi", "Lampu dinding"};
    }
}
