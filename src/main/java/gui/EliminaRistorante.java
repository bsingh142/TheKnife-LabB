package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mycompany.theknife.clientTK;

public class EliminaRistorante extends JFrame {
    private JTextField txtid;
    private JButton elimina;
    private JPanel mainPanel;

    private String username;
    private homePageU homeParent; // Riferimento alla Home

    public EliminaRistorante(String u, homePageU parent){
        this.username = u;
        this.homeParent = parent;

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente");
        }

        setContentPane(mainPanel);
        setTitle("Eliminazione ristorante - TheKnife");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. Spaziatura perimetrale pulita per non far toccare gli elementi ai bordi
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // 2. Bordo moderno con padding interno per il campo ID
        CompoundBorder campoTestoStyle = new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );

        if (txtid != null) {
            txtid.setFont(new Font("SansSerif", Font.PLAIN, 12));
            txtid.setBorder(campoTestoStyle);
        }

        // 3. Styling del pulsante (Azione distruttiva = Testo Rosso)
        if (elimina != null) {
            elimina.setFont(new Font("SansSerif", Font.BOLD, 12));
            elimina.setForeground(new Color(192, 57, 43)); // Rosso elegante per richiamare attenzione
            elimina.setCursor(new Cursor(Cursor.HAND_CURSOR));
            elimina.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    verificaElimazione(txtid.getText());
                }
            });
        }

        // Adatta la finestra ai nuovi margini senza deformare la griglia
        pack();
        setLocationRelativeTo(parent);
    }

    private void verificaElimazione(String id){
        if(id == null || id.trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "Inserisci un ID.", "Valore non inserito", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!id.trim().matches("\\d+")){
            JOptionPane.showMessageDialog(this, "Id non valido. Inserisci solo numeri.", "Valore non consentito", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer i = clientTK.inviaRichiesta(new String[] {"ELIMINA_RISTORANTE", username, id.trim()});

        if (i != null && i > 0) {
            JOptionPane.showMessageDialog(this, "Ristorante eliminato con successo.", "Operazione conclusa", JOptionPane.INFORMATION_MESSAGE);

            // AGGIORNIAMO LA TABELLA DELLA HOME!
            if (homeParent != null) {
                homeParent.aggiornaVistaProprietario();
            }

            this.dispose(); // Chiudiamo la finestrella
        } else {
            JOptionPane.showMessageDialog(this, "Impossibile eliminare. Verifica l'ID o i permessi.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}