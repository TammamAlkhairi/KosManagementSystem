package id.kos.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Pembantu tampilan: warna, tema, tombol, tabel, dan dialog agar kode panel tidak berulang. */
final class Ui {
    // ---- palet warna: diambil dari foto referensi (greige, abu muda, taupe tua, pasir) ----
    //   #938F84 greige   #D7CFCA abu muda   #7E7468 taupe tua   #D5C0AB pasir
    static final Color SIDEBAR   = new Color(0xEBE7E3);
    static final Color SIDEBAR_HOVER = new Color(0xE0D9D3);
    static final Color AKSEN     = new Color(0x7E7468);
    static final Color AKSEN_GELAP = new Color(0x675E53);
    static final Color LATAR     = new Color(0xF8F6F3);
    static final Color KARTU     = Color.WHITE;
    static final Color GARIS     = new Color(0xD7CFCA);
    static final Color TEKS      = new Color(0x2F2B26);
    static final Color TEKS_REDUP = new Color(0x6F675C);
    static final Color HIJAU     = new Color(0x5E7D5B);
    static final Color ORANYE    = new Color(0xA06B2C);
    static final Color MERAH     = new Color(0xA4473A);
    static final Color ZEBRA     = new Color(0xFBF9F7);
    static final Color PILIH     = new Color(0xE6D9CC);
    static final Color LATAR_KOLOM = new Color(0xEFEAE5);   // judul kolom tabel, kolom terkunci
    static final Color LATAR_FOTO  = new Color(0xE9E4DF);   // pengganti foto yang belum ada

    static final String FONT = pilihFont();

    private Ui() { }

    private static String pilihFont() {
        String manrope = id.kos.util.FontAplikasi.siapkan();
        if (manrope != null) return manrope;
        String[] kandidat = {"Segoe UI", "Helvetica Neue", "Noto Sans", "Ubuntu", "SansSerif"};
        java.util.Set<String> ada = new java.util.HashSet<>(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : kandidat) if (ada.contains(f)) return f;
        return "SansSerif";
    }

    static Font font(int gaya, float ukuran) { return new Font(FONT, gaya, 1).deriveFont(ukuran); }

    /** Dipanggil sekali dari Main sebelum jendela dibuat. Memakai tampilan bawaan sistem operasi. */
    static void pasangTema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        Font biasa = font(Font.PLAIN, 13f);
        for (String k : new String[]{"Label", "Button", "TextField", "PasswordField", "TextArea", "ComboBox",
                "Table", "TableHeader", "OptionPane", "CheckBox", "RadioButton", "List", "Spinner"}) {
            UIManager.put(k + ".font", biasa);
        }
        UIManager.put("OptionPane.messageFont", biasa);
        UIManager.put("OptionPane.buttonFont", biasa);
        UIManager.put("Table.showGrid", Boolean.FALSE);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 0));
    }

    // ---- dialog ----
    static void peringatan(Component parent, String pesan) {
        JOptionPane.showMessageDialog(parent, pesan, "Informasi", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component parent, String pesan) {
        JOptionPane.showMessageDialog(parent, pesan);
    }

    static boolean konfirmasi(Component parent, String pesan) {
        return JOptionPane.showConfirmDialog(parent, pesan, "Konfirmasi", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    // ---- tata letak umum ----
    /** Judul halaman + garis bawah tipis. */
    static JComponent judul(String teks) { return judul(teks, null); }

    static JComponent judul(String teks, String subjudul) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 14, 24));
        JLabel l = new JLabel(teks);
        l.setFont(font(Font.BOLD, 20f));
        l.setForeground(TEKS);
        p.add(l, BorderLayout.NORTH);
        if (subjudul != null) {
            JLabel s = new JLabel(subjudul);
            s.setFont(font(Font.PLAIN, 13f));
            s.setForeground(TEKS_REDUP);
            s.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
            p.add(s, BorderLayout.SOUTH);
        }
        return p;
    }

    /** Panel putih bersudut halus dengan judul bagian; isi diletakkan oleh pemanggil. */
    static JPanel kartu(String judulBagian) {
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(KARTU);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 6, 6));
                g2.setColor(GARIS);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 6, 6));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        if (judulBagian != null) {
            JLabel l = new JLabel(judulBagian);
            l.setFont(font(Font.BOLD, 13f));
            l.setForeground(TEKS);
            p.add(l, BorderLayout.NORTH);
        }
        return p;
    }

    static JPanel panelHalaman() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LATAR);
        return p;
    }

    /** Panel form dua kolom (label + input) berbasis GridBagLayout. */
    static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    static void baris(JPanel form, int row, String label, Component isi) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 0, 4, 8); g.anchor = GridBagConstraints.WEST;
        g.gridy = row; g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JLabel l = new JLabel(label);
        l.setForeground(TEKS_REDUP);
        form.add(l, g);
        g = new GridBagConstraints();
        g.insets = new Insets(4, 0, 4, 0); g.anchor = GridBagConstraints.WEST;
        g.gridy = row; g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        form.add(isi, g);
    }

    static Border bingkaiBulat() {
        return BorderFactory.createLineBorder(GARIS);
    }

    // ---- tombol ----
    /** PRIMER = isi warna aksen. Gaya lain berupa tombol berbingkai; SUKSES dan BAHAYA hanya mewarnai teksnya. */
    enum Gaya { PRIMER, SUKSES, BAHAYA, NETRAL }

    static JButton tombol(String teks, Gaya gaya) {
        return new Tombol(teks, gaya);
    }

    static JButton tombol(String teks) { return tombol(teks, Gaya.NETRAL); }

    private static final class Tombol extends JButton {
        private final Gaya gaya;
        private boolean hover;

        Tombol(String teks, Gaya gaya) {
            super(teks);
            this.gaya = gaya;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(font(Font.PLAIN, 13f));
            setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D isi = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
            Color teks;
            if (!isEnabled()) {
                g2.setColor(LATAR_KOLOM);
                g2.fill(isi);
                teks = new Color(0xA8A196);
            } else if (gaya == Gaya.PRIMER) {
                g2.setColor(hover ? AKSEN_GELAP : AKSEN);
                g2.fill(isi);
                teks = Color.WHITE;
            } else {
                g2.setColor(hover ? LATAR_KOLOM : Color.WHITE);
                g2.fill(isi);
                g2.setColor(GARIS.darker());
                g2.draw(isi);
                teks = gaya == Gaya.BAHAYA ? MERAH : gaya == Gaya.SUKSES ? HIJAU : TEKS;
            }
            g2.dispose();
            setForeground(teks);
            super.paintComponent(g);
        }
    }

    // ---- tabel ----
    /** Model tabel yang tidak dapat diedit langsung oleh pengguna. */
    static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    /** Renderer dasar: baris belang (zebra) + teks berwarna sesuai nilai sel (opsional). */
    static class SelRenderer extends DefaultTableCellRenderer {
        private final Map<String, Color> warnaTeks;
        private final int rata;
        private final java.util.function.Function<Object, String> pemformat;

        SelRenderer(Map<String, Color> warnaTeks, int rata, java.util.function.Function<Object, String> pemformat) {
            this.warnaTeks = warnaTeks; this.rata = rata; this.pemformat = pemformat;
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, pemformat == null ? v : pemformat.apply(v), sel, false, r, c);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(rata);
            if (sel) {
                setBackground(PILIH);
                setForeground(TEKS);
            } else {
                setBackground(r % 2 == 0 ? Color.WHITE : ZEBRA);
                Color warna = warnaTeks == null ? null : warnaTeks.get(String.valueOf(v));
                setForeground(warna != null ? warna : TEKS);
            }
            setFont(font(Font.PLAIN, 13f));
            return this;
        }
    }

    static SelRenderer rendererStatus(Map<String, Color> warna) {
        return new SelRenderer(warna, SwingConstants.LEFT, null);
    }

    static SelRenderer rendererAngka(java.util.function.Function<Object, String> pemformat) {
        return new SelRenderer(null, SwingConstants.RIGHT, pemformat);
    }

    /** Meratakan kolom (nomor kolom, mulai 0) ke kanan, termasuk judulnya, untuk nilai rupiah yang sudah berupa teks. */
    static void kolomRupiah(JTable t, int... kolom) {
        for (int c : kolom) t.getColumnModel().getColumn(c).setCellRenderer(rendererAngka(o -> String.valueOf(o)));
    }

    static void gayaTabel(JTable t) {
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(PILIH);
        t.setSelectionForeground(TEKS);
        t.setBackground(Color.WHITE);
        t.setFont(font(Font.PLAIN, 13f));
        t.setDefaultRenderer(Object.class, new SelRenderer(null, SwingConstants.LEFT, null));
        t.setDefaultRenderer(Number.class, new SelRenderer(null, SwingConstants.LEFT, null));
        t.setDefaultRenderer(java.time.LocalDate.class, new SelRenderer(null, SwingConstants.LEFT, null));
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 30));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, false, false, r, c);
                setBackground(LATAR_KOLOM);
                setForeground(TEKS);
                setFont(font(Font.BOLD, 12.5f));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                javax.swing.table.TableColumn kol = tb.getColumnModel().getColumn(c);
                boolean kanan = kol.getCellRenderer() instanceof SelRenderer
                        && ((SelRenderer) kol.getCellRenderer()).rata == SwingConstants.RIGHT;
                setHorizontalAlignment(kanan ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        });
    }

    /** Lebar relatif kolom (dipakai sebagai bobot; tabel tetap mengisi lebar panel). */
    static void lebarKolom(JTable t, int... lebar) {
        for (int i = 0; i < lebar.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    /** Membungkus tabel dalam JScrollPane yang bersih (tanpa bingkai ganda). */
    static JScrollPane gulir(JTable t) {
        gayaTabel(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(GARIS));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    static JTextField kolom(int lebar) {
        JTextField f = new JTextField(lebar);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GARIS), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    static String teks(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
