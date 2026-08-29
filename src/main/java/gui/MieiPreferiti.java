package gui;

import com.mycompany.theknife.clientTK;
import modelli.Ristorante;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MieiPreferiti extends JDialog {
    private JPanel panel1;
    private JLabel TitoloPreferiti;
    private JTable TabellaPreferiti;

    private final String nomeUtente;

    public MieiPreferiti(JFrame parent, String nomeUtente) {
        super(parent, "Ristoranti preferiti", true);
        this.nomeUtente = nomeUtente;

        if (panel1 == null) {
            throw new IllegalStateException("Il pannello panel1 non è stato associato correttamente nel file MieiPreferiti.form");
        }

        setContentPane(panel1);

        // 1. Spaziatura perimetrale pulita
        panel1.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 2. Intestazione in HTML elegante
        if (TitoloPreferiti != null) {
            TitoloPreferiti.setText("<html><h2 style='font-family: sans-serif; color: #2C3E50; margin: 0; padding-bottom: 5px;'>I tuoi ristoranti preferiti</h2></html>");
        }

        String[] colonne = {"Id Ristorante", "Nome", "Città", "Nazione", "Fascia prezzo", "Tipo di cucina"};
        TabellaPreferiti.setModel(new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        TabellaPreferiti.getTableHeader().setReorderingAllowed(false);

        // 3. Styling avanzato della Tabella
        TabellaPreferiti.setRowHeight(28);
        TabellaPreferiti.setFont(new Font("SansSerif", Font.PLAIN, 12));
        TabellaPreferiti.setSelectionBackground(new Color(232, 240, 254));
        TabellaPreferiti.setSelectionForeground(Color.BLACK);

        TabellaPreferiti.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        TabellaPreferiti.getTableHeader().setBackground(new Color(240, 243, 244));
        TabellaPreferiti.getTableHeader().setForeground(new Color(44, 62, 80));
        TabellaPreferiti.setGridColor(new Color(225, 230, 235));
        TabellaPreferiti.setShowGrid(true);

        TabellaPreferiti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = TabellaPreferiti.getSelectedRow();
                    if (row != -1) {
                        int idRistorante = ((Number) TabellaPreferiti.getValueAt(row, 0)).intValue();
                        String nomeRistorante = (String) TabellaPreferiti.getValueAt(row, 1);

                        DettaglioPreferiti dettaglio = new DettaglioPreferiti(
                                MieiPreferiti.this, idRistorante, nomeRistorante, nomeUtente
                        );
                        dettaglio.setVisible(true);

                        caricaPreferiti(); // refresh dopo eventuale rimozione
                    }
                }
            }
        });

        caricaPreferiti();

        // Impostiamo una dimensione fissa spaziosa per far respirare la tabella
        setSize(700, 450);
        setLocationRelativeTo(parent);
    }

    private void caricaPreferiti() {
        DefaultTableModel dtm = (DefaultTableModel) TabellaPreferiti.getModel();
        dtm.setRowCount(0);

        List<Ristorante> preferiti = clientTK.inviaRichiesta(new String[]{"GET_PREFERITI", nomeUtente});

        if (preferiti != null) {
            for (Ristorante r : preferiti) {
                dtm.addRow(new Object[]{
                        r.getId(),
                        r.getNome(),
                        r.getCitta(),
                        r.getNazione(),
                        r.getFasciaPrezzo(),
                        r.getTipoCucina()
                });
            }
        }
    }
}