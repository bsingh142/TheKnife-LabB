package gui;

import com.mycompany.theknife.clientTK;
import modelli.Recensione;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MieRecensioni extends JDialog {
    private JPanel Lista;                  // mainPanel del form
    private JPanel ListaMiaRecensioni;     // pannello dentro lo scroll, dove aggiungiamo le card

    private final String nomeUtente;

    public MieRecensioni(JFrame parent, String nomeUtente) {
        super(parent, "Le mie recensioni", true);
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

    private void caricaMieRecensioni() {
        ListaMiaRecensioni.removeAll();

        List<Recensione> recensioni = clientTK.inviaRichiesta(new String[]{"GET_RECENSIONI_UTENTE", nomeUtente});

        if (recensioni == null || recensioni.isEmpty()) {
            JLabel lblEmpty = new JLabel("Non hai ancora scritto nessuna recensione.");
            lblEmpty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ListaMiaRecensioni.add(lblEmpty);
        } else {
            for (Recensione r : recensioni) {
                JPanel card = new JPanel(new BorderLayout(5, 5));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createEtchedBorder()
                ));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));

                String stelleStr = "★".repeat(r.getStelle()) + "☆".repeat(5 - r.getStelle());
                JLabel lblHeader = new JLabel("Ristorante ID: " + r.getRistoranteId() + "  " + stelleStr + "  (" + r.getData() + ")");
                lblHeader.setFont(new Font("Arial", Font.BOLD, 12));

                JTextArea txtCommento = new JTextArea(r.getTesto());
                txtCommento.setEditable(false);
                txtCommento.setOpaque(false);
                txtCommento.setLineWrap(true);

                card.add(lblHeader, BorderLayout.NORTH);
                card.add(txtCommento, BorderLayout.CENTER);

                // Click sulla card -> apre il dettaglio con Modifica/Elimina
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        DettaglioRecensione dettaglio = new DettaglioRecensione((JFrame) getParent(), r, nomeUtente);
                        dettaglio.setVisible(true);
                        caricaMieRecensioni(); // refresh dopo eventuale modifica/eliminazione
                    }
                });

                ListaMiaRecensioni.add(card);
            }
        }

        ListaMiaRecensioni.revalidate();
        ListaMiaRecensioni.repaint();
    }
}

