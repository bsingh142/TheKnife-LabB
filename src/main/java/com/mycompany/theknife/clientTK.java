/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.theknife;

import gui.paginaIniziale;
import java.io.IOException;
import java.net.Socket;
import javax.swing.SwingUtilities;

/**
 *
 * @author Balkaran
 */
public class clientTK {

    public static void main(String[] args) {
        try (Socket s = new Socket("localhost", 5000)) {
            SwingUtilities.invokeLater(() -> {
                new paginaIniziale().setVisible(true);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Connessione non riuscita al server");
        }

    }
}
