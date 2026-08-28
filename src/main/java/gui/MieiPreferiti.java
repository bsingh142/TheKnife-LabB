package gui;

import com.mycompany.theknife.clientTK;
import modelli.Ristorante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
        setSize(650, 500);
        setLocationRelativeTo(parent);

        String[] colonne = {"Id Ristorante", "Nome", "Città", "Nazione", "Fascia prezzo", "Tipo di cucina"};
        TabellaPreferiti.setModel(new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        TabellaPreferiti.getTableHeader().setReorderingAllowed(false);

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