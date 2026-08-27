/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.theknife;

import gui.Home;
import modelli.Ristorante;

import javax.swing.SwingUtilities;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Punto di ingresso principale per il Client e gestore della comunicazione di rete.
 */
public class clientTK {

    // Parametri di connessione al Server centralizzati
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;

    /**
     * Metodo main: avvia l'interfaccia grafica iniziale.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Home().setVisible(true);
        });
    }

    /**
     * Apre un Socket, invia una richiesta al server e attende la risposta.
     * Questo metodo può essere usato da qualsiasi finestra della GUI.
     *
     * @param richiesta L'oggetto da inviare (es. l'oggetto Utente o l'array "LOGIN")
     * @return La risposta del server (di solito "OK:..." o "ERRORE:...")
     */
    /*public static String inviaRichiesta(Object richiesta) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            // Inizializza il flusso di output
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 1. Invia la richiesta al server
            out.writeObject(richiesta);
            out.flush();

            // 2. Legge la risposta del Server
            Object risposta = in.readObject();

            // 3. Restituisce la risposta alla GUI
            if (risposta instanceof String) {
                return (String) risposta;
            }else{
                return "ERRORE: Risposta del server non riconosciuta.";
            }

        } catch (Exception e) {
            System.err.println("[CLIENT] Errore di rete: " + e.getMessage());
            return "ERRORE: Impossibile connettersi al Server. Assicurati che serverTK sia avviato.";
        }
    }*/

    public static <T> T inviaRichiesta(Object richiesta) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            // Inizializza il flusso di output
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 1. Invia la richiesta al server
            out.writeObject(richiesta);
            out.flush();

            // 2. cLegge la risposta del Server
            Object risposta = in.readObject();

            // 3. Restituisce la risposta alla GUI
            T risultato=(T) risposta;
            return risultato;

        } catch (Exception e) {
            System.err.println("[CLIENT] Errore di rete: " + e.getMessage());
            return null;
        }
    }
}