package gui;

import com.mycompany.theknife.clientTK;
import modelli.Recensione;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/// Permette ad un utente di visualizzare, modificare, eliminare una recensione da lui pubblicata.
public class DettaglioRecensione extends JDialog {
    private JPanel mainPanel;
    private JLabel lblRistorante;
    private JComboBox comboStelle;
    private JTextArea txtTesto;
    private JButton btnSalva;
    private JButton btnElimina;

    private final Recensione recensione;
    private final String nomeUtente;

    /// @param parent JFrame genitore
    /// @param recensione Recensione da visualizzare
    /// @param nomeUtente String username utente
    /// @param nomeRistorante String nome ristorante
    public DettaglioRecensione(JFrame parent, Recensione recensione, String nomeUtente, String nomeRistorante) {
        super(parent, "Modifica recensione", true);
        this.recensione = recensione;
        this.nomeUtente = nomeUtente;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file DettaglioRecensione.form");
        }

        setContentPane(mainPanel);
        setSize(480, 380);
        setLocationRelativeTo(parent);

        // 1. Spaziatura globale della finestra (padding interno di 15px)
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 2. Intestazione in HTML elegante
        lblRistorante.setText("<html><span style='font-family: sans-serif; font-size: 12pt; color: #2C3E50;'>Recensione per: <b>"
                + nomeRistorante + "</b></span></html>");

        // 3. Popolamento e stile Stelle
        if (comboStelle.getItemCount() == 0) {
            comboStelle.setModel(new DefaultComboBoxModel<>(new Integer[]{1, 2, 3, 4, 5}));
        }
        comboStelle.setSelectedItem(recensione.getStelle());
        comboStelle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 4. Stile e bordo pulito per il campo di testo
        txtTesto.setText(recensione.getTesto());
        txtTesto.setLineWrap(true);
        txtTesto.setWrapStyleWord(true);
        txtTesto.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtTesto.setBorder(new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        // 5. Fix refuso pulsante "Elimana" -> "Elimina" e stili visivi
        btnSalva.setText("Salva Modifiche");
        btnSalva.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnElimina.setText("Elimina Recensione");
        btnElimina.setForeground(new Color(192, 57, 43)); // Rosso elegante per azione distruttiva
        btnElimina.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSalva.addActionListener(e -> salvaModifiche());
        btnElimina.addActionListener(e -> eliminaRecensione());
    }

    /// Consente la modifica del testo della recensione.
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

    /// Consente di eliminare la recensione.
    private void eliminaRecensione() {
        int conferma = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler eliminare questa recensione?",
                "Conferma Eliminazione",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

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