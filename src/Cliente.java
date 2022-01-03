import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Cliente {

    private static boolean parser(String userInput,DataInputStream dis, DataOutputStream dos) throws IOException {
        String[] tokens = userInput.split(" ");

        if(tokens.length==1){
            if(tokens[0].equals("voos")) {
                dos.writeUTF("voos");
                dos.flush();
                VoosList voos = VoosList.deserialize(dis);
                System.out.println(voos);
                return false;
            }
            else if(tokens[0].equals("quit")){
                dos.writeUTF("quit");
                dis.close();
                dos.close();
                return true;
            }
        }else if(tokens.length==2){
            if(tokens[0].equals("encerra")){
                //todo: encerra dia token[1](admin)
            }else if(tokens[0].equals("cancela")){
                dos.writeUTF("cancela");
                dos.writeUTF(tokens[1]);
                dos.flush();
                System.out.println(dis.readUTF());
                //todo: cancela reserva codReserva = token[1] (utilizador)
            }
        }else if(tokens.length==3){
            if(tokens[0].equals("login")){
                dos.writeUTF("login");
                dos.writeUTF(tokens[1]);
                dos.writeUTF(tokens[2]);
                dos.flush();
                System.out.println(dis.readUTF());
                return false;
            }else if(tokens[0].equals("registo")){
                dos.writeUTF("registo");
                dos.writeUTF(tokens[1]);
                dos.writeUTF(tokens[2]);
                dos.flush();
                System.out.println(dis.readUTF());
                return false;
            }else if(tokens[0].equals("reserva")){
                //todo: reserva cidades(separadas ;) = token[1] datas(separadas ;) = token[2]
                dos.writeUTF("reserva");
                dos.writeUTF(tokens[2]);
                dos.writeUTF(tokens[3]);
                dos.flush();
            }
        }else if(tokens.length==4 && tokens[0].equals("addvoo")){
            dos.writeUTF("addvoo");
            dos.writeUTF(tokens[1]);
            dos.writeUTF(tokens[2]);
            dos.writeUTF(tokens[3]);
            dos.flush();
            System.out.println(dis.readUTF());
            return false;
        }

        System.out.println("Comando inexistente!");
        return false;

    }

    public static void main (String[] args) throws IOException {

        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        String userInput;
        boolean finish = false;
            while ((!finish && (userInput = in.readLine()) != null)) {
                try {
                    finish = parser(userInput,dis,dos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        socket.close();


        System.out.println("Goodbye!");
    }
}
