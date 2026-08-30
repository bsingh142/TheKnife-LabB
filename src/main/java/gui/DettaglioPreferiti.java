package gui;

import com.mycompany.theknife.clientTK;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/// Classe che gestisce la finestra per la rimozione di ristoranti dalla lista preferiti.
public class DettaglioPreferiti extends JDialog {
    private JButton BottonRimuovi;
    private JLabel TestoRimuovi;
    private JPanel PanelDP;

    private final int idRistorante;
    private final String nomeUtente;

    /// @param parent Jdialog padre
    /// @param idRistorante int id del ristorante
    /// @param nomeRistorante String nome del ristorante
    /// @param nomeUtente String username dell' utente
    public DettaglioPreferiti(JDialog parent, int idRistorante, String nomeRistorante, String nomeUtente) {
        super(parent, "Elimina ristorante preferito", true);
        this.idRistorante = idRistorante;
        this.nomeUtente = nomeUtente;

        setContentPane(PanelDP);

        // Spaziatura perimetrale
        PanelDP.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Styling della label con testo centrato HTML
        if (TestoRimuovi != null) {
            TestoRimuovi.setText("<html><div style='text-align: center; font-family: sans-serif; font-size: 11pt; color: #2C3E50;'>" +
                    "Vuoi rimuovere <b>" + nomeRistorante + "</b> dai preferiti?</div></html>");
        }

        // Styling del pulsante di rimozione (rosso)
        if (BottonRimuovi != null) {
            BottonRimuovi.setFont(new Font("SansSerif", Font.BOLD, 12));
            BottonRimuovi.setForeground(new Color(192, 57, 43)); // Rosso per eliminazione
            BottonRimuovi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            BottonRimuovi.addActionListener(e -> rimuoviDaPreferiti());
        }

        pack(); // Adatta le dimensioni automaticamente
        setLocationRelativeTo(parent);
    }

    /// Effetua l' eliminazione del ristorante dai preferiti.
    private void rimuoviDaPreferiti() {
        int conferma = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler rimuovere questo ristorante dai preferiti?",
                "Conferma Rimozione",
                JOptionPane.YES_NO_OPTION);

        if (conferma != JOptionPane.YES_OPTION) return;

        String[] pacchetto = {"RIMUOVI_PREFERITO", nomeUtente, String.valueOf(idRistorante)};
        String risposta = clientTK.inviaRichiesta(pacchetto);

        if (risposta != null) {
            JOptionPane.showMessageDialog(this, risposta, "Esito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}