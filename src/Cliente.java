import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Cliente {

    private void parser(String userInput){
        String[] tokens = userInput.split(" ");

        if(tokens.length==1 && tokens[0].equals("voos")){
            //todo: lista de voos origem->destino
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

    }

    public static void main (String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        VoosList voos = VoosList.deserialize(dis);
        dis.close();
        socket.close();
        System.out.println(voos.toString());
    }
}
