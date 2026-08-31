package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mycompany.theknife.clientTK;
import modelli.Ristorante;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Hashtable;
import java.util.List;

/// Classe che si occupa della gestione della finestra per l' inserimento dei filtri di ricerca
public class dialogFiltri extends JDialog {
    private JPanel mainPanel;
    private JButton ricercaButton;
    private JButton resetButton;
    private JSlider raggioRicerca;
    private JTextField prezzoMin;
    private JTextField prezzoMax;
    private JRadioButton deliveryN;
    private JRadioButton prenotazioneN;
    private JRadioButton prenotazioneS;
    private JRadioButton deliveryS;
    private JComboBox numeroStelle;
    private JRadioButton deliveryI;
    private JRadioButton prenotazioneI;
    private JComboBox tipiCucina;

    private String posUtente;
    private List<Ristorante> ristorantiFiltrati;

    /// @param padre     JFrame padre
    /// @param posUtente String posizione utente formato latitudine/longitudine
    public dialogFiltri(JFrame padre, String posUtente) {
        super(padre, "Filtri ristoranti", true);
        this.posUtente = posUtente;

        setContentPane(mainPanel);

        // 1. Spaziatura perimetrale della finestra
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        fixSlider();
        fixBottoni();
        fixStelle();
        prezzoMin.setText("1");
        prezzoMax.setText("100");
        fixTipiCucina();

        // 2. Styling dei componenti (Bordi, Font e Cursori)
        Font baseFont = new Font("SansSerif", Font.PLAIN, 12);
        Font btnFont = new Font("SansSerif", Font.BOLD, 12);
        CompoundBorder textStyle = new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        );

        if (prezzoMin != null) {
            prezzoMin.setFont(baseFont);
            prezzoMin.setBorder(textStyle);
        }
        if (prezzoMax != null) {
            prezzoMax.setFont(baseFont);
            prezzoMax.setBorder(textStyle);
        }

        if (numeroStelle != null) {
            numeroStelle.setFont(baseFont);
            numeroStelle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        if (tipiCucina != null) {
            tipiCucina.setFont(baseFont);
            tipiCucina.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        if (raggioRicerca != null) {
            raggioRicerca.setFont(baseFont);
            raggioRicerca.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        // Styling dei Radio Button
        JRadioButton[] radios = {deliveryS, deliveryN, deliveryI, prenotazioneS, prenotazioneN, prenotazioneI};
        for (JRadioButton rb : radios) {
            if (rb != null) {
                rb.setFont(baseFont);
                rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }

        // Styling dei Pulsanti
        if (ricercaButton != null) {
            ricercaButton.setFont(btnFont);
            ricercaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            ricercaButton.addActionListener(e -> applicaFiltri());
        }
        if (resetButton != null) {
            resetButton.setFont(btnFont);
            resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            resetButton.addActionListener(e -> resetScelte());
        }

        pack(); // Compatta gli spazi in automatico
        setLocationRelativeTo(padre);
    }

    /// Inizializza i valori degli slider
    public void fixSlider() {
        raggioRicerca.setMinimum(0);
        raggioRicerca.setMaximum(4);
        raggioRicerca.setValue(4);
        raggioRicerca.setMajorTickSpacing(1);
        raggioRicerca.setPaintTicks(true);
        raggioRicerca.setPaintLabels(true);

        Hashtable<Integer, JLabel> indici = new Hashtable<>();
        indici.put(0, new JLabel("5 km"));
        indici.put(1, new JLabel("10 km"));
        indici.put(2, new JLabel("20 km"));
        indici.put(3, new JLabel("50 km"));
        indici.put(4, new JLabel("Qualsiasi"));
        raggioRicerca.setLabelTable(indici);
    }

    /// Inizializza i bottoni
    public void fixBottoni() {
        ButtonGroup delivery = new ButtonGroup();
        delivery.add(deliveryS);
        delivery.add(deliveryN);
        delivery.add(deliveryI);
        deliveryI.setSelected(true);

        ButtonGroup prenotazioni = new ButtonGroup();
        prenotazioni.add(prenotazioneS);
        prenotazioni.add(prenotazioneN);
        prenotazioni.add(prenotazioneI);
        prenotazioneI.setSelected(true);
    }

    /// Inizializza le stelle
    public void fixStelle() {
        numeroStelle.addItem("Qualsiasi");
        numeroStelle.addItem(1);
        numeroStelle.addItem(2);
        numeroStelle.addItem(3);
        numeroStelle.addItem(4);
        numeroStelle.addItem(5);
    }

    /// Inizializza i tipi di cucina
    public void fixTipiCucina() {
        tipiCucina.addItem("Qualsiasi");
        List<String> risultati = clientTK.inviaRichiesta(new String[]{"TIPI_CUCINA"});
        if (risultati != null) {
            for (String s : risultati) tipiCucina.addItem(s);
        }
    }

    /// Ottine la lista di ristoranti che corrispondono ai filtri applicati dopo aver verificato la validità degli stessi.
    public void applicaFiltri() {
        String filtriScelti = "";
        int cont = 0;

        switch (raggioRicerca.getValue()) {
            case 0:
                filtriScelti = "5/" + posUtente + "=";
                break;
            case 1:
                filtriScelti = "10/" + posUtente + "=";
                break;
            case 2:
                filtriScelti = "20/" + posUtente + "=";
                break;
            case 3:
                filtriScelti = "50/" + posUtente + "=";
                break;
            case 4:
                filtriScelti = "Qualsiasi=";
                cont++;
                break;
        }

        String s1 = prezzoMin.getText().trim();
        String s2 = prezzoMax.getText().trim();

        if (s1.isEmpty() || s2.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Per favore assicurarsi di non aver lasciato vuoti i campi del prezzo",
                    "Fascia prezzo non valida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int pMin = Integer.parseInt(s1);
            int pMax = Integer.parseInt(s2);

            if ((pMin < 0 || pMin > 100) || (pMax < 0 || pMax > 100) || (pMin >= pMax)) {
                JOptionPane.showMessageDialog(this,
                        "Per favore inserire solo numeri compresi tra 1 e 100",
                        "Fascia prezzo non valida",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Per favore inserire solo numeri nei campi della fascia prezzo",
                    "Fascia prezzo non valida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (s1.equals("1") && s2.equals("100")) {
            cont++;
        }

        if (tipiCucina.getSelectedIndex() == 0) cont++;
        if (deliveryI.isSelected() && prenotazioneI.isSelected()) cont += 2;
        if (numeroStelle.getSelectedIndex() == 0) cont++;

        if (cont == 6) {
            ristorantiFiltrati = clientTK.inviaRichiesta(new String[]{"RISTORANTI", "TUTTI"});
            this.dispose();
            return;
        }

        filtriScelti += s1 + "/" + s2 + "=";
        filtriScelti += tipiCucina.getItemAt(tipiCucina.getSelectedIndex()) + "=";

        if (deliveryS.isSelected()) {
            filtriScelti += "true=";
        } else if (deliveryN.isSelected()) {
            filtriScelti += "false=";
        } else {
            filtriScelti += "Qualsiasi=";
        }

        if (prenotazioneS.isSelected()) {
            filtriScelti += "true=";
        } else if (prenotazioneN.isSelected()) {
            filtriScelti += "false=";
        } else {
            filtriScelti += "Qualsiasi=";
        }

        filtriScelti += String.valueOf(numeroStelle.getSelectedIndex());
        ristorantiFiltrati = clientTK.inviaRichiesta(new String[]{"RISTORANTI", filtriScelti, posUtente});
        this.dispose();
    }

    /// Riporta i valori scelti ai loro valori di inizializzazione
    public void resetScelte() {
        int risposta = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler cancellare i dati inseriti?",
                "Conferma Annullamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (risposta == JOptionPane.YES_OPTION) {
            raggioRicerca.setValue(4);
            prezzoMin.setText("1");
            prezzoMax.setText("100");
            deliveryI.setSelected(true);
            prenotazioneI.setSelected(true);
            numeroStelle.setSelectedIndex(0);
            tipiCucina.setSelectedIndex(0);
        }
    }

    /**
     * Metodo get per la lista di ristoranti filtrati
     *
     * @return Lista di ristoranti ottenuta
     */
    public List<Ristorante> getRistorantiFiltrati() {
        return ristorantiFiltrati;
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
        mainPanel.setLayout(new GridLayoutManager(7, 9, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setPreferredSize(new Dimension(800, 400));
        ricercaButton = new JButton();
        ricercaButton.setText("Ricerca");
        mainPanel.add(ricercaButton, new GridConstraints(6, 5, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resetButton = new JButton();
        resetButton.setText("Reset");
        mainPanel.add(resetButton, new GridConstraints(6, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        raggioRicerca = new JSlider();
        mainPanel.add(raggioRicerca, new GridConstraints(0, 2, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Distanza");
        mainPanel.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Fascia di prezzo");
        mainPanel.add(label2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        prezzoMin = new JTextField();
        mainPanel.add(prezzoMin, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Da");
        mainPanel.add(label3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        prezzoMax = new JTextField();
        mainPanel.add(prezzoMax, new GridConstraints(1, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("A");
        mainPanel.add(label4, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Delivery");
        mainPanel.add(label5, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Prenotazione online");
        mainPanel.add(label6, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Media stelle");
        mainPanel.add(label7, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(6, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        deliveryN = new JRadioButton();
        deliveryN.setText("No");
        mainPanel.add(deliveryN, new GridConstraints(3, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        prenotazioneS = new JRadioButton();
        prenotazioneS.setText("Si");
        mainPanel.add(prenotazioneS, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        prenotazioneN = new JRadioButton();
        prenotazioneN.setText("No");
        mainPanel.add(prenotazioneN, new GridConstraints(4, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deliveryS = new JRadioButton();
        deliveryS.setText("Si");
        mainPanel.add(deliveryS, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numeroStelle = new JComboBox();
        mainPanel.add(numeroStelle, new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deliveryI = new JRadioButton();
        deliveryI.setText("Indifferente");
        mainPanel.add(deliveryI, new GridConstraints(3, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        prenotazioneI = new JRadioButton();
        prenotazioneI.setText("Indifferente");
        mainPanel.add(prenotazioneI, new GridConstraints(4, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Tipo di cucina");
        mainPanel.add(label8, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tipiCucina = new JComboBox();
        mainPanel.add(tipiCucina, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}