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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Layar Transaksi Sewa: memilih kamar KOSONG (dengan foto) dan penghuni, lalu memulai / mengakhiri sewa. */
public class SewaPanel extends JPanel implements Refreshable {
    private final JComboBox<Kamar> cmbKamar = new JComboBox<>();
    private final JComboBox<Penghuni> cmbPenghuni = new JComboBox<>();
    private final JTextField txtMulai = Ui.kolom(12);
    private final JTextField txtSelesai = Ui.kolom(12);
    private final JRadioButton rbBulanan = new JRadioButton("BULANAN", true);
    private final JRadioButton rbHarian = new JRadioButton("HARIAN");
    private final JLabel lblEstimasi = new JLabel("-");
    private final JLabel lblKeterangan = new JLabel(" ");
    private final FotoKamarPanel foto = new FotoKamarPanel(320, 180);
    private final JTextField txtCari = Ui.kolom(12);
    private final JCheckBox chkAktif = new JCheckBox("Hanya yang AKTIF");
    private final DefaultTableModel model = Ui.modelTabel("ID", "Kamar", "Penghuni", "Mulai", "Selesai", "Periode", "Status");
    private final JTable tabel = new JTable(model);

    private final KamarDAO kamarDao = new KamarDAO();
    private final PenghuniDAO penghuniDao = new PenghuniDAO();
    private final SewaDAO sewaDao = new SewaDAO();
    private boolean memuat = false;

    public SewaPanel() {
        setLayout(new BorderLayout());
        setBackground(Ui.LATAR);
        add(Ui.judul("Transaksi Sewa (Reservasi)", "Pilih kamar kosong, penghuni, dan periode sewa"), BorderLayout.NORTH);

        ButtonGroup grup = new ButtonGroup();
        grup.add(rbBulanan); grup.add(rbHarian);
        rbBulanan.setOpaque(false); rbHarian.setOpaque(false);
        JPanel periode = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        periode.setOpaque(false);
        periode.add(rbBulanan); periode.add(rbHarian);

        JPanel formIsi = Ui.formPanel();
        Ui.baris(formIsi, 0, "Kamar (KOSONG)", cmbKamar);
        Ui.baris(formIsi, 1, "Penghuni", cmbPenghuni);
        Ui.baris(formIsi, 2, "Tanggal mulai", txtMulai);
        Ui.baris(formIsi, 3, "Tanggal selesai", txtSelesai);
        Ui.baris(formIsi, 4, "Jenis periode", periode);

        JButton btnMulai = Ui.tombol("Mulai Sewa", Ui.Gaya.PRIMER);
        JButton btnAkhiri = Ui.tombol("Akhiri Sewa", Ui.Gaya.BAHAYA);
        JButton btnBersih = Ui.tombol("Bersihkan");
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        actions.add(btnMulai); actions.add(btnAkhiri); actions.add(btnBersih);
        JPanel form = Ui.kartu("Form Sewa");
        form.add(formIsi, BorderLayout.CENTER);
        form.add(actions, BorderLayout.SOUTH);

        lblEstimasi.setFont(Ui.font(Font.BOLD, 17f));
        lblEstimasi.setForeground(Ui.AKSEN_GELAP);
        lblKeterangan.setFont(Ui.font(Font.PLAIN, 11.5f));
        lblKeterangan.setForeground(Ui.TEKS_REDUP);
        JPanel est = new JPanel(new GridLayout(2, 1));
        est.setOpaque(false);
        est.add(lblEstimasi); est.add(lblKeterangan);
        JPanel kartuFoto = Ui.kartu("Kamar Dipilih");
        kartuFoto.add(foto, BorderLayout.CENTER);
        kartuFoto.add(est, BorderLayout.SOUTH);

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(400, 0));
        left.add(form, BorderLayout.NORTH);
        left.add(kartuFoto, BorderLayout.CENTER);
        left.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));
        add(left, BorderLayout.WEST);

        JButton btnCari = Ui.tombol("Cari", Ui.Gaya.PRIMER);
        chkAktif.setOpaque(false);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        search.setOpaque(false);
        search.add(new JLabel("Cari:")); search.add(txtCari);
        search.add(btnCari); search.add(chkAktif);
        JPanel center = Ui.kartu(null);
        center.add(search, BorderLayout.NORTH);
        center.add(Ui.gulir(tabel), BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 20, 20));
        wrap.add(center, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);

        Map<String, Color> warna = new HashMap<>();
        warna.put(Sewa.AKTIF, Ui.HIJAU);
        warna.put(Sewa.SELESAI, Ui.TEKS_REDUP);
        tabel.getColumnModel().getColumn(6).setCellRenderer(Ui.rendererStatus(warna));
        Ui.lebarKolom(tabel, 40, 70, 140, 100, 100, 100, 90);

        cmbKamar.addActionListener(e -> perbaruiEstimasi());
        rbBulanan.addActionListener(e -> perbaruiEstimasi());
        rbHarian.addActionListener(e -> perbaruiEstimasi());
        btnMulai.addActionListener(e -> mulai());
        btnAkhiri.addActionListener(e -> akhiri());
        btnBersih.addActionListener(e -> bersihkan());
        btnCari.addActionListener(e -> muatTabel());
        txtCari.addActionListener(e -> muatTabel());
        chkAktif.addActionListener(e -> muatTabel());
        bersihkan();
        refresh();
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

    /** Memperbarui foto + estimasi biaya (per bulan atau per hari sesuai jenis periode). */
    private void perbaruiEstimasi() {
        if (memuat) return;
        Kamar k = (Kamar) cmbKamar.getSelectedItem();
        foto.setKamar(k);
        if (k == null) {
            lblEstimasi.setText("-");
            lblKeterangan.setText("Tidak ada kamar KOSONG");
        } else if (rbHarian.isSelected()) {
            lblEstimasi.setText(Format.rupiah(k.hitungTarifHarian()) + " / hari");
            lblKeterangan.setText("total bulanan \u00F7 " + Kamar.HARI_PER_BULAN + ", dibulatkan ke atas ke Rp1.000");
        } else {
            lblEstimasi.setText(Format.rupiah(k.hitungTotalBulanan()) + " / bulan");
            lblKeterangan.setText("sewa + utility + biaya khusus tipe kamar");
        }
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
                model.addRow(new Object[]{s.getIdSewa(), s.getNomorKamar(), s.getNamaPenghuni(), Format.tanggal(s.getTanggalMulai()),
                        Format.tanggal(s.getTanggalSelesai()), s.getJenisPeriode(), s.getStatusSewa()});
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
        perbaruiEstimasi();
    }
}
