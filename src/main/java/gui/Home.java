package gui;

import javax.swing.*;

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
        // EXIT_ON_CLOSE chiude l'intero programma se si chiude la finestra principale
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Collegamento delle azioni ai pulsanti
        if (accediButton != null) {
            accediButton.addActionListener(e -> apriLogin());
        }

        if (registratiButton != null) {
            registratiButton.addActionListener(e -> apriRegistrazione());
        }

        if (continuaComeOspiteButton != null) {
            continuaComeOspiteButton.addActionListener(e -> continuaComeOspite());
        }

        // Adatta le dimensioni e centra la finestra sullo schermo
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Metodo per aprire la finestra di Login.
     */
    private void apriLogin() {
        // Crea e mostra la finestra di Login
        new Login().setVisible(true);
        this.dispose();
    }

    /**
     * Metodo per aprire la finestra di Registrazione.
     */
    private void apriRegistrazione() {
        // Crea e mostra la finestra di Registrazione
        new Registrazione().setVisible(true);
        this.dispose();
    }

    /**
     * Metodo per gestire l'accesso limitato come ospite.
     */
    private void continuaComeOspite() {
        new richiestaPosGuest(Home.this).setVisible(true);
        //this.dispose();
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