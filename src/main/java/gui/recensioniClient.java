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
    private boolean isPreferito = false;

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

        // --- CONTROLLO RUOLI E VISIBILITÀ ---
        if (nomeUtente == null) {
            // Utente Guest: nasconde sia l'inserimento recensione che i preferiti
            VistaLog.setVisible(false);
            if (PreferitiButton != null) PreferitiButton.setVisible(false);
        } else {
            // Utente Loggato: verifichiamo il ruolo
            modelli.Utente u = clientTK.inviaRichiesta(new String[]{"GET_UTENTE", nomeUtente});

            if (u != null && u.getRuolo().equals("Ristoratore")) {
                // Il Ristoratore non può recensire né gestire i preferiti
                VistaLog.setVisible(false);
                if (PreferitiButton != null) PreferitiButton.setVisible(false);
            } else {
                // SE È UN CLIENTE NORMALE: colleghiamo i listener e controlliamo lo stato preferiti
                if (ButtonInvio != null) ButtonInvio.addActionListener(e -> inviaRecensione());
                if (PreferitiButton != null) {
                    controllaStatoPreferito();
                    PreferitiButton.addActionListener(e -> gestisciPreferito());
                }
            }
        }

        // SOLO IL RISTORATORE PROPRIETARIO PUÒ VEDERE LE STATISTICHE
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

            List<Recensione> recensioni = com.mycompany.theknife.clientTK.inviaRichiesta(new String[]{"GET_RECENSIONI", String.valueOf(idRistorante)});

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

                    // Entità HTML Unicode compatibili al 100% su Windows, Mac e Linux
                    String stellePiene = "&#9733;".repeat(r.getStelle());
                    String stelleVuote = "&#9734;".repeat(5 - r.getStelle());

                    // Gestione visualizzazione autore (Anonimo se l'utente attuale è Guest, altrimenti mostra lo Username)
                    String nomeAutore = (nomeUtente == null) ? "Utente Anonimo" : r.getIdUtente();

                    // Header con Stelle Dorate, Username e Data
                    JLabel lblHeader = new JLabel("<html><span style='font-family: sans-serif; font-size: 11pt; color: #D4AC0D;'>"
                            + stellePiene + stelleVuote + "</span> <b style='font-family: sans-serif; font-size: 10pt; color: #000000;'> "
                            + nomeAutore + "</b> <span style='font-family: sans-serif; font-size: 9pt; color: #555555;'>("
                            + r.getData() + ")</span></html>");

                    JTextArea txtCommento = new JTextArea(r.getTesto());
                    txtCommento.setEditable(false);
                    txtCommento.setOpaque(false);
                    txtCommento.setLineWrap(true);
                    txtCommento.setWrapStyleWord(true);

                    card.add(lblHeader, BorderLayout.NORTH);
                    card.add(txtCommento, BorderLayout.CENTER);

                    // --- GESTIONE PARTE INFERIORE: BOX RISPOSTA ELEGANTE O BOTTONE ---
                    JPanel bottomPanel = new JPanel(new BorderLayout());
                    bottomPanel.setOpaque(false);

                    if (r.getRisposta() != null && !r.getRisposta().trim().isEmpty()) {
                        // Box rientrato stile TripAdvisor / Google Reviews
                        JPanel rispostaPanel = new JPanel(new BorderLayout(5, 5));
                        rispostaPanel.setBackground(new Color(245, 247, 250)); // Grigio/Blu chiaro moderno
                        rispostaPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(8, 15, 5, 5), // Indentazione a sinistra
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(41, 128, 185)), // Linea verticale blu d'accento
                                        BorderFactory.createEmptyBorder(6, 10, 6, 8) // Padding interno
                                )
                        ));

                        // Intestazione con ruolo del gestore ben distinto
                        JLabel lblTitoloGestore = new JLabel("<html><b style='color: #2980B9; font-family: sans-serif; font-size: 10pt;'>Risposta del Ristoratore</b></html>");

                        JTextArea txtRisposta = new JTextArea(r.getRisposta());
                        txtRisposta.setEditable(false);
                        txtRisposta.setOpaque(false);
                        txtRisposta.setLineWrap(true);
                        txtRisposta.setWrapStyleWord(true);
                        txtRisposta.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        txtRisposta.setForeground(new Color(44, 62, 80));

                        rispostaPanel.add(lblTitoloGestore, BorderLayout.NORTH);
                        rispostaPanel.add(txtRisposta, BorderLayout.CENTER);

                        bottomPanel.add(rispostaPanel, BorderLayout.CENTER);
                    } else if (isProprietario) {
                        // SE NON C'È RISPOSTA E L'UTENTE È IL PROPRIETARIO: Mostra il bottone
                        JButton btnRispondi = new JButton("Rispondi");
                        btnRispondi.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        btnRispondi.addActionListener(e -> apriPannelloRisposta(r));

                        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                        btnWrapper.setOpaque(false);
                        btnWrapper.add(btnRispondi);
                        bottomPanel.add(btnWrapper, BorderLayout.EAST);
                    }

                    card.add(bottomPanel, BorderLayout.SOUTH);
                    ListaRecensioni.add(card);

                    // Aggiorniamo le statistiche in alto per il proprietario
                    if (isProprietario) {
                        if (Media != null) Media.setText(String.format("%.1f", (float) totale / n));
                        if (Tot != null) Tot.setText(String.valueOf(n));
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
    private void controllaStatoPreferito() {
        List<Ristorante> preferiti = clientTK.inviaRichiesta(new String[]{"GET_PREFERITI", nomeUtente});
        isPreferito = false;
        if (preferiti != null) {
            for (Ristorante r : preferiti) {
                if (r.getId() == idRistorante) {
                    isPreferito = true;
                    break;
                }
            }
        }
        aggiornaTestoBottonePreferiti();
    }

    private void aggiornaTestoBottonePreferiti() {
        if (PreferitiButton != null) {
            PreferitiButton.setText(isPreferito ? "Rimuovi dai preferiti" : "Aggiungi ai preferiti");
        }
    }

    private void gestisciPreferito() {
        if (isPreferito) {
            String[] pacchetto = {"RIMUOVI_PREFERITO", nomeUtente, String.valueOf(idRistorante)};
            String risposta = clientTK.inviaRichiesta(pacchetto);
            if (risposta != null && !risposta.startsWith("ERRORE")) {
                JOptionPane.showMessageDialog(this, "Ristorante rimosso dai preferiti con successo!", "Esito", JOptionPane.INFORMATION_MESSAGE);
                isPreferito = false;
                aggiornaTestoBottonePreferiti();
            } else {
                JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            String[] pacchetto = {"AGGIUNGI_PREFERITO", nomeUtente, String.valueOf(idRistorante)};
            String risposta = clientTK.inviaRichiesta(pacchetto);
            if (risposta != null && risposta.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Ristorante aggiunto ai preferiti!", "Esito", JOptionPane.INFORMATION_MESSAGE);
                isPreferito = true;
                aggiornaTestoBottonePreferiti();
            } else {
                JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}