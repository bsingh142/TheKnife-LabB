package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import com.mycompany.theknife.clientTK;
import modelli.Ristorante;

public class homePageU extends JFrame{

    private JPanel mainPanel;
    private JLabel labelUsername;
    private JTable tabellaRistoranti;
    private JButton bottoneFiltri;
    private String posizioneUtente; //La stringa è salvata come latitudine/longitudine

    public homePageU(String nomeUtente,String pos){
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file homePageU.form");
        }

        this.posizioneUtente=pos;
        setContentPane(mainPanel);

        String[] colonne={"Id Ristorante","Nome","Indirizzo","Città","Nazione","Latitudine","Longitudine","Fascia prezzo","Delivery","Prenotazioni Online","Tipo di cucina"};
        tabellaRistoranti.setModel(new DefaultTableModel(colonne,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tabellaRistoranti.getTableHeader().setReorderingAllowed(false);
        tabellaRistoranti.getTableHeader().setResizingAllowed(false);

        riempiTabella(tabellaRistoranti);

        if(nomeUtente==null){
            labelUsername.setVisible(false);
        }else{
            labelUsername.setText(nomeUtente);
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
        }

        bottoneFiltri.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new dialogFiltri(homePageU.this).setVisible(true);
            }
        });

        pack();
        setLocationRelativeTo(null);
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

    public static void riempiTabella(JTable table){
        DefaultTableModel dtm=(DefaultTableModel) table.getModel();
        dtm.setRowCount(0);

        String[] richiesta={"RISTORANTI","TUTTI"};
        List<Ristorante> ristoranti=clientTK.inviaRichiesta(richiesta);

        for(Ristorante r:ristoranti){
            dtm.addRow(new Object[]{
                    r.getId(),
                    r.getNome(),
                    r.getIndirizzo(),
                    r.getCitta(),
                    r.getNazione(),
                    r.getLatitudine(),
                    r.getLongitudine(),
                    r.getFasciaPrezzo(),
                    r.isDelivery(),
                    r.isPrenotazioneOnline(),
                    r.getTipoCucina()
            });
        }

    }

}

