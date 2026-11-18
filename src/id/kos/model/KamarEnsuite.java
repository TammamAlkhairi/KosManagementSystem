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
    @Override public String getTipe() { return "ENSUITE"; }
}
