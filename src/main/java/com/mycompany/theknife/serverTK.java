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

    public static void main(String[] args) {
        if(!accessoDB()){
            System.out.println("Connessione al DB non riuscita");
            return;
        }
        
        try(ServerSocket serverSocket=new ServerSocket(5000)) {
            System.out.println("Server avviato");
            
            while(true){
                Socket client=serverSocket.accept();
                new serverSlave(client);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean accessoDB() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Inserisci l'host del DB: (Il default è localhost) ");
        String host = scanner.nextLine();

        System.out.println("Inserisci la porta del DB: (Il default è 5432) ");
        String porta = scanner.nextLine();

        System.out.println("Inserisci il nome del DB: ");
        String nomeDB = scanner.nextLine();

        System.out.println("Inserisci lo username: (Il default è postgres)");
        String usernameDB = scanner.nextLine();

        System.out.println("Inserisci la password per il DB: ");
        String pwd = scanner.nextLine();

        String url = "jdbc:postgresql://" + host + ":" + porta + "/" + nomeDB;

        try (Connection conn = DriverManager.getConnection(url, usernameDB, pwd)) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
