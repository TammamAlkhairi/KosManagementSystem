package id.kos.view;

import id.kos.dao.KamarDAO;
import id.kos.dao.TagihanDAO;
import id.kos.model.Kamar;
import id.kos.model.Tagihan;
import id.kos.util.Format;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
  Laporan Tunggakan: filter tanggal acuan + kamar, tabel tunggakan, total tunggakan,
  serta tombol Cetak, bisa Simpan sebagai PDF, lewat dialog cetak dan Ekspor CSV dibuka di Excel.
 */
public class LaporanPanel extends JPanel implements Refreshable {
    private static final String SEMUA_KAMAR = "Semua kamar";

    private final JTextField txtAcuan = Ui.kolom(10);
    private final JComboBox<Object> cmbKamar = new JComboBox<>();
    private final DefaultTableModel model = Ui.modelTabel("Kamar", "Penghuni", "No. HP", "Periode", "Jatuh tempo",
            "Total", "Dibayar", "Sisa", "Terlambat", "Status");
    private final JTable tabel = new JTable(model);
    private final JLabel lblRingkasan = new JLabel(" ");
    private final JLabel lblTotal = new JLabel("Rp0");
    private final TagihanDAO tagihanDao = new TagihanDAO();
    private final KamarDAO kamarDao = new KamarDAO();
    private final List<Tagihan> data = new ArrayList<>();
    private LocalDate acuanTampil = LocalDate.now();
    private boolean memuat = false;

    public LaporanPanel() {
        setLayout(new BorderLayout());
        setBackground(Ui.LATAR);
        add(Ui.judul("Laporan Tunggakan", "Tagihan belum lunas yang jatuh temponya sudah lewat dari tanggal acuan"),
                BorderLayout.NORTH);

        JButton btnTampil = Ui.tombol("Tampilkan", Ui.Gaya.PRIMER);
        JButton btnHariIni = Ui.tombol("Hari ini");
        JButton btnCetak = Ui.tombol("Cetak / PDF");
        JButton btnCsv = Ui.tombol("Ekspor CSV", Ui.Gaya.SUKSES);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.setOpaque(false);
        filter.add(new JLabel("Tanggal acuan:"));
        filter.add(txtAcuan);
        filter.add(btnHariIni);
        filter.add(Box.createHorizontalStrut(10));
        filter.add(new JLabel("Kamar:"));
        filter.add(cmbKamar);
        filter.add(btnTampil);
        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        aksi.setOpaque(false);
        aksi.add(btnCetak); aksi.add(btnCsv);
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(filter, BorderLayout.WEST);
        bar.add(aksi, BorderLayout.EAST);

        lblRingkasan.setForeground(Ui.TEKS_REDUP);
        lblTotal.setFont(Ui.font(Font.BOLD, 20f));
        lblTotal.setForeground(Ui.MERAH);
        JLabel capTotal = new JLabel("Total tunggakan:");
        capTotal.setFont(Ui.font(Font.BOLD, 13f));
        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kanan.setOpaque(false);
        kanan.add(capTotal); kanan.add(lblTotal);
        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        bawah.add(lblRingkasan, BorderLayout.WEST);
        bawah.add(kanan, BorderLayout.EAST);

        JPanel kartu = Ui.kartu(null);
        JPanel atas = new JPanel(new BorderLayout(0, 10));
        atas.setOpaque(false);
        atas.add(bar, BorderLayout.CENTER);
        kartu.add(atas, BorderLayout.NORTH);
        kartu.add(Ui.gulir(tabel), BorderLayout.CENTER);
        kartu.add(bawah, BorderLayout.SOUTH);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        wrap.add(kartu, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);

        Map<String, Color> warna = new HashMap<>();
        warna.put(Tagihan.MENUNGGAK, Ui.MERAH);
        tabel.getColumnModel().getColumn(9).setCellRenderer(Ui.rendererStatus(warna));
        for (int c : new int[]{5, 6, 7}) tabel.getColumnModel().getColumn(c).setCellRenderer(Ui.rendererAngka(o -> String.valueOf(o)));
        tabel.getColumnModel().getColumn(8).setCellRenderer(Ui.rendererAngka(o -> String.valueOf(o)));

        Ui.lebarKolom(tabel, 60, 130, 120, 190, 100, 100, 100, 100, 90, 110);

        btnTampil.addActionListener(e -> muatLaporan());
        txtAcuan.addActionListener(e -> muatLaporan());
        cmbKamar.addActionListener(e -> { if (!memuat) muatLaporan(); });
        btnHariIni.addActionListener(e -> { txtAcuan.setText(Format.tanggal(LocalDate.now())); muatLaporan(); });
        btnCetak.addActionListener(e -> cetak());
        btnCsv.addActionListener(e -> ekspor());

        txtAcuan.setText(Format.tanggal(LocalDate.now()));
        isiPilihanKamar();
        muatLaporan();
    }

    @Override public void refresh() {
        isiPilihanKamar();
        muatLaporan();
    }

    // Mengisi pilihan kamar.
    private void isiPilihanKamar() {
        Object lama = cmbKamar.getSelectedItem();
        int idLama = lama instanceof Kamar ? ((Kamar) lama).getIdKamar() : -1;
        memuat = true;
        try {
            cmbKamar.removeAllItems();
            cmbKamar.addItem(SEMUA_KAMAR);
            for (Kamar k : kamarDao.getAll("")) {
                cmbKamar.addItem(k);
                if (k.getIdKamar() == idLama) cmbKamar.setSelectedItem(k);
            }
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        } finally {
            memuat = false;
        }
    }

    private void muatLaporan() {
        try {
            LocalDate acuan = Format.tanggal(txtAcuan.getText(), "Tanggal acuan");
            Object pilih = cmbKamar.getSelectedItem();
            int idKamar = pilih instanceof Kamar ? ((Kamar) pilih).getIdKamar() : -1;
            data.clear();
            data.addAll(tagihanDao.getMenunggak(acuan, idKamar));
            acuanTampil = acuan;
            model.setRowCount(0);
            BigDecimal total = BigDecimal.ZERO;
            for (Tagihan t : data) {
                total = total.add(t.hitungSisa());
                model.addRow(new Object[]{t.getNomorKamar(), t.getNamaPenghuni(), Ui.teks(t.getNoHp()).isEmpty() ? "-" : t.getNoHp(),
                        Format.tanggal(t.getPeriodeMulai()) + " s/d " + Format.tanggal(t.getPeriodeAkhir()),
                        Format.tanggal(t.getJatuhTempo()),
                        Format.rupiah(t.getTotalTagihan()), Format.rupiah(t.getTotalBayar()), Format.rupiah(t.hitungSisa()),
                        t.hariTerlambat(acuan) + " hari", t.getStatus(acuan)});
            }
            lblTotal.setText(Format.rupiah(total));
            lblRingkasan.setText(data.size() + " tagihan menunggak per " + Format.tanggal(acuan)
                    + (pilih instanceof Kamar ? " \u2022 kamar " + ((Kamar) pilih).getNomorKamar() : ""));
        } catch (IllegalArgumentException ex) {
            Ui.peringatan(this, ex.getMessage());
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        }
    }

    private BigDecimal totalSisa() {
        BigDecimal total = BigDecimal.ZERO;
        for (Tagihan t : data) total = total.add(t.hitungSisa());
        return total;
    }

    // Mencetak ke PDF.
    private void cetak() {
        if (data.isEmpty()) { Ui.info(this, "Tidak ada tunggakan untuk dicetak."); return; }
        try {
            MessageFormat header = new MessageFormat("Laporan Tunggakan Kos - per " + Format.tanggal(acuanTampil));
            MessageFormat footer = new MessageFormat("Total tunggakan: " + Format.rupiah(totalSisa())
                    + "   |   Halaman {0}");
            tabel.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException | java.awt.HeadlessException ex) {
            Ui.peringatan(this, "Gagal mencetak: " + ex.getMessage());
        }
    }

    // Ekspor ke CSV.
    private void ekspor() {
        if (data.isEmpty()) { Ui.info(this, "Tidak ada tunggakan untuk diekspor."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("laporan_tunggakan_" + acuanTampil + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getParentFile(), f.getName() + ".csv");
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Kamar;Penghuni;No. HP;Periode mulai;Periode akhir;Jatuh tempo;Total;Dibayar;Sisa;Hari terlambat;Status\r\n");
        for (Tagihan t : data) {
            sb.append(csv(t.getNomorKamar())).append(';').append(csv(t.getNamaPenghuni())).append(';')
              .append(csv(Ui.teks(t.getNoHp()))).append(';')
              .append(t.getPeriodeMulai()).append(';').append(t.getPeriodeAkhir()).append(';').append(t.getJatuhTempo()).append(';')
              .append(t.getTotalTagihan().toPlainString()).append(';').append(t.getTotalBayar().toPlainString()).append(';')
              .append(t.hitungSisa().toPlainString()).append(';').append(t.hariTerlambat(acuanTampil)).append(';')
              .append(t.getStatus(acuanTampil)).append("\r\n");
        }
        try {
            Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            Ui.info(this, "Laporan berhasil diekspor ke:\n" + f.getAbsolutePath());
        } catch (IOException ex) {
            Ui.peringatan(this, "Gagal menyimpan file: " + ex.getMessage());
        }
    }

    private static String csv(String s) {
        if (s == null) return "";
        return s.contains(";") || s.contains("\"") || s.contains("\n") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}
