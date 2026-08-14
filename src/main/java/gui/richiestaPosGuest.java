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
        JOptionPane.showMessageDialog(
                this,
                "Stai accedendo come ospite.\nAlcune funzionalità potrebbero essere limitate.",
                "Accesso Ospite",
                JOptionPane.INFORMATION_MESSAGE
        );
        if(rispServer.startsWith("ERRORE:")){
            //DOMICILIO NON RICONOSCIUTO
            new homePageU(null,null).setVisible(true);
        }else{
            new homePageU(null,rispServer).setVisible(true);
        }
        this.dispose();
    }
}


