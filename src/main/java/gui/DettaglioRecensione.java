package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
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

    /// @param parent         JFrame genitore
    /// @param recensione     Recensione da visualizzare
    /// @param nomeUtente     String username utente
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(400, 200));
        lblRistorante = new JLabel();
        lblRistorante.setText("Label");
        mainPanel.add(lblRistorante, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(1, 2, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        comboStelle = new JComboBox();
        mainPanel.add(comboStelle, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtTesto = new JTextArea();
        mainPanel.add(txtTesto, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnSalva = new JButton();
        btnSalva.setText("Salva Modifiche");
        panel1.add(btnSalva, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnElimina = new JButton();
        btnElimina.setText("Elimana Recensione");
        panel1.add(btnElimina, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Stelle:");
        mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}