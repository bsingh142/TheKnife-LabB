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
    // Rimosse latitudine e longitudine!
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
        String prezzo = fasciaprezzo.getText().trim();
        boolean del = (boolean) combodelivery.getSelectedItem();
        boolean prenot = (boolean)  comboprenotazione.getSelectedItem();
        String tipocucina = txttipocucina.getText().trim();

        // Controllo campi vuoti senza più lat e lon
        if (nome.isEmpty() || indirizzo.isEmpty() || citta.isEmpty() ||
                nazione.isEmpty() || tipocucina.isEmpty() || prezzo.isEmpty()){
            JOptionPane.showMessageDialog(this, "Compilare tutti i campi.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int prezzoVal = Integer.parseInt(prezzo);

            if (prezzoVal < 1 || prezzoVal > 100) {
                JOptionPane.showMessageDialog(this, "Attenzione: La fascia di prezzo deve essere un numero compreso tra 1 e 100!", "Errore Inserimento", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // --- RICERCA AUTOMATICA DELLE COORDINATE (CON VIA) ---
            String[] richiestaPos = {"POSIZIONE_RISTORANTE", indirizzo, citta, nazione};
            String rispServer = clientTK.inviaRichiesta(richiestaPos);

            if(rispServer == null || rispServer.startsWith("ERRORE:")){
                JOptionPane.showMessageDialog(this,"Impossibile trovare le coordinate per l'indirizzo inserito. Verifica la correttezza di Via, Città e Nazione.","Indirizzo non trovato",JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Estraggo lat e lon precisissime
            String[] tmp = rispServer.split("/");
            String lat = tmp[0];
            String lon = tmp[1];
            // -----------------------------------------------------

            Ristorante nuovoRistorante = new Ristorante(nome, indirizzo, citta, nazione, lat, lon, prezzo, del, prenot, tipocucina, username);
            String messaggioServer = clientTK.inviaRichiesta(nuovoRistorante);

            if (messaggioServer != null && messaggioServer.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, messaggioServer, "Esito Inserimento ristorante", JOptionPane.INFORMATION_MESSAGE);

                if(homeParent != null) {
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
}