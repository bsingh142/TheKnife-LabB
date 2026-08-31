package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import com.mycompany.theknife.clientTK;

/// Gestisce la schermata per l'inserimento della posizione degli utenti guest
public class richiestaPosGuest extends JDialog {
    private JPanel mainPanel;
    private JTextField nomeCitta;
    private JTextField nomeNazione;
    private JButton invioButton;

    // Variabile mantenuta dal branch "modifiche" per chiudere la Home correttamente
    private boolean successo = false;

    /// @param padre J_Frame padre
    public richiestaPosGuest(JFrame padre) {
        super(padre, "Inserisci Posizione", true);

        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file .form");
        }
        setContentPane(mainPanel);

        // 1. Spaziatura perimetrale pulita e proporzioni Dialog
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        setSize(380, 250);
        setResizable(false);

        // 2. Bordo moderno con padding interno per i campi di testo
        CompoundBorder campoTestoStyle = new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );

        if (nomeCitta != null) {
            nomeCitta.setFont(new Font("SansSerif", Font.PLAIN, 12));
            nomeCitta.setBorder(campoTestoStyle);
        }

        if (nomeNazione != null) {
            nomeNazione.setFont(new Font("SansSerif", Font.PLAIN, 12));
            nomeNazione.setBorder(campoTestoStyle);
        }

        // 3. Styling del pulsante Invio
        if (invioButton != null) {
            invioButton.setText("Cerca Ristoranti");
            invioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            invioButton.setFont(new Font("SansSerif", Font.BOLD, 12));
            invioButton.addActionListener(e -> apriHomePageU());
        }

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(padre);
    }

    /// Controlla i valori inseriti e se sono accettabili procede all'apertura
    /// della schermata homepage
    private void apriHomePageU() {
        String citta = (nomeCitta != null) ? nomeCitta.getText().trim() : "";
        String nazione = (nomeNazione != null) ? nomeNazione.getText().trim() : "";

        if (citta.isEmpty() || nazione.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Inserire sia la città che la nazione.",
                    "Attenzione: Dati Mancanti",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String[] richiesta = {"POSIZIONE", citta, nazione};
        String rispServer = clientTK.inviaRichiesta(richiesta);
        System.out.println("[DEBUG] COORDINATE RICEVUTE: " + rispServer);

        if (rispServer == null) {
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

        successo = true; // Impostiamo a true per far chiudere la Home
        new homePageU(null, rispServer).setVisible(true);
        this.dispose();
    }

    /// Metodo getter necessario alla classe Home
    public boolean getSuccesso() {
        return successo;
    }
}