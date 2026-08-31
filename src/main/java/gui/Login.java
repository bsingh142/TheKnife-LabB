package gui;

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
}