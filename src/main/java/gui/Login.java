package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.mycompany.theknife.clientTK;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Finestra di Login per l'applicazione TheKnife.
 * Raccoglie le credenziali, esegue la validazione Front-End e comunica con il Server.
 */
public class Login extends JFrame {

    // Contenitore principale
    private JPanel mainPanel;

    // Componenti dell'interfaccia grafica
    private JTextField txtUsername;
    private JPasswordField txtPw;
    private JButton accediButton;
    private JButton annullaButton;

    /**
     * Costruttore della classe Login.
     */
    public Login() {
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file Login.form");
        }

        setContentPane(mainPanel);
        setTitle("Login - TheKnife");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 1. Margini interni puliti
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Intercettiamo il click sulla "X" in alto a destra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tornaAllaHome();
            }
        });

        // 2. Bordo moderno con padding interno per Username e Password
        CompoundBorder campoTestoStyle = new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );
        Font fontInput = new Font("SansSerif", Font.PLAIN, 12);

        if (txtUsername != null) {
            txtUsername.setFont(fontInput);
            txtUsername.setBorder(campoTestoStyle);
        }

        if (txtPw != null) {
            txtPw.setFont(fontInput);
            txtPw.setBorder(campoTestoStyle);
        }

        // 3. Font e cursore dinamico per i pulsanti
        Font btnFont = new Font("SansSerif", Font.BOLD, 12);

        if (accediButton != null) {
            accediButton.setFont(btnFont);
            accediButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            accediButton.addActionListener(e -> gestisciLogin());
        }

        if (annullaButton != null) {
            annullaButton.setFont(btnFont);
            annullaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            annullaButton.addActionListener(e -> pulisciCampi());
        }

        // 4. Adattamento compatto al layout nativo del form
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Raccoglie le credenziali, applica i filtri di validazione e delega l'invio al ClientTK.
     */
    private void gestisciLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPw.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Inserire sia lo Username che la Password per accedere.",
                    "Attenzione: Credenziali Mancanti",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!isPasswordSicura(password)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le credenziali inserite non sono corrette.\n(Verifica di aver inserito correttamente maiuscole, numeri e caratteri speciali).",
                    "Errore di Accesso",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String[] pacchettoLogin = {"LOGIN", username, password};
        String messaggioServer = clientTK.inviaRichiesta(pacchettoLogin);

        if (messaggioServer.startsWith("OK")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Accesso eseguito con successo!\nBenvenuto " + username + ".",
                    "Login Completato",
                    JOptionPane.INFORMATION_MESSAGE
            );
            new homePageU(username, messaggioServer.substring(messaggioServer.indexOf("!") + 1)).setVisible(true);
            this.dispose();

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    messaggioServer,
                    "Errore di Accesso",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /// @param password Stringa contenente la password
    /// @return Boolean, se la password è accettabile
    private boolean isPasswordSicura(String password) {
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(regex);
    }

    /// Mostra un messaggio per confermare il ripristino dei dati inseriti
    private void pulisciCampi() {
        int risposta = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler cancellare i dati inseriti?",
                "Conferma Annullamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (risposta == JOptionPane.YES_OPTION) {
            txtUsername.setText("");
            txtPw.setText("");
        }
    }

    /// Permette di ritornare alla home
    private void tornaAllaHome() {
        new Home().setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(4, 2, new Insets(1, 30, 1, 30), -1, -1));
        mainPanel.setMinimumSize(new Dimension(600, 300));
        mainPanel.setPreferredSize(new Dimension(600, 300));
        final JLabel label1 = new JLabel();
        label1.setText("ACCEDI A THE KNIFE");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Username");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(179, 27), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Password");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(179, 27), null, 0, false));
        annullaButton = new JButton();
        annullaButton.setText("Annulla");
        mainPanel.add(annullaButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        accediButton = new JButton();
        accediButton.setText("Accedi");
        mainPanel.add(accediButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtUsername = new JTextField();
        mainPanel.add(txtUsername, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtPw = new JPasswordField();
        mainPanel.add(txtPw, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}