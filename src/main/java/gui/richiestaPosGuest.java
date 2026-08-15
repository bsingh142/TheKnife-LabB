package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mycompany.theknife.clientTK;

public class richiestaPosGuest extends JDialog{
    private JPanel mainPanel;
    private JTextField nomeCitta;
    private JTextField nomeNazione;
    private JButton invioButton;

    public richiestaPosGuest(JFrame padre){
        super(padre,"Inserisci posizione",true);
        setContentPane(mainPanel);

        if(invioButton!=null){
            invioButton.addActionListener(e->apriHomePageU());
        }

        pack();
        setLocationRelativeTo(padre);
    }

    private void apriHomePageU(){
        String citta=nomeCitta.getText();
        String nazione=nomeNazione.getText();

        if (citta.isEmpty() || nazione.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Inserire sia la città che la nazione.",
                    "Attenzione: Dati Mancanti",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        String[] richiesta={"POSIZIONE",citta,nazione};
        String rispServer=clientTK.inviaRichiesta(richiesta);
        System.out.println("[DEBUG] COORDINATE RICEVUTE: " + rispServer);
        if(rispServer==null){
            JOptionPane.showMessageDialog(
                    this,
                    "Domicilio non riconosciuto",
                    "Accesso Ospite",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        JOptionPane.showMessageDialog(
                this,
                "Stai accedendo come ospite.\nAlcune funzionalità potrebbero essere limitate.",
                "Accesso Ospite",
                JOptionPane.INFORMATION_MESSAGE
        );
        new homePageU(null,rispServer).setVisible(true);
        this.dispose();
    }
}


