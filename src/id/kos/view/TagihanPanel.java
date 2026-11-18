package id.kos.view;

import id.kos.dao.KamarDAO;
import id.kos.dao.PembayaranDAO;
import id.kos.dao.SewaDAO;
import id.kos.dao.TagihanDAO;
import id.kos.model.Kamar;
import id.kos.model.Pembayaran;
import id.kos.model.Sewa;
import id.kos.model.Tagihan;
import id.kos.util.Format;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Layar Tagihan dan Pembayaran: daftar tagihan, pencatatan pembayaran (termasuk cicilan), dan riwayat. */
public class TagihanPanel extends JPanel implements Refreshable {
    /** Tanggal jatuh tempo default setiap bulan (nilai simulasi; dapat diubah sesuai aturan kos). */
    private static final int TANGGAL_JATUH_TEMPO = 5;

    private final DefaultTableModel modelTagihan = Ui.modelTabel(
            "ID", "Kamar", "Penghuni", "Periode", "Jatuh tempo", "Total", "Dibayar", "Sisa", "Status");
    private final JTable tabelTagihan = new JTable(modelTagihan);
    private final DefaultTableModel modelRiwayat = Ui.modelTabel("Tanggal", "Jumlah", "Metode");
    private final JTable tabelRiwayat = new JTable(modelRiwayat);
    private final JTextField txtTerpilih = new JTextField(18);
    private final JTextField txtTanggal = new JTextField(12);
    private final JTextField txtJumlah = new JTextField(12);
    private final JComboBox<String> cmbMetode = new JComboBox<>(new String[]{"Tunai", "Transfer", "E-Wallet"});

    private final TagihanDAO tagihanDao = new TagihanDAO();
    private final PembayaranDAO pembayaranDao = new PembayaranDAO();
    private final SewaDAO sewaDao = new SewaDAO();
    private final KamarDAO kamarDao = new KamarDAO();
    private final List<Tagihan> daftar = new ArrayList<>();
    private int selectedId = -1;

    public TagihanPanel() {
        setLayout(new BorderLayout(10, 10));
        add(Ui.judul("Tagihan & Pembayaran"), BorderLayout.NORTH);

        JPanel atas = new JPanel(new BorderLayout());
        atas.setBorder(BorderFactory.createTitledBorder("Daftar Tagihan"));
        atas.add(new JScrollPane(tabelTagihan), BorderLayout.CENTER);
        tabelTagihan.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());

        txtTerpilih.setEditable(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Catat Pembayaran"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        baris(form, g, 0, "Tagihan terpilih", txtTerpilih);
        baris(form, g, 1, "Tanggal bayar", txtTanggal);
        baris(form, g, 2, "Jumlah bayar", txtJumlah);
        baris(form, g, 3, "Metode", cmbMetode);
        JButton btnSimpan = new JButton("Simpan Pembayaran");
        JButton btnBuat = new JButton("Buat Tagihan");
        JPanel tombol = new JPanel(new GridLayout(1, 2, 5, 5));
        tombol.add(btnSimpan); tombol.add(btnBuat);
        g.gridy = 4; g.gridx = 0; g.gridwidth = 2; form.add(tombol, g);

        JPanel riwayat = new JPanel(new BorderLayout());
        riwayat.setBorder(BorderFactory.createTitledBorder("Riwayat Pembayaran (tagihan terpilih)"));
        riwayat.add(new JScrollPane(tabelRiwayat), BorderLayout.CENTER);

        JPanel bawah = new JPanel(new GridLayout(1, 2, 10, 0));
        bawah.add(form); bawah.add(riwayat);
        bawah.setPreferredSize(new Dimension(0, 240));

        JPanel isi = new JPanel(new BorderLayout(10, 10));
        isi.add(atas, BorderLayout.CENTER);
        isi.add(bawah, BorderLayout.SOUTH);
        isi.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(isi, BorderLayout.CENTER);

        tabelTagihan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilihTagihan();
        });
        btnSimpan.addActionListener(e -> simpanPembayaran());
        btnBuat.addActionListener(e -> buatTagihan());
        txtTanggal.setText(LocalDate.now().toString());
        refresh();
    }

    private void baris(JPanel p, GridBagConstraints g, int row, String label, Component c) {
        g.gridwidth = 1; g.gridy = row; g.gridx = 0; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1; p.add(c, g);
    }

    @Override public void refresh() {
        int pilih = selectedId;
        try {
            daftar.clear();
            daftar.addAll(tagihanDao.getAll());
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        }
        LocalDate hariIni = LocalDate.now();
        modelTagihan.setRowCount(0);
        int barisPilih = -1;
        for (int i = 0; i < daftar.size(); i++) {
            Tagihan t = daftar.get(i);
            if (t.getIdTagihan() == pilih) barisPilih = i;
            modelTagihan.addRow(new Object[]{t.getIdTagihan(), t.getNomorKamar(), t.getNamaPenghuni(),
                    t.getPeriodeMulai() + " s/d " + t.getPeriodeAkhir(), t.getJatuhTempo(),
                    Format.angka(t.getTotalTagihan()), Format.angka(t.getTotalBayar()),
                    Format.angka(t.hitungSisa()), t.getStatus(hariIni)});
        }
        if (barisPilih >= 0) tabelTagihan.setRowSelectionInterval(barisPilih, barisPilih);
        else { selectedId = -1; pilihTagihan(); }
    }

    private Tagihan terpilih() {
        int row = tabelTagihan.getSelectedRow();
        return row < 0 ? null : daftar.get(tabelTagihan.convertRowIndexToModel(row));
    }

    private void pilihTagihan() {
        Tagihan t = terpilih();
        modelRiwayat.setRowCount(0);
        if (t == null) { selectedId = -1; txtTerpilih.setText(""); txtJumlah.setText(""); return; }
        selectedId = t.getIdTagihan();
        txtTerpilih.setText("#" + t.getIdTagihan() + " - " + t.getNomorKamar() + " - sisa " + Format.rupiah(t.hitungSisa()));
        txtJumlah.setText(t.hitungSisa().stripTrailingZeros().toPlainString());
        try {
            for (Pembayaran p : pembayaranDao.getByTagihan(t.getIdTagihan())) {
                modelRiwayat.addRow(new Object[]{p.getTanggalBayar(), Format.angka(p.getJumlahBayar()), p.getMetodePembayaran()});
            }
        } catch (SQLException ex) {
            Ui.peringatan(this, "Gagal memuat riwayat: " + ex.getMessage());
        }
    }

    private void simpanPembayaran() {
        Tagihan t = terpilih();
        if (t == null) { Ui.peringatan(this, "Pilih tagihan pada tabel terlebih dahulu."); return; }
        try {
            LocalDate tgl = Format.tanggal(txtTanggal.getText(), "Tanggal bayar");
            BigDecimal jumlah = Format.desimal(txtJumlah.getText(), "Jumlah bayar");
            pembayaranDao.simpan(new Pembayaran(-1, t.getIdTagihan(), tgl, jumlah, (String) cmbMetode.getSelectedItem()));
            Ui.info(this, "Pembayaran berhasil dicatat.");
            refresh();
        } catch (IllegalArgumentException | SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    /** Membuat tagihan bulan berjalan untuk sewa AKTIF; totalnya dihitung dari Kamar.hitungTotalBulanan(). */
    private void buatTagihan() {
        try {
            List<Sewa> aktif = sewaDao.getAktif();
            if (aktif.isEmpty()) { Ui.peringatan(this, "Tidak ada sewa AKTIF."); return; }
            Sewa pilihan = (Sewa) JOptionPane.showInputDialog(this, "Pilih sewa untuk dibuatkan tagihan bulan ini:",
                    "Buat Tagihan", JOptionPane.PLAIN_MESSAGE, null, aktif.toArray(), aktif.get(0));
            if (pilihan == null) return;
            if (Sewa.HARIAN.equals(pilihan.getJenisPeriode())) {
                // TODO: tagihan untuk sewa HARIAN (tarif per hari) belum didukung
                Ui.peringatan(this, "Tagihan untuk sewa HARIAN belum didukung.");
                return;
            }
            Kamar kamar = kamarDao.getById(pilihan.getIdKamar());
            LocalDate awal = LocalDate.now().withDayOfMonth(1);
            LocalDate akhir = awal.withDayOfMonth(awal.lengthOfMonth());
            LocalDate tempo = awal.withDayOfMonth(TANGGAL_JATUH_TEMPO);
            tagihanDao.insert(new Tagihan(-1, pilihan.getIdSewa(), awal, akhir, tempo,
                    kamar.hitungTotalBulanan(), BigDecimal.ZERO));
            Ui.info(this, "Tagihan " + awal.getMonth() + " " + awal.getYear() + " untuk kamar "
                    + pilihan.getNomorKamar() + " berhasil dibuat: " + Format.rupiah(kamar.hitungTotalBulanan()));
            refresh();
        } catch (SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    /** Memberi warna pada kolom status. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            if (!sel) {
                String s = String.valueOf(v);
                comp.setForeground(Tagihan.LUNAS.equals(s) ? new Color(0x2E8B57)
                        : Tagihan.MENUNGGAK.equals(s) ? new Color(0xC0392B) : Color.BLACK);
            }
            return comp;
        }
    }
}
