package com.socket;

import com.ui.ChatFrame;
import java.io.*;
import java.net.*;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class SocketClient implements Runnable{
    
    public int port;
    public String serverAddr;
    public Socket socket;
    public ChatFrame ui;
    public ObjectInputStream In;
    public ObjectOutputStream Out;
    
    public SocketClient(ChatFrame frame) throws IOException{
        ui = frame; this.serverAddr = ui.serverAddr; this.port = ui.port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);
            
        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());
        
    }

    @Override
    public void run() {
        boolean keepRunning = true;
        while(keepRunning){
            try {
                Message msg = (Message) In.readObject();
                System.out.println("Incoming : "+msg.toString());
                
                if(msg.type.equals("message")){
                    if(msg.recipient.equals(ui.username)){
                        ui.ChatBox.append("["+msg.sender +" > Me] : " + msg.content + "\n");
                    }
                    else{
                        ui.ChatBox.append("["+ msg.sender +" > "+ msg.recipient +"] : " + msg.content + "\n");
                    }
                }
                else if(msg.type.equals("login")){
                    if(msg.content.equals("TRUE")){
                        ui.Login.setEnabled(false); ui.SignUp.setEnabled(false);                        
                        ui.Send.setEnabled(true); 
                        ui.ChatBox.append("[SERVER > Me] : Login Successful\n");
                        ui.User.setEnabled(false); ui.Passwd.setEnabled(false);
                    }
                    else{
                        ui.ChatBox.append("[SERVER > Me] : Login Failed\n");
                    }
                }
                else if(msg.type.equals("test")){
                    ui.Connect.setEnabled(false);
                    ui.Login.setEnabled(true); ui.SignUp.setEnabled(true);
                    ui.User.setEnabled(true); ui.Passwd.setEnabled(true);
                    ui.Server.setEditable(false); ui.Port.setEditable(false);
                }
                else if(msg.type.equals("newuser")){
                    if(!msg.content.equals(ui.username)){
                        boolean exists = false;
                        for(int i = 0; i < ui.model.getSize(); i++){
                            if(ui.model.getElementAt(i).equals(msg.content)){
                                exists = true; break;
                            }
                        }
                        if(!exists){ ui.model.addElement(msg.content); }
                    }
                }
                else if(msg.type.equals("signup")){
                    if(msg.content.equals("TRUE")){
                        ui.Login.setEnabled(false); ui.SignUp.setEnabled(false);
                        ui.Send.setEnabled(true);
                        ui.ChatBox.append("[SERVER > Me] : Singup Successful\n");
                    }
                    else{
                        ui.ChatBox.append("[SERVER > Me] : Signup Failed\n");
                    }
                }
                else if(msg.type.equals("signout")){
                    if(msg.content.equals(ui.username)){
                        ui.ChatBox.append("["+ msg.sender +" > Me] : Bye\n");
                        ui.Connect.setEnabled(true); ui.Send.setEnabled(false); 
                        ui.Server.setEditable(true); ui.Port.setEditable(true);
                        
                        for(int i = 1; i < ui.model.size(); i++){
                            ui.model.removeElementAt(i);
                        }
                        
                        ui.clientThread.stop();
                    }
                    else{
                        ui.model.removeElement(msg.content);
                        ui.ChatBox.append("["+ msg.sender +" > All] : "+ msg.content +" has signed out\n");
                    }
                }
            }
            catch(Exception ex) {
                keepRunning = false;
                ui.ChatBox.append("[Application > Me] : Connection Failure\n");
                ui.Connect.setEnabled(true); ui.Server.setEditable(true); ui.Port.setEditable(true);
                ui.Send.setEnabled(false);
                for(int i = 1; i < ui.model.size(); i++){
                    ui.model.removeElementAt(i);
                }
                
                ui.clientThread.stop();
                
                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }
    }
    
    public void send(Message msg){
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : "+msg.toString());
        } 
        catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }
    
    public void closeThread(Thread t){
        t = null;
    }
}
