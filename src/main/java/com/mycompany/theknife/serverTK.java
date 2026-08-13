/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.theknife;

import java.io.Console;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

/**
 *
 * @author Balkaran
 */
public class serverTK {

    // inserimento variabili per le credenziali del DB
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

        public static void main(String[] args) {
            if(!accessoDB()){
                System.out.println("Connessione al DB non riuscita");
                return;
            }

            try(ServerSocket serverSocket=new ServerSocket(5000)) {
                System.out.println("Server avviato, in ascolto sulla porta 5000");
                while(true){
                    // Il server accetta la connessione
                    Socket client = serverSocket.accept();
                    // Avvia uno serverSlave su un Thread separato
                    serverSlave slave = new serverSlave(client, dbUrl, dbUser, dbPassword);
                    slave.start(); // Il metodo start() esegue il metodo run() in background
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    private static boolean accessoDB() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Inserisci l'host del DB: (Il default è localhost - premi invio) ");
        String host = scanner.nextLine();
        if(host.isEmpty()) host = "localhost"; // Comodità: se premi solo invio, usa il default

        System.out.println("Inserisci la porta del DB: (Il default è 5432 - premi invio) ");
        String porta = scanner.nextLine();
        if(porta.isEmpty()) porta = "5432";

        System.out.println("Inserisci il nome del DB: ");
        String nomeDB = scanner.nextLine();

        System.out.println("Inserisci lo username: (Il default è postgres - premi invio)");
        String usernameDB = scanner.nextLine();
        if(usernameDB.isEmpty()) usernameDB = "postgres";
        System.out.println("Inserisci la password per il DB: ");
        String pwd = scanner.nextLine();

        // Salviamo i dati nelle nostre variabili statiche per usarli dopo
        dbUrl = "jdbc:postgresql://" + host + ":" + porta + "/" + nomeDB;
        dbUser = usernameDB;
        dbPassword = pwd;

        // Facciamo un test veloce di connessione
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            System.out.println("Connessione al Database stabilita con successo!");
            return true;
        } catch (Exception e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
            return false;
        }
    }
}
