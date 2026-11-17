import view.KamarFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KamarFrame().setVisible(true));
    }
}
