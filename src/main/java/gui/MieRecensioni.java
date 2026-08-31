package gui;

import com.mycompany.theknife.clientTK;
import modelli.Recensione;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/// Classe per la gestione della finestra che mostra le proprie recensioni
public class MieRecensioni extends JDialog {
    private JPanel Lista;                  // mainPanel del form
    private JPanel ListaMiaRecensioni;     // pannello dentro lo scroll, dove aggiungiamo le card

    private final String nomeUtente;

    /// @param parent JFrame genitore
    /// @param nomeUtente String nome utente
    public MieRecensioni(JFrame parent, String nomeUtente) {
        super(parent, "Recensioni", true);
        this.nomeUtente = nomeUtente;

        if (Lista == null) {
            throw new IllegalStateException("Il pannello Lista non è stato associato correttamente nel file MieRecensioni.form");
        }

        setContentPane(Lista);
        setSize(550, 600);
        setLocationRelativeTo(parent);

        ListaMiaRecensioni.setLayout(new BoxLayout(ListaMiaRecensioni, BoxLayout.Y_AXIS));

        caricaMieRecensioni();
    }

    /// Crea e popola la tabella delle proprie recensioni
    private void caricaMieRecensioni() {
        ListaMiaRecensioni.removeAll();

        List<Recensione> recensioni = clientTK.inviaRichiesta(new String[]{"GET_RECENSIONI_UTENTE", nomeUtente});

        if (recensioni == null || recensioni.isEmpty()) {
            JLabel lblEmpty = new JLabel("Non hai ancora scritto nessuna recensione.");
            lblEmpty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ListaMiaRecensioni.add(lblEmpty);
        } else {
            for (Recensione r : recensioni) {
                // Recupera il nome del ristorante
                modelli.Ristorante risto = clientTK.inviaRichiesta(new String[]{"RICERCA_ID", String.valueOf(r.getRistoranteId())});
                String nomeRistorante = (risto != null) ? risto.getNome() : "Ristorante #" + r.getRistoranteId();

                JPanel card = new JPanel(new BorderLayout(5, 5));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createEtchedBorder()
                ));

                // Header con Nome Ristorante, Stelle Dorate e Data
                String stellePiene = "&#9733;".repeat(r.getStelle());
                String stelleVuote = "&#9734;".repeat(5 - r.getStelle());

                JLabel lblHeader = new JLabel("<html><b style='font-family: sans-serif; font-size: 10pt; color: #000000;'>"
                        + nomeRistorante + "</b> &nbsp;&nbsp;<span style='font-family: sans-serif; font-size: 11pt; color: #D4AC0D;'>"
                        + stellePiene + stelleVuote + "</span> <span style='font-family: sans-serif; font-size: 9pt; color: #555555;'>("
                        + r.getData() + ")</span></html>");

                JTextArea txtCommento = new JTextArea(r.getTesto());
                txtCommento.setEditable(false);
                txtCommento.setOpaque(false);
                txtCommento.setLineWrap(true);
                txtCommento.setWrapStyleWord(true);
                txtCommento.setFont(new Font("SansSerif", Font.PLAIN, 12));

                card.add(lblHeader, BorderLayout.NORTH);
                card.add(txtCommento, BorderLayout.CENTER);

                // Container per risposta ed eventuale bottone
                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.setOpaque(false);

                // Box per risposta del gestore (se presente)
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
                }

                // Pulsante esplicito "Modifica / Elimina"
                JButton btnModifica = new JButton("Modifica / Elimina");
                btnModifica.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnModifica.addActionListener(e -> {
                    DettaglioRecensione dettaglio = new DettaglioRecensione((JFrame) getParent(), r, nomeUtente, nomeRistorante);
                    dettaglio.setVisible(true);
                    caricaMieRecensioni(); // refresh automatico alla chiusura
                });

                JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnWrapper.setOpaque(false);
                btnWrapper.add(btnModifica);
                bottomPanel.add(btnWrapper, BorderLayout.EAST);

                card.add(bottomPanel, BorderLayout.SOUTH);
                ListaMiaRecensioni.add(card);
            }
        }

        ListaMiaRecensioni.revalidate();
        ListaMiaRecensioni.repaint();
    }
}

