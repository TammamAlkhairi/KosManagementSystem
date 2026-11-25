package id.kos.dao;

import id.kos.database.KoneksiDatabase;
import id.kos.model.Tagihan;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TagihanDAO {
    private static final String SELECT_JOIN =
            "SELECT t.*, k.id_kamar, k.nomor_kamar, p.nama, p.no_hp FROM tagihan t "
          + "JOIN sewa s ON s.id_sewa = t.id_sewa "
          + "JOIN kamar k ON k.id_kamar = s.id_kamar "
          + "JOIN penghuni p ON p.id_penghuni = s.id_penghuni ";

    public List<Tagihan> getAll() throws SQLException {
        List<Tagihan> hasil = new ArrayList<>();
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_JOIN + "ORDER BY t.jatuh_tempo DESC, t.id_tagihan DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) hasil.add(buatObjek(rs));
        }
        return hasil;
    }

    // Menambah tagihan baru, menolak bila periode yang sama untuk sewa tersebut sudah ada.
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

    
     // Laporan Tunggakan,tagihan yang belum lunas dan jatuh temponya sudah lewat dari tanggal
    public List<Tagihan> getMenunggak(LocalDate tanggalAcuan, int idKamar) throws SQLException {
        return ambilBelumLunas("t.jatuh_tempo < ?", tanggalAcuan, idKamar);
    }

    // OVERLOADING:
    // Nama method sama, tetapi parameter berbeda. Method ini memanggil versi dengan idKamar = -1.
    public List<Tagihan> getMenunggak(LocalDate tanggalAcuan) throws SQLException {
        return getMenunggak(tanggalAcuan, -1);
    }

    // Laporan Pengingat, tagihan yang belum lunas dan jatuh temponya dalam beberapa hari ke depan.
    public List<Tagihan> getPengingat(LocalDate hariIni, int hariKeDepan) throws SQLException {
        return ambilBelumLunas("t.jatuh_tempo <= ?", hariIni.plusDays(hariKeDepan), -1);
    }

    private List<Tagihan> ambilBelumLunas(String syaratTempo, LocalDate batas, int idKamar) throws SQLException {
        List<Tagihan> hasil = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE t.total_bayar < t.total_tagihan AND " + syaratTempo
                + (idKamar > 0 ? " AND k.id_kamar = ?" : "")
                + " ORDER BY t.jatuh_tempo ASC, t.id_tagihan ASC";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(batas));
            if (idKamar > 0) ps.setInt(2, idKamar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) hasil.add(buatObjek(rs));
            }
        }
        return hasil;
    }

    private Tagihan buatObjek(ResultSet rs) throws SQLException {
        Tagihan t = new Tagihan(rs.getInt("id_tagihan"), rs.getInt("id_sewa"),
                rs.getDate("periode_mulai").toLocalDate(), rs.getDate("periode_akhir").toLocalDate(),
                rs.getDate("jatuh_tempo").toLocalDate(),
                rs.getBigDecimal("total_tagihan"), rs.getBigDecimal("total_bayar"));
        t.setNomorKamar(rs.getString("nomor_kamar"));
        t.setNamaPenghuni(rs.getString("nama"));
        t.setNoHp(rs.getString("no_hp"));
        return t;
    }
}
