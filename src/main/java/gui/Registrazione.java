package gui;

import com.mycompany.theknife.clientTK;
import modelli.Utente;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 1. Spaziatura perimetrale contenuta per non deformare il layout
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Intercettiamo il click sulla "X" in alto a destra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tornaAllaHome();
            }
        });

        // 2. Styling pulito dei campi di testo senza alterarne le dimensioni della griglia
        CompoundBorder campoTestoStyle = new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        );
        Font fontInput = new Font("SansSerif", Font.PLAIN, 12);

        JTextField[] campiTesto = {txtNome, txtCognome, txtUsername, txtPw, txtBd, txtDomicilioC, txtDomicilioN};
        for (JTextField campo : campiTesto) {
            if (campo != null) {
                campo.setFont(fontInput);
                campo.setBorder(campoTestoStyle);
            }
        }

        if (comboRuolo != null) {
            comboRuolo.setFont(fontInput);
            comboRuolo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (comboRuolo.getItemCount() == 0) {
                comboRuolo.addItem("Cliente");
                comboRuolo.addItem("Ristoratore");
            }
        }

        // 3. Styling dei pulsanti
        Font btnFont = new Font("SansSerif", Font.BOLD, 12);

        if (registratiButton != null) {
            registratiButton.setFont(btnFont);
            registratiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            registratiButton.addActionListener(e -> gestisciRegistrazione());
        }

        if (annullaButton != null) {
            annullaButton.setFont(btnFont);
            annullaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            annullaButton.addActionListener(a -> pulisciCampi());
        }

        // Usiamo pack() per adattare la finestra al layout grafico di IntelliJ
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
        String nome = txtNome.getText().trim();
        String cognome = txtCognome.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPw.getPassword()).trim();
        String dob = txtBd.getText().trim();
        String domicilioC = txtDomicilioC.getText().trim();
        String domicilioN = txtDomicilioN.getText().trim();
        String ruolo = (String) comboRuolo.getSelectedItem();

        if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() ||
                password.isEmpty() || domicilioC.isEmpty() || domicilioN.isEmpty() || ruolo == null) {
            JOptionPane.showMessageDialog(this, "Compilare tutti i campi obbligatori.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isPasswordSicura(password)) {
            JOptionPane.showMessageDialog(this,
                    "La password è troppo debole.\nDeve contenere:\n- Almeno 8 caratteri\n- Almeno una lettera maiuscola\n- Almeno un numero\n- Almeno un carattere speciale",
                    "Sicurezza Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isDataValida(dob)) {
            JOptionPane.showMessageDialog(this,
                    "Il formato della data di nascita non è corretto.\nUsa il formato: gg/mm/aaaa (es. 15/04/1998)",
                    "Data non valida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] richiesta = {"POSIZIONE", domicilioC, domicilioN};
        String rispServer = clientTK.inviaRichiesta(richiesta);
        if (rispServer == null || rispServer.startsWith("ERRORE:")) {
            JOptionPane.showMessageDialog(this, "Attenzione: Il domicilio non è valido", "Domicilio non valido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String[] tmp = rispServer.split("/");
        Double latitudine = Double.valueOf(tmp[0]);
        Double longitudine = Double.valueOf(tmp[1]);

        Utente nuovoUtente = new Utente(nome, cognome, username, password, dob, latitudine, longitudine, ruolo);
        String messaggioServer = clientTK.inviaRichiesta(nuovoUtente);

        if (messaggioServer.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, messaggioServer, "Esito Registrazione", JOptionPane.INFORMATION_MESSAGE);
            svuotaInterfaccia();
            new homePageU(username, rispServer).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, messaggioServer, "Avviso Server", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isPasswordSicura(String password) {
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(regex);
    }

    private boolean isDataValida(String dataStringa) {
        if (dataStringa == null || dataStringa.isEmpty()) {
            return true;
        }

        try {
            DateTimeFormatter traduttore = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate.parse(dataStringa, traduttore);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

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