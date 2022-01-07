import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


class VoosManager {
    private HashMap<String,Utilizador> users;
    private HashMap<Integer,Reserva> reservas;
    private HashMap<Integer,Voo> voos;
    private ReadWriteLock userslock = new ReentrantReadWriteLock();
    private ReadWriteLock reservasLock = new ReentrantReadWriteLock();
    private ReadWriteLock voosLock = new ReentrantReadWriteLock();
    private String utilizadoresCsv;
    private String voosCsv;
    private String registosCsv;
    private int lastidReserva;
    private int lastidVoos;

    public VoosManager(String utilizadoresCsv,String voosCsv, String registosCsv) {
        users = new HashMap<>();
        reservas = new HashMap<>();
        voos = new HashMap<>();
        this.utilizadoresCsv = utilizadoresCsv;
        this.voosCsv = voosCsv;
        this.registosCsv = registosCsv;
        this.lastidReserva = 0;
    }

    public void updateUtilizadores(Utilizador u) {
        userslock.writeLock().lock();
        users.put(u.getNome(),u);
        userslock.writeLock().unlock();
    }

    public boolean updateReservas(Reserva r){
        boolean done = false;
        reservasLock.writeLock().lock();
        done = updateLotacao(r.getViagem(),1);
        if(done){
            lastidReserva++;
            r.setCodigo(lastidReserva);
            reservas.put(r.getCodigo(),r);
        }
        reservasLock.writeLock().unlock();
        return done;
    }

    public void removeReserva(int codReserva){
        reservasLock.writeLock().lock();
        reservas.remove(codReserva);
        reservasLock.writeLock().unlock();
    }

    public void updateVoos(Voo v){
        voosLock.writeLock().lock();
        this.lastidVoos++;
        voos.put(lastidVoos,v);
        voosLock.writeLock().unlock();
    }

    public boolean updateLotacao(List<Integer> idsVoos,int lugares){
       //não tem locks os locks são feitos no metodo update reservas
        int lot;
        int cap;
        for(Integer ids : idsVoos){
           lot =  voos.get(ids).getLotacao();
           cap =  voos.get(ids).getCapacidade();
            System.out.println(lot+" "+lugares+"\n");
            System.out.println(cap);
           if((lot + lugares) > cap){
               return false;
           }
        }
        for(Integer ids : idsVoos){
             voos.get(ids).addLotacao(lugares);
        }
        return true;
    }
    public int existsVoo(String origem,String destino){
        try{
            voosLock.readLock().lock();
            for(Voo v : voos.values()){
                if(v.getOrigem().equals(origem) && v.getDestino().equals(destino)){
                    return v.getId();
                }

            }
            return -1;
        }
        finally {
            voosLock.readLock().unlock();
        }
    }

    public boolean existeUtilizador(String name){
        return this.users.containsKey(name);
    }
    public boolean existeReserva(int cod){ return this.reservas.containsKey(cod);  }

    public VoosList getVoos () {
        try{
            voosLock.readLock().lock();
            VoosList ret = new VoosList();
            ret.addAll(voos.values());
            return ret;
        }
        finally {
            voosLock.readLock().unlock();
        }
    }

    public ArrayList<String> getReservasVoos(String nome){
        ArrayList<String> listaReservas = new ArrayList<>();
        StringBuilder sb;
        for(Reserva r : reservas.values()){
            int i = 0;
            sb = new StringBuilder();
            sb.append("#CodigoReserva ").append(r.getCodigo()).append(" Viagem: ");
            voosLock.readLock().lock();
            for(int idVoo : r.getViagem() ){
                Voo v = voos.get(idVoo);
                if(i==0){
                    sb.append(v.getOrigem());
                    sb.append("->");
                    sb.append(v.getDestino());
                }else{
                    sb.append("->");
                    sb.append(v.getDestino());
                }
                i++;
            }
            voosLock.readLock().unlock();
            listaReservas.add(sb.toString());
        }
        return listaReservas;
    }

    public int getLastidReserva(){
        return lastidReserva;
    }

    public Reserva getReserva(int cod){
        return reservas.get(cod);
    }

    public Utilizador getUtilizador(String nome) {
            return users.get(nome);
    }

    public void registoUtilizadorCsv(String nome,String password ,int adminPermission) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.utilizadoresCsv,true));
        bw.write("\n");
        bw.write(nome+";"+password+";"+adminPermission);
        bw.close();
    }

    public void registoVooCsv(int id, String origem,String destino,int capacidade) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.voosCsv,true));
        bw.write("\n");
        bw.write(id+";"+origem+";"+destino+";"+capacidade+";"+0);
        bw.close();
    }

    public void loadUtilizadoresCsv() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.utilizadoresCsv));
        String line;
        while ((line = br.readLine()) != null){
            String[] parsed = line.split(";");
            if(parsed.length == 3){
                try{
                    String nome = parsed[0];
                    String password = parsed[1];
                    int admin = Integer.parseInt(parsed[2]);
                    if(admin == 1 || admin == 0){
                        boolean permissao;
                        permissao = admin == 1;
                        updateUtilizadores(new Utilizador(nome,password,permissao));
                    }
                }
                catch(NumberFormatException ignored){}
            }
        }
        br.close();

    }

    public void loadVoosCsv() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.voosCsv));
        String line;
        while ((line = br.readLine()) != null){
            String[] parsed = line.split(";");
            if(parsed.length == 5){
                try{
                    int id = Integer.parseInt(parsed[0]);
                    String origem = parsed[1];
                    String destino = parsed[2];
                    int cap = Integer.parseInt(parsed[3]);
                    int lot = Integer.parseInt(parsed[4]);
                    updateVoos(new Voo(id,origem,destino,cap,lot));
                }
                catch(NumberFormatException ignored){}
            }
        }
        br.close();

    }

    public void load() throws IOException {
        loadUtilizadoresCsv();
        loadVoosCsv();
    }
}

class Handler implements Runnable {
    private Socket socket;
    private VoosManager manager;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Utilizador user = null;
    private boolean logged = false;
    private int idC;

    public Handler (Socket socket, VoosManager manager, int idC){
        this.socket = socket;
        this.manager = manager;
        this.idC = idC;
        try{
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean quit() throws IOException {
        dis.close();
        dos.close();
        socket.close();
        return true;
    }

    public void registo() throws IOException {
        boolean ret;
        boolean admin = dis.readBoolean();
        if(admin) {
            if (logged && user.isAdmin()) {
                String nome = dis.readUTF();
                String password = dis.readUTF();
                if (manager.existeUtilizador(nome)) {
                    ret = false;
                } else {
                    manager.updateUtilizadores(new Utilizador(nome, password, true));
                    manager.registoUtilizadorCsv(nome, password, 0);
                    ret = true;
                }
            } else {
                ret = false;
            }
        }
        else {
            if (!logged) {
                String nome = dis.readUTF();
                String password = dis.readUTF();
                if (manager.existeUtilizador(nome)) {
                    ret = false;
                } else {
                    manager.updateUtilizadores(new Utilizador(nome, password, false));
                    manager.registoUtilizadorCsv(nome, password, 0);
                    ret = true;
                }
            } else {
                ret = false;
            }
        }
        dos.writeBoolean(ret);
    }

    public void logout() throws IOException {
        if(logged) {
            logged = false;
            user = null;
            dos.writeUTF("Deslogado com sucesso!");
        }
        else dos.writeUTF("Você não se encontra logado!");
        dos.flush();
    }

    public void login() throws IOException {
        StringBuilder sb;
        sb = new StringBuilder();
        if(!logged){
            String nome = dis.readUTF();
            String password = dis.readUTF();
            if(manager.existeUtilizador(nome)){
                user = manager.getUtilizador(nome);
                if(password.equals(user.getPassword())){
                    logged = true;
                    sb.append("Logado com sucesso!");
                }else{
                    sb.append("Erro: Password incorreta!");
                }
            }else {
                sb.append("Erro: Utilizador não existe no sistema, experimente registar primeiro!");
            }
        }else{
            sb.append("Vocé ja se encontra logado!");
        }
        dos.writeUTF(sb.toString());
        dos.writeBoolean(logged);
    }

    public void voos() throws IOException {
        if(logged) {
            VoosList voos = manager.getVoos();
            voos.serialize(dos);
        }
        else{
            VoosList voos = new VoosList();
            voos.serialize(dos);
        }
    }

    public void reservas() throws IOException {/*todo listar reservas do utilizador logado*/
       ArrayList<String> rs = manager.getReservasVoos(user.getNome());
       dos.writeInt(rs.size());
       for(String s : rs){
           dos.writeUTF(s);
       }
    }

    public void reserva() throws IOException {
        if(logged) {
            String[] viagem = dis.readUTF().split(";");
            String[] datas = dis.readUTF().split(";");
            boolean valido = true;
            List<Integer> idsVoos = new ArrayList<>();
            List<LocalDate> datasVoos = new ArrayList<>();
            //verifica se é valida a viagem que o utilizador quer
            if (viagem.length >= 2) {
                for (int i = 0; i < viagem.length - 1 && valido; i++) {
                    int id;
                    if ((id = manager.existsVoo(viagem[i], viagem[i + 1])) != -1) {
                        idsVoos.add(id);
                    } else {
                        valido = false;
                    }

                    for(String d : datas){
                        datasVoos.add(LocalDate.parse(d));
                    }
                }
                //neste momento está a escolher a primeira data possivel
                if(valido){
                    if(manager.updateReservas(new Reserva(idsVoos,datasVoos.get(0),user.getNome()))){
                          dos.writeUTF("Viagem reservada com sucesso.");
                    }else{
                        dos.writeUTF("Lotação esgotada");
                    }

                }else{
                    dos.writeUTF("Não foi possivel concluir reserva");
                }
            }
        }
    }

    public void cancela() throws IOException {
         StringBuilder sb;
         sb = new StringBuilder();
         if(logged) {
            int codReserva = dis.readInt();
            if(manager.existeReserva(codReserva)){
                Reserva r = manager.getReserva(codReserva);
                if(r.getUtilizador().equals(user.getNome())){
                      manager.removeReserva(codReserva);
                }else{
                    sb.append("Essa reserva não existe");
                }

            }else{
                sb.append("Essa reserva não existe");
             }
            sb.append("Reserva ").append(codReserva).append(" cancelada");
            dos.writeUTF(sb.toString());
        }
    }

    public void encerra(){/*todo admin encerrar dia*/}

    public void addvoo() throws IOException {
        StringBuilder sb;
        sb = new StringBuilder();
        if(logged && user.isAdmin()) {
            boolean validoOD = true;
            boolean validoC = true;
            String origem = dis.readUTF();
            String destino = dis.readUTF();
            String Scapacidade = dis.readUTF();
            if (origem.equals(destino)) {
                validoOD = false;
            }
            try {
                int capacidade = Integer.parseInt(Scapacidade);
                if (capacidade < 100 || capacidade > 250) {
                    validoC = false;
                }
                if (validoC && validoOD) {
                    manager.updateVoos(new Voo(-1,origem, destino, capacidade,0));
                    manager.registoVooCsv(-1,origem,destino,capacidade);
                    sb.append("O voo ").append(origem).append(" -> ").append(destino).append(" com a capacidade de ").append(capacidade).append(" passageiros, foi registado com sucesso.");
                } else if (!validoC) {
                    sb.append("Erro ao registar voo: ").append(capacidade).append(" não é uma capacidade válida, experimente [100-250]");
                } else
                    sb.append("Erro ao registar voo: Origem e destino inválidos");
            } catch (NumberFormatException e) {
                sb.append("Erro ao registar voo: ").append(Scapacidade).append(" não é uma capacidade válida");
            } finally {
                dos.writeUTF(sb.toString());
                dos.flush();
            }
        }else{
            sb.append("Não tem permissão para adicionar voo");
            dos.writeUTF(sb.toString());
            dos.flush();
        }

    }

    public void run() {
            try{
                boolean finish = false;
                while(!finish) {
                    int opcode = dis.readInt();
                    System.out.println("opcode recebido do cliente "+idC+": "+opcode);
                    switch (opcode){
                        case 0 -> finish =  quit();
                        case 1 -> registo();
                        case 2 -> login();
                        case 3 -> logout();
                        case 4 -> voos();
                        case 5 -> reservas();
                        case 6 -> reserva();
                        case 7 -> cancela();
                        case 8 -> encerra();
                        case 9 -> addvoo();
                        default -> {}
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
    }
}

public class Server{

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        VoosManager manager = new VoosManager("../ProjetoSD/cp/utilizadores.csv",
                                                "../ProjetoSD/cp/voos.csv","../ProjetoSD/cp/registos.csv");//os ficheiros de persistencia são passados na criação do manager
        manager.load();
        int i = 0;

        while (true) {
            Socket socket = serverSocket.accept();
            i++;
            Thread handler = new Thread(new Handler(socket, manager,i));
            handler.start();
        }
    }

}
