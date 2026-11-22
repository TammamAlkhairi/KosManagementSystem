package id.kos.view;

import id.kos.model.*;
import id.kos.util.GambarKamar;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Map;
import javax.swing.*;

/**
 * Foto tipe kamar (AC / Non-AC / Ensuite), dipotong mengisi bingkai. Bila fasilitas ditampilkan,
 * nama tipe dan daftar fasilitas ditulis di bawah foto, bukan di atasnya.
 */
class FotoKamarPanel extends JPanel {
    private static final int TINGGI_KETERANGAN = 46;

    private Kamar kamar;
    private boolean tampilFasilitas = true;

    FotoKamarPanel(int lebar, int tinggi) {
        setOpaque(false);
        setPreferredSize(new Dimension(lebar, tinggi));
        setMinimumSize(new Dimension(80, 60));
    }

    FotoKamarPanel tanpaFasilitas() { tampilFasilitas = false; return this; }

    void setKamar(Kamar k) { this.kamar = k; repaint(); }

    /** Pratinjau berdasarkan tipe saja (mis. saat memilih jenis kamar di form sebelum disimpan). */
    void setTipe(String tipe) {
        BigDecimal nol = BigDecimal.ZERO;
        if ("AC".equals(tipe)) kamar = new KamarAC(0, "", nol, nol, "");
        else if ("ENSUITE".equals(tipe)) kamar = new KamarEnsuite(0, "", nol, nol, "");
        else if ("NON_AC".equals(tipe)) kamar = new KamarNonAC(0, "", nol, nol, "");
        else kamar = null;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Teks digambar dengan pengaturan yang sama seperti komponen Swing lain (pengaturan desktop sistem),
        // ditambah fractional metrics supaya jarak antarhuruf tidak dibulatkan ke piksel penuh dan terlihat renggang.
        Map<?, ?> hintDesktop = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (hintDesktop != null) g2.addRenderingHints(hintDesktop);
        else g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        int w = getWidth(), h = getHeight();
        boolean denganKet = tampilFasilitas && kamar != null;
        // Bila ada keterangan, foto dijaga rasio 16:9 (sisa tinggi dibiarkan kosong); tanpa keterangan foto mengisi bingkai.
        int hf = denganKet ? Math.min(h - TINGGI_KETERANGAN, Math.round(w * 9f / 16f)) : h;
        if (hf < 1) hf = 1;

        Shape area = new RoundRectangle2D.Float(0, 0, w, hf, 6, 6);
        Shape lama = g2.getClip();
        g2.clip(area);
        BufferedImage img = GambarKamar.untuk(kamar, w, hf);
        if (img != null) {
            double skala = Math.max((double) w / img.getWidth(), (double) hf / img.getHeight());
            int iw = (int) Math.ceil(img.getWidth() * skala), ih = (int) Math.ceil(img.getHeight() * skala);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, (w - iw) / 2, (hf - ih) / 2, iw, ih, null);
        } else {
            g2.setColor(Ui.LATAR_FOTO);
            g2.fillRect(0, 0, w, hf);
            g2.setColor(Ui.TEKS_REDUP);
            g2.setFont(Ui.font(Font.PLAIN, 12.5f));
            String t = kamar == null ? "Pilih kamar untuk melihat foto" : "Foto belum ditemukan: images/" + kamar.getNamaGambar();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, Math.max(6, (w - fm.stringWidth(t)) / 2), hf / 2);
        }
        g2.setClip(lama);
        g2.setColor(new Color(0, 0, 0, 35));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, hf - 1, 6, 6));

        if (denganKet) {
            g2.setColor(Ui.TEKS);
            g2.setFont(Ui.font(Font.BOLD, 13.5f));
            g2.drawString(kamar.getNamaTipe(), 2, hf + 19);
            g2.setFont(Ui.font(Font.PLAIN, 12f));
            g2.setColor(Ui.TEKS_REDUP);
            String f = String.join(", ", kamar.getFasilitas());
            FontMetrics fm = g2.getFontMetrics();
            while (fm.stringWidth(f) > w - 4 && f.lastIndexOf(", ") > 0) f = f.substring(0, f.lastIndexOf(", "));
            g2.drawString(f, 2, hf + 37);
        }
        g2.dispose();
    }
}
