package gui;

import javax.swing.*;

public class dialogFiltri extends JDialog {
    private JPanel mainPanel;

    public dialogFiltri(JFrame padre){
        super(padre, "Filtri ristoranti", true);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(padre);
    }
}
