package id.kos.dao;

import id.kos.database.KoneksiDatabase;
import id.kos.model.Tagihan;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TagihanDAO {
    private static final String SELECT_JOIN =
            "SELECT t.*, k.nomor_kamar, p.nama FROM tagihan t "
          + "JOIN sewa s ON s.id_sewa = t.id_sewa "
          + "JOIN kamar k ON k.id_kamar = s.id_kamar "
          + "JOIN penghuni p ON p.id_penghuni = s.id_penghuni ";

    public List<Tagihan> getAll() throws SQLException {
        List<Tagihan> hasil = new ArrayList<>();
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_JOIN + "ORDER BY t.jatuh_tempo DESC, t.id_tagihan DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tagihan t = new Tagihan(rs.getInt("id_tagihan"), rs.getInt("id_sewa"),
                        rs.getDate("periode_mulai").toLocalDate(), rs.getDate("periode_akhir").toLocalDate(),
                        rs.getDate("jatuh_tempo").toLocalDate(),
                        rs.getBigDecimal("total_tagihan"), rs.getBigDecimal("total_bayar"));
                t.setNomorKamar(rs.getString("nomor_kamar"));
                t.setNamaPenghuni(rs.getString("nama"));
                hasil.add(t);
            }
        }
        return hasil;
    }

    // Menambah tagihan baru; menolak bila periode yang sama untuk sewa tersebut sudah ada. 
    public void insert(Tagihan t) throws SQLException {
        try (Connection c = KoneksiDatabase.getConnection()) {
            try (PreparedStatement cek = c.prepareStatement(
                    "SELECT COUNT(*) FROM tagihan WHERE id_sewa = ? AND periode_mulai = ?")) {
                cek.setInt(1, t.getIdSewa());
                cek.setDate(2, Date.valueOf(t.getPeriodeMulai()));
                try (ResultSet rs = cek.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) throw new SQLException("Tagihan untuk periode tersebut sudah ada.");
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO tagihan(id_sewa, periode_mulai, periode_akhir, jatuh_tempo, total_tagihan, total_bayar) "
                  + "VALUES(?,?,?,?,?,0)")) {
                ps.setInt(1, t.getIdSewa());
                ps.setDate(2, Date.valueOf(t.getPeriodeMulai()));
                ps.setDate(3, Date.valueOf(t.getPeriodeAkhir()));
                ps.setDate(4, Date.valueOf(t.getJatuhTempo()));
                ps.setBigDecimal(5, t.getTotalTagihan());
                ps.executeUpdate();
            }
        }
    }

    /**
     * TODO (tahap berikutnya - Laporan Tunggakan):
     * ambil tagihan dengan total_bayar &lt; total_tagihan dan jatuh_tempo &lt; tanggalAcuan,
     * lengkap dengan no. HP penghuni untuk ditampilkan pada laporan.
     */
    public List<Tagihan> getMenunggak(LocalDate tanggalAcuan) throws SQLException {
        throw new UnsupportedOperationException("Laporan tunggakan belum diimplementasikan.");
    }
}
