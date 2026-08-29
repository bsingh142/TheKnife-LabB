package gui;

import javax.swing.*;
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

    public homePageU(String nomeUtente, String pos) {
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file homePageU.form");
        }
        //bottone esci nascosto per utenti loggati
        if (esciOspiteButton != null) {
            esciOspiteButton.setVisible(false);
        }

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

        riempiTabella(tabellaRistoranti, new String[]{"RISTORANTI", "TUTTI"});

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
                riempiTabella(tabellaRistoranti, new String[]{"RISTORANTI", "TUTTI"});
            }
        });

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
        else if (u != null && u.getRuolo().equals("Ristoratore")) {
            JMenuItem aggiungi = new JMenuItem("Aggiungi ristorante");
            JMenuItem ristoranti = new JMenuItem("I miei ristoranti");

            ristoranti.addActionListener(e -> proprietario(username));
            aggiungi.addActionListener(e -> inserisci());

            menuTendina.add(aggiungi);
            menuTendina.add(ristoranti);
        }

        // Il pulsante Logout viene aggiunto per tutti gli utenti loggati
        menuTendina.add(logout);
        return menuTendina;
    }

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

    private void proprietario(String username) {
        List<Ristorante> list = clientTK.inviaRichiesta(new String[]{"PROPRIETARIO", username});
        if (list == null) list = new ArrayList<>();
        applicaFiltri(tabellaRistoranti, list);
    }

    private void inserisci() {
        // Passiamo la Home al form di aggiunta
        AddRistorante addr = new AddRistorante(username, homePageU.this);
        addr.setVisible(true);
    }

    // Metodo PUBBLICO per far riaggiornare la tabella dall'esterno
    public void aggiornaVistaProprietario() {
        proprietario(username);
    }
}