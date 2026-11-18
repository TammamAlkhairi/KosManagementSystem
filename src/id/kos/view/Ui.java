package id.kos.view;

import java.awt.Component;
import java.awt.Font;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Pembantu kecil agar kode panel tidak berulang. */
final class Ui {
    private Ui() { }

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

    static JLabel judul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        l.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        return l;
    }

    /** Model tabel yang tidak dapat diedit langsung oleh pengguna. */
    static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    static String teks(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
