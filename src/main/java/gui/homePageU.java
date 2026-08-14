package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.mycompany.theknife.clientTK;

public class homePageU extends JFrame{

    private JPanel mainPanel;
    private JLabel labelUsername;
    private JTable tabellaRistoranti;
    private JButton bottoneFiltri;

    public homePageU(){
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file homePageU.form");
        }

        setContentPane(mainPanel);

        String[] colonne={"Nome","Indirizzo","Città","Nazione","Latitudine","Longitudine","Fascia prezzo","Delivery","Prenotazioni Online","Tipo di cucina"};
        tabellaRistoranti.setModel(new DefaultTableModel(colonne,0));

        labelUsername.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                labelUsername.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        labelUsername.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                labelUsername.setCursor(new Cursor(Cursor.getDefaultCursor().getType()));
            }
        });

        JPopupMenu menuTendina=creaMenu();

        labelUsername.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                menuTendina.show(labelUsername,0,labelUsername.getHeight());
            }
        });

        bottoneFiltri.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new dialogFiltri(homePageU.this).setVisible(true);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new homePageU().setVisible(true);
        });
    }

    public static JPopupMenu creaMenu(){
        JPopupMenu menuTendina=new JPopupMenu();
        JMenuItem preferiti=new JMenuItem("I miei ristoranti preferiti");
        JMenuItem recensioni=new JMenuItem("Le mie recensioni");
        JMenuItem logout=new JMenuItem("Logout");

        preferiti.addActionListener(e->{
            System.out.println("PREFERITI");
        });
        recensioni.addActionListener(e->{
            System.out.println("RECENSIONI");
        });
        logout.addActionListener(e->{
            System.out.println("LOGOUT");
        });
        menuTendina.add(preferiti);
        menuTendina.add(recensioni);
        menuTendina.add(logout);

        return menuTendina;
    }

    public static void selezioneRistoranti(){
        String[] richiesta={"RISTORANTI"};
        String rispServer= clientTK.inviaRichiesta(richiesta);
    }

}

