package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Schermata principale dell'applicazione (Home Page).
 * Gestisce la navigazione verso le finestre di Login, Registrazione o Accesso Ospite.
 */
public class Home extends JFrame {

    // Contenitore principale (da collegare al file Home.form nel GUI designer)
    private JPanel mainPanel;

    // Componenti dell'interfaccia grafica
    private JButton accediButton;
    private JButton registratiButton;
    private JButton continuaComeOspiteButton;

    /**
     * Costruttore della classe Home.
     */
    public Home() {
        // Controllo di sicurezza per il collegamento con il file .form
        if (mainPanel == null) {
            throw new IllegalStateException("Il mainPanel non è stato associato correttamente nel file Home.form");
        }

        // Impostazioni base della finestra
        setContentPane(mainPanel);
        setTitle("The Knife - Menu Principale");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Styling grafico: margini interni e dimensioni fisse proporzionate
        mainPanel.setBorder(new EmptyBorder(25, 35, 25, 35));
        setSize(420, 360);
        setResizable(false);
        setLocationRelativeTo(null);

        // Font e cursori dinamici per i pulsanti
        Font btnFont = new Font("SansSerif", Font.BOLD, 13);

        // Collegamento delle azioni e dello stile ai pulsanti
        if (accediButton != null) {
            accediButton.setFont(btnFont);
            accediButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            accediButton.addActionListener(e -> apriLogin());
        }

        if (registratiButton != null) {
            registratiButton.setFont(btnFont);
            registratiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            registratiButton.addActionListener(e -> apriRegistrazione());
        }

        if (continuaComeOspiteButton != null) {
            continuaComeOspiteButton.setFont(btnFont);
            continuaComeOspiteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            continuaComeOspiteButton.addActionListener(e -> continuaComeOspite());
        }
    }

    /**
     * Metodo per aprire la finestra di Login.
     */
    private void apriLogin() {
        new Login().setVisible(true);
        this.dispose();
    }

    /**
     * Metodo per aprire la finestra di Registrazione.
     */
    private void apriRegistrazione() {
        new Registrazione().setVisible(true);
        this.dispose();
    }

    /**
     * Metodo per gestire l'accesso limitato come ospite.
     */
    private void continuaComeOspite() {
        richiestaPosGuest dialog = new richiestaPosGuest(Home.this);
        dialog.setVisible(true);
        if (dialog.getSuccesso()) {
            this.dispose(); // Chiude definitivamente la Home solo in caso di esito positivo
        }
    }

    /**
     * Metodo main per testare la finestra direttamente.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Home().setVisible(true);
        });
    }
}