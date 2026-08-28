package gui;

import modelli.Recensione;
import modelli.Ristorante;
import javax.swing.*;
import com.mycompany.theknife.clientTK;
import java.awt.*;
import java.util.List;

public class recensioniClient extends JDialog {
    private JPanel mainPanel;
    private JLabel NomeRistorante;
    private JComboBox comboStelle;
    private JTextArea TestoNuovaRecensione;
    private JButton ButtonInvio;
    private JPanel VistaLog;
    private JPanel ListaRecensioni;
    private JScrollPane ScorriRecensioni;
    private JLabel Stelle;
    private JLabel RichiediRecensione;

    // Dal branch rece+risto (Ristoratore)
    private JLabel mediaRecensioni;
    private JLabel Media;
    private JLabel recensioniTotali;
    private JLabel Tot;

    // Dal branch modifiche (Cliente)
    private JButton PreferitiButton;

    private final int idRistorante;
    private final String nomeUtente;
    private boolean isProprietario = false;

    public recensioniClient(JFrame parent, int idRistorante, String nomeRistoranteText, String nomeUtente) {
        super(parent, "Recensioni - " + nomeRistoranteText, true);
        this.idRistorante = idRistorante;
        this.nomeUtente = nomeUtente;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file recensioniClient.form");
        }
        setContentPane(mainPanel);
        setSize(700, 700);
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
            if (PreferitiButton != null) PreferitiButton.setVisible(false);
        } else {
            if (ButtonInvio != null) ButtonInvio.addActionListener(e -> inviaRecensione());
            if (PreferitiButton != null) PreferitiButton.addActionListener(e -> aggiungiPreferito());
        }

        // SOLO IL RISTORATORE PUÒ VEDERE LA MEDIA E RISPONDERE
        if (nomeUtente != null) {
            Ristorante ristorante = clientTK.inviaRichiesta(new String[]{"RICERCA_ID", String.valueOf(idRistorante)});
            if (ristorante != null && nomeUtente.equals(ristorante.getProprietario())) {
                isProprietario = true;
            }
        }

        if (!isProprietario) {
            if (mediaRecensioni != null) mediaRecensioni.setVisible(false);
            if (Media != null) Media.setVisible(false);
            if (recensioniTotali != null) recensioniTotali.setVisible(false);
            if (Tot != null) Tot.setVisible(false);
        }

        // Carica le recensioni dal Server
        caricaRecensioni();
    }

    private void caricaRecensioni() {
        ListaRecensioni.removeAll();
        int n = 0, totale = 0;

        List<Recensione> recensioni = clientTK.inviaRichiesta(new String[]{"GET_RECENSIONI", String.valueOf(idRistorante)});

        if (recensioni == null || recensioni.isEmpty()) {
            JLabel lblEmpty = new JLabel("Nessuna recensione ancora presente per questo ristorante.");
            lblEmpty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ListaRecensioni.add(lblEmpty);
        } else {
            for (Recensione r : recensioni) {
                n++;
                totale += r.getStelle();
                JPanel card = new JPanel(new BorderLayout(5, 5));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createEtchedBorder()
                ));

                String stelleStr = " ".repeat(r.getStelle()) + " ".repeat(5 - r.getStelle());
                JLabel lblHeader = new JLabel(stelleStr + "  (" + r.getData() + ")");
                lblHeader.setFont(new Font("Arial", Font.BOLD, 12));

                JTextArea txtCommento = new JTextArea(r.getTesto());
                txtCommento.setEditable(false);
                txtCommento.setOpaque(false);
                txtCommento.setLineWrap(true);
                txtCommento.setWrapStyleWord(true);

                card.add(lblHeader, BorderLayout.NORTH);
                card.add(txtCommento, BorderLayout.CENTER);

                if (r.getRisposta() != null && !r.getRisposta().isEmpty()) {
                    JLabel lblRisposta = new JLabel("  Risposta del Ristoratore: " + r.getRisposta());
                    lblRisposta.setFont(new Font("Arial", Font.ITALIC, 11));
                    lblRisposta.setForeground(new Color(0, 100, 0));
                    card.add(lblRisposta, BorderLayout.SOUTH);
                }

                ListaRecensioni.add(card);

                // SOLO IL PROPRIETARIO PUÒ CLICCARE PER RISPONDERE
                if (isProprietario) {
                    if (Media != null) Media.setText(String.valueOf((float) totale / n));
                    if (Tot != null) Tot.setText(String.valueOf(n));

                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    card.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            apriPannelloRisposta(r);
                        }
                    });
                }
            }
        }
        ListaRecensioni.revalidate();
        ListaRecensioni.repaint();
    }

    private void inviaRecensione() {
        String testo = TestoNuovaRecensione.getText().trim();
        if (testo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci un commento prima di inviare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int stelle = (Integer) comboStelle.getSelectedItem();
        Recensione nuovaRecensione = new Recensione(nomeUtente, idRistorante, stelle, testo);
        String risposta = clientTK.inviaRichiesta(nuovaRecensione);

        if (risposta != null && risposta.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, "Recensione pubblicata con successo!", "Esito", JOptionPane.INFORMATION_MESSAGE);
            TestoNuovaRecensione.setText("");
            caricaRecensioni();
        } else {
            JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aggiungiPreferito() {
        String[] pacchetto = {"AGGIUNGI_PREFERITO", nomeUtente, String.valueOf(idRistorante)};
        String risposta = clientTK.inviaRichiesta(pacchetto);
        if (risposta != null && risposta.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, risposta.substring(3), "Esito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void apriPannelloRisposta(Recensione recensione) {
        JTextArea areaRisposta = new JTextArea(5, 30);
        areaRisposta.setLineWrap(true);
        areaRisposta.setWrapStyleWord(true);
        int risultato = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(areaRisposta),
                "Rispondi alla recensione",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (risultato == JOptionPane.OK_OPTION) {
            String risposta = areaRisposta.getText().trim();
            if (risposta.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci una risposta.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
            clientTK.inviaRichiesta(new String[]{"RISPONDI_RECENSIONE", String.valueOf(recensione.getIdRecensione()), risposta});
            caricaRecensioni(); // Ricarica le recensioni per visualizzare subito la risposta
        }
    }
}