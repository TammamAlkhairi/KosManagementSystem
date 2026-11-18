package id.kos.view;

import id.kos.dao.KamarDAO;
import id.kos.dao.PenghuniDAO;
import id.kos.dao.SewaDAO;
import id.kos.model.Kamar;
import id.kos.model.Penghuni;
import id.kos.model.Sewa;
import id.kos.util.Format;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Layar Transaksi Sewa: memilih kamar KOSONG dan penghuni, lalu memulai / mengakhiri sewa. */
public class SewaPanel extends JPanel implements Refreshable {
    private final JComboBox<Kamar> cmbKamar = new JComboBox<>();
    private final JComboBox<Penghuni> cmbPenghuni = new JComboBox<>();
    private final JTextField txtMulai = new JTextField(12);
    private final JTextField txtSelesai = new JTextField(12);
    private final JRadioButton rbBulanan = new JRadioButton("BULANAN", true);
    private final JRadioButton rbHarian = new JRadioButton("HARIAN");
    private final JLabel lblEstimasi = new JLabel("-");
    private final JTextField txtCari = new JTextField(12);
    private final JCheckBox chkAktif = new JCheckBox("Hanya yang AKTIF");
    private final DefaultTableModel model = Ui.modelTabel("ID", "Kamar", "Penghuni", "Mulai", "Selesai", "Periode", "Status");
    private final JTable tabel = new JTable(model);

    private final KamarDAO kamarDao = new KamarDAO();
    private final PenghuniDAO penghuniDao = new PenghuniDAO();
    private final SewaDAO sewaDao = new SewaDAO();
    private boolean memuat = false;

    public SewaPanel() {
        setLayout(new BorderLayout(10, 10));
        add(Ui.judul("Transaksi Sewa (Reservasi)"), BorderLayout.NORTH);

        ButtonGroup grup = new ButtonGroup();
        grup.add(rbBulanan); grup.add(rbHarian);
        JPanel periode = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        periode.add(rbBulanan); periode.add(rbHarian);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Sewa"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        baris(form, g, 0, "Kamar (KOSONG)", cmbKamar);
        baris(form, g, 1, "Penghuni", cmbPenghuni);
        baris(form, g, 2, "Tanggal mulai", txtMulai);
        baris(form, g, 3, "Tanggal selesai", txtSelesai);
        baris(form, g, 4, "Jenis periode", periode);
        lblEstimasi.setFont(new Font("SansSerif", Font.BOLD, 16));
        JPanel est = new JPanel(new GridLayout(2, 1));
        est.setBorder(BorderFactory.createTitledBorder("Estimasi biaya per bulan"));
        est.add(lblEstimasi);
        est.add(new JLabel("sewa + utility + Wi-Fi + biaya khusus tipe kamar"));
        g.gridy = 5; g.gridx = 0; g.gridwidth = 2; form.add(est, g);

        JButton btnMulai = new JButton("Mulai Sewa");
        JButton btnAkhiri = new JButton("Akhiri Sewa");
        JButton btnBersih = new JButton("Bersihkan");
        JPanel actions = new JPanel(new GridLayout(1, 3, 5, 5));
        actions.add(btnMulai); actions.add(btnAkhiri); actions.add(btnBersih);
        JPanel left = new JPanel(new BorderLayout(5, 10));
        left.setPreferredSize(new Dimension(380, 0));
        left.add(form, BorderLayout.NORTH);
        left.add(actions, BorderLayout.CENTER);
        left.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        add(left, BorderLayout.WEST);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Cari:")); search.add(txtCari);
        JButton btnCari = new JButton("Cari");
        search.add(btnCari); search.add(chkAktif);
        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(search, BorderLayout.NORTH);
        center.add(new JScrollPane(tabel), BorderLayout.CENTER);
        center.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 10));
        add(center, BorderLayout.CENTER);

        cmbKamar.addActionListener(e -> perbaruiEstimasi());
        btnMulai.addActionListener(e -> mulai());
        btnAkhiri.addActionListener(e -> akhiri());
        btnBersih.addActionListener(e -> bersihkan());
        btnCari.addActionListener(e -> muatTabel());
        chkAktif.addActionListener(e -> muatTabel());
        bersihkan();
        refresh();
    }

    private void baris(JPanel p, GridBagConstraints g, int row, String label, Component c) {
        g.gridwidth = 1; g.gridy = row; g.gridx = 0; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1; p.add(c, g);
    }

    @Override public void refresh() {
        try {
            memuat = true;
            cmbKamar.removeAllItems();
            for (Kamar k : kamarDao.getByStatus("KOSONG")) cmbKamar.addItem(k);
            cmbPenghuni.removeAllItems();
            for (Penghuni p : penghuniDao.getAll("")) cmbPenghuni.addItem(p);
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        } finally {
            memuat = false;
        }
        perbaruiEstimasi();
        muatTabel();
    }

    private void perbaruiEstimasi() {
        if (memuat) return;
        Kamar k = (Kamar) cmbKamar.getSelectedItem();
        lblEstimasi.setText(k == null ? "-" : Format.rupiah(k.hitungTotalBulanan()));
    }

    private void mulai() {
        Kamar kamar = (Kamar) cmbKamar.getSelectedItem();
        Penghuni penghuni = (Penghuni) cmbPenghuni.getSelectedItem();
        if (kamar == null) { Ui.peringatan(this, "Tidak ada kamar KOSONG yang dapat dipilih."); return; }
        if (penghuni == null) { Ui.peringatan(this, "Tambahkan data penghuni terlebih dahulu."); return; }
        try {
            LocalDate mulai = Format.tanggal(txtMulai.getText(), "Tanggal mulai");
            LocalDate selesai = txtSelesai.getText().trim().isEmpty() ? null : Format.tanggal(txtSelesai.getText(), "Tanggal selesai");
            if (selesai != null && selesai.isBefore(mulai)) throw new IllegalArgumentException("Tanggal selesai tidak boleh sebelum tanggal mulai.");
            Sewa s = new Sewa(-1, kamar.getIdKamar(), penghuni.getIdPenghuni(), mulai, selesai,
                    rbHarian.isSelected() ? Sewa.HARIAN : Sewa.BULANAN, Sewa.AKTIF);
            sewaDao.mulaiSewa(s);
            Ui.info(this, "Sewa kamar " + kamar.getNomorKamar() + " berhasil dimulai.");
            bersihkan(); refresh();
        } catch (IllegalArgumentException | SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    private void akhiri() {
        int row = tabel.getSelectedRow();
        if (row < 0) { Ui.peringatan(this, "Pilih sewa pada tabel terlebih dahulu."); return; }
        int m = tabel.convertRowIndexToModel(row);
        if (!Sewa.AKTIF.equals(model.getValueAt(m, 6))) { Ui.peringatan(this, "Sewa yang dipilih sudah berakhir."); return; }
        if (!Ui.konfirmasi(this, "Akhiri sewa kamar " + model.getValueAt(m, 1) + "?")) return;
        try {
            sewaDao.akhiriSewa((int) model.getValueAt(m, 0), LocalDate.now());
            Ui.info(this, "Sewa berhasil diakhiri; kamar kembali KOSONG.");
            refresh();
        } catch (SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    private void muatTabel() {
        try {
            List<Sewa> data = sewaDao.getAll(txtCari.getText().trim(), chkAktif.isSelected());
            model.setRowCount(0);
            for (Sewa s : data) {
                model.addRow(new Object[]{s.getIdSewa(), s.getNomorKamar(), s.getNamaPenghuni(), s.getTanggalMulai(),
                        s.getTanggalSelesai() == null ? "-" : s.getTanggalSelesai(), s.getJenisPeriode(), s.getStatusSewa()});
            }
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        }
    }

    private void bersihkan() {
        tabel.clearSelection();
        txtMulai.setText(LocalDate.now().toString());
        txtSelesai.setText("");
        rbBulanan.setSelected(true);
    }
}
