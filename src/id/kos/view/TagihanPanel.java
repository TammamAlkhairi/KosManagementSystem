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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Tagihan dan Pembayaran: daftar tagihan, pencatatan pembayaran (termasuk cicilan), dan riwayat.
public class TagihanPanel extends JPanel implements Refreshable {
    // Tanggal jatuh tempo default setiap bulan. 
    private static final int TANGGAL_JATUH_TEMPO = 5;

    private final DefaultTableModel modelTagihan = Ui.modelTabel(
            "ID", "Kamar", "Penghuni", "Periode", "Jatuh tempo", "Total", "Dibayar", "Sisa", "Status");
    private final JTable tabelTagihan = new JTable(modelTagihan);
    private final DefaultTableModel modelRiwayat = Ui.modelTabel("Tanggal", "Jumlah", "Metode");
    private final JTable tabelRiwayat = new JTable(modelRiwayat);
    private final JTextField txtTerpilih = Ui.kolom(18);
    private final JTextField txtTanggal = Ui.kolom(12);
    private final JTextField txtJumlah = Ui.kolom(12);
    private final JComboBox<String> cmbMetode = new JComboBox<>(new String[]{"Tunai", "Transfer", "E-Wallet"});

    private final TagihanDAO tagihanDao = new TagihanDAO();
    private final PembayaranDAO pembayaranDao = new PembayaranDAO();
    private final SewaDAO sewaDao = new SewaDAO();
    private final KamarDAO kamarDao = new KamarDAO();
    private final List<Tagihan> daftar = new ArrayList<>();
    private int selectedId = -1;

    public TagihanPanel() {
        setLayout(new BorderLayout());
        setBackground(Ui.LATAR);
        add(Ui.judul("Tagihan & Pembayaran", "Buat tagihan bulanan/harian, catat pembayaran, dan lihat riwayat cicilan"),
                BorderLayout.NORTH);

        JButton btnBuat = Ui.tombol("+ Buat Tagihan", Ui.Gaya.PRIMER);
        JPanel atas = Ui.kartu("Daftar Tagihan");
        atas.add(Ui.gulir(tabelTagihan), BorderLayout.CENTER);
        JPanel barAtas = new JPanel(new BorderLayout());
        barAtas.setOpaque(false);
        barAtas.add(new JLabel("Pilih tagihan untuk mencatat pembayaran"), BorderLayout.WEST);
        barAtas.add(btnBuat, BorderLayout.EAST);
        atas.add(barAtas, BorderLayout.SOUTH);

        Map<String, Color> warna = new HashMap<>();
        warna.put(Tagihan.LUNAS, Ui.HIJAU);
        warna.put(Tagihan.MENUNGGAK, Ui.MERAH);
        warna.put(Tagihan.BELUM_LUNAS, Ui.ORANYE);
        tabelTagihan.getColumnModel().getColumn(8).setCellRenderer(Ui.rendererStatus(warna));
        Ui.kolomRupiah(tabelRiwayat, 1);
        Ui.lebarKolom(tabelTagihan, 30, 60, 100, 210, 100, 95, 95, 95, 105);
        for (int c : new int[]{5, 6, 7}) tabelTagihan.getColumnModel().getColumn(c).setCellRenderer(Ui.rendererAngka(o -> String.valueOf(o)));

        txtTerpilih.setEditable(false);
        JPanel formIsi = Ui.formPanel();
        Ui.baris(formIsi, 0, "Tagihan terpilih", txtTerpilih);
        Ui.baris(formIsi, 1, "Tanggal bayar", txtTanggal);
        Ui.baris(formIsi, 2, "Jumlah bayar", txtJumlah);
        Ui.baris(formIsi, 3, "Metode", cmbMetode);
        JButton btnSimpan = Ui.tombol("Simpan Pembayaran", Ui.Gaya.SUKSES);
        JPanel form = Ui.kartu("Catat Pembayaran");
        form.add(formIsi, BorderLayout.CENTER);
        form.add(btnSimpan, BorderLayout.SOUTH);

        JPanel riwayat = Ui.kartu("Riwayat Pembayaran (tagihan terpilih)");
        riwayat.add(Ui.gulir(tabelRiwayat), BorderLayout.CENTER);

        JPanel bawah = new JPanel(new GridLayout(1, 2, 12, 0));
        bawah.setOpaque(false);
        bawah.add(form); bawah.add(riwayat);
        bawah.setPreferredSize(new Dimension(0, 235));

        JPanel isi = new JPanel(new BorderLayout(0, 12));
        isi.setOpaque(false);
        isi.add(atas, BorderLayout.CENTER);
        isi.add(bawah, BorderLayout.SOUTH);
        isi.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        add(isi, BorderLayout.CENTER);

        tabelTagihan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilihTagihan();
        });
        btnSimpan.addActionListener(e -> simpanPembayaran());
        btnBuat.addActionListener(e -> buatTagihan());
        txtTanggal.setText(LocalDate.now().toString());
        refresh();
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
                    Format.tanggal(t.getPeriodeMulai()) + " s/d " + Format.tanggal(t.getPeriodeAkhir()),
                    Format.tanggal(t.getJatuhTempo()),
                    Format.rupiah(t.getTotalTagihan()), Format.rupiah(t.getTotalBayar()),
                    Format.rupiah(t.hitungSisa()), t.getStatus(hariIni)});
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
                modelRiwayat.addRow(new Object[]{Format.tanggal(p.getTanggalBayar()), Format.rupiah(p.getJumlahBayar()), p.getMetodePembayaran()});
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

    // Membuat tagihan untuk sewa perbulan atau harian yang statusnya aktif.
     
    private void buatTagihan() {
        try {
            List<Sewa> aktif = sewaDao.getAktif();
            if (aktif.isEmpty()) { Ui.peringatan(this, "Tidak ada sewa AKTIF."); return; }
            Sewa pilihan = (Sewa) JOptionPane.showInputDialog(this, "Pilih sewa untuk dibuatkan tagihan:",
                    "Buat Tagihan", JOptionPane.PLAIN_MESSAGE, null, aktif.toArray(), aktif.get(0));
            if (pilihan == null) return;
            Kamar kamar = kamarDao.getById(pilihan.getIdKamar());
            if (Sewa.HARIAN.equals(pilihan.getJenisPeriode())) {
                buatTagihanHarian(pilihan, kamar);
                return;
            }
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

    private void buatTagihanHarian(Sewa sewa, Kamar kamar) throws SQLException {
        LocalDate awal = sewa.getTanggalMulai();
        LocalDate akhir = sewa.getTanggalSelesai();
        if (akhir == null) {
            String masuk = JOptionPane.showInputDialog(this,
                    "Sewa harian ini belum punya tanggal selesai.\nBerapa hari menginap?", "1");
            if (masuk == null) return;
            try {
                int hari = Integer.parseInt(masuk.trim());
                if (hari < 1) throw new NumberFormatException();
                akhir = awal.plusDays(hari - 1L);
            } catch (NumberFormatException ex) {
                Ui.peringatan(this, "Jumlah hari harus berupa bilangan bulat minimal 1.");
                return;
            }
        }
        long hari = ChronoUnit.DAYS.between(awal, akhir) + 1;
        BigDecimal tarif = kamar.hitungTarifHarian();
        BigDecimal total = tarif.multiply(BigDecimal.valueOf(hari));
        tagihanDao.insert(new Tagihan(-1, sewa.getIdSewa(), awal, akhir, awal, total, BigDecimal.ZERO));
        Ui.info(this, "Tagihan harian kamar " + sewa.getNomorKamar() + " berhasil dibuat:\n"
                + hari + " hari x " + Format.rupiah(tarif) + " = " + Format.rupiah(total));
        refresh();
    }
}
