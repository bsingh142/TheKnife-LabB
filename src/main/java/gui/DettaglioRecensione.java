package gui;

import com.mycompany.theknife.clientTK;
import modelli.Recensione;

import javax.swing.*;

public class DettaglioRecensione extends JDialog {
    private JPanel mainPanel;
    private JLabel lblRistorante;
    private JComboBox comboStelle;
    private JTextArea txtTesto;
    private JButton btnSalva;
    private JButton btnElimina;

    private final Recensione recensione;
    private final String nomeUtente;

    public DettaglioRecensione(JFrame parent, Recensione recensione, String nomeUtente) {
        super(parent, "Modifica recensione", true);
        this.recensione = recensione;
        this.nomeUtente = nomeUtente;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file DettaglioRecensione.form");
        }

        setContentPane(mainPanel);
        setSize(450, 400);
        setLocationRelativeTo(parent);

        lblRistorante.setText("Ristorante ID: " + recensione.getRistoranteId());

        if (comboStelle.getItemCount() == 0) {
            comboStelle.setModel(new DefaultComboBoxModel<>(new Integer[]{1, 2, 3, 4, 5}));
        }
        comboStelle.setSelectedItem(recensione.getStelle());
        txtTesto.setText(recensione.getTesto());

        btnSalva.addActionListener(e -> salvaModifiche());
        btnElimina.addActionListener(e -> eliminaRecensione());
    }

    private void salvaModifiche() {
        String testo = txtTesto.getText().trim();
        if (testo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il testo non può essere vuoto.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int stelle = (Integer) comboStelle.getSelectedItem();

        String[] pacchetto = {
                "MODIFICA_RECENSIONE",
                String.valueOf(recensione.getIdRecensione()),
                nomeUtente,
                String.valueOf(stelle),
                testo
        };

        String risposta = clientTK.inviaRichiesta(pacchetto);
        if (risposta != null && risposta.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, "Recensione modificata con successo!", "Esito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminaRecensione() {
        int conferma = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler eliminare questa recensione?",
                "Conferma Eliminazione",
                JOptionPane.YES_NO_OPTION);

        if (conferma != JOptionPane.YES_OPTION) return;

        String[] pacchetto = {
                "ELIMINA_RECENSIONE",
                String.valueOf(recensione.getIdRecensione()),
                nomeUtente
        };

        String risposta = clientTK.inviaRichiesta(pacchetto);
        if (risposta != null && risposta.startsWith("OK")) {
            JOptionPane.showMessageDialog(this, "Recensione eliminata con successo!", "Esito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, risposta, "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}
