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
            else if(tokens[0].equals("bye")){
                dos.writeUTF("bye");
                return true;
            }
        }else if(tokens.length==2){
            if(tokens[0].equals("encerra")){
                //todo: encerra dia token[1](admin)
            }else if(tokens[0].equals("cancela")){
                //todo: cancela reserva codReserva = token[1] (utilizador)
            }
        }else if(tokens.length==3){
            if(tokens[0].equals("login")){
                //todo: login name = token[1] pass = token[2]
            }else if(tokens[0].equals("registo")){
                //todo: registo name = token[1] pass = token[2]
            }else if(tokens[0].equals("reserva")){
                //todo: reserva cidades(separadas ;) = token[1] datas(separadas ;) = token[2]
            }
        }else if(tokens.length==4 && tokens[0].equals("addvoo")){
            //todo: adiciona voo origem = token[1] destino = token[2] capacidade = token[3]
        }
        return false;

    }

    public static void main (String[] args) throws IOException {

        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        String userInput;
        boolean finish = false;
            while ((userInput = in.readLine()) != null && !finish) {
                try {
                    finish = parser(userInput,dis,dos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        System.out.println("Bye Bye!");

        dis.close();
        dos.close();

        socket.close();
        System.out.println("Bye Bye!");
    }
}
