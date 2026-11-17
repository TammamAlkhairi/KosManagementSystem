package view;

import collection.KamarManager;
import dao.KamarDAO;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class KamarFrame extends JFrame {
    private final JTextField txtNomor = new JTextField(12);
    private final JTextField txtHarga = new JTextField(12);
    private final JTextField txtUtility = new JTextField(12);
    private final JTextField txtWifi = new JTextField(12);
    private final JTextField txtCari = new JTextField(14);
    private final JComboBox<String> cmbTipe =
            new JComboBox<>(new String[]{"AC", "NON_AC", "ENSUITE"});
    private final JComboBox<String> cmbStatus =
            new JComboBox<>(new String[]{"KOSONG", "TERISI"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Nomor", "Tipe", "Harga", "Utility", "Wi-Fi", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabel = new JTable(model);
    private final KamarDAO dao = new KamarDAO();
    private final KamarManager manager = new KamarManager();
    private int selectedId = -1;

    public KamarFrame() {
        txtUtility.setText("0");
        txtWifi.setText("0");
        setTitle("Kos Management System - Manajemen Kamar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("KOS MANAGEMENT SYSTEM  |  DATA KAMAR");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Form Kamar"));
        form.add(new JLabel("Nomor kamar")); form.add(txtNomor);
        form.add(new JLabel("Jenis kamar")); form.add(cmbTipe);
        form.add(new JLabel("Harga sewa")); form.add(txtHarga);
        form.add(new JLabel("Biaya utility")); form.add(txtUtility);
        form.add(new JLabel("Biaya Wi-Fi")); form.add(txtWifi);
        form.add(new JLabel("Status")); form.add(cmbStatus);

        JButton btnSimpan = new JButton("Simpan");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan");
        JPanel actions = new JPanel(new GridLayout(2, 2, 5, 5));
        actions.add(btnSimpan); actions.add(btnUpdate);
        actions.add(btnHapus); actions.add(btnBersih);
        JPanel left = new JPanel(new BorderLayout(5, 10));
        left.setPreferredSize(new Dimension(300, 0));
        left.add(form, BorderLayout.NORTH);
        left.add(actions, BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Cari nomor kamar:"));
        search.add(txtCari);
        JButton btnCari = new JButton("Cari");
        JButton btnRefresh = new JButton("Tampilkan Semua");
        search.add(btnCari); search.add(btnRefresh);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(search, BorderLayout.NORTH);
        center.add(new JScrollPane(tabel), BorderLayout.CENTER);
        center.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 10));
        add(center, BorderLayout.CENTER);

        btnSimpan.addActionListener(e -> simpan());
        btnUpdate.addActionListener(e -> update());
        btnHapus.addActionListener(e -> hapus());
        btnCari.addActionListener(e -> muatData(txtCari.getText().trim()));
        btnRefresh.addActionListener(e -> { txtCari.setText(""); muatData(""); });
        btnBersih.addActionListener(e -> bersihkan());

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabel.getSelectedRow() >= 0) {
                int row = tabel.convertRowIndexToModel(tabel.getSelectedRow());
                selectedId = (int) model.getValueAt(row, 0);
                txtNomor.setText(String.valueOf(model.getValueAt(row, 1)));
                cmbTipe.setSelectedItem(model.getValueAt(row, 2));
                txtHarga.setText(String.valueOf(model.getValueAt(row, 3)));
                txtUtility.setText(String.valueOf(model.getValueAt(row, 4)));
                txtWifi.setText(String.valueOf(model.getValueAt(row, 5)));
                cmbStatus.setSelectedItem(model.getValueAt(row, 6));
            }
        });
        muatData("");
    }

    private Kamar buatObjek() {
        String nomor = txtNomor.getText().trim();
        if (nomor.isEmpty()) throw new IllegalArgumentException("Nomor kamar wajib diisi.");
        BigDecimal harga = new BigDecimal(txtHarga.getText().trim());
        BigDecimal utility = new BigDecimal(txtUtility.getText().trim());
        BigDecimal wifi = new BigDecimal(txtWifi.getText().trim());
        String tipe = (String) cmbTipe.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();
        if ("AC".equals(tipe)) return new KamarAC(selectedId, nomor, harga, utility, wifi, status);
        if ("ENSUITE".equals(tipe)) return new KamarEnsuite(selectedId, nomor, harga, utility, wifi, status);
        return new KamarNonAC(selectedId, nomor, harga, utility, wifi, status);
    }

    private void simpan() {
        try {
            Kamar k = buatObjek();
            dao.insert(k);
            JOptionPane.showMessageDialog(this, "Data kamar berhasil disimpan.");
            bersihkan(); muatData("");
        } catch (NumberFormatException ex) {
            pesan("Harga dan biaya harus berupa angka.");
        } catch (IllegalArgumentException | SQLException ex) {
            pesan(ex.getMessage());
        }
    }

    private void update() {
        if (selectedId < 0) { pesan("Pilih data pada tabel terlebih dahulu."); return; }
        try {
            dao.update(buatObjek());
            JOptionPane.showMessageDialog(this, "Data kamar berhasil diperbarui.");
            bersihkan(); muatData("");
        } catch (NumberFormatException ex) {
            pesan("Harga dan biaya harus berupa angka.");
        } catch (IllegalArgumentException | SQLException ex) {
            pesan(ex.getMessage());
        }
    }

    private void hapus() {
        if (selectedId < 0) { pesan("Pilih data pada tabel terlebih dahulu."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Hapus kamar terpilih?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                dao.delete(selectedId);
                bersihkan(); muatData("");
            } catch (SQLException ex) { pesan("Gagal menghapus: " + ex.getMessage()); }
        }
    }

    private void muatData(String keyword) {
        try {
            var data = dao.getAll(keyword);
            manager.setData(data);
            model.setRowCount(0);
            for (Kamar k : manager.getDaftarKamar()) {
                model.addRow(new Object[]{k.getIdKamar(), k.getNomorKamar(),
                    k.getTipe(), k.getHargaSewa(), k.getBiayaUtility(),
                    k.getBiayaWifi(), k.getStatus()});
            }
        } catch (SQLException ex) {
            pesan("Koneksi/database bermasalah: " + ex.getMessage());
        }
    }

    private void bersihkan() {
        selectedId = -1; tabel.clearSelection();
        txtNomor.setText(""); txtHarga.setText("");
        txtUtility.setText("0"); txtWifi.setText("0");
        cmbTipe.setSelectedIndex(0); cmbStatus.setSelectedIndex(0);
    }

    private void pesan(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Informasi", JOptionPane.WARNING_MESSAGE);
    }
}
