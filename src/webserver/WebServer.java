package src.webserver;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.Inet4Address;


public class WebServer implements Runnable{
    
    static final File rootFile = new File("src/serverFiles");
    static final String indexFile = "index.html";
    static final String file404 = "404.html";
    static final int PORT = 2428;   
    private Socket socket;
    private static int clientCount = 0;
    
    public WebServer(Socket c){
        socket = c;
    }
    
    public static void main(String[] args){
        try{
            ServerSocket serverSocket = new ServerSocket(PORT);            
            System.out.println("Server Started.");
            String serverIP = Inet4Address.getLocalHost().toString();
            serverIP = serverIP.split("/")[1];
            System.out.println("Server IP Address: "+serverIP);
            System.out.println("Server Port No.: " + PORT);  
            Thread thread;
            while (true && clientCount < 4){
                WebServer myServer = new WebServer(serverSocket.accept());                
                thread = new Thread(myServer);
                thread.start();             
                clientCount++;
                System.out.println("Client Request: " +clientCount);
            }
            serverSocket.close();
        }
        catch (Exception e){
            System.out.println("Server Connection Error :- " + e.getMessage());
        }     
    }    
    
    public void run() {
        
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;    
        try {            
            in = new BufferedReader (new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());            
            String input = in.readLine();
            StringTokenizer parse = new StringTokenizer(input);            
            fileRequested = parse.nextToken().toLowerCase();                   
            if(fileRequested.endsWith("/"))
                fileRequested += indexFile;
            File file = new File(rootFile, fileRequested);
            int fileLength = (int) file.length();
            String fileType = getcontentType(fileRequested);
            byte[] fileData = getFileData(file, fileLength);
            out.println("HTTP/1.0 200 OK");
            out.println("Date: " + new Date());
            out.println("Content-type: " + fileType);
            out.println("File Length: " + fileLength);
            out.println();
            out.flush();                    
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();         
        }
        catch (FileNotFoundException e){
            try {                
                fileNotFound(out, dataOut, fileRequested);                
            }
            catch (IOException ioe) {
                System.out.println("Error in File Not Found Exception: " +ioe.getMessage());
            } 
        }
        catch (IOException e){
            System.out.println("Server Error: " + e.getMessage());
        }    
        finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                socket.close();
                System.out.println("Connection Closed.");                
            }
            catch (IOException ioe){
                System.out.println("Error in Closing File: " +ioe.getMessage());
            }
        }
    }
    
    private byte[] getFileData(File file, int fileLength) throws IOException{
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];       
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);           
        }
        finally {
            if (fileIn != null)
                fileIn.close();
        }    
        return fileData;
    }
    
    private String getcontentType(String  fileRequested){
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }
    
    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException{
        File file = new File(rootFile, file404);
        int fileLength = (int) file.length();
        String fileType = "text/html";                
        byte[] fileData = getFileData(file, fileLength);
        out.println("HTTP/1.0 404 File Not Found.");
        out.println("Date: " + new Date());
        out.println("Content-type: " + fileType);
        out.println("File Name: " + fileRequested);
        out.println("File Length: " + fileLength);
        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }    
}