package id.kos.model;
import java.math.BigDecimal;

public class KamarAC extends Kamar {
    public KamarAC(int id, String nomor, BigDecimal harga, BigDecimal utility,
                   BigDecimal wifi, String status) {
        super(id, nomor, harga, utility, wifi, status);
    }
    @Override public BigDecimal hitungBiayaKhusus() {
        return new BigDecimal("75000");
    }
    @Override public String getTipe() { return "AC"; }
}
