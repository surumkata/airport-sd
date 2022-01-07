import java.io.*;
import java.net.Socket;

public class Cliente {

    private static boolean logged = false;
    private static boolean admin = false;

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

        public void sendAddVoo(String origem, String destino, int capacidade) {
            try{
                this.dos.writeInt(9); //advoo
                dos.writeUTF(origem);
                dos.writeUTF(destino);
                dos.writeInt(capacidade);
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

        public boolean receiveBoolean(){
            try{
                return dis.readBoolean();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return false;
        }

        public void receiveRegisto() {
            try{
                if(dis.readBoolean()){
                    System.out.println("Registado novo utilizador com sucesso!\n");
                }
                else
                    System.out.println("Erro ao registar novo utilizador. Experimente outro nome de utilizador.\n");
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public void receiveRegistoA() {
            try{
                boolean registado = dis.readBoolean();
                if(registado)
                    System.out.println("Registado novo admin com sucesso!\n");
                else
                    System.out.println("Erro ao registar novo admin. Experimente outro nome de utilizador.\n");
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public int receiveAddVoo() {
            int id = -1;
            try{
                id = dis.readInt();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            return id;
        }
    }

    private  boolean commands(DataInputStream dis, DataOutputStream dos) throws IOException {
        Sender sender = new Sender(dos);
        Receiver receiver = new Receiver(dis);
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        boolean finish = false;
        if (!admin) {
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("VooManager\n");
            sb.append("1) Ver voos.\n");
            sb.append("2) Ver reservas\n");
            sb.append("3) Fazer reserva\n");
            sb.append("4) Cancelar reserva\n");
            sb.append("5) Logout\n");
            sb.append("6) Sair\n");
            sb.append("\n");
            sb.append("Insira a opção: ");
            System.out.println(sb);
            String option = stdin.readLine();

            switch (option) {
                case "1" -> {
                    sender.sendVoos();
                    receiver.receiveVoos();
                }
                case "2" -> {
                    sender.sendReservas();
                    receiver.receiveReservas();
                }
                case "3" -> {
                    System.out.println("Escreva o percuso de toda a vigem. ex: origem;destino1;destino2");
                    String viagens = stdin.readLine();
                    System.out.println("Escreva as datas que tem desponiblidade. ex: data1;data2;data3");
                    String datas = stdin.readLine(); //todo: devia validar ja as datas aqui antes de chatear o server
                    sender.sendReserva(viagens, datas);
                    receiver.receiveMessage();
                }
                case "4" -> {
                    System.out.print("Escreva o id da reserva que pretende cancelar: ");
                    String id = stdin.readLine();
                    sender.sendCancela(id);
                    System.out.println(dis.readUTF());
                }
                case "5" -> {
                    sender.sendLogout();
                    logged = false;
                    receiver.receiveMessage();
                }
                case "6" -> {
                    sender.sendQuit();
                    finish = true;
                }
            }
        }
        else {
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("VooManager\n");
            sb.append("1) Ver voos.\n");
            sb.append("2) Registar novo admin\n");
            sb.append("3) Adicionar voo\n");
            sb.append("4) Encerrar dia\n");
            sb.append("5) Logout\n");
            sb.append("6) Sair\n");
            sb.append("\n");
            sb.append("Insira a opção: ");
            System.out.println(sb);
            String option = stdin.readLine();
            switch (option) {
                case "1" -> {
                    sender.sendVoos();
                    receiver.receiveVoos();
                }
                case "2" -> {
                    System.out.print("Escreva o nome de utilizador: ");
                    String nome = stdin.readLine();
                    System.out.print("Escreva a password: ");
                    String password = stdin.readLine();
                    sender.sendRegisto(nome, password, true);
                    receiver.receiveRegistoA();
                }
                case "3" -> {
                    System.out.println("Escreva o percuso de toda a vigem. ex: origem;destino1;destino2:");
                    String viagens = stdin.readLine();
                    System.out.println("Escreva as datas que tem desponiblidade. ex: data1;data2;data3:");
                    String datas = stdin.readLine(); //todo: devia validar ja as datas aqui antes de chatear o server
                    sender.sendReserva(viagens, datas);
                    receiver.receiveMessage();
                }
                case "4" -> {
                    System.out.println("Insira a origem do voo: ");
                    String origem = stdin.readLine();
                    System.out.println("Insira o destino do voo: ");
                    String destino = stdin.readLine();
                    System.out.println("Insira a capacidade [de 100 a 250] do voo: ");
                    boolean valido = false;
                    int cap = 0;
                    while (!valido){
                        try{
                            cap = Integer.parseInt(stdin.readLine());
                            if(cap > 100 && cap < 250){
                                valido = true;
                            }
                        }
                        catch (NumberFormatException ignored){}
                        if(!valido) System.out.println("Erro: capacidade inválida, insira novamente.\n");
                    }
                    sender.sendAddVoo(origem,destino,cap);
                    int id;
                    if((id = receiver.receiveAddVoo()) != -1){
                        System.out.println("Adicionado voo com sucesso. Id do Voo: "+id+".\n");
                    }
                    else System.out.println("Erro ao adicionar voo!\n");
                }
                case "5" -> {
                    sender.sendLogout();
                    logged = false;
                    admin = false;
                    receiver.receiveMessage();
                }
                case "6" -> {
                    sender.sendQuit();
                    finish = true;
                }
            }
        }
        return finish;
    }


    public static void main (String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        Cliente client = new Cliente();

        boolean finish = false;
            while (!finish) {
                try {
                    if (!logged) client.login(dis,dos);
                    if (logged) finish = client.commands(dis,dos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        dis.close();
        dos.close();
        socket.close();


        System.out.println("Goodbye!");
    }

    private void login(DataInputStream dis, DataOutputStream dos) throws IOException {
        Sender sender = new Sender(dos);
        Receiver receiver = new Receiver(dis);
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("VooManager\n");
        sb.append("1) Registar nova conta.\n");
        sb.append("2) Iniciar sessão.\n");
        sb.append("\n");
        sb.append("Insira a opção: ");
        System.out.println(sb);
        String option = stdin.readLine();

        if(option.equals("1") || option.equals("2")){
            System.out.print("Escreva o nome de utilizador: ");
            String nome = stdin.readLine();
            System.out.print("Escreva a password: ");
            String password = stdin.readLine();
            if(option.equals("1")){
                sender.sendRegisto(nome,password,false);
                receiver.receiveRegisto();
            }
            else{
                sender.sendLogin(nome,password);
                logged = receiver.receiveBoolean();
                if(logged) {
                    admin = dis.readBoolean();
                    if(admin) System.out.println("Admin logado com sucesso!\n");
                    else System.out.println("Utilizador logado com sucesso!\n");
                }
                else System.out.println("Erro ao dar login!\n");
            }
        }
        else{
            System.out.println("Opção inválida!\n");
        }
    }
}
