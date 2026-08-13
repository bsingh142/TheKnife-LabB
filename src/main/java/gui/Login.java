package gui;

import com.mycompany.theknife.clientTK;
import javax.swing.*;
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

        //la chiusura della pagina dalla X non viene gestita autonomamente, ma viene gestita dall'WindowAdapter
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Intercettiamo il click sulla "X" in alto a destra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tornaAllaHome();
            }
        });

        if (accediButton != null) {
            accediButton.addActionListener(e -> gestisciLogin());
        }

        if (annullaButton != null) {
            annullaButton.addActionListener(e -> pulisciCampi());
        }

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Raccoglie le credenziali, applica i filtri di validazione e delega l'invio al ClientTK.
     */
    private void gestisciLogin() {
        // 1. Estrazione del testo inserito
        String username = txtUsername.getText().trim();
        String password = new String(txtPw.getPassword()).trim();

        // 2. CONTROLLI FRONT-END (Validazione)

        // Controllo 2.1: Campi vuoti
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Inserire sia lo Username che la Password per accedere.",
                    "Attenzione: Credenziali Mancanti",
                    JOptionPane.WARNING_MESSAGE
            );
            return; // Blocca l'esecuzione
        }

        // Controllo 2.2: Verifica struttura Password (Ottimizzazione di Rete)
        // Se la password digitata non rispetta la nostra Regex, è inutile inviarla al Server
        // perché sappiamo per certo che non corrisponderà a nessuna password valida nel nostro DB.
        if (!isPasswordSicura(password)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le credenziali inserite non sono corrette.\n(Verifica di aver inserito correttamente maiuscole, numeri e caratteri speciali).",
                    "Errore di Accesso",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 3. INVIO AL SERVER TRAMITE IL GESTORE CENTRALIZZATO
        String[] pacchettoLogin = {"LOGIN", username, password};
        String messaggioServer = clientTK.inviaRichiesta(pacchettoLogin);

        // 4. Gestione della risposta
        if (messaggioServer.startsWith("OK")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Accesso eseguito con successo!\nBenvenuto " + username + ".",
                    "Login Completato",
                    JOptionPane.INFORMATION_MESSAGE
            );
            this.dispose(); // Chiude la finestra di login al successo

        } else {
            // Se le credenziali sono davvero errate secondo il Database
            JOptionPane.showMessageDialog(
                    this,
                    messaggioServer,
                    "Errore di Accesso",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Verifica che la password inserita rispetti i criteri strutturali di sicurezza di base.
     *
     * @param password La password in chiaro da controllare.
     * @return true se strutturalmente potrebbe essere corretta, false se è sicuramente errata.
     */
    private boolean isPasswordSicura(String password) {
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(regex);
    }

    /**
     * Chiede conferma all'utente e svuota i campi di testo.
     */
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


    //Chiude la finestra corrente e riapre il menu principale (Home).

    private void tornaAllaHome() {
        new Home().setVisible(true);
        this.dispose();
    }

    /**
     * Metodo main per testare l'interfaccia.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}