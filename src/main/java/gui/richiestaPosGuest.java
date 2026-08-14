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

        invioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] richiesta={"POSIZIONE",nomeCitta.getText(),nomeNazione.getText()};
                String rispServer=clientTK.inviaRichiesta(richiesta);
                System.out.println(rispServer);
            }
        });

        pack();
        setLocationRelativeTo(padre);
    }
}
