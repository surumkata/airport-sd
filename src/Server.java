import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class VoosManager {
    private HashMap<String,Utilizador> users; //map dos users; chave é o nome do user
    private HashMap<Integer,Reserva> reservas; //map das reservas; chave é o codigo da reserva
    private HashMap<String,Voo> voos; //map dos voos; chave é o id do voo
    private ReadWriteLock userslock = new ReentrantReadWriteLock();//lock do map dos users
    private ReadWriteLock reservasLock = new ReentrantReadWriteLock();//lock do map das reservas
    private ReadWriteLock voosLock = new ReentrantReadWriteLock();//lock do map dos voos
    private ReentrantLock [] fileLock = new ReentrantLock[4]; //lock dos ficheiros
    private String utilizadoresCsv; //[0] //nome do ficheiro dos users
    private String voosCsv; //[1] //nome do ficheiro dos voos
    private String reservasCsv; //[2] //nome do ficheiro das reservas
    private String datasEncerradasCsv; //[3] //nome do ficheiro das datas encerradas
    private int lastidReserva; //ultimo codigo de reserva
    private LocalDate dataArranque  = LocalDate.now(); // data de arranque do manager
    private List<LocalDate> datasEncerradas; //lista de datas encerras (nenhuma pré data de arranque)
    private ReadWriteLock datasLock = new ReentrantReadWriteLock(); //lock da lista de datas encerradas


    /**
     * Construtor do VoosManager
     * @param utilizadoresCsv nome do ficheiro dos utilizadores.
     * @param voosCsv nome do ficheiro dos voos.
     * @param reservasCsv nome do ficheiro das reservas.
     * @param datasEncerradasCsv nome do ficheiro das datas encerradas.
     */
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

    /**
     * Adiciona um utilizador ao manager.
     * @param u utilizador a ser adicionado.
     * @return true se for bem adicionado, false caso contrário.
     */
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

    /**
     * Adiciona uma reserva ao manager.
     * @param user nome do utilizador.
     * @param pontos lista de pontos por onde a viagem vai ocorrer.
     * @param datas lista de datas (escolhe a primeira válida).
     * @return Reserva adicionado. null caso aja insucesso.
     */
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
                if(!(haveLotacao(id,data)))
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
            String voo = id;
            voosLock.readLock().lock();
            boolean existe = false;
            boolean havelotacao = false;
            if(voos.containsKey(voo)){
                existe = true;
            }
            int i = 2;
            while(existe && !havelotacao){
                Voo v = voos.get(voo);
                if(v.getLotacao(data) < v.getCapacidade()){
                    havelotacao = true;
                }
                if(!havelotacao){
                    voo = id + "#" +i;
                    i++;
                    existe = voos.containsKey(voo);
                }
            }
            return havelotacao;
        }
        finally {
            voosLock.readLock().unlock();
        }
    }

    /**
     * Adiciona um voo ao manager.
     * @param v voo.
     * @return Voo adicionado (id é mudado).
     */
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

    /**
     * Verifica se um voo existe.
     * @param id id do voo.
     * @return true se voo existir, false caso contrário.
     */
    public boolean existsVoo(String id){
        try{
            voosLock.readLock().lock();
            return voos.containsKey(id);
        }
        finally {
            voosLock.readLock().unlock();
        }
    }

    /**
     * Verifica se um utilizador existe.
     * @param name nome do utilizador.
     * @return true se utilizador existir, false caso contrário.
     */
    public boolean existeUtilizador(String name){
        try{
            userslock.readLock().lock();
            return this.users.containsKey(name);
        }
        finally {
            userslock.readLock().unlock();
        }
    }

    /**
     * Get da lista de voos.
     * @return retorna a lista de todos as voos.
     */
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

    /**
     * Get da lista de reservas de um utilizador.
     * @param nome nome do utilizador.
     * @return retorna a lista de todas as reservas de um utilizador.
     */
    public ReservasList getReservas (String nome) {
        try{
            reservasLock.readLock().lock();
            ReservasList ret = new ReservasList();
            for(Reserva r : reservas.values()){
                if(r.getUtilizador().equals(nome))
                    ret.add(r);
            }
            return ret;
        }
        finally {
            reservasLock.readLock().unlock();
        }
    }

    /**
     * Get de um utilizador pelo nome.
     * @param nome nome do utilizador
     * @return retorna o utilizador referente ao nome.
     */
    public Utilizador getUtilizador(String nome) {
        try{
            userslock.readLock().lock();
            return users.get(nome);
        }
        finally {
            userslock.readLock().unlock();
        }
    }

    /**
     * Regista um utilizador no CSV
     * @param u utilizador a ser registada
     */
    public void registoUtilizadorCsv(Utilizador u) throws IOException {
        try{
            fileLock[0].lock();
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.utilizadoresCsv,true));
            bw.write(u.getNome()+";"+u.getPassword()+";");
            if(u.isAdmin()) bw.write("1");
            else bw.write("0");
            bw.write("\n");
            bw.close();
        }
        finally {
            fileLock[0].unlock();
        }
    }

    /**
     * Regista um voo no CSV
     * @param v voo a ser registada
     */
    public void registoVooCsv(Voo v) throws IOException {
        try{
            fileLock[1].lock();
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.voosCsv,true));
            bw.write(v.getId()+";"+v.getOrigem()+";"+v.getDestino()+";"+v.getCapacidade()+"\n");
            bw.close();
        }
        finally {
            fileLock[1].unlock();
        }
    }

    /**
     * Regista um reserva no CSV
     * @param r reserva a ser registada
     */
    public void registoReservaCsv(Reserva r) throws IOException {
        try{
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
        }
        finally {
            fileLock[2].unlock();
        }
    }


    private void registoTodasReservasCsv() throws IOException {
        try{
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
            }
            bw.close();
        }
        finally {

            fileLock[2].unlock();
        }
    }


    /**
     * Regista uma data no ficheiro CSV.
     * @param data data a ser registada
     */
    public void registoDataEncerrada(LocalDate data) throws IOException {

        try{
            fileLock[3].lock();
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.datasEncerradasCsv,true));
            bw.write(data.toString()+"\n");
            bw.close();
        }
        finally {

            fileLock[3].unlock();
        }
    }

    private void registoTodasDatasEncerradasCsv() throws IOException {
        try {
            fileLock[3].lock();
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.datasEncerradasCsv));
            for (LocalDate data : datasEncerradas) {
                bw.write(data.toString() + "\n");
            }
            bw.close();
        }
        finally {
            fileLock[3].unlock();
        }
    }

    private void loadUtilizadoresCsv() throws IOException {
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

    private void loadVoosCsv() throws IOException {
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


    private void loadReservasCsv() throws IOException {
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

    private void loadDatasEncerradasCsv() throws IOException {
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

    /**
     * Carrega dos ficheiros as informações do manager.
     */
    public void load() throws IOException {
        loadUtilizadoresCsv();
        loadVoosCsv();
        loadReservasCsv();
        loadDatasEncerradasCsv();
    }

    /**
     * Verifica se as credenciais do utilizador estão corretos.
     * @param nome nome do utilizador.
     * @param password password do utilizador.
     * @return true se estiverem corretas, false caso contrário.
     */
    public boolean checkCredentials(String nome, String password) {
        try{
            userslock.readLock().lock();
            return users.containsKey(nome) && users.get(nome).getPassword().equals(password);
        }
        finally {
            userslock.readLock().unlock();
        }
    }

    /**
     * Encerra um dia. (Passa a ser impossivel criar/cancelar reservas nesse dia).
     * @param data data a ser encerrada
     * @return true se o dia for encerrado com sucesso, false caso contrário.
     */
    public boolean encerraDia(LocalDate data) {
        try {
            datasLock.writeLock().lock();
            if (!datasEncerradas.contains(data) && (!data.isBefore(dataArranque))) {
                datasEncerradas.add(data);
                registoDataEncerrada(data);
                return true;
            } else return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            datasLock.writeLock().unlock();
        }
    }

    /**
     * Cancela a reserva de um determinado user
     * @param codReserva codigo que identifica a reserva a ser cancelada
     * @param user nome do utilizador da reserva.
     * @return true se a reserva tiver sido cancelada com sucesso, false caso contrário.
     */
    public boolean cancelaReserva(int codReserva, String user) {
        boolean cancelada = false;
        reservasLock.writeLock().lock();
        if(reservas.containsKey(codReserva)) {
            Reserva r = reservas.get(codReserva);
            if(r.getUtilizador().equals(user)){
                LocalDate data = r.getData();
                datasLock.readLock().lock();
                if (!datasEncerradas.contains(data)) {
                    List<String> viagem = r.getViagem();
                    datasLock.readLock().unlock();
                    voosLock.writeLock().lock();
                    for (String id : viagem) {
                        voos.get(id).removePassageiro(data);
                    }
                    voosLock.writeLock().unlock();
                    reservas.remove(codReserva);
                    try {
                        registoTodasReservasCsv();
                    } catch (IOException ignored) {
                    }
                    cancelada = true;
                }
                else {
                    datasLock.readLock().unlock();
                }
            }
        }
        reservasLock.writeLock().unlock();
        return cancelada;
    }
}

class Handler implements Runnable {
    private Socket socket;
    private VoosManager manager;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Utilizador user = null;
    private int idC;

    /**
     * Construtor do Handler.
     * @param socket para conseguir establecer conexão com o cliente;
     * @param manager para gerir os voos/reservas etc...
     * @param idC para identificação estética (logs) do cliente conectado.
     */
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

    private boolean quit() throws IOException {
        dis.close();
        dos.close();
        socket.close();
        System.out.println("C"+idC+": fechando a socket");
        return true;
    }

    private void registo() throws IOException {
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

    private void logout() throws IOException {
        user = null;
        System.out.println("C"+idC+": logout");
    }

    private void login() throws IOException {
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

    private void voos() throws IOException {
        System.out.println("C"+idC+": getting voos");
        VoosList voos = manager.getVoos();
        voos.serialize(dos);
        System.out.println("C"+idC+": mandando os voos");
    }

    private void reservas() throws IOException {
        System.out.println("C"+idC+": getting reservas");
        ReservasList reservas = manager.getReservas(user.getNome());
        reservas.serialize(dos);
        System.out.println("C"+idC+": mandando as reservas");
    }

    private void reserva() throws IOException {
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

    private void cancela() throws IOException {
        System.out.println("C"+idC+": processando cancelamento");
        boolean cancelada;
        int codReserva = dis.readInt();
        cancelada = manager.cancelaReserva(codReserva, user.getNome());
        System.out.println("C"+idC+": mandando ao cliente o booleano");
        dos.writeBoolean(cancelada);
    }

    private void encerra() throws IOException {
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

    private void addvoo() throws IOException {
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


    /**
     * Função run do handler, trata o pedido do cliente.
     */
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
            catch(SocketException se){
                System.out.println("\u001B[31mC"+idC+": Conexão terminada abruptamente!\u001B[0m");
            }
            catch(IOException e){
                e.printStackTrace();
            }
    }
}

public class Server{


    /**
     * Função main do server, fica à escuta por pedidos, e quando recebe inicializa um Handler que tratará do pedido.
     */
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
