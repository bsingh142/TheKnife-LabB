package gui;

import com.mycompany.theknife.clientTK;
import modelli.Ristorante;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddRistorante extends JFrame {
    private String username;
    private JPanel mainPanel;
    private JButton finalizzaButton;
    private JButton resetButton;
    private JTextField txtNome;
    private JTextField txtindirizzo;
    private JTextField txtcitta;
    private JTextField txtnazione;
    private JTextField latitudine;
    private JTextField longitudine;
    private JTextField fasciaprezzo;
    private JComboBox<Boolean> combodelivery;
    private JComboBox<Boolean> comboprenotazione;
    private JTextField txttipocucina;

    private homePageU homeParent; // Riferimento alla Home

    public AddRistorante(String u, homePageU parent) {
        this.username = u;
        this.homeParent = parent;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente");
        }
        setContentPane(mainPanel);
        setTitle("Registrazione nuovo ristorante - TheKnife");

        // Alla chiusura della X, eliminiamo semplicemente questa finestra
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if (combodelivery != null && combodelivery.getItemCount() == 0) {
            combodelivery.addItem(true);
            combodelivery.addItem(false);
        }
        if (comboprenotazione!= null && comboprenotazione.getItemCount() == 0) {
            comboprenotazione.addItem(true);
            comboprenotazione.addItem(false);
        }

        finalizzaButton.addActionListener(e -> gestisciAggiunta());
        resetButton.addActionListener(a -> pulisciCampi());

        pack();
        setLocationRelativeTo(parent);
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
        txtindirizzo.setText("");
        txtcitta.setText("");
        txtnazione.setText("");
        latitudine.setText("");
        longitudine.setText("");
        fasciaprezzo.setText("");
        if (combodelivery != null && combodelivery.getItemCount() > 0) {
            combodelivery.setSelectedIndex(0);
        }
        if (comboprenotazione != null && comboprenotazione.getItemCount() > 0) {
            comboprenotazione.setSelectedIndex(0);
        }
    }

    private void gestisciAggiunta() {
        String nome = txtNome.getText().trim();
        String indirizzo = txtindirizzo.getText().trim();
        String citta = txtcitta.getText().trim();
        String nazione = txtnazione.getText().trim();

        String lat = latitudine.getText().trim().replace(",", ".");
        String lon = longitudine.getText().trim().replace(",", ".");

        String prezzo = fasciaprezzo.getText().trim();
        boolean del = (boolean) combodelivery.getSelectedItem();
        boolean prenot = (boolean)  comboprenotazione.getSelectedItem();
        String tipocucina = txttipocucina.getText().trim();

        if (nome.isEmpty() || indirizzo.isEmpty() || citta.isEmpty() ||
                nazione.isEmpty() || tipocucina.isEmpty() || lat.isEmpty() || lon.isEmpty() || prezzo.isEmpty()){
            JOptionPane.showMessageDialog(this, "Compilare tutti i campi.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(prezzo);
            double latVal = Double.parseDouble(lat);
            double lonVal = Double.parseDouble(lon);

            if (latVal < -90 || latVal > 90 || lonVal < -180 || lonVal > 180) {
                JOptionPane.showMessageDialog(this, "Coordinate non valide!\nLa latitudine deve essere tra -90 e 90.\nLa longitudine tra -180 e 180.", "Errore Coordinate", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Ristorante nuovoRistorante = new Ristorante(nome, indirizzo, citta, nazione, lat, lon, prezzo, del, prenot, tipocucina, username);
            String messaggioServer = clientTK.inviaRichiesta(nuovoRistorante);

            if (messaggioServer != null && messaggioServer.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, messaggioServer, "Esito Inserimento ristorante", JOptionPane.INFORMATION_MESSAGE);

                // AGGIORNIAMO LA HOME PAGE ATTUALE E CHIUDIAMO LA SCHERMATA
                if(homeParent != null) {
                    homeParent.aggiornaVistaProprietario();
                }
                this.dispose();

            } else {
                String errore = (messaggioServer != null) ? messaggioServer : "ERRORE: Impossibile contattare il server.";
                JOptionPane.showMessageDialog(this, errore, "Avviso Server", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Errore: Inserisci solo valori numerici per Latitudine, Longitudine e Fascia Prezzo.", "Errore Formato", JOptionPane.ERROR_MESSAGE);
        }
    }
}