package dao;

import database.KoneksiDatabase;
import model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KamarDAO {
    public List<Kamar> getAll(String keyword) throws SQLException {
        List<Kamar> hasil = new ArrayList<>();
        String sql = "SELECT * FROM kamar WHERE nomor_kamar LIKE ? ORDER BY nomor_kamar";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_kamar");
                    String nomor = rs.getString("nomor_kamar");
                    String tipe = rs.getString("tipe");
                    BigDecimal harga = rs.getBigDecimal("harga_sewa");
                    BigDecimal utility = rs.getBigDecimal("biaya_utility");
                    BigDecimal wifi = rs.getBigDecimal("biaya_wifi");
                    String status = rs.getString("status");

                    Kamar kamar;
                    switch (tipe) {
                        case "AC":
                            kamar = new KamarAC(id, nomor, harga, utility, wifi, status);
                            break;
                        case "ENSUITE":
                            kamar = new KamarEnsuite(id, nomor, harga, utility, wifi, status);
                            break;
                        default:
                            kamar = new KamarNonAC(id, nomor, harga, utility, wifi, status);
                    }
                    hasil.add(kamar);
                }
            }
        }
        return hasil;
    }

    public void insert(Kamar k) throws SQLException {
        String sql = "INSERT INTO kamar(nomor_kamar, tipe, harga_sewa, biaya_utility, biaya_wifi, status) VALUES(?,?,?,?,?,?)";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, k);
            ps.executeUpdate();
        }
    }

    public void update(Kamar k) throws SQLException {
        String sql = "UPDATE kamar SET nomor_kamar=?, tipe=?, harga_sewa=?, biaya_utility=?, biaya_wifi=?, status=? WHERE id_kamar=?";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, k);
            ps.setInt(7, k.getIdKamar());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM kamar WHERE id_kamar=?";
        try (Connection c = KoneksiDatabase.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void isiParameter(PreparedStatement ps, Kamar k) throws SQLException {
        ps.setString(1, k.getNomorKamar());
        ps.setString(2, k.getTipe());
        ps.setBigDecimal(3, k.getHargaSewa());
        ps.setBigDecimal(4, k.getBiayaUtility());
        ps.setBigDecimal(5, k.getBiayaWifi());
        ps.setString(6, k.getStatus());
    }
}
