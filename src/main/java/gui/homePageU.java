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

        riempiTabella(tabellaRistoranti,new String[]{"RISTORANTI","TUTTI"});

        tabellaRistoranti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Rileva il doppio click
                    int row = tabellaRistoranti.getSelectedRow();
                    if (row != -1) {
                        int idRistorante = ((Number) tabellaRistoranti.getValueAt(row, 0)).intValue();
                        String nomeRistorante = (String) tabellaRistoranti.getValueAt(row, 1);

                        // Apre il JDialog recensioniClient passandogli i dati del ristorante selezionato
                        recensioniClient dialog = new recensioniClient(
                                homePageU.this,
                                idRistorante,
                                nomeRistorante,
                                nomeUtente
                        );
                        dialog.setVisible(true);
                    }
                }
            }
        });

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

            JPopupMenu menuTendina=creaMenu(this, nomeUtente);
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
                dialogFiltri dF=new dialogFiltri(homePageU.this,posizioneUtente);
                dF.setVisible(true);

                List<Ristorante> listaR=dF.getRistorantiFiltrati();
                if(listaR!=null){
                    applicaFiltri(tabellaRistoranti,listaR);
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    public static JPopupMenu creaMenu(JFrame parent, String nomeUtente){
        JPopupMenu menuTendina=new JPopupMenu();
        JMenuItem preferiti=new JMenuItem("I miei ristoranti preferiti");
        JMenuItem recensioni=new JMenuItem("Le mie recensioni");
        JMenuItem logout=new JMenuItem("Logout");

        preferiti.addActionListener(e->{
            System.out.println("PREFERITI");
        });
        recensioni.addActionListener(e->{
            MieRecensioni dialog = new MieRecensioni(parent, nomeUtente);
            dialog.setVisible(true);
        });
        logout.addActionListener(e->{
            System.out.println("LOGOUT");
        });
        menuTendina.add(preferiti);
        menuTendina.add(recensioni);
        menuTendina.add(logout);

        return menuTendina;
    }

    public static void riempiTabella(JTable table,String[] richiesta){
        DefaultTableModel dtm=(DefaultTableModel) table.getModel();
        dtm.setRowCount(0);

        //String[] richiesta={"RISTORANTI","TUTTI"};
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

    public static void applicaFiltri(JTable table,List<Ristorante> ristoranti){
        DefaultTableModel dtm=(DefaultTableModel) table.getModel();
        dtm.setRowCount(0);

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

