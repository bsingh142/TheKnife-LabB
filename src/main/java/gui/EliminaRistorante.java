package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mycompany.theknife.clientTK;

public class EliminaRistorante extends JFrame {
    private JTextField txtid;
    private JButton elimina;
    private JPanel mainPanel;
    private String username;

    EliminaRistorante(String u){
        username=u;
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente");
        }

        setContentPane(mainPanel);
        setTitle("Eliminazione ristorante - TheKnife");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pack();
        setLocationRelativeTo(null);

        elimina.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               verificaElimazione(txtid.getText());
            }
        });

    }

    private void verificaElimazione(String id){
        if(id == null){
            JOptionPane.showMessageDialog(this, "Id non valido", "Valore non inserito", JOptionPane.ERROR_MESSAGE);
            this.dispose();}
        if(!id.trim().matches("\\d+")){
            JOptionPane.showMessageDialog(this, "Id non valido", "Valore non consentito", JOptionPane.ERROR_MESSAGE);
            this.dispose();}

        Integer i= clientTK.inviaRichiesta(new String[] {"ELIMINA_RISTORANTE", username, id.trim()});
        JOptionPane.showMessageDialog(this, "L' eliminazione ha modificato "+ i + "righe.", "Operazione conclusa", JOptionPane.INFORMATION_MESSAGE);


        }
    }

