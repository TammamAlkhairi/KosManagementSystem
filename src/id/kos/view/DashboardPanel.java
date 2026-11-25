package id.kos.view;

import id.kos.dao.KamarDAO;
import id.kos.dao.TagihanDAO;
import id.kos.model.*;
import id.kos.util.Format;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// dasboard
public class DashboardPanel extends JPanel implements Refreshable {
    private static final int HARI_PENGINGAT = 7;

    private final JLabel[] nilai = {new JLabel("0"), new JLabel("0"), new JLabel("0"), new JLabel("0")};
    private final DefaultTableModel model = Ui.modelTabel("Nomor", "Tipe", "Status", "Total / bulan");
    private final DefaultTableModel modelPengingat = Ui.modelTabel("Kamar / Penghuni", "Jatuh tempo", "Sisa", "Keterangan");
    private final JPanel isiPengingat = new JPanel(new CardLayout());
    private final KartuTipe[] kartuTipe = {
            new KartuTipe("AC", new KamarAC(0, "", BigDecimal.ZERO, BigDecimal.ZERO, "")),
            new KartuTipe("NON_AC", new KamarNonAC(0, "", BigDecimal.ZERO, BigDecimal.ZERO, "")),
            new KartuTipe("ENSUITE", new KamarEnsuite(0, "", BigDecimal.ZERO, BigDecimal.ZERO, ""))
    };
    private final KamarDAO kamarDao = new KamarDAO();
    private final TagihanDAO tagihanDao = new TagihanDAO();

    public DashboardPanel(Consumer<String> navigator) {
        setLayout(new BorderLayout());
        setBackground(Ui.LATAR);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Ui.judul("Dashboard", "Kondisi kos per " + Format.tanggal(LocalDate.now())), BorderLayout.CENTER);
        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        aksi.setOpaque(false);
        aksi.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 24));
        aksi.add(pintasan("Sewa baru", MainFrame.SEWA, Ui.Gaya.PRIMER, navigator));
        aksi.add(pintasan("Catat pembayaran", MainFrame.TAGIHAN, Ui.Gaya.NETRAL, navigator));
        aksi.add(pintasan("Tambah kamar", MainFrame.KAMAR, Ui.Gaya.NETRAL, navigator));
        aksi.add(pintasan("Laporan", MainFrame.LAPORAN, Ui.Gaya.NETRAL, navigator));
        header.add(aksi, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel statistik = new JPanel(new GridLayout(1, 4, 12, 0));
        statistik.setOpaque(false);
        String[] judul = {"Tingkat hunian", "Kamar kosong", "Tagihan menunggak", "Belum terbayar"};
        for (int i = 0; i < 4; i++) statistik.add(kotakAngka(judul[i], nilai[i]));
        statistik.setPreferredSize(new Dimension(0, 76));

        JPanel tipe = new JPanel(new GridLayout(1, 3, 12, 0));
        tipe.setOpaque(false);
        for (KartuTipe k : kartuTipe) tipe.add(k);
        tipe.setPreferredSize(new Dimension(0, 245));

        JPanel atas = new JPanel(new BorderLayout(0, 12));
        atas.setOpaque(false);
        atas.add(statistik, BorderLayout.NORTH);
        atas.add(tipe, BorderLayout.CENTER);

        JTable tabelStatus = new JTable(model);
        JPanel statusKamar = Ui.kartu("Status kamar");
        statusKamar.add(Ui.gulir(tabelStatus), BorderLayout.CENTER);
        Map<String, Color> warnaStatus = new HashMap<>();
        warnaStatus.put("TERISI", Ui.ORANYE);
        warnaStatus.put("KOSONG", Ui.HIJAU);
        tabelStatus.getColumnModel().getColumn(2).setCellRenderer(Ui.rendererStatus(warnaStatus));

        JTable tabelPengingat = new JTable(modelPengingat);
        JLabel kosong = new JLabel("Tidak ada tagihan yang perlu ditagih.", SwingConstants.CENTER);
        kosong.setForeground(Ui.TEKS_REDUP);
        isiPengingat.setOpaque(false);
        isiPengingat.add(Ui.gulir(tabelPengingat), "tabel");
        isiPengingat.add(kosong, "kosong");
        JPanel pengingat = Ui.kartu("Perlu ditagih (terlambat atau jatuh tempo dalam " + HARI_PENGINGAT + " hari)");
        pengingat.add(isiPengingat, BorderLayout.CENTER);
        tabelPengingat.getColumnModel().getColumn(3).setCellRenderer(new Ui.SelRenderer(null, SwingConstants.LEFT, null) {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) comp.setForeground(String.valueOf(v).startsWith("Terlambat") ? Ui.MERAH : Ui.ORANYE);
                return comp;
            }
        });

        JPanel tengah = new JPanel(new BorderLayout(12, 0));
        tengah.setOpaque(false);
        statusKamar.setPreferredSize(new Dimension(390, 0));
        tengah.add(statusKamar, BorderLayout.WEST);
        tengah.add(pengingat, BorderLayout.CENTER);
        Ui.kolomRupiah(tabelStatus, 3);
        Ui.kolomRupiah(tabelPengingat, 2);
        Ui.lebarKolom(tabelStatus, 70, 85, 75, 115);
        Ui.lebarKolom(tabelPengingat, 160, 100, 90, 150);

        JPanel isi = new JPanel(new BorderLayout(0, 12));
        isi.setOpaque(false);
        isi.add(atas, BorderLayout.NORTH);
        isi.add(tengah, BorderLayout.CENTER);
        isi.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));
        add(isi, BorderLayout.CENTER);
        refresh();
    }

    private static JButton pintasan(String teks, String layar, Ui.Gaya gaya, Consumer<String> navigator) {
        JButton b = Ui.tombol(teks, gaya);
        b.addActionListener(e -> navigator.accept(layar));
        return b;
    }

    private static JPanel kotakAngka(String judul, JLabel nilai) {
        JPanel p = Ui.kartu(null);
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel t = new JLabel(judul);
        t.setForeground(Ui.TEKS_REDUP);
        nilai.setFont(Ui.font(Font.BOLD, 24f));
        nilai.setForeground(Ui.TEKS);
        p.add(t, BorderLayout.NORTH);
        p.add(nilai, BorderLayout.CENTER);
        return p;
    }

    // tampilan di dashboard untuk tiap tipe kamar (AC, NON_AC, ENSUITE)
    private static final class KartuTipe extends JPanel {
        private final String tipe;
        private final String namaTipe;
        private final JLabel lblJumlah = new JLabel("-");
        private final JLabel lblHarga = new JLabel(" ");

        KartuTipe(String tipe, Kamar contoh) {
            this.tipe = tipe;
            this.namaTipe = contoh.getNamaTipe();
            setOpaque(false);
            setLayout(new BorderLayout());
            JPanel kartu = Ui.kartu(null);
            kartu.setLayout(new BorderLayout(0, 8));
            kartu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            FotoKamarPanel foto = new FotoKamarPanel(288, 162).tanpaFasilitas();
            foto.setKamar(contoh);
            kartu.add(foto, BorderLayout.CENTER);

            JLabel nama = new JLabel(namaTipe);
            nama.setFont(Ui.font(Font.BOLD, 13.5f));
            nama.setForeground(Ui.TEKS);
            lblJumlah.setFont(Ui.font(Font.PLAIN, 12.5f));
            lblHarga.setFont(Ui.font(Font.PLAIN, 12.5f));
            lblHarga.setForeground(Ui.TEKS_REDUP);
            JPanel teks = new JPanel(new GridLayout(3, 1, 0, 1));
            teks.setOpaque(false);
            teks.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            teks.add(nama); teks.add(lblJumlah); teks.add(lblHarga);
            kartu.add(teks, BorderLayout.SOUTH);
            add(kartu, BorderLayout.CENTER);
        }

        void perbarui(List<Kamar> semua) {
            int total = 0, kosong = 0;
            BigDecimal termurah = null;
            for (Kamar k : semua) {
                if (!tipe.equals(k.getTipe())) continue;
                total++;
                if ("KOSONG".equals(k.getStatus())) kosong++;
                BigDecimal t = k.hitungTotalBulanan();
                if (termurah == null || t.compareTo(termurah) < 0) termurah = t;
            }
            if (total == 0) {
                lblJumlah.setText("Belum ada kamar");
                lblJumlah.setForeground(Ui.TEKS_REDUP);
                lblHarga.setText(" ");
            } else {
                lblJumlah.setText(kosong + " kosong dari " + total);
                lblJumlah.setForeground(kosong > 0 ? Ui.HIJAU : Ui.ORANYE);
                lblHarga.setText("Mulai " + Format.rupiah(termurah) + " per bulan");
            }
        }
    }

    @Override public void refresh() {
        try {
            List<Kamar> kamar = kamarDao.getAll("");
            int terisi = 0;
            model.setRowCount(0);
            for (Kamar k : kamar) {
                if ("TERISI".equals(k.getStatus())) terisi++;
                model.addRow(new Object[]{k.getNomorKamar(), k.getTipe(), k.getStatus(), Format.rupiah(k.hitungTotalBulanan())});
            }
            for (KartuTipe k : kartuTipe) k.perbarui(kamar);

            LocalDate hariIni = LocalDate.now();
            int menunggak = 0;
            BigDecimal belumTerbayar = BigDecimal.ZERO;
            for (Tagihan t : tagihanDao.getAll()) {
                if (t.isMenunggak(hariIni)) menunggak++;
                if (!t.isLunas()) belumTerbayar = belumTerbayar.add(t.hitungSisa());
            }
            
            nilai[0].setText(terisi + " dari " + kamar.size() );
            nilai[0].setFont(Ui.font(Font.BOLD, 20f));
            nilai[1].setText(String.valueOf(kamar.size() - terisi));
            nilai[2].setText(String.valueOf(menunggak));
            nilai[2].setForeground(menunggak > 0 ? Ui.MERAH : Ui.TEKS);
            nilai[3].setText(Format.rupiah(belumTerbayar));
            nilai[3].setFont(Ui.font(Font.BOLD, 20f));

            modelPengingat.setRowCount(0);
            for (Tagihan t : tagihanDao.getPengingat(hariIni, HARI_PENGINGAT)) {
                modelPengingat.addRow(new Object[]{t.getNomorKamar() + " - " + t.getNamaPenghuni(),
                        Format.tanggal(t.getJatuhTempo()), Format.rupiah(t.hitungSisa()), keterangan(t, hariIni)});
            }
            ((CardLayout) isiPengingat.getLayout()).show(isiPengingat, modelPengingat.getRowCount() == 0 ? "kosong" : "tabel");
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        }
    }

    private static String keterangan(Tagihan t, LocalDate hariIni) {
        long terlambat = t.hariTerlambat(hariIni);
        if (terlambat > 0) return "Terlambat " + terlambat + " hari";
        long sisaHari = ChronoUnit.DAYS.between(hariIni, t.getJatuhTempo());
        return sisaHari == 0 ? "Jatuh tempo hari ini" : "Jatuh tempo " + sisaHari + " hari lagi";
    }
}
