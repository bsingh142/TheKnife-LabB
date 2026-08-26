package gui;

import com.mycompany.theknife.clientTK;
import modelli.Recensione;
import modelli.Ristorante;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MieiPreferiti  extends JDialog{
    private JPanel panel1;
    private JLabel TitoloPreferiti;
    private JPanel ListaPreferiti;
    private JScrollPane ScorriPreferiti;

    private final String Utente;

    public MieiPreferiti(JFrame parent, String nomeUtente) {
        super(parent, "I miei preferiti", true);
        this.Utente = nomeUtente;

        if (panel1 == null) {
            throw new IllegalStateException("Il pannello listaPreferiti non è stato associato correttamente nel file MieiPreferiti.form");
        }

        setContentPane(panel1);
        setSize(550, 600);
        setLocationRelativeTo(parent);

        ListaPreferiti.setLayout(new BoxLayout(ListaPreferiti, BoxLayout.Y_AXIS));

        caricaPreferiti();
    }

    private void caricaPreferiti() {
        ListaPreferiti.removeAll();

        List<Ristorante> preferiti = clientTK.inviaRichiesta(new String[]{"GET_PREFERITI", Utente});

        if ( preferiti == null || preferiti.isEmpty()) {
            JLabel lblEmpty = new JLabel("Non hai ancora nessun reistorante tra i preferiti.");
            lblEmpty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ListaPreferiti.add(lblEmpty);
        } else {
            for (Ristorante r : preferiti) {
                JPanel card = new JPanel(new BorderLayout(5, 5));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createEtchedBorder()
                ));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JLabel lblHeader = new JLabel(r.getNome() + " — " + r.getCitta() + ", " + r.getNazione());
                lblHeader.setFont(new Font("Arial", Font.BOLD, 12));

                card.add(lblHeader, BorderLayout.NORTH);

                ListaPreferiti.add(card);
            }
        }

        ListaPreferiti.revalidate();
        ListaPreferiti.repaint();
    }
}
