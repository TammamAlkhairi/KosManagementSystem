package id.kos.dao;

import id.kos.database.KoneksiDatabase;
import id.kos.model.Penghuni;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PenghuniDAO {
    public List<Penghuni> getAll(String keyword) throws SQLException {
        List<Penghuni> hasil = new ArrayList<>();
        String sql = "SELECT * FROM penghuni WHERE nama LIKE ? ORDER BY nama";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new Penghuni(rs.getInt("id_penghuni"), rs.getString("nama"),
                            rs.getString("no_identitas"), rs.getString("no_hp"), rs.getString("alamat")));
                }
            }
        }
        return hasil;
    }

    public void insert(Penghuni p) throws SQLException {
        String sql = "INSERT INTO penghuni(nama, no_identitas, no_hp, alamat) VALUES(?,?,?,?)";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, p);
            ps.executeUpdate();
        }
    }

    public void update(Penghuni p) throws SQLException {
        String sql = "UPDATE penghuni SET nama=?, no_identitas=?, no_hp=?, alamat=? WHERE id_penghuni=?";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, p);
            ps.setInt(5, p.getIdPenghuni());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM penghuni WHERE id_penghuni=?";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void isiParameter(PreparedStatement ps, Penghuni p) throws SQLException {
        ps.setString(1, p.getNama());
        // string kosong disimpan sebagai NULL agar tidak bentrok dengan constraint UNIQUE
        ps.setString(2, kosongJadiNull(p.getNoIdentitas()));
        ps.setString(3, kosongJadiNull(p.getNoHp()));
        ps.setString(4, kosongJadiNull(p.getAlamat()));
    }

    private String kosongJadiNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
