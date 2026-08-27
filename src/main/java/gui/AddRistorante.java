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

    public AddRistorante(String u) {
        username=u;
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente");
        }

        setContentPane(mainPanel);
        setTitle("Registrazione nuovo ristorante - TheKnife");
        //la chiusura della pagina dalla X non viene gestita autonomamente, ma viene gestita dall'WindowAdapter
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Intercettiamo il click sulla "X" in alto a destra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tornaAllaHome();
            }
        });

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
        // 1. Estrazione dei valori
        String nome = txtNome.getText().trim();
        String indirizzo = txtindirizzo.getText().trim();
        String citta = txtcitta.getText().trim();
        String nazione = txtnazione.getText().trim();
        String lat = latitudine.getText().trim();
        String lon = longitudine.getText().trim();
        String prezzo = fasciaprezzo.getText().trim();
        boolean del = (boolean) combodelivery.getSelectedItem();
        boolean prenot = (boolean)  comboprenotazione.getSelectedItem();
        String tipocucina = txttipocucina.getText().trim();



        // Controllo: Campi obbligatori
        if (nome.isEmpty() || indirizzo.isEmpty() || citta.isEmpty() ||
                nazione.isEmpty() || tipocucina.isEmpty() || lat.isEmpty() || lon.isEmpty() || prezzo.isEmpty()){
            JOptionPane.showMessageDialog(this, "Compilare tutti i campi.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return; // Blocca l'esecuzione
        }

        // Creazione del pacchetto Ristorante
        Ristorante nuovoRistorante = new Ristorante(nome, indirizzo, citta, nazione, lat, lon, prezzo, del, prenot, tipocucina, username);

        // INVIO AL SERVER TRAMITE IL GESTORE CENTRALIZZATO
        String messaggioServer = clientTK.inviaRichiesta(nuovoRistorante);

        // Gestione della risposta
        if (messaggioServer.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, messaggioServer, "Esito Inserimento ristorante", JOptionPane.INFORMATION_MESSAGE);
            svuotaInterfaccia(); // Pulisce i campi in automatico
            new homePageU(username,latitudine.getText()+"/"+longitudine.getText()).setVisible(true);
            this.dispose();
        } else {
            // Mostra l'errore (ad esempio se lo username esiste già, errore generato dal database!)
            JOptionPane.showMessageDialog(this, messaggioServer, "Avviso Server", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verifica che l' indirizzo sia nel formato corretto .
     * @param Stringa Il testo inserito dall'utente.
     * @return true se è formattata bene, false se è sbagliata.
     */
    //private boolean isindirizzovalido(String Stringa) {}




    //Chiude la finestra corrente e riapre il menu principale (Home)
    private void tornaAllaHome() {
        new homePageU(username,"");
        this.dispose();
    }

}
