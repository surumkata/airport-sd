import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Objects;

public class Cliente {
    private static boolean logged;

    public Cliente(){
        logged = false;
    }

    public static class Sender{
        private DataOutputStream dos;

        //quit -> 0
        //registo -> 1 + (naoadmin 0,admin 1)
        //login -> 2
        //logout -> 3
        //voos -> 4
        //reservas -> 5
        //reserva -> 6
        //cancela -> 7
        //encerra -> 8
        //addvoo -> 9

        public Sender(DataOutputStream dos){
            this.dos = dos;
        }

        public void sendVoos(){
            try{
                this.dos.writeInt(4); //voos
                this.dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendLogin(String nome, String password){
            try{
                this.dos.writeInt(2); //login
                this.dos.writeUTF(nome);
                this.dos.writeUTF(password);
                this.dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendLogout(){
            try{
                this.dos.writeInt(3); //logout
                this.dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendRegisto(String nome, String password, boolean admin) {
            try{
                this.dos.writeInt(1); //registo
                this.dos.writeBoolean(admin);
                this.dos.writeUTF(nome);
                this.dos.writeUTF(password);
                this.dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendCancela(String numRegisto) {
            try{
                this.dos.writeInt(7); //cancela
                dos.writeInt(Integer.parseInt(numRegisto));
                dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendReservas() {
            try{
                this.dos.writeInt(5); //reservas
                //todo: o cliente poder ver as suas reservas
                //como estará previamente logado aqui é enviado o nome do mesmo
                dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendQuit() {
            try {
                this.dos.writeInt(0); //quit
                dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendReserva(String viagem, String datas) {
            try{
                //todo: reserva cidades(separadas ;) = token[1] datas(separadas ;) = token[2]
                this.dos.writeInt(6); //reserva
                dos.writeUTF(viagem);
                dos.writeUTF(datas);
                dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendAddVoo(String origem, String destino, String capacidade) {
            try{
                this.dos.writeInt(9); //advoo
                dos.writeUTF(origem);
                dos.writeUTF(destino);
                dos.writeUTF(capacidade);
                dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendEncerra(String dia) {
            //todo: encerra dia (admin)
            try{
                this.dos.writeInt(8);
                this.dos.flush();
                dos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static class Receiver{
        private DataInputStream dis;

        public Receiver(DataInputStream dis){
            this.dis = dis;
        }

        public void receiveVoos(){
            try{
                VoosList voos = VoosList.deserialize(dis);
                System.out.println(voos);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public void receiveReservas() throws IOException {
            int size = dis.readInt();
            for(int i=0;i<size;i++){
                System.out.println(dis.readUTF());
            }
        }

        public void receiveMessage(){
            try{
                System.out.println(dis.readUTF());
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public boolean receiveLogin() {
            boolean logged = false;
            try{
                System.out.println(dis.readUTF());
                logged = dis.readBoolean();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            return logged;
        }

        public void receiveRegisto() {
            boolean registado = false;
            try{
                registado = dis.readBoolean();
                if(registado)
                    System.out.println("Registado novo utilizador com sucesso!");
                else
                    System.out.println("Erro ao registar novo utilizador.");
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public void receiveRegistoA() {
            boolean registado = false;
            try{
                registado = dis.readBoolean();
                if(registado)
                    System.out.println("Registado novo admin com sucesso!");
                else
                    System.out.println("Erro ao registar novo admin.");
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private  boolean parser(String userInput,DataInputStream dis,DataOutputStream dos) throws IOException {
        String[] tokens = userInput.split(" ");
        Sender sender = new Sender(dos);
        Receiver receiver = new Receiver(dis);
        if(!logged && !Objects.equals(tokens[0], "login") && !Objects.equals(tokens[0], "registo")&&!Objects.equals(tokens[0], "quit") ){
            System.out.println("Ainda não se encontra loggado");
        }else if(tokens.length==1){
            switch (tokens[0]) {
                case "voos" -> {
                    sender.sendVoos();
                    receiver.receiveVoos();
                }
                case "reservas" -> {
                    sender.sendReservas();
                    receiver.receiveReservas();
                    dos.flush();
                }
                case "logout" -> {
                    sender.sendLogout();
                    logged = false;
                    receiver.receiveMessage();
                }
                case "help" -> System.out.println(comandosDisponiveis());
                case "quit" -> {
                    sender.sendQuit();
                    return true;
                }
            }
        }else if(tokens.length==2){
            if(tokens[0].equals("encerra")){
                sender.sendEncerra(tokens[1]);
            }else if(tokens[0].equals("cancela")){
                sender.sendCancela(tokens[1]);

                System.out.println(dis.readUTF());
                //todo: cancela reserva codReserva = token[1] (utilizador)
            }
        }else if(tokens.length==3){
            switch (tokens[0]) {
                case "login" -> {
                    sender.sendLogin(tokens[1], tokens[2]);
                    logged = receiver.receiveLogin();
                }
                case "registo" -> {
                    sender.sendRegisto(tokens[1], tokens[2], false);
                    receiver.receiveRegisto();
                }
                case "registoA" -> {
                    sender.sendRegisto(tokens[1], tokens[2], true);
                    receiver.receiveRegistoA();
                }
                case "reserva" -> {
                    sender.sendReserva(tokens[1],tokens[2]);
                    receiver.receiveMessage();
                }
            }
        }else if(tokens.length==4 && tokens[0].equals("addvoo")){
            sender.sendAddVoo(tokens[1],tokens[2],tokens[3]);
            receiver.receiveMessage();
        }
        else System.out.println("Comando inexistente!");

        return false;

    }

    public static String comandosDisponiveis(){
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("Comandos Disponíveis\n");
        sb.append("Registar -> registo nome password\n");
        sb.append("Login -> login nome password\n");
        sb.append("Logout -> logout\n");
        //todo: meter todos
        return sb.toString();
    }

    public static void main (String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        Cliente client = new Cliente();

        String userInput;
        boolean finish = false;
            while ((!finish && (userInput = in.readLine()) != null)) {
                try {
                    finish = client.parser(userInput,dis,dos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        dis.close();
        dos.close();
        socket.close();


        System.out.println("Goodbye!");
    }
}
