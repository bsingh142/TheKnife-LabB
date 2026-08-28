package gui;

import com.mycompany.theknife.clientTK;
import modelli.Ristorante;

import javax.swing.*;
import java.util.Hashtable;
import java.util.List;

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

    public dialogFiltri(JFrame padre, String posUtente) {
        super(padre, "Filtri ristoranti", true);
        this.posUtente = posUtente;

        setContentPane(mainPanel);

        fixSlider();
        fixBottoni();
        fixStelle();
        prezzoMin.setText("1");
        prezzoMax.setText("100");
        fixTipiCucina();

        if (ricercaButton != null) {
            ricercaButton.addActionListener(e -> applicaFiltri());
        }
        if (resetButton != null) {
            resetButton.addActionListener(e -> resetScelte());
        }

        pack();
        setLocationRelativeTo(padre);
    }

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

    public void fixStelle() {
        numeroStelle.addItem("Qualsiasi");
        numeroStelle.addItem(1);
        numeroStelle.addItem(2);
        numeroStelle.addItem(3);
        numeroStelle.addItem(4);
        numeroStelle.addItem(5);
    }

    public void fixTipiCucina() {
        tipiCucina.addItem("Qualsiasi");
        List<String> risultati = clientTK.inviaRichiesta(new String[]{"TIPI_CUCINA"});
        if (risultati != null) {
            for (String s : risultati) tipiCucina.addItem(s);
        }
    }

    public void applicaFiltri() {
        String filtriScelti = "";
        int cont = 0; // Contatore per controllare se l'utente non ha modificato nessun parametro dei filtri

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

        // Se arriva a 6 significa che vuole vedere tutti i ristoranti senza filtrarli
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

        System.out.println("Filtri inviati: " + filtriScelti);
        ristorantiFiltrati = clientTK.inviaRichiesta(new String[]{"RISTORANTI", filtriScelti, posUtente});

        this.dispose();
    }

    public void resetScelte() {
        int risposta = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler cancellare i dati inseriti?",
                "Conferma Annullamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (risposta == JOptionPane.YES_OPTION) {
            raggioRicerca.setValue(4); // Imposta su "Qualsiasi" di default
            prezzoMin.setText("1");
            prezzoMax.setText("100");
            deliveryI.setSelected(true); // Indifferente
            prenotazioneI.setSelected(true); // Indifferente
            numeroStelle.setSelectedIndex(0);
            tipiCucina.setSelectedIndex(0);
        }
    }

    public List<Ristorante> getRistorantiFiltrati() {
        return ristorantiFiltrati;
    }
}