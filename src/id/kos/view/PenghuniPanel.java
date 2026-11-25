package id.kos.view;

import id.kos.dao.PenghuniDAO;
import id.kos.model.Penghuni;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

// Data Penghuni: CRUD, sama seperti Data Kamar.
public class PenghuniPanel extends JPanel implements Refreshable {
    private final JTextField txtNama = Ui.kolom(14);
    private final JTextField txtIdentitas = Ui.kolom(14);
    private final JTextField txtHp = Ui.kolom(14);
    private final JTextArea txtAlamat = new JTextArea(4, 14);
    private final JTextField txtCari = Ui.kolom(14);
    private final DefaultTableModel model = Ui.modelTabel("ID", "Nama", "No. Identitas", "No. HP", "Alamat");
    private final JTable tabel = new JTable(model);
    private final PenghuniDAO dao = new PenghuniDAO();
    private int selectedId = -1;

    public PenghuniPanel() {
        setLayout(new BorderLayout());
        setBackground(Ui.LATAR);
        add(Ui.judul("Data Penghuni", "Kelola data identitas dan kontak penghuni kos"), BorderLayout.NORTH);

        JPanel formIsi = Ui.formPanel();
        Ui.baris(formIsi, 0, "Nama lengkap", txtNama);
        Ui.baris(formIsi, 1, "No. identitas", txtIdentitas);
        Ui.baris(formIsi, 2, "No. HP", txtHp);
        txtAlamat.setLineWrap(true); txtAlamat.setWrapStyleWord(true);
        JScrollPane gulirAlamat = new JScrollPane(txtAlamat);
        gulirAlamat.setBorder(Ui.bingkaiBulat());
        Ui.baris(formIsi, 3, "Alamat", gulirAlamat);
        JPanel form = Ui.kartu("Form Penghuni");
        form.add(formIsi, BorderLayout.CENTER);

        JButton btnSimpan = Ui.tombol("Simpan", Ui.Gaya.PRIMER);
        JButton btnUpdate = Ui.tombol("Update", Ui.Gaya.SUKSES);
        JButton btnHapus = Ui.tombol("Hapus", Ui.Gaya.BAHAYA);
        JButton btnBersih = Ui.tombol("Bersihkan");
        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        actions.add(btnSimpan); actions.add(btnUpdate); actions.add(btnHapus); actions.add(btnBersih);
        form.add(actions, BorderLayout.SOUTH);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(360, 0));
        left.add(form, BorderLayout.NORTH);
        left.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));
        add(left, BorderLayout.WEST);

        JButton btnCari = Ui.tombol("Cari", Ui.Gaya.PRIMER);
        JButton btnSemua = Ui.tombol("Tampilkan Semua");
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        search.setOpaque(false);
        search.add(new JLabel("Cari nama:"));
        search.add(txtCari);
        search.add(btnCari); search.add(btnSemua);
        JPanel center = Ui.kartu(null);
        center.add(search, BorderLayout.NORTH);
        center.add(Ui.gulir(tabel), BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 20, 20));
        wrap.add(center, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
        Ui.lebarKolom(tabel, 40, 140, 185, 140, 170);
        txtCari.addActionListener(e -> muatData(txtCari.getText().trim()));

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
