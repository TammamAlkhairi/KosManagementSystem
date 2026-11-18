package id.kos.view;

import id.kos.dao.PenghuniDAO;
import id.kos.model.Penghuni;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/** Layar Data Penghuni: CRUD dengan pola yang sama seperti Data Kamar. */
public class PenghuniPanel extends JPanel implements Refreshable {
    private final JTextField txtNama = new JTextField(14);
    private final JTextField txtIdentitas = new JTextField(14);
    private final JTextField txtHp = new JTextField(14);
    private final JTextArea txtAlamat = new JTextArea(4, 14);
    private final JTextField txtCari = new JTextField(14);
    private final DefaultTableModel model = Ui.modelTabel("ID", "Nama", "No. Identitas", "No. HP", "Alamat");
    private final JTable tabel = new JTable(model);
    private final PenghuniDAO dao = new PenghuniDAO();
    private int selectedId = -1;

    public PenghuniPanel() {
        setLayout(new BorderLayout(10, 10));
        add(Ui.judul("Data Penghuni"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Penghuni"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        tambahBaris(form, g, 0, "Nama lengkap", txtNama);
        tambahBaris(form, g, 1, "No. identitas", txtIdentitas);
        tambahBaris(form, g, 2, "No. HP", txtHp);
        txtAlamat.setLineWrap(true); txtAlamat.setWrapStyleWord(true);
        tambahBaris(form, g, 3, "Alamat", new JScrollPane(txtAlamat));

        JButton btnSimpan = new JButton("Simpan");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan");
        JPanel actions = new JPanel(new GridLayout(2, 2, 5, 5));
        actions.add(btnSimpan); actions.add(btnUpdate); actions.add(btnHapus); actions.add(btnBersih);

        JPanel left = new JPanel(new BorderLayout(5, 10));
        left.setPreferredSize(new Dimension(330, 0));
        left.add(form, BorderLayout.NORTH);
        left.add(actions, BorderLayout.CENTER);
        left.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        add(left, BorderLayout.WEST);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Cari nama:"));
        search.add(txtCari);
        JButton btnCari = new JButton("Cari");
        JButton btnSemua = new JButton("Tampilkan Semua");
        search.add(btnCari); search.add(btnSemua);
        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(search, BorderLayout.NORTH);
        center.add(new JScrollPane(tabel), BorderLayout.CENTER);
        center.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 10));
        add(center, BorderLayout.CENTER);

        btnSimpan.addActionListener(e -> simpan());
        btnUpdate.addActionListener(e -> update());
        btnHapus.addActionListener(e -> hapus());
        btnBersih.addActionListener(e -> bersihkan());
        btnCari.addActionListener(e -> muatData(txtCari.getText().trim()));
        btnSemua.addActionListener(e -> { txtCari.setText(""); muatData(""); });
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabel.getSelectedRow() >= 0) {
                int row = tabel.convertRowIndexToModel(tabel.getSelectedRow());
                selectedId = (int) model.getValueAt(row, 0);
                txtNama.setText(Ui.teks(model.getValueAt(row, 1)));
                txtIdentitas.setText(Ui.teks(model.getValueAt(row, 2)));
                txtHp.setText(Ui.teks(model.getValueAt(row, 3)));
                txtAlamat.setText(Ui.teks(model.getValueAt(row, 4)));
            }
        });
        muatData("");
    }

    private void tambahBaris(JPanel p, GridBagConstraints g, int row, String label, Component c) {
        g.gridy = row; g.gridx = 0; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1; p.add(c, g);
    }

    @Override public void refresh() { muatData(txtCari.getText().trim()); }

    private Penghuni buatObjek() {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) throw new IllegalArgumentException("Nama wajib diisi.");
        return new Penghuni(selectedId, nama, txtIdentitas.getText(), txtHp.getText(), txtAlamat.getText());
    }

    private void simpan() {
        try {
            dao.insert(buatObjek());
            Ui.info(this, "Data penghuni berhasil disimpan.");
            bersihkan(); muatData("");
        } catch (IllegalArgumentException | SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    private void update() {
        if (selectedId < 0) { Ui.peringatan(this, "Pilih data pada tabel terlebih dahulu."); return; }
        try {
            dao.update(buatObjek());
            Ui.info(this, "Data penghuni berhasil diperbarui.");
            bersihkan(); muatData("");
        } catch (IllegalArgumentException | SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    private void hapus() {
        if (selectedId < 0) { Ui.peringatan(this, "Pilih data pada tabel terlebih dahulu."); return; }
        if (Ui.konfirmasi(this, "Hapus penghuni terpilih?")) {
            try {
                dao.delete(selectedId);
                bersihkan(); muatData("");
            } catch (SQLException ex) {
                Ui.peringatan(this, "Gagal menghapus (mungkin masih memiliki data sewa): " + ex.getMessage());
            }
        }
    }

    private void muatData(String keyword) {
        try {
            model.setRowCount(0);
            for (Penghuni p : dao.getAll(keyword)) {
                model.addRow(new Object[]{p.getIdPenghuni(), p.getNama(), p.getNoIdentitas(), p.getNoHp(), p.getAlamat()});
            }
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        }
    }

    private void bersihkan() {
        selectedId = -1; tabel.clearSelection();
        txtNama.setText(""); txtIdentitas.setText(""); txtHp.setText(""); txtAlamat.setText("");
    }
}
