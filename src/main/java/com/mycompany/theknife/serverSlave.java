/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.theknife;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Balkaran
 */
public class serverSlave extends Thread{
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    
    public serverSlave(Socket s){
        this.socket=s;
        try {
            this.input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output=new PrintWriter(socket.getOutputStream(),true);
            this.run();
        } catch (IOException ex) {
            System.getLogger(serverSlave.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    @Override
    public void run(){
        
    }
}
