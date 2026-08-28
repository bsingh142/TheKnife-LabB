package gui;

import com.mycompany.theknife.clientTK;

import javax.swing.*;

public class DettaglioPreferiti extends JDialog{
    private JButton BottonRimuovi;
    private JLabel TestoRimuovi;
    private JPanel PanelDP;

    private final int idRistorante;
    private final String nomeUtente;

    public DettaglioPreferiti(JDialog parent, int idRistorante, String nomeRistorante, String nomeUtente) {
        super(parent, "Elimina ristorante preferito", true);
        this.idRistorante = idRistorante;
        this.nomeUtente = nomeUtente;

        setContentPane(PanelDP);
        setSize(400, 250);
        setLocationRelativeTo(parent);


        BottonRimuovi.addActionListener(e -> rimuoviDaPreferiti());
    }

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
