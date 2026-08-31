package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mycompany.theknife.clientTK;
import modelli.Ristorante;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/// Classe per la gestione della finestra di dialogo che consente l' inserimento dei dati
/// per la registrazione di un nuovo ristorante.
public class AddRistorante extends JFrame {
    private String username;
    private JPanel mainPanel;
    private JButton finalizzaButton;
    private JButton resetButton;
    private JTextField txtNome;
    private JTextField txtindirizzo;
    private JTextField txtcitta;
    private JTextField txtnazione;
    private JTextField fasciaprezzo;
    private JComboBox<Boolean> combodelivery;
    private JComboBox<Boolean> comboprenotazione;
    private JTextField txttipocucina;

    private homePageU homeParent; // Riferimento alla Home

    /// @param u      Stringa con nome utente
    /// @param parent Riferimento alla home
    /// Si occupa di preparare la gui per l'inserimento dei dati di un nuovo ristorante.
    public AddRistorante(String u, homePageU parent) {
        this.username = u;
        this.homeParent = parent;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente");
        }
        setContentPane(mainPanel);
        setTitle("Registrazione nuovo ristorante - TheKnife");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. Spaziatura perimetrale pulita
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // 2. Styling uniforme dei campi di testo
        CompoundBorder campoTestoStyle = new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );
        Font fontInput = new Font("SansSerif", Font.PLAIN, 12);

        JTextField[] campiTesto = {txtNome, txtindirizzo, txtcitta, txtnazione, fasciaprezzo, txttipocucina};
        for (JTextField campo : campiTesto) {
            if (campo != null) {
                campo.setFont(fontInput);
                campo.setBorder(campoTestoStyle);
            }
        }

        if (combodelivery != null) {
            combodelivery.setFont(fontInput);
            combodelivery.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (combodelivery.getItemCount() == 0) {
                combodelivery.addItem(true);
                combodelivery.addItem(false);
            }
        }

        if (comboprenotazione != null) {
            comboprenotazione.setFont(fontInput);
            comboprenotazione.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (comboprenotazione.getItemCount() == 0) {
                comboprenotazione.addItem(true);
                comboprenotazione.addItem(false);
            }
        }

        // 3. Styling dei pulsanti
        Font btnFont = new Font("SansSerif", Font.BOLD, 12);

        if (finalizzaButton != null) {
            finalizzaButton.setFont(btnFont);
            finalizzaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            finalizzaButton.addActionListener(e -> gestisciAggiunta());
        }

        if (resetButton != null) {
            resetButton.setFont(btnFont);
            resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            resetButton.addActionListener(a -> pulisciCampi());
        }

        // Adattamento compatto al layout nativo
        pack();
        setLocationRelativeTo(parent);
    }

    /// Mostra una finestra di dialogo per confermare il reset dei dati inseriti per poi procedere con la cancellazione.
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

    /// Effettua il reset.
    private void svuotaInterfaccia() {
        txtNome.setText("");
        txtindirizzo.setText("");
        txtcitta.setText("");
        txtnazione.setText("");
        fasciaprezzo.setText("");
        txttipocucina.setText("");
        if (combodelivery != null && combodelivery.getItemCount() > 0) {
            combodelivery.setSelectedIndex(0);
        }
        if (comboprenotazione != null && comboprenotazione.getItemCount() > 0) {
            comboprenotazione.setSelectedIndex(0);
        }
    }

    /// Si occupa della gestione dei dati inseriti dall' utente e poi procede a registrare il ristorante nel database.
    private void gestisciAggiunta() {
        String nome = txtNome.getText().trim();
        String indirizzo = txtindirizzo.getText().trim();
        String citta = txtcitta.getText().trim();
        String nazione = txtnazione.getText().trim();
        String prezzo = fasciaprezzo.getText().trim();
        boolean del = (boolean) combodelivery.getSelectedItem();
        boolean prenot = (boolean) comboprenotazione.getSelectedItem();
        String tipocucina = txttipocucina.getText().trim();

        if (nome.isEmpty() || indirizzo.isEmpty() || citta.isEmpty() ||
                nazione.isEmpty() || tipocucina.isEmpty() || prezzo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Compilare tutti i campi.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int prezzoVal = Integer.parseInt(prezzo);

            if (prezzoVal < 1 || prezzoVal > 100) {
                JOptionPane.showMessageDialog(this, "Attenzione: La fascia di prezzo deve essere un numero compreso tra 1 e 100!", "Errore Inserimento", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] richiestaPos = {"POSIZIONE_RISTORANTE", indirizzo, citta, nazione};
            String rispServer = clientTK.inviaRichiesta(richiestaPos);

            if (rispServer == null || rispServer.startsWith("ERRORE:")) {
                JOptionPane.showMessageDialog(this, "Impossibile trovare le coordinate per l'indirizzo inserito. Verifica la correttezza di Via, Città e Nazione.", "Indirizzo non trovato", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] tmp = rispServer.split("/");
            String lat = tmp[0];
            String lon = tmp[1];

            Ristorante nuovoRistorante = new Ristorante(nome, indirizzo, citta, nazione, lat, lon, prezzo, del, prenot, tipocucina, username);
            String messaggioServer = clientTK.inviaRichiesta(nuovoRistorante);

            if (messaggioServer != null && messaggioServer.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, messaggioServer, "Esito Inserimento ristorante", JOptionPane.INFORMATION_MESSAGE);

                if (homeParent != null) {
                    homeParent.aggiornaVistaProprietario();
                }
                this.dispose();

            } else {
                String errore = (messaggioServer != null) ? messaggioServer : "ERRORE: Impossibile contattare il server.";
                JOptionPane.showMessageDialog(this, errore, "Avviso Server", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Errore: Inserisci un valore numerico valido per la Fascia Prezzo.", "Errore Formato", JOptionPane.ERROR_MESSAGE);
        }
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
        mainPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Fascia prezzo");
        mainPanel.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        combodelivery = new JComboBox();
        mainPanel.add(combodelivery, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Delivery");
        mainPanel.add(label2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Prenotazioni");
        mainPanel.add(label3, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboprenotazione = new JComboBox();
        mainPanel.add(comboprenotazione, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Tipo di cucina");
        mainPanel.add(label4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txttipocucina = new JTextField();
        mainPanel.add(txttipocucina, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        fasciaprezzo = new JTextField();
        mainPanel.add(fasciaprezzo, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        txtnazione = new JTextField();
        mainPanel.add(txtnazione, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtcitta = new JTextField();
        mainPanel.add(txtcitta, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtindirizzo = new JTextField();
        mainPanel.add(txtindirizzo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtNome = new JTextField();
        mainPanel.add(txtNome, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Nazione");
        mainPanel.add(label5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Città");
        mainPanel.add(label6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Indirizzo");
        mainPanel.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Nome");
        mainPanel.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        finalizzaButton = new JButton();
        finalizzaButton.setText("Finalizza");
        mainPanel.add(finalizzaButton, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resetButton = new JButton();
        resetButton.setText("Reset");
        mainPanel.add(resetButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}