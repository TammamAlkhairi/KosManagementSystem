package id.kos.dao;

import id.kos.database.KoneksiDatabase;
import id.kos.model.Pembayaran;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PembayaranDAO {
    public List<Pembayaran> getByTagihan(int idTagihan) throws SQLException {
        List<Pembayaran> hasil = new ArrayList<>();
        String sql = "SELECT * FROM pembayaran WHERE id_tagihan = ? ORDER BY tanggal_bayar, id_pembayaran";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTagihan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new Pembayaran(rs.getInt("id_pembayaran"), rs.getInt("id_tagihan"),
                            rs.getDate("tanggal_bayar").toLocalDate(), rs.getBigDecimal("jumlah_bayar"),
                            rs.getString("metode_pembayaran")));
                }
            }
        }
        return hasil;
    }

    // Menyimpan pembayaran dan menambah tagihan,total_bayar dalam satu transaksi.
    public void simpan(Pembayaran p) throws SQLException {
        try (Connection c = KoneksiDatabase.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal sisa;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT total_tagihan - total_bayar FROM tagihan WHERE id_tagihan = ? FOR UPDATE")) {
                    ps.setInt(1, p.getIdTagihan());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Tagihan tidak ditemukan.");
                        sisa = rs.getBigDecimal(1);
                    }
                }
                if (p.getJumlahBayar().signum() <= 0) throw new SQLException("Jumlah bayar harus lebih dari 0.");
                if (p.getJumlahBayar().compareTo(sisa) > 0) throw new SQLException("Jumlah bayar melebihi sisa tagihan.");
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO pembayaran(id_tagihan, tanggal_bayar, jumlah_bayar, metode_pembayaran) VALUES(?,?,?,?)")) {
                    ps.setInt(1, p.getIdTagihan());
                    ps.setDate(2, Date.valueOf(p.getTanggalBayar()));
                    ps.setBigDecimal(3, p.getJumlahBayar());
                    ps.setString(4, p.getMetodePembayaran());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE tagihan SET total_bayar = total_bayar + ? WHERE id_tagihan = ?")) {
                    ps.setBigDecimal(1, p.getJumlahBayar());
                    ps.setInt(2, p.getIdTagihan());
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
