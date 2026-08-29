package gui;

import com.mycompany.theknife.clientTK;
import modelli.Utente;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Finestra di registrazione per l'applicazione TheKnife.
 * Raccoglie i dati, esegue controlli di validazione (password forte, formato data)
 * e delega l'invio al ClientTK.
 */
public class Registrazione extends JFrame {

    private JPanel mainPanel;
    private JButton registratiButton;
    private JTextField txtNome;
    private JTextField txtCognome;
    private JTextField txtUsername;
    private JPasswordField txtPw;
    private JTextField txtBd;
    private JTextField txtDomicilioC;
    private JComboBox<String> comboRuolo;
    private JButton annullaButton;
    private JTextField txtDomicilioN;

    public Registrazione() {
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente");
        }

        setContentPane(mainPanel);
        setTitle("Registrazione - TheKnife");
        //la chiusura della pagina dalla X non viene gestita autonomamente, ma viene gestita dall'WindowAdapter
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Intercettiamo il click sulla "X" in alto a destra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tornaAllaHome();
            }
        });

        if (comboRuolo != null && comboRuolo.getItemCount() == 0) {
            comboRuolo.addItem("Cliente");
            comboRuolo.addItem("Ristoratore");
        }

        registratiButton.addActionListener(e -> gestisciRegistrazione());
        annullaButton.addActionListener(a -> pulisciCampi());

        pack();
        setLocationRelativeTo(null);
    }

    private void pulisciCampi() {
        int risposta = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler cancellare tutti i dati inseriti?",
                "Conferma Annullamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (risposta == JOptionPane.YES_OPTION) {
            svuotaInterfaccia();
        }
    }

    /**
     * Metodo di supporto per ripulire le caselle di testo.
     */
    private void svuotaInterfaccia() {
        txtNome.setText("");
        txtCognome.setText("");
        txtUsername.setText("");
        txtPw.setText("");
        txtBd.setText("");
        txtDomicilioC.setText("");
        txtDomicilioN.setText("");
        if (comboRuolo != null && comboRuolo.getItemCount() > 0) {
            comboRuolo.setSelectedIndex(0);
        }
    }

    private void gestisciRegistrazione() {
        // 1. Estrazione dei valori
        String nome = txtNome.getText().trim();
        String cognome = txtCognome.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPw.getPassword()).trim();
        String dob = txtBd.getText().trim();
        String domicilioC = txtDomicilioC.getText().trim();
        String domicilioN = txtDomicilioN.getText().trim();
        String ruolo = (String) comboRuolo.getSelectedItem();

        // 2. CONTROLLI FRONT-END (Validazione)

        // Controllo 2.1: Campi obbligatori
        if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() ||
                password.isEmpty() || domicilioC.isEmpty() || domicilioN.isEmpty() || ruolo == null) {
            JOptionPane.showMessageDialog(this, "Compilare tutti i campi obbligatori.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return; // Blocca l'esecuzione
        }

        // Controllo 2.2: Sicurezza della Password
        if (!isPasswordSicura(password)) {
            JOptionPane.showMessageDialog(this,
                    "La password è troppo debole.\nDeve contenere:\n- Almeno 8 caratteri\n- Almeno una lettera maiuscola\n- Almeno un numero\n- Almeno un carattere speciale",
                    "Sicurezza Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Controllo 2.3: Formato Data di Nascita (se inserita)
        if (!isDataValida(dob)) {
            JOptionPane.showMessageDialog(this,
                    "Il formato della data di nascita non è corretto.\nUsa il formato: gg/mm/aaaa (es. 15/04/1998)",
                    "Data non valida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        //3. Controllo del domicilio inserito dall'utente
        String[] richiesta = {"POSIZIONE",domicilioC,domicilioN};
        String rispServer = clientTK.inviaRichiesta(richiesta);
        if(rispServer == null || rispServer.startsWith("ERRORE:")){
            JOptionPane.showMessageDialog(this,"Attenzione: Il domicilio non è valido","Domicilio non valido",JOptionPane.WARNING_MESSAGE);
            return;
        }
        String[] tmp=rispServer.split("/");
        Double latitudine= Double.valueOf(tmp[0]);
        Double longitudine=Double.valueOf(tmp[1]);
        // 4. Creazione del pacchetto Utente
        Utente nuovoUtente = new Utente(nome, cognome, username, password, dob, latitudine,longitudine, ruolo);

        // 5. INVIO AL SERVER TRAMITE IL GESTORE CENTRALIZZATO
        String messaggioServer = clientTK.inviaRichiesta(nuovoUtente);

        // 6. Gestione della risposta
        if (messaggioServer.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, messaggioServer, "Esito Registrazione", JOptionPane.INFORMATION_MESSAGE);
            svuotaInterfaccia(); // Pulisce i campi in automatico
            new homePageU(username,rispServer).setVisible(true);
            this.dispose();
        } else {
            // Mostra l'errore (ad esempio se lo username esiste già, errore generato dal database!)
            JOptionPane.showMessageDialog(this, messaggioServer, "Avviso Server", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verifica che la password rispetti i criteri moderni di sicurezza.
     * @param password La password in chiaro da controllare.
     * @return true se la password è sicura, false altrimenti.
     */
    private boolean isPasswordSicura(String password) {
        // La Regex controlla:
        // (?=.*[0-9])       -> Almeno un numero
        // (?=.*[A-Z])       -> Almeno una lettera maiuscola
        // (?=.*[!@#$%^&*])  -> Almeno un carattere speciale (puoi aggiungerne altri tra le parentesi quadre)
        // .{8,}             -> Lunghezza minima di 8 caratteri
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(regex);
    }

    /**
     * Verifica che la data (se presente) sia nel formato corretto gg/mm/aaaa.
     * @param dataStringa Il testo inserito dall'utente.
     * @return true se è formattata bene o è vuota, false se è sbagliata.
     */
    private boolean isDataValida(String dataStringa) {
        // Se l'utente non ha scritto nulla, consideriamo la validazione superata
        // (poiché il campo è facoltativo nel database)
        if (dataStringa == null || dataStringa.isEmpty()) {
            return true;
        }

        try {
            // Proviamo a convertire il testo in una data reale
            DateTimeFormatter traduttore = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate.parse(dataStringa, traduttore);
            return true;
        } catch (DateTimeParseException e) {
            // Se la conversione fallisce, il formato è errato
            return false;
        }
    }


    //Chiude la finestra corrente e riapre il menu principale (Home)
    private void tornaAllaHome() {
        new Home().setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Registrazione().setVisible(true);
        });
    }
}