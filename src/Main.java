import id.kos.view.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame.pasangTema();
            new MainFrame().setVisible(true);
        });
    }
}
