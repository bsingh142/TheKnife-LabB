package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
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

/// Gestisce la finestra per la registrazione di un nuovo utente
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

    /// Costruttore per la classe Registrazione
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

    /// Mostra un messaggio per confermare il ripristino dei valori base
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

    /// Ripristina tutti i campi ai loro valori base
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

    /// Racoglie i dati inseriti dall'utente, li controlla e procede alla creazione e registrazione
    /// presso il db di un nuovo utente.
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

    /// @param password Stringa contenente la password
    /// @return Boolean contenente se la password rispetta le condizioni di accettazione
    private boolean isPasswordSicura(String password) {
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(regex);
    }

    /// @param dataStringa Stringa contenente la data
    /// @return boolean mostra se la data è in un formato accettabile
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

    /// Permette di ritornare alla schermata home
    private void tornaAllaHome() {
        new Home().setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Registrazione().setVisible(true);
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
        mainPanel.setLayout(new GridLayoutManager(10, 7, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Nome");
        mainPanel.add(label1, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 27), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Cognome");
        mainPanel.add(label2, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 27), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Username");
        mainPanel.add(label3, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 27), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Password");
        mainPanel.add(label4, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 27), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Data di Nascita");
        mainPanel.add(label5, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 27), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Domicilio");
        mainPanel.add(label6, new GridConstraints(6, 1, 2, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 27), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Ruolo");
        mainPanel.add(label7, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 27), null, 0, false));
        txtNome = new JTextField();
        txtNome.setText("");
        mainPanel.add(txtNome, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtBd = new JTextField();
        mainPanel.add(txtBd, new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        registratiButton = new JButton();
        registratiButton.setText("Registrati");
        mainPanel.add(registratiButton, new GridConstraints(9, 4, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        annullaButton = new JButton();
        annullaButton.setText("Annulla");
        mainPanel.add(annullaButton, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboRuolo = new JComboBox();
        mainPanel.add(comboRuolo, new GridConstraints(8, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtPw = new JPasswordField();
        txtPw.setText("");
        mainPanel.add(txtPw, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtCognome = new JTextField();
        mainPanel.add(txtCognome, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtUsername = new JTextField();
        mainPanel.add(txtUsername, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtDomicilioC = new JTextField();
        mainPanel.add(txtDomicilioC, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtDomicilioN = new JTextField();
        mainPanel.add(txtDomicilioN, new GridConstraints(7, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Città");
        mainPanel.add(label8, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Nazione");
        mainPanel.add(label9, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}