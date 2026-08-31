package gui;

import modelli.Recensione;
import modelli.Ristorante;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import com.mycompany.theknife.clientTK;
import java.awt.*;
import java.util.List;

/// Classe che gestisce la finestra per l'inserimento di una nuova recensione
/// visualizzazione recensioni altrui e aggiunta ai preferiti.
/// Consente al ristoratore di rispondere alle recensioni.
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

    /// @param parent JFrame genitore
    /// @param idRistorante int id del ristorante
    /// @param nomeRistoranteText String nome ristorante
    /// @param nomeUtente String nome utente
    public recensioniClient(JFrame parent, int idRistorante, String nomeRistoranteText, String nomeUtente) {
        super(parent, "Recensioni - " + nomeRistoranteText, true);
        this.idRistorante = idRistorante;
        this.nomeUtente = nomeUtente;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file recensioniClient.form");
        }
        setContentPane(mainPanel);

        // 1. Padding globale della finestra
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Imposta l'intestazione con il nome del ristorante
        if (NomeRistorante != null) {
            NomeRistorante.setText("<html><h2 style='font-family: sans-serif; color: #2C3E50; margin: 0; padding-bottom: 5px;'>Recensioni per: " + nomeRistoranteText + "</h2></html>");
        }

        // Inizializza le opzioni delle stelle
        if (comboStelle != null) {
            if (comboStelle.getItemCount() == 0) {
                DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>(new Integer[]{1, 2, 3, 4, 5});
                comboStelle.setModel(model);
                comboStelle.setSelectedIndex(4); // Default: 5 stelle
            }
            comboStelle.setCursor(new Cursor(Cursor.HAND_CURSOR));
            comboStelle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        }

        // 2. Styling area di testo per nuova recensione
        if (TestoNuovaRecensione != null) {
            TestoNuovaRecensione.setFont(new Font("SansSerif", Font.PLAIN, 12));
            TestoNuovaRecensione.setBorder(new CompoundBorder(
                    new LineBorder(new Color(189, 195, 199), 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }

        // 3. Styling dei Bottoni
        Font btnFont = new Font("SansSerif", Font.BOLD, 12);
        if (ButtonInvio != null) {
            ButtonInvio.setFont(btnFont);
            ButtonInvio.setCursor(new Cursor(Cursor.HAND_CURSOR));
            ButtonInvio.addActionListener(e -> inviaRecensione());
        }

        if (PreferitiButton != null) {
            PreferitiButton.setFont(btnFont);
            PreferitiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        // Font e allineamenti etichette
        if (Stelle != null) Stelle.setFont(new Font("SansSerif", Font.BOLD, 12));
        if (RichiediRecensione != null) RichiediRecensione.setFont(new Font("SansSerif", Font.PLAIN, 12));
        if (mediaRecensioni != null) mediaRecensioni.setFont(new Font("SansSerif", Font.BOLD, 12));
        if (recensioniTotali != null) recensioniTotali.setFont(new Font("SansSerif", Font.BOLD, 12));
        if (Media != null) Media.setFont(new Font("SansSerif", Font.PLAIN, 12));
        if (Tot != null) Tot.setFont(new Font("SansSerif", Font.PLAIN, 12));

        ListaRecensioni.setLayout(new BoxLayout(ListaRecensioni, BoxLayout.Y_AXIS));
        if(ScorriRecensioni != null) {
            ScorriRecensioni.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        }

        // --- CONTROLLO RUOLI E VISIBILITÀ ---
        if (nomeUtente == null) {
            VistaLog.setVisible(false);
            if (PreferitiButton != null) PreferitiButton.setVisible(false);
        } else {
            modelli.Utente u = clientTK.inviaRichiesta(new String[]{"GET_UTENTE", nomeUtente});

            if (u != null && u.getRuolo().equals("Ristoratore")) {
                VistaLog.setVisible(false);
                if (PreferitiButton != null) PreferitiButton.setVisible(false);
            } else {
                if (PreferitiButton != null) {
                    controllaStatoPreferito();
                    PreferitiButton.addActionListener(e -> gestisciPreferito());
                }
            }
        }

        if (nomeUtente != null) {
            Ristorante ristorante = clientTK.inviaRichiesta(new String[]{"RICERCA_ID", String.valueOf(idRistorante)});
            if (ristorante != null && nomeUtente.equals(ristorante.getProprietario())) {
                isProprietario = true;
            }
        }

        /*if (!isProprietario) {
            if (mediaRecensioni != null) mediaRecensioni.setVisible(false);
            if (Media != null) Media.setVisible(false);
            if (recensioniTotali != null) recensioniTotali.setVisible(false);
            if (Tot != null) Tot.setVisible(false);
        }*/
        if(isProprietario){
            String info=getInfoRistorante(idRistorante);
            String testo= String.format(
                    "<html>%s<br>%s</html>",
                    nomeRistoranteText,
                    info
            );
            NomeRistorante.setText(testo);
        }

        caricaRecensioni();

        setSize(700, 700);
        setLocationRelativeTo(parent);
    }

    /// Crea e popola la lista delle recensioni altrui
    private void caricaRecensioni() {
        ListaRecensioni.removeAll();
        int n = 0, totale = 0;

        List<Recensione> recensioni = com.mycompany.theknife.clientTK.inviaRichiesta(new String[]{"GET_RECENSIONI", String.valueOf(idRistorante)});

        if (recensioni == null || recensioni.isEmpty()) {
            JLabel lblEmpty = new JLabel("Nessuna recensione ancora presente per questo ristorante.");
            lblEmpty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            lblEmpty.setFont(new Font("SansSerif", Font.ITALIC, 12));
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

                String stellePiene = "&#9733;".repeat(r.getStelle());
                String stelleVuote = "&#9734;".repeat(5 - r.getStelle());

                String nomeAutore = (nomeUtente == null) ? "Utente Anonimo" : r.getIdUtente();

                JLabel lblHeader = new JLabel("<html><span style='font-family: sans-serif; font-size: 11pt; color: #D4AC0D;'>"
                        + stellePiene + stelleVuote + "</span> <b style='font-family: sans-serif; font-size: 10pt; color: #000000;'> "
                        + nomeAutore + "</b> <span style='font-family: sans-serif; font-size: 9pt; color: #555555;'>("
                        + r.getData() + ")</span></html>");

                JTextArea txtCommento = new JTextArea(r.getTesto());
                txtCommento.setEditable(false);
                txtCommento.setOpaque(false);
                txtCommento.setLineWrap(true);
                txtCommento.setWrapStyleWord(true);
                txtCommento.setFont(new Font("SansSerif", Font.PLAIN, 12));

                card.add(lblHeader, BorderLayout.NORTH);
                card.add(txtCommento, BorderLayout.CENTER);

                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.setOpaque(false);

                if (r.getRisposta() != null && !r.getRisposta().trim().isEmpty()) {
                    JPanel rispostaPanel = new JPanel(new BorderLayout(5, 5));
                    rispostaPanel.setBackground(new Color(245, 247, 250));
                    rispostaPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(8, 15, 5, 5),
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(41, 128, 185)),
                                    BorderFactory.createEmptyBorder(6, 10, 6, 8)
                            )
                    ));

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
                    JButton btnRispondi = new JButton("Rispondi");
                    btnRispondi.setFont(new Font("SansSerif", Font.BOLD, 11));
                    btnRispondi.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btnRispondi.addActionListener(e -> apriPannelloRisposta(r));

                    JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    btnWrapper.setOpaque(false);
                    btnWrapper.add(btnRispondi);
                    bottomPanel.add(btnWrapper, BorderLayout.EAST);
                }

                card.add(bottomPanel, BorderLayout.SOUTH);
                ListaRecensioni.add(card);

                if (isProprietario) {
                    if (Media != null) Media.setText(String.format("%.1f", (float) totale / n));
                    if (Tot != null) Tot.setText(String.valueOf(n));
                }
            }
        }
        ListaRecensioni.revalidate();
        ListaRecensioni.repaint();
    }

    /// Controlla e invia la recensione appena scritta al db
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

    /// @param recensione La recensione a cui rispondere
    /// Apre il pannello da cui il ristoratore può rispondere alla recensione
    private void apriPannelloRisposta(Recensione recensione) {
        JTextArea areaRisposta = new JTextArea(5, 30);
        areaRisposta.setFont(new Font("SansSerif", Font.PLAIN, 12));
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
            caricaRecensioni();
        }
    }

    /// Controlla se il ristorante è già un prefererito e aggiorna il bottone per aggiunta o
    /// rimozione preferito di conseguenza
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

    /// Effettua l'aggiornamento del testo nel bottone preferiti
    private void aggiornaTestoBottonePreferiti() {
        if (PreferitiButton != null) {
            PreferitiButton.setText(isPreferito ? "Rimuovi dai preferiti" : "Aggiungi ai preferiti");
        }
    }

    /// Modifica l'effetto del bottone preferiti in base al valore di isPreferito
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

    /// @param id int id del ristorante
    /// @return String contiene quante recensioni sono presenti per questo ristorante e la loro media di stelle
    private String getInfoRistorante(int id){
        return clientTK.inviaRichiesta(new String[]{"INFO_RISTORANTE",String.valueOf(idRistorante)});
    }
}