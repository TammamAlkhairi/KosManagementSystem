package id.kos.view;

import javax.swing.*;
import java.awt.*;

/**
 * Layar Laporan Tunggakan - BELUM DIIMPLEMENTASIKAN (kerangka saja).
 *
 * TODO tahap berikutnya:
 *  1. Filter: tanggal acuan (yyyy-MM-dd) dan pilihan kamar.
 *  2. Muat data dari TagihanDAO.getMenunggak(tanggalAcuan) lalu isi JTable
 *     (kamar, penghuni, no. HP, periode, jatuh tempo, sisa, hari terlambat, status).
 *  3. Hitung dan tampilkan total tunggakan di bagian bawah.
 *  4. Tombol "Ekspor PDF" dan "Cetak".
 */
public class LaporanPanel extends JPanel implements Refreshable {
    public LaporanPanel() {
        setLayout(new BorderLayout());
        add(Ui.judul("Laporan Tunggakan"), BorderLayout.NORTH);
        JLabel info = new JLabel("<html><center>Fitur ini belum diimplementasikan.<br>"
                + "Lihat catatan TODO pada LaporanPanel dan TagihanDAO.getMenunggak().</center></html>", SwingConstants.CENTER);
        info.setForeground(Color.GRAY);
        add(info, BorderLayout.CENTER);
    }

    @Override public void refresh() { /* TODO: muat ulang data laporan */ }
}
