package id.kos.dao;

import id.kos.database.KoneksiDatabase;
import id.kos.model.Sewa;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SewaDAO {
    private static final String SELECT_JOIN =
            "SELECT s.*, k.nomor_kamar, p.nama FROM sewa s "
          + "JOIN kamar k ON k.id_kamar = s.id_kamar "
          + "JOIN penghuni p ON p.id_penghuni = s.id_penghuni ";

    public List<Sewa> getAll(String keyword, boolean hanyaAktif) throws SQLException {
        List<Sewa> hasil = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE (k.nomor_kamar LIKE ? OR p.nama LIKE ?) "
                + (hanyaAktif ? "AND s.status_sewa = 'AKTIF' " : "")
                + "ORDER BY s.id_sewa DESC";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) hasil.add(buatObjek(rs));
            }
        }
        return hasil;
    }

    // Sewa aktif untuk pilihan saat membuat tagihan.
    public List<Sewa> getAktif() throws SQLException {
        return getAll("", true);
    }

    // Menyimpan sewa dan mengubah kamar menjadi TERISI dalam satu transaksi.
    public void mulaiSewa(Sewa sewa) throws SQLException {
        try (Connection c = KoneksiDatabase.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM kamar WHERE id_kamar = ? FOR UPDATE")) {
                    ps.setInt(1, sewa.getIdKamar());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Kamar tidak ditemukan.");
                        if (!"KOSONG".equals(rs.getString(1))) throw new SQLException("Kamar sudah terisi.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO sewa(id_kamar, id_penghuni, tanggal_mulai, tanggal_selesai, jenis_periode, status_sewa) "
                      + "VALUES(?,?,?,?,?,'AKTIF')")) {
                    ps.setInt(1, sewa.getIdKamar());
                    ps.setInt(2, sewa.getIdPenghuni());
                    ps.setDate(3, Date.valueOf(sewa.getTanggalMulai()));
                    if (sewa.getTanggalSelesai() == null) ps.setNull(4, Types.DATE);
                    else ps.setDate(4, Date.valueOf(sewa.getTanggalSelesai()));
                    ps.setString(5, sewa.getJenisPeriode());
                    ps.executeUpdate();
                }
                ubahStatusKamar(c, sewa.getIdKamar(), "TERISI");
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    // Mengakhiri sewa dan mengembalikan kamar menjadi KOSONG dalam satu transaksi. 
    public void akhiriSewa(int idSewa, LocalDate tanggalSelesai) throws SQLException {
        try (Connection c = KoneksiDatabase.getConnection()) {
            c.setAutoCommit(false);
            try {
                int idKamar;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id_kamar, status_sewa FROM sewa WHERE id_sewa = ? FOR UPDATE")) {
                    ps.setInt(1, idSewa);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Data sewa tidak ditemukan.");
                        if (!Sewa.AKTIF.equals(rs.getString("status_sewa"))) throw new SQLException("Sewa sudah berakhir.");
                        idKamar = rs.getInt("id_kamar");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE sewa SET status_sewa='SELESAI', tanggal_selesai=? WHERE id_sewa=?")) {
                    ps.setDate(1, Date.valueOf(tanggalSelesai));
                    ps.setInt(2, idSewa);
                    ps.executeUpdate();
                }
                ubahStatusKamar(c, idKamar, "KOSONG");
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private void ubahStatusKamar(Connection c, int idKamar, String status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE kamar SET status=? WHERE id_kamar=?")) {
            ps.setString(1, status);
            ps.setInt(2, idKamar);
            ps.executeUpdate();
        }
    }

    private Sewa buatObjek(ResultSet rs) throws SQLException {
        Date selesai = rs.getDate("tanggal_selesai");
        Sewa s = new Sewa(rs.getInt("id_sewa"), rs.getInt("id_kamar"), rs.getInt("id_penghuni"),
                rs.getDate("tanggal_mulai").toLocalDate(),
                selesai == null ? null : selesai.toLocalDate(),
                rs.getString("jenis_periode"), rs.getString("status_sewa"));
        s.setNomorKamar(rs.getString("nomor_kamar"));
        s.setNamaPenghuni(rs.getString("nama"));
        return s;
    }
}
