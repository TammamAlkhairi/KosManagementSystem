package id.kos.view;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Jendela utama: menu samping dan area konten (CardLayout) untuk setiap layar. */
public class MainFrame extends JFrame {
    public static final String DASHBOARD = "dashboard";
    public static final String KAMAR = "kamar";
    public static final String PENGHUNI = "penghuni";
    public static final String SEWA = "sewa";
    public static final String TAGIHAN = "tagihan";
    public static final String LAPORAN = "laporan";

    private final CardLayout cards = new CardLayout();
    private final JPanel konten = new JPanel(cards);
    private final Map<String, Refreshable> layar = new LinkedHashMap<>();
    private final Map<String, JButton> menu = new LinkedHashMap<>();

    public MainFrame() {
        setTitle("Kos Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        daftar(DASHBOARD, "Dashboard", new DashboardPanel(this::tampilkan));
        daftar(KAMAR, "Data Kamar", new KamarPanel());
        daftar(PENGHUNI, "Data Penghuni", new PenghuniPanel());
        daftar(SEWA, "Transaksi Sewa", new SewaPanel());
        daftar(TAGIHAN, "Tagihan & Pembayaran", new TagihanPanel());
        daftar(LAPORAN, "Laporan Tunggakan", new LaporanPanel());

        JPanel samping = new JPanel();
        samping.setLayout(new BoxLayout(samping, BoxLayout.Y_AXIS));
        samping.setBackground(new Color(0x2F4858));
        samping.setPreferredSize(new Dimension(200, 0));
        samping.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        JLabel logo = new JLabel("<html><b>KOS MANAGEMENT<br><font color='#9FC5E8'>SYSTEM</font></b></html>");
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 6, 20, 0));
        samping.add(logo);
        for (JButton b : menu.values()) samping.add(b);
        samping.add(Box.createVerticalGlue());
        JButton keluar = gayaMenu(new JButton("Keluar"));
        keluar.addActionListener(e -> dispose());
        samping.add(keluar);

        add(samping, BorderLayout.WEST);
        add(konten, BorderLayout.CENTER);
        tampilkan(DASHBOARD);
    }

    private void daftar(String kunci, String teks, JPanel panel) {
        konten.add(panel, kunci);
        layar.put(kunci, (Refreshable) panel);
        JButton b = gayaMenu(new JButton(teks));
        b.addActionListener(e -> tampilkan(kunci));
        menu.put(kunci, b);
    }

    private JButton gayaMenu(JButton b) {
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0x2F4858));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        return b;
    }

    /** Menampilkan layar, menyorot menu aktif, dan memuat ulang datanya. */
    public void tampilkan(String kunci) {
        cards.show(konten, kunci);
        menu.forEach((k, b) -> b.setBackground(k.equals(kunci) ? new Color(0x4A90C2) : new Color(0x2F4858)));
        layar.get(kunci).refresh();
    }
}
