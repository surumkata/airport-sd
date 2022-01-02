import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Cliente {

/*
    public static Voo parseLine (String userInput) {
        String[] tokens = userInput.split(" ");

        if (tokens[3].equals("null")) tokens[3] = null;

        return new Voo(
                tokens[0],
                Integer.parseInt(tokens[1]),
                Long.parseLong(tokens[2]),
                tokens[3],
                new ArrayList<>(Arrays.asList(tokens).subList(4, tokens.length)));
    }*/

    public static void main (String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        VoosList voos = VoosList.deserialize(dis);
        dis.close();
        socket.close();
        System.out.println(voos.toString());
    }
}
