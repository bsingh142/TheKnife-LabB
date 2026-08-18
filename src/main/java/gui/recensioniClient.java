package gui;

import modelli.Recensione;

import javax.swing.*;

import com.mycompany.theknife.clientTK;
import java.awt.*;
import java.util.List;

public class recensioniClient extends JDialog{
    private JPanel mainPanel;
    private JLabel NomeRistorante;
    private JComboBox comboStelle;
    private JTextArea TestoNuovaRecensione;
    private JButton ButtonInvio;
    private JPanel VistaLog;
    private JPanel ListaRecensioni;
    private JScrollPane ScorriRecensioni;

    private final int idRistorante;
    private final String nomeUtente;

    public recensioniClient(JFrame parent, int idRistorante, String nomeRistoranteText, String nomeUtente) {
        super(parent, "Recensioni - " + nomeRistoranteText, true);
        this.idRistorante = idRistorante;
        this.nomeUtente = nomeUtente;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file recensioniClient.form");
        }

        setContentPane(mainPanel);
        setSize(550, 600);
        setLocationRelativeTo(parent);

        // Imposta l'intestazione con il nome del ristorante
        NomeRistorante.setText("Recensioni per: " + nomeRistoranteText);

        // Inizializza le opzioni delle stelle se il ComboBox è vuoto
        if (comboStelle.getItemCount() == 0) {
            DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>(new Integer[]{1, 2, 3, 4, 5});
            comboStelle.setModel(model);
            comboStelle.setSelectedIndex(4); // Default: 5 stelle
        }

        // Imposta il layout verticale per il pannello interno dello ScrollPane
        ListaRecensioni.setLayout(new BoxLayout(ListaRecensioni, BoxLayout.Y_AXIS));

        // Gestione visibilità form per utente Guest
        if (nomeUtente == null) {
            VistaLog.setVisible(false); // Nasconde l'intero pannello di inserimento se non loggato
        } else {
            ButtonInvio.addActionListener(e -> inviaRecensione());
        }

        // Carica le recensioni dal Server
        caricaRecensioni();
    }

    /**
     * Recupera le recensioni inviate dal Server e le aggiunge a ListaRecensioni
     */
    private void caricaRecensioni() {
        ListaRecensioni.removeAll();

        List<Recensione> recensioni = clientTK.inviaRichiesta(new String[]{"GET_RECENSIONI", String.valueOf(idRistorante)});

        if (recensioni == null || recensioni.isEmpty()) {
            JLabel lblEmpty = new JLabel("Nessuna recensione ancora presente per questo ristorante.");
            lblEmpty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ListaRecensioni.add(lblEmpty);
        } else {
            for (Recensione r : recensioni) {
                JPanel card = new JPanel(new BorderLayout(5, 5));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createEtchedBorder()
                ));

                String stelleStr = "★".repeat(r.getStelle()) + "☆".repeat(5 - r.getStelle());
                JLabel lblHeader = new JLabel(stelleStr + "  (" + r.getData() + ")");
                lblHeader.setFont(new Font("Arial", Font.BOLD, 12));

                JTextArea txtCommento = new JTextArea(r.getTesto());
                txtCommento.setEditable(false);
                txtCommento.setOpaque(false);
                txtCommento.setLineWrap(true);

                card.add(lblHeader, BorderLayout.NORTH);
                card.add(txtCommento, BorderLayout.CENTER);

                // Eventuale risposta del ristoratore
                if (r.getRisposta() != null && !r.getRisposta().isEmpty()) {
                    JLabel lblRisposta = new JLabel("  ↳ Risposta del Ristoratore: " + r.getRisposta());
                    lblRisposta.setFont(new Font("Arial", Font.ITALIC, 11));
                    lblRisposta.setForeground(new Color(0, 100, 0));
                    card.add(lblRisposta, BorderLayout.SOUTH);
                }

                ListaRecensioni.add(card);
            }
        }

        ListaRecensioni.revalidate();
        ListaRecensioni.repaint();
    }

    /**
     * Raccoglie i dati dal Form e li invia al server tramite clientTK
     */
    private void inviaRecensione() {
        String testo = TestoNuovaRecensione.getText().trim();
        if (testo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci un commento prima di inviare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int stelle = (Integer) comboStelle.getSelectedItem();


        Recensione nuovaRecensione = new Recensione(nomeUtente, idRistorante, stelle, testo);
        // Se invii la richiesta come array di stringhe o oggetto
        String risposta = clientTK.inviaRichiesta(nuovaRecensione);
        if (risposta != null && risposta.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, "Recensione pubblicata con successo!", "Esito", JOptionPane.INFORMATION_MESSAGE);
            TestoNuovaRecensione.setText("");
            caricaRecensioni();
        } else {
            JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
