package id.kos.view;

import id.kos.collection.KamarManager;
import id.kos.dao.KamarDAO;
import id.kos.model.*;
import id.kos.util.Format;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// Layar Data Kamar: CRUD kamar + foto sesuai tipe kamar (AC , Non-AC , Ensuite).
public class KamarPanel extends JPanel implements Refreshable {
    private final JTextField txtNomor = Ui.kolom(12);
    private final JTextField txtHarga = Ui.kolom(12);
    private final JTextField txtUtility = Ui.kolom(12);
    private final JTextField txtCari = Ui.kolom(14);
    private final JComboBox<String> cmbTipe = new JComboBox<>(new String[]{"AC", "NON_AC", "ENSUITE"});
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"KOSONG", "TERISI"});
    private final DefaultTableModel model = Ui.modelTabel("ID", "Nomor", "Tipe", "Harga", "Utility", "Status", "Total / bulan");
    private final JTable tabel = new JTable(model);
    private final FotoKamarPanel foto = new FotoKamarPanel(320, 180);
    private final JLabel lblTotal = new JLabel("-");
    private final KamarDAO dao = new KamarDAO();
    private final KamarManager manager = new KamarManager();
    private int selectedId = -1;
    private boolean memuatBaris;   // true saat form diisi dari baris tabel.

    public KamarPanel() {
        setLayout(new BorderLayout());
        setBackground(Ui.LATAR);
        add(Ui.judul("Data Kamar", "Kelola kamar AC, Non-AC, dan Ensuite beserta harganya"), BorderLayout.NORTH);

        JPanel formIsi = Ui.formPanel();
        Ui.baris(formIsi, 0, "Nomor kamar", txtNomor);
        Ui.baris(formIsi, 1, "Jenis kamar", cmbTipe);
        Ui.baris(formIsi, 2, "Harga sewa", txtHarga);
        Ui.baris(formIsi, 3, "Biaya utility", txtUtility);
        Ui.baris(formIsi, 4, "Status", cmbStatus);

        // Harga dan utility mengikuti jenis kamar, jadi tidak diketik manual.
        for (JTextField t : new JTextField[]{txtHarga, txtUtility}) {
            t.setEditable(false);
            t.setBackground(Ui.LATAR_KOLOM);
            t.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        JPanel form = Ui.kartu("Form Kamar");
        form.add(formIsi, BorderLayout.CENTER);

        JButton btnSimpan = Ui.tombol("Simpan", Ui.Gaya.PRIMER);
        JButton btnUpdate = Ui.tombol("Update", Ui.Gaya.SUKSES);
        JButton btnHapus = Ui.tombol("Hapus", Ui.Gaya.BAHAYA);
        JButton btnBersih = Ui.tombol("Bersihkan");
        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        actions.add(btnSimpan); actions.add(btnUpdate);
        actions.add(btnHapus); actions.add(btnBersih);
        form.add(actions, BorderLayout.SOUTH);

        JPanel kartuFoto = Ui.kartu("Foto Tipe Kamar");
        kartuFoto.add(foto, BorderLayout.CENTER);
        lblTotal.setFont(Ui.font(Font.BOLD, 13f));
        lblTotal.setForeground(Ui.TEKS);
        kartuFoto.add(lblTotal, BorderLayout.SOUTH);

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(350, 0));
        left.add(form, BorderLayout.NORTH);
        left.add(kartuFoto, BorderLayout.CENTER);
        left.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));
        add(left, BorderLayout.WEST);

        JButton btnCari = Ui.tombol("Cari", Ui.Gaya.PRIMER);
        JButton btnRefresh = Ui.tombol("Tampilkan Semua");
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        search.setOpaque(false);
        search.add(new JLabel("Cari nomor kamar:"));
        search.add(txtCari);
        search.add(btnCari); search.add(btnRefresh);

        JPanel center = Ui.kartu(null);
        center.add(search, BorderLayout.NORTH);
        center.add(Ui.gulir(tabel), BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 20, 20));
        wrap.add(center, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);

        Ui.lebarKolom(tabel, 40, 70, 90, 120, 120, 90, 130);
        tabel.getColumnModel().getColumn(3).setCellRenderer(Ui.rendererAngka(o -> Format.rupiah((BigDecimal) o)));
        tabel.getColumnModel().getColumn(4).setCellRenderer(Ui.rendererAngka(o -> Format.rupiah((BigDecimal) o)));
        tabel.getColumnModel().getColumn(6).setCellRenderer(Ui.rendererAngka(o -> Format.rupiah((BigDecimal) o)));
        Map<String, Color> warnaStatus = new HashMap<>();
        warnaStatus.put("TERISI", Ui.ORANYE);
        warnaStatus.put("KOSONG", Ui.HIJAU);
        tabel.getColumnModel().getColumn(5).setCellRenderer(Ui.rendererStatus(warnaStatus));

        btnSimpan.addActionListener(e -> simpan());
        btnUpdate.addActionListener(e -> update());
        btnHapus.addActionListener(e -> hapus());
        btnCari.addActionListener(e -> muatData(txtCari.getText().trim()));
        txtCari.addActionListener(e -> muatData(txtCari.getText().trim()));
        btnRefresh.addActionListener(e -> { txtCari.setText(""); muatData(""); });
        btnBersih.addActionListener(e -> bersihkan());

        // Foto ikut berganti begitu jenis kamar diganti di form.
        cmbTipe.addActionListener(e -> {
            foto.setTipe((String) cmbTipe.getSelectedItem());
            if (!memuatBaris) isiHargaStandar();
            tampilTotal();
        });

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabel.getSelectedRow() >= 0) {
                int row = tabel.convertRowIndexToModel(tabel.getSelectedRow());
                selectedId = (int) model.getValueAt(row, 0);
                txtNomor.setText(String.valueOf(model.getValueAt(row, 1)));
                memuatBaris = true;
                cmbTipe.setSelectedItem(model.getValueAt(row, 2));
                memuatBaris = false;
                txtHarga.setText(Format.angka((BigDecimal) model.getValueAt(row, 3)));
                txtUtility.setText(Format.angka((BigDecimal) model.getValueAt(row, 4)));
                cmbStatus.setSelectedItem(model.getValueAt(row, 5));
                foto.setTipe(String.valueOf(model.getValueAt(row, 2)));
                lblTotal.setText("Total / bulan: " + Format.rupiah((BigDecimal) model.getValueAt(row, 6)));
            }
        });
        bersihkan();
        muatData("");
    }

    @Override public void refresh() { muatData(txtCari.getText().trim()); }

    // Menampilkan total bulanan hanya kalau form berisi angka yang valid.
    private void tampilTotal() {
        try {
            Kamar k = kamarContoh((String) cmbTipe.getSelectedItem());
            k.setHargaSewa(Format.desimal(txtHarga.getText(), "Harga sewa"));
            k.setBiayaUtility(Format.desimal(txtUtility.getText(), "Biaya utility"));
            lblTotal.setText("Total / bulan: " + Format.rupiah(k.hitungTotalBulanan()));
        } catch (RuntimeException ex) {
            lblTotal.setText(" ");
        }
    }

    private Kamar buatObjek() {
        String nomor = txtNomor.getText().trim();
        if (nomor.isEmpty()) throw new IllegalArgumentException("Nomor kamar wajib diisi.");
        BigDecimal harga = Format.desimal(txtHarga.getText(), "Harga sewa");
        BigDecimal utility = Format.desimal(txtUtility.getText(), "Biaya utility");
        String tipe = (String) cmbTipe.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();
        if ("AC".equals(tipe)) return new KamarAC(selectedId, nomor, harga, utility, status);
        if ("ENSUITE".equals(tipe)) return new KamarEnsuite(selectedId, nomor, harga, utility, status);
        return new KamarNonAC(selectedId, nomor, harga, utility, status);
    }

    private void simpan() {
        try {
            dao.insert(buatObjek());
            Ui.info(this, "Data kamar berhasil disimpan.");
            bersihkan(); muatData("");
        } catch (NumberFormatException ex) {
            Ui.peringatan(this, "Harga dan biaya harus berupa angka.");
        } catch (IllegalArgumentException | SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    private void update() {
        if (selectedId < 0) { Ui.peringatan(this, "Pilih data pada tabel terlebih dahulu."); return; }
        try {
            dao.update(buatObjek());
            Ui.info(this, "Data kamar berhasil diperbarui.");
            bersihkan(); muatData("");
        } catch (NumberFormatException ex) {
            Ui.peringatan(this, "Harga dan biaya harus berupa angka.");
        } catch (IllegalArgumentException | SQLException ex) {
            Ui.peringatan(this, ex.getMessage());
        }
    }

    private void hapus() {
        if (selectedId < 0) { Ui.peringatan(this, "Pilih data pada tabel terlebih dahulu."); return; }
        if (Ui.konfirmasi(this, "Hapus kamar terpilih?")) {
            try {
                dao.delete(selectedId);
                bersihkan(); muatData("");
            } catch (SQLException ex) {
                Ui.peringatan(this, "Gagal menghapus: " + ex.getMessage());
            }
        }
    }

    private void muatData(String keyword) {
        try {
            manager.setData(dao.getAll(keyword));
            model.setRowCount(0);
            for (Kamar k : manager.getDaftarKamar()) {
                model.addRow(new Object[]{k.getIdKamar(), k.getNomorKamar(), k.getTipe(),
                        k.getHargaSewa(), k.getBiayaUtility(), k.getStatus(), k.hitungTotalBulanan()});
            }
        } catch (SQLException ex) {
            Ui.peringatan(this, "Koneksi/database bermasalah: " + ex.getMessage());
        }
    }

    // Mengisi harga sewa dan utility sesuai standar jenis kamar yang dipilih. 
    private void isiHargaStandar() {
        Kamar contoh = kamarContoh((String) cmbTipe.getSelectedItem());
        txtHarga.setText(Format.angka(contoh.getHargaStandar()));
        txtUtility.setText(Format.angka(contoh.getUtilityStandar()));
    }

    private static Kamar kamarContoh(String tipe) {
        BigDecimal nol = BigDecimal.ZERO;
        if ("AC".equals(tipe)) return new KamarAC(0, "", nol, nol, "");
        if ("ENSUITE".equals(tipe)) return new KamarEnsuite(0, "", nol, nol, "");
        return new KamarNonAC(0, "", nol, nol, "");
    }

    private void bersihkan() {
        selectedId = -1; tabel.clearSelection();
        txtNomor.setText("");
        memuatBaris = true;
        cmbTipe.setSelectedIndex(0); cmbStatus.setSelectedIndex(0);
        memuatBaris = false;
        isiHargaStandar();
        foto.setTipe("AC");
        tampilTotal();
    }
}
