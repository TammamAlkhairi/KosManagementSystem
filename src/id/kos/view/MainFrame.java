package id.kos.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Jendela utama: menu samping dan area konten. */
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
    private final Map<String, MenuButton> menu = new LinkedHashMap<>();

    /** Memasang tema tampilan. Panggil sebelum membuat jendela apa pun. */
    public static void pasangTema() { Ui.pasangTema(); }

    public MainFrame() {
        setTitle("Manajemen Kos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1040, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        konten.setBackground(Ui.LATAR);

        daftar(DASHBOARD, "Dashboard", new DashboardPanel(this::tampilkan));
        daftar(KAMAR, "Data Kamar", new KamarPanel());
        daftar(PENGHUNI, "Data Penghuni", new PenghuniPanel());
        daftar(SEWA, "Transaksi Sewa", new SewaPanel());
        daftar(TAGIHAN, "Tagihan & Pembayaran", new TagihanPanel());
        daftar(LAPORAN, "Laporan Tunggakan", new LaporanPanel());

        JPanel samping = new JPanel();
        samping.setLayout(new BoxLayout(samping, BoxLayout.Y_AXIS));
        samping.setBackground(Ui.SIDEBAR);
        samping.setPreferredSize(new Dimension(210, 0));
        samping.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Ui.GARIS),
                BorderFactory.createEmptyBorder(20, 10, 14, 10)));

        JPanel kepala = new JPanel();
        kepala.setOpaque(false);
        kepala.setLayout(new BoxLayout(kepala, BoxLayout.Y_AXIS));
        kepala.setAlignmentX(Component.LEFT_ALIGNMENT);
        kepala.setBorder(BorderFactory.createEmptyBorder(0, 8, 20, 0));
        JLabel nama = new JLabel("Manajemen Kos");
        nama.setFont(Ui.font(Font.BOLD, 16f));
        nama.setForeground(Ui.TEKS);
        kepala.add(nama);
        samping.add(kepala);
        for (MenuButton b : menu.values()) {
            samping.add(b);
            samping.add(Box.createVerticalStrut(2));
        }
        samping.add(Box.createVerticalGlue());
        MenuButton keluar = new MenuButton("Keluar");
        keluar.addActionListener(e -> {
            if (Ui.konfirmasi(this, "Tutup aplikasi?")) dispose();
        });
        samping.add(keluar);

        add(samping, BorderLayout.WEST);
        add(konten, BorderLayout.CENTER);
        tampilkan(DASHBOARD);
    }

    private void daftar(String kunci, String teks, JPanel panel) {
        konten.add(panel, kunci);
        layar.put(kunci, (Refreshable) panel);
        MenuButton b = new MenuButton(teks);
        b.addActionListener(e -> tampilkan(kunci));
        menu.put(kunci, b);
    }

    /** Menampilkan layar, menandai menu aktif, dan memuat ulang datanya. */
    public void tampilkan(String kunci) {
        if (!layar.containsKey(kunci)) return;
        cards.show(konten, kunci);
        menu.forEach((k, b) -> b.setAktif(k.equals(kunci)));
        layar.get(kunci).refresh();
    }

    /** Tombol menu samping: latar sorot untuk menu aktif dan saat disorot kursor. */
    private static final class MenuButton extends JButton {
        private boolean aktif, hover;

        MenuButton(String teks) {
            super(teks);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(Ui.font(Font.PLAIN, 13f));
            setForeground(Ui.TEKS);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setAktif(boolean a) {
            aktif = a;
            setFont(Ui.font(a ? Font.BOLD : Font.PLAIN, 13f));
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            if (aktif || hover) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(aktif ? Ui.PILIH : Ui.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
