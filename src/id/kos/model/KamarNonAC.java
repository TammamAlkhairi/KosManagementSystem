package id.kos.model;
import java.math.BigDecimal;

public class KamarNonAC extends Kamar {
    public KamarNonAC(int id, String nomor, BigDecimal harga, BigDecimal utility,
                      String status) {
        super(id, nomor, harga, utility, status);
    }
    @Override public BigDecimal hitungBiayaKhusus() {
        return BigDecimal.ZERO;
    }
    @Override public String getTipe() { return "NON_AC"; }
}
