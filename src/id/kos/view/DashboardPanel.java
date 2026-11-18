package id.kos.view;

import id.kos.dao.KamarDAO;
import id.kos.dao.TagihanDAO;
import id.kos.model.Kamar;
import id.kos.model.Tagihan;
import id.kos.util.Format;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/** Dashboard: kartu ringkasan, status kamar, dan aksi cepat. */
public class DashboardPanel extends JPanel implements Refreshable {
    private final JLabel lblTotal = angka(new Color(0x2F6DB5));
    private final JLabel lblTerisi = angka(new Color(0xC77B12));
    private final JLabel lblKosong = angka(new Color(0x2E8B57));
    private final JLabel lblMenunggak = angka(new Color(0xC0392B));
    private final DefaultTableModel model = Ui.modelTabel("Nomor", "Tipe", "Status", "Total / bulan");
    private final KamarDAO kamarDao = new KamarDAO();
    private final TagihanDAO tagihanDao = new TagihanDAO();

    public DashboardPanel(Consumer<String> navigator) {
        setLayout(new BorderLayout(10, 10));
        add(Ui.judul("Dashboard"), BorderLayout.NORTH);

        JPanel kartu = new JPanel(new GridLayout(1, 4, 12, 0));
        kartu.add(kartu("Total Kamar", lblTotal));
        kartu.add(kartu("Kamar Terisi", lblTerisi));
        kartu.add(kartu("Kamar Kosong", lblKosong));
        kartu.add(kartu("Tagihan Menunggak", lblMenunggak));
        kartu.setPreferredSize(new Dimension(0, 90));

        JPanel statusKamar = new JPanel(new BorderLayout());
        statusKamar.setBorder(BorderFactory.createTitledBorder("Status Kamar"));
        statusKamar.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        // TODO (tahap berikutnya): Pengingat Tagihan - tampilkan tagihan jatuh tempo / terlambat
        JPanel pengingat = new JPanel(new BorderLayout());
        pengingat.setBorder(BorderFactory.createTitledBorder("Pengingat Tagihan"));
        JLabel belum = new JLabel("Belum diimplementasikan.", SwingConstants.CENTER);
        belum.setForeground(Color.GRAY);
        pengingat.add(belum, BorderLayout.CENTER);

        JPanel tengah = new JPanel(new GridLayout(1, 2, 12, 0));
        tengah.add(statusKamar); tengah.add(pengingat);

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        aksi.setBorder(BorderFactory.createTitledBorder("Aksi Cepat"));
        JButton btnSewa = new JButton("+ Sewa Baru");
        JButton btnBayar = new JButton("+ Catat Pembayaran");
        JButton btnKamar = new JButton("Tambah Kamar");
        JButton btnLaporan = new JButton("Lihat Laporan");
        aksi.add(btnSewa); aksi.add(btnBayar); aksi.add(btnKamar); aksi.add(btnLaporan);
        btnSewa.addActionListener(e -> navigator.accept(MainFrame.SEWA));
        btnBayar.addActionListener(e -> navigator.accept(MainFrame.TAGIHAN));
        btnKamar.addActionListener(e -> navigator.accept(MainFrame.KAMAR));
        btnLaporan.addActionListener(e -> navigator.accept(MainFrame.LAPORAN));

        JPanel isi = new JPanel(new BorderLayout(0, 12));
        isi.add(kartu, BorderLayout.NORTH);
        isi.add(tengah, BorderLayout.CENTER);
        isi.add(aksi, BorderLayout.SOUTH);
        isi.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        add(isi, BorderLayout.CENTER);
        refresh();
    }

    private static JLabel angka(Color warna) {
        JLabel l = new JLabel("0");
        l.setFont(new Font("SansSerif", Font.BOLD, 28));
        l.setForeground(warna);
        return l;
    }

    private static JPanel kartu(String judul, JLabel nilai) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBFC6CF)), BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        JLabel t = new JLabel(judul);
        t.setForeground(Color.DARK_GRAY);
        p.add(t, BorderLayout.NORTH);
        p.add(nilai, BorderLayout.CENTER);
        return p;
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
            lblTotal.setText(String.valueOf(kamar.size()));
            lblTerisi.setText(String.valueOf(terisi));
            lblKosong.setText(String.valueOf(kamar.size() - terisi));

            LocalDate hariIni = LocalDate.now();
            int menunggak = 0;
            for (Tagihan t : tagihanDao.getAll()) if (t.isMenunggak(hariIni)) menunggak++;
            lblMenunggak.setText(String.valueOf(menunggak));
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        }
    }
}
