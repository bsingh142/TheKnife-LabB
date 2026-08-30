package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.theknife.clientTK;
import modelli.Ristorante;
import modelli.Utente;

/// Classe per la gestione della finestra che mostra la lista dei ristoranti
public class homePageU extends JFrame {

    private JPanel mainPanel;
    private JLabel labelUsername;
    private JTable tabellaRistoranti;
    private JButton bottoneFiltri;
    private JButton eliminaButton;
    private JButton buttonReset;
    private JButton esciOspiteButton;
    private String posizioneUtente;
    private static String username;
    private Utente u;

    /// @param nomeUtente String username utente
    /// @param pos String posizione utente formato latitudine/longitudine
    /// Si occupa di inizializzare la schermata in base alla tipologia di utente
    public homePageU(String nomeUtente, String pos) {
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file homePageU.form");
        }

        // 1. Spaziatura perimetrale pulita per la dashboard
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // bottone esci nascosto per utenti loggati
        if (esciOspiteButton != null) {
            esciOspiteButton.setVisible(false);
        }

        // Impostiamo l'operazione di chiusura manuale
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // click sulla X della finestra
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confermaUscita();
            }
        });

        this.posizioneUtente = pos;
        setContentPane(mainPanel);
        username = nomeUtente;

        if (username != null) {
            u = clientTK.inviaRichiesta(new String[]{"GET_UTENTE", username});
        }

        String[] colonne = {"Id Ristorante", "Nome", "Indirizzo", "Città", "Nazione", "Latitudine", "Longitudine", "Fascia prezzo", "Delivery", "Prenotazioni Online", "Tipo di cucina"};
        tabellaRistoranti.setModel(new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tabellaRistoranti.getTableHeader().setReorderingAllowed(false);
        tabellaRistoranti.getTableHeader().setResizingAllowed(false);

        // 2. Styling avanzato della Tabella Ristoranti
        tabellaRistoranti.setRowHeight(28);
        tabellaRistoranti.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabellaRistoranti.setSelectionBackground(new Color(232, 240, 254)); // Evidenziazione azzurra
        tabellaRistoranti.setSelectionForeground(Color.BLACK);

        tabellaRistoranti.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabellaRistoranti.getTableHeader().setBackground(new Color(240, 243, 244));
        tabellaRistoranti.getTableHeader().setForeground(new Color(44, 62, 80));
        tabellaRistoranti.setGridColor(new Color(225, 230, 235));
        tabellaRistoranti.setShowGrid(true);

        if(username!=null && u.getRuolo().equals("Ristoratore")){
            riempiTabella(tabellaRistoranti,new String[]{"PROPRIETARIO",u.getUsername()});
            bottoneFiltri.setVisible(false);
            buttonReset.setText("Aggiungi ristorante");
        }else{
            riempiTabella(tabellaRistoranti, new String[]{"RISTORANTI", "TUTTI"});
        }

        tabellaRistoranti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabellaRistoranti.getSelectedRow();
                    if (row != -1) {
                        int idRistorante = ((Number) tabellaRistoranti.getValueAt(row, 0)).intValue();
                        String nomeRistorante = (String) tabellaRistoranti.getValueAt(row, 1);

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

        // 3. Styling dei pulsanti della Top Bar
        Font topBtnFont = new Font("SansSerif", Font.BOLD, 12);

        if (buttonReset != null) {
            buttonReset.setFont(topBtnFont);
            buttonReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        if (bottoneFiltri != null && bottoneFiltri.isVisible()) {
            bottoneFiltri.setFont(topBtnFont);
            bottoneFiltri.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        if (eliminaButton != null) {
            eliminaButton.setFont(topBtnFont);
            eliminaButton.setForeground(new Color(192, 57, 43)); // Rosso per l'eliminazione
            eliminaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        if (esciOspiteButton != null) {
            esciOspiteButton.setFont(topBtnFont);
            esciOspiteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        if (nomeUtente == null) {
            labelUsername.setVisible(false);
            if (esciOspiteButton != null) {
                esciOspiteButton.setVisible(true); // Lo mostra all'ospite
                esciOspiteButton.addActionListener(e -> {
                    this.dispose(); // Chiude la pagina dei ristoranti
                    new Home().setVisible(true); // Torna al menu principale
                });
            }
        } else {
            // Forziamo il 'nowrap' per impedire l'a capo automatico e bloccare gli elementi affiancati
            labelUsername.setText("<html><span style='white-space: nowrap; font-family: sans-serif; font-size: 10pt; color: #2C3E50;'><b>"
                    + nomeUtente + "</b> &nbsp;<span style='font-size: 8pt; color: #555555;'>&#9660;</span></span></html>");
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

            JPopupMenu menuTendina = creaMenu(this, nomeUtente);
            labelUsername.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    menuTendina.show(labelUsername, 0, labelUsername.getHeight());
                }
            });
        }

        bottoneFiltri.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogFiltri dF = new dialogFiltri(homePageU.this, posizioneUtente);
                dF.setVisible(true);

                List<Ristorante> listaR = dF.getRistorantiFiltrati();
                if (listaR != null) {
                    applicaFiltri(tabellaRistoranti, listaR);
                }
            }
        });

        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(buttonReset.getText().equals("Aggiungi ristorante")){
                    inserisci();
                }else{
                    riempiTabella(tabellaRistoranti, new String[]{"RISTORANTI", "TUTTI"});
                    buttonReset.setText("Ripristina filtri");
                }

            }
        });

        // Adatta il layout al form grafico mantenendo la griglia intatta
        pack();
        setLocationRelativeTo(null);

        if (u == null || !u.getRuolo().equals("Ristoratore")) {
            if (eliminaButton != null) {
                eliminaButton.setVisible(false);
            }
        }

        if (eliminaButton != null) {
            eliminaButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Passiamo la Home al form di eliminazione
                    EliminaRistorante er = new EliminaRistorante(username, homePageU.this);
                    er.setVisible(true);
                }
            });
        }
    }

    /// @param parent JFrame genitore
    /// @param nomeUtente String username
    /// @return Il menu a tendina che si apre premendo sul nome utente
    public JPopupMenu creaMenu(JFrame parent, String nomeUtente) {
        JPopupMenu menuTendina = new JPopupMenu();
        JMenuItem logout = new JMenuItem("Logout");

        logout.addActionListener(e -> {
            int conferma = JOptionPane.showConfirmDialog(
                    parent,
                    "Sei sicuro di voler effettuare il logout?",
                    "Conferma Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (conferma == JOptionPane.YES_OPTION) {
                parent.dispose(); // Chiude la homePageU
                new Home().setVisible(true); // Riapre il menu principale
            }
        });

        // Voci esclusive per i Clienti
        if (u != null && u.getRuolo().equals("Cliente")) {
            JMenuItem preferiti = new JMenuItem("I miei ristoranti preferiti");
            JMenuItem recensioni = new JMenuItem("Le mie recensioni");

            preferiti.addActionListener(e -> {
                MieiPreferiti dialog = new MieiPreferiti(parent, nomeUtente);
                dialog.setVisible(true);
            });

            recensioni.addActionListener(e -> {
                MieRecensioni dialog = new MieRecensioni(parent, nomeUtente);
                dialog.setVisible(true);
            });

            menuTendina.add(preferiti);
            menuTendina.add(recensioni);
        }
        // Voci esclusive per i Ristoratori
        /*else if (u != null && u.getRuolo().equals("Ristoratore")) {
            JMenuItem aggiungi = new JMenuItem("Aggiungi ristorante");
            JMenuItem ristoranti = new JMenuItem("I miei ristoranti");

            ristoranti.addActionListener(e -> proprietario(username));
            aggiungi.addActionListener(e -> inserisci());

            menuTendina.add(aggiungi);
            menuTendina.add(ristoranti);
        }*/

        // Il pulsante Logout viene aggiunto per tutti gli utenti loggati
        menuTendina.add(logout);
        return menuTendina;
    }

    /// @param table Jtable la tabella da riempire
    /// @param richiesta String[] stringa che indica quali ristoranti inserire
    /// Riempe la tabella fornita con tutti i ristoranti che corrispondono alla richiesta
    public static void riempiTabella(JTable table, String[] richiesta) {
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        dtm.setRowCount(0);

        List<Ristorante> ristoranti = clientTK.inviaRichiesta(richiesta);

        for (Ristorante r : ristoranti) {
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

    /// @param table JTable la tabella su cui applicare i filtri
    /// @param ristoranti List<Ristorante> i ristoranti con cui popolare la tablella
    /// Popola la tabella fornita con la lista fornita
    public static void applicaFiltri(JTable table, List<Ristorante> ristoranti) {
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        dtm.setRowCount(0);

        for (Ristorante r : ristoranti) {
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

    /// @param username String username dell'utente
    /// Popola la tabella con solo ristoranti posseduti dall'utente
    private void proprietario(String username) {
        List<Ristorante> list = clientTK.inviaRichiesta(new String[]{"PROPRIETARIO", username});
        if (list == null) list = new ArrayList<>();
        applicaFiltri(tabellaRistoranti, list);
        //if (buttonReset != null) buttonReset.setText("Indietro");
    }

    /// Apre la finestra per l'inserimento di un nuovo ristorante
    private void inserisci() {
        // Passiamo la Home al form di aggiunta
        AddRistorante addr = new AddRistorante(username, homePageU.this);
        addr.setVisible(true);
    }

    /// Metodo PUBBLICO per far riaggiornare la tabella dall'esterno
    public void aggiornaVistaProprietario() {
        proprietario(username);
    }

    /// Mostra un messaggio di conferma prima di uscire dall'applicazione
    private void confermaUscita() {
        int risposta = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler uscire?",
                "Conferma Uscita",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (risposta == JOptionPane.YES_OPTION) {
            System.exit(0); // Arresta la JVM e chiude definitivamente il programma
        }
    }
}