import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


class VoosManager {
    private HashMap<String,Utilizador> users;
    private HashMap<Integer,Reserva> reservas;
    private HashMap<String,Voo> voos;
    private ReadWriteLock userslock = new ReentrantReadWriteLock();
    private ReadWriteLock reservasLock = new ReentrantReadWriteLock();
    private ReadWriteLock voosLock = new ReentrantReadWriteLock();
    private ReentrantLock [] fileLock = new ReentrantLock[4];
    private String utilizadoresCsv; //[0]
    private String voosCsv; //[1]
    private String reservasCsv; //[2]
    private String datasEncerradasCsv; //[3]
    private int lastidReserva;
    private LocalDate dataArranque  = LocalDate.now();
    private List<LocalDate> datasEncerradas;
    private ReadWriteLock datasLock = new ReentrantReadWriteLock();

    public VoosManager(String utilizadoresCsv, String voosCsv, String reservasCsv,String datasEncerradasCsv) {
        users = new HashMap<>();
        reservas = new HashMap<>();
        voos = new HashMap<>();
        this.utilizadoresCsv = utilizadoresCsv;
        this.voosCsv = voosCsv;
        this.reservasCsv = reservasCsv;
        this.lastidReserva = 0;
        this.datasEncerradas = new ArrayList<>();
        this.datasEncerradasCsv = datasEncerradasCsv;
        for(int i = 0; i < 4; i++){
            fileLock[i] = new ReentrantLock();
        }
    }

    public boolean updateUtilizadores(Utilizador u) {
        boolean added = false;
        userslock.writeLock().lock();
        if(!users.containsKey(u.getNome())) {
            users.put(u.getNome(), u);
            added = true;
        }
        userslock.writeLock().unlock();
        return added;
    }

    public Reserva createReserva(String user, String[] pontos, String[] datas){
        Reserva nova = null;
        LocalDate data = null;
        boolean dataValida = false;

        datasLock.readLock().lock();
        for(int i = 0; i < datas.length && !dataValida; i++){
            try{
                LocalDate dia = LocalDate.parse(datas[i]);
                if(!datasEncerradas.contains(dia) && !dia.isBefore(dataArranque)) {
                    dataValida = true;
                    data = dia;
                }
            }
            catch (DateTimeParseException ignored){}
        }
        boolean pontosValidos = true;
        List<String> viagem = new ArrayList<>();
        if(data != null){
            voosLock.writeLock().lock();
            String origem = pontos[0];
            String destino = pontos[pontos.length-1];
            for(int i = 0; i < pontos.length-1 && pontosValidos; i++){
                String id = pontos[i]+pontos[i+1];
                if(!(existsVoo(id) && haveLotacao(id,data)))
                    pontosValidos = false;
                else viagem.add(id);
            }
            if(pontosValidos){
                for(String id : viagem){
                    voos.get(id).addPassageiro(data);
                }
                reservasLock.writeLock().lock();
                datasLock.readLock().unlock();
                voosLock.writeLock().unlock();
                this.lastidReserva++;
                int cod = lastidReserva;
                nova = new Reserva(origem,destino,cod,viagem,data,user);
                reservas.put(cod,nova);
                reservasLock.writeLock().unlock();
            }
        }

        return nova;
    }

    private boolean haveLotacao(String id, LocalDate data) {
        try{
            voosLock.readLock().lock();
            Voo v = voos.get(id);
            return v.getLotacao(data) < v.getCapacidade();
        }
        finally {
            voosLock.readLock().unlock();
        }
    }

    public Voo updateVoos(Voo v){
        Voo novo = v.clone();
        String origemedestino = novo.getOrigem()+novo.getDestino();
        int i = 2;
        String id = origemedestino;
        voosLock.writeLock().lock();
        while(voos.containsKey(id)){
            id = origemedestino+"#"+i;
            i++;
        }
        novo.setId(id);
        voos.put(novo.getId(),novo);
        voosLock.writeLock().unlock();
        return novo;
    }

    public boolean existsVoo(String id){
        try{
            voosLock.readLock().lock();
            return voos.containsKey(id);
        }
        finally {
            voosLock.readLock().unlock();
        }
    }

    public boolean existeUtilizador(String name){
        try{
            userslock.readLock().lock();
            return this.users.containsKey(name);
        }
        finally {
            userslock.readLock().unlock();
        }
    }

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

    public ReservasList getReservas (String utilizador) {
        try{
            reservasLock.readLock().lock();
            ReservasList ret = new ReservasList();
            for(Reserva r : reservas.values()){
                if(r.getUtilizador().equals(utilizador))
                    ret.add(r);
            }
            return ret;
        }
        finally {
            reservasLock.readLock().unlock();
        }
    }

    public ArrayList<String> getReservasVoos(String nome){
        ArrayList<String> listaReservas = new ArrayList<>();
        StringBuilder sb;
        reservasLock.readLock().lock();
        Collection<Reserva> rs = reservas.values();
        reservasLock.readLock().unlock();
        for(Reserva r : rs){
            if(r.getUtilizador().equals(nome)) {
                int i = 0;
                sb = new StringBuilder();
                sb.append("#CodigoReserva ").append(r.getCodigo()).append(" Viagem: ");
                voosLock.readLock().lock();
                for (String idVoo : r.getViagem()) {
                    Voo v = voos.get(idVoo);
                    if (i == 0) {
                        sb.append(v.getOrigem());
                        sb.append("->");
                        sb.append(v.getDestino());
                    } else {
                        sb.append("->");
                        sb.append(v.getDestino());
                    }
                    i++;
                }
                voosLock.readLock().unlock();
                listaReservas.add(sb.toString());
            }
        }

        return listaReservas;
    }

    public Utilizador getUtilizador(String nome) {
        try{
            userslock.readLock().lock();
            return users.get(nome);
        }
        finally {
            userslock.readLock().unlock();
        }
    }

    public void registoUtilizadorCsv(Utilizador u) throws IOException {
        fileLock[0].lock();
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.utilizadoresCsv,true));
        bw.write(u.getNome()+";"+u.getPassword()+";");
        if(u.isAdmin()) bw.write("1");
        else bw.write("0");
        bw.write("\n");
        bw.close();
        fileLock[0].unlock();
    }

    public void registoVooCsv(Voo v) throws IOException {
        fileLock[1].lock();
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.voosCsv,true));
        bw.write(v.getId()+";"+v.getOrigem()+";"+v.getDestino()+";"+v.getCapacidade()+"\n");
        bw.close();
        fileLock[1].unlock();
    }

    public void registoReservaCsv(Reserva r) throws IOException {
        fileLock[2].lock();
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.reservasCsv,true));
        bw.write(r.getCodigo()+";"+r.getOrigem()+";"+r.getDestino()+";"+r.getUtilizador()+";"+r.getData()+";");
        r.getViagem().forEach(k -> {
            try {
                bw.write(k+"-");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw.write("\n");
        bw.close();
        fileLock[2].unlock();
    }

    public void registoTodasReservasCsv() throws IOException {
        fileLock[2].lock();
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.reservasCsv,true));
        for(Reserva r : reservas.values()) {
            bw.write(r.getCodigo() +";"+r.getOrigem()+";"+r.getDestino()+ ";" + r.getUtilizador() + ";" + r.getData() + ";");
            r.getViagem().forEach(k -> {
                try {
                    bw.write(k + "-");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.write("\n");
            bw.close();
        }
        fileLock[2].unlock();
    }

    public void registoDataEncerrada(LocalDate data) throws IOException {
        fileLock[3].lock();
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.datasEncerradasCsv,true));
        bw.write(data.toString()+"\n");
        bw.close();
        fileLock[3].unlock();
    }

    public void registoTodasDatasEncerradasCsv() throws IOException {
        fileLock[3].lock();
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.datasEncerradasCsv));
        for(LocalDate data : datasEncerradas) {
            bw.write(data.toString()+"\n");
            bw.write("\n");
            bw.close();
        }
        fileLock[3].unlock();
    }

    public void loadUtilizadoresCsv() throws IOException {
        System.out.println("Lendo o ficheiro dos utilizadores...");
        BufferedReader br = new BufferedReader(new FileReader(this.utilizadoresCsv));
        String line;
        int i = 0;
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
                        Utilizador u = new Utilizador(nome,password,permissao);
                        users.put(nome,u);
                        i++;
                    }
                }
                catch(NumberFormatException ignored){}
            }
        }
        System.out.println("Ficheiro lido. "+i+" utilizadores carregados.");
        br.close();

    }

    public void loadVoosCsv() throws IOException {
        System.out.println("Lendo o ficheiro de voos...");
        BufferedReader br = new BufferedReader(new FileReader(this.voosCsv));
        String line;
        int i = 0;
        while ((line = br.readLine()) != null){
            String[] parsed = line.split(";");
            if(parsed.length == 4){
                try{
                    String id = parsed[0];
                    String origem = parsed[1];
                    String destino = parsed[2];
                    int cap = Integer.parseInt(parsed[3]);
                    Voo v = new Voo(id,origem,destino,cap,new HashMap<>());
                    voos.put(id,v);
                    i++;
                }
                catch(NumberFormatException ignored){}
            }
        }
        System.out.println("Ficheiro lido. "+i+" voos carregados.");
        br.close();
    }


    public void loadReservasCsv() throws IOException {
        System.out.println("Lendo o ficheiro de reservas...");
        BufferedReader br = new BufferedReader(new FileReader(this.reservasCsv));
        String line;
        int rs = 0;
        while ((line = br.readLine()) != null){
            String[] parsed = line.split(";");
            if(parsed.length == 6){
                try{
                    int cod = Integer.parseInt(parsed[0]);
                    String origem = parsed[1];
                    String destino = parsed [2];
                    String utilizador = parsed[3];
                    LocalDate data = LocalDate.parse(parsed[4]);
                    String[] idsViagem = parsed[5].split("-");
                    if(existeUtilizador(utilizador) && !data.isBefore(dataArranque) && idsViagem.length >= 1){
                        boolean existem = true;
                        List<String> viagem = new ArrayList<>();
                        for(int i = 0; i < idsViagem.length && existem; i++){
                            String id = idsViagem[i];
                            existem = existsVoo(id);
                            viagem.add(id);
                        }
                        if(existem) {
                            Reserva r = new Reserva(origem,destino,cod,viagem,data,utilizador);
                            for(String id : viagem){
                                voos.get(id).addPassageiro(data);
                            }
                            reservas.put(cod,r);
                            rs++;
                            if(lastidReserva < cod) lastidReserva = cod;
                        }
                    }
                }
                catch (DateTimeParseException | NumberFormatException ignored){}
            }
        }
        System.out.println("Ficheiro lido. "+rs+" reservas carregados.");
        br.close();
    }

    public void loadDatasEncerradasCsv() throws IOException {
        System.out.println("Lendo o ficheiro de datas encerradas...");
        BufferedReader br = new BufferedReader(new FileReader(this.datasEncerradasCsv));
        String data;
        int ds = 0;
        while ((data = br.readLine()) != null){
            try{
                LocalDate dataEncerrada = LocalDate.parse(data);
                if(!dataEncerrada.isBefore(dataArranque) && !datasEncerradas.contains(dataEncerrada)){
                    datasEncerradas.add(dataEncerrada);
                    ds++;
                }
            }
            catch (DateTimeParseException ignored){}
        }
        br.close();
        System.out.println("Ficheiro lido. "+ds+" datas encerradas carregados.");
        registoTodasDatasEncerradasCsv(); //para caso tenha lido datas invalidas, reescrever corretamente
    }

    public void load() throws IOException {
        loadUtilizadoresCsv();
        loadVoosCsv();
        loadReservasCsv();
        loadDatasEncerradasCsv();
    }

    public boolean checkCredentials(String nome, String password) {
        try{
            userslock.readLock().lock();
            return users.containsKey(nome) && users.get(nome).getPassword().equals(password);
        }
        finally {
            userslock.readLock().unlock();
        }
    }

    public boolean encerraDia(LocalDate data) {
        try {
            datasLock.writeLock().lock();
            if (!datasEncerradas.contains(data) && (data.isAfter(dataArranque) || data.equals(dataArranque))) {
                datasEncerradas.add(data);
                registoDataEncerrada(data);
                return true;
            } else return false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            datasLock.writeLock().unlock();
        }
        return false;
    }

    public boolean cancelaReserva(int codReserva) {
        boolean cancelada = false;
        reservasLock.writeLock().lock();
        Reserva r = reservas.get(codReserva);
        LocalDate data = r.getData();
        datasLock.readLock().lock();
        if(!datasEncerradas.contains(data)){
            List<String> viagem = r.getViagem();
            datasLock.readLock().unlock();
            voosLock.writeLock().lock();
            for(String id : viagem){
                voos.get(id).removePassageiro(data);
            }
            voosLock.writeLock().unlock();
            reservas.remove(codReserva);
            try{
                registoTodasReservasCsv();
            }
            catch(IOException ignored){}
            reservasLock.writeLock().unlock();
            cancelada = true;
        }
        return cancelada;
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
        System.out.println("C"+idC+": fechando a socket");
        return true;
    }

    public void registo() throws IOException {
        boolean ret;
        boolean admin = dis.readBoolean();
        if(admin) {
            System.out.println("C"+idC+": começando registo admin");
            String nome = dis.readUTF();
            String password = dis.readUTF();
            Utilizador u = new Utilizador(nome, password, true);
            ret = manager.updateUtilizadores(u);
            if(ret) {
                manager.registoUtilizadorCsv(u);
                System.out.println("C"+idC+": registado admin com sucesso");
            }
        } else {
            System.out.println("C"+idC+": começando registo utilizador");
            String nome = dis.readUTF();
            String password = dis.readUTF();
            Utilizador u = new Utilizador(nome, password, false);
            ret = manager.updateUtilizadores(u);
            if(ret) {
                manager.registoUtilizadorCsv(u);
                System.out.println("C"+idC+": registado utilizador com sucesso");
            }
        }
        dos.writeBoolean(ret);
        System.out.println("C"+idC+": mandando o booleano");
    }

    public void logout() throws IOException {
        user = null;
        System.out.println("C"+idC+": logout");
    }

    public void login() throws IOException {
        System.out.println("C"+idC+": começando login");
        boolean admin = false;
        boolean logged = false;
        String nome = dis.readUTF();
        String password = dis.readUTF();
        if(manager.checkCredentials(nome,password)){
            user = manager.getUtilizador(nome);
            logged = true;
            admin = user.isAdmin();
        }
        dos.writeBoolean(logged);
        System.out.println("C"+idC+": mandando o booleano de logged");
        if(logged) {
            dos.writeBoolean(admin);
            System.out.println("C"+idC+": mandando o booleano de admin");
        }
    }

    public void voos() throws IOException {
        System.out.println("C"+idC+": getting voos");
        VoosList voos = manager.getVoos();
        voos.serialize(dos);
        System.out.println("C"+idC+": mandando os voos");
    }

    public void reservas() throws IOException {
        System.out.println("C"+idC+": getting reservas");
        ReservasList reservas = manager.getReservas(user.getNome());
        reservas.serialize(dos);
        System.out.println("C"+idC+": mandando as reservas");
    }

    public void reserva() throws IOException {
        System.out.println("C"+idC+": começando a reserva");
        String[] viagem = dis.readUTF().split(";");
        String[] datas = dis.readUTF().split(";");
        Reserva nova = manager.createReserva(user.getNome(),viagem,datas);
        if (nova != null) {
            System.out.println("C"+idC+": reserva feita com sucesso");
            manager.registoReservaCsv(nova);
            System.out.println("C"+idC+": mandando o booleano e o codigo");
            dos.writeBoolean(true);
            dos.writeInt(nova.getCodigo());
        }
        else dos.writeBoolean(false);
        System.out.println("C"+idC+": mandando o booleano");
    }

    public void cancela() throws IOException {
        System.out.println("C"+idC+": processando cancelamento");
        boolean cancelada = false;
        int codReserva = dis.readInt();
        cancelada = manager.cancelaReserva(codReserva);
        System.out.println("C"+idC+": mandando ao cliente o booleano");
        dos.writeBoolean(cancelada);
    }

    public void encerra() throws IOException {
        System.out.println("C"+idC+": encerrando dia...");
        boolean encerrado = false;
        try{
            LocalDate data = LocalDate.parse(dis.readUTF());
            encerrado = manager.encerraDia(data);
            System.out.println("C"+idC+": dia encerrado com sucesso.");
        }
        catch (DateTimeParseException ignored){}
        System.out.println("C"+idC+": mandando o booleano");
        dos.writeBoolean(encerrado);
        dos.flush();
    }

    public void addvoo() throws IOException {
        System.out.println("C"+idC+": adicionando voo...");
        boolean adicionado = false;
        String origem = dis.readUTF();
        String destino = dis.readUTF();
        int cap = dis.readInt();
        Voo novo = manager.updateVoos(new Voo(null,origem, destino, cap, new HashMap<>()));
        if(novo.getId() != null) {
            System.out.println("C"+idC+": voo adicionado com sucesso.");
            manager.registoVooCsv(novo);
            adicionado = true;
        }
        System.out.println("C"+idC+": mandando o booleano");
        dos.writeBoolean(adicionado);
        dos.flush();
    }

    public void run() {
            try{
                boolean finish = false;
                System.out.println("C"+idC+": Establecendo conexão.");
                while(!finish) {
                    int opcode = dis.readInt();
                    System.out.println("C"+idC+": recebido opcode ["+opcode+"].");
                    switch (opcode){
                        case 0 -> finish = quit();
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
                System.out.println("C"+idC+": Conexão terminada.");
            }
            catch(IOException e){
                e.printStackTrace();
            }
    }
}

public class Server{

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Inicializando o servidor...");
        VoosManager manager = new VoosManager("../ProjetoSD/cp/utilizadores.csv",
                                                "../ProjetoSD/cp/voos.csv","../ProjetoSD/cp/reservas.csv","../ProjetoSD/cp/datasEncerradas.csv");//os ficheiros de persistencia são passados na criação do manager
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
