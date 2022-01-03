import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class VoosManager {
    private HashMap<String,Utilizador> utilizadores;
    private HashMap<Integer,Reserva> reservas;
    private HashMap<Integer,Voo> voos;
    private ReentrantLock lock;
    private int lastidVoo;
    private int lastidReserva;

    public VoosManager() {
        lock = new ReentrantLock();
        utilizadores = new HashMap<>();
        reservas = new HashMap<>();
        voos = new HashMap<>();
        //pre população
        this.lastidVoo = 0;
        this.lastidReserva = 0;
        updateVoos(new Voo(1,"Porto","Lisboa",150));
        updateVoos(new Voo(2,"Madrid","Lisboa",150));
        updateVoos(new Voo(3,"Lisboa","Tokyo",150));
        updateVoos(new Voo(4,"Barcelona","Paris",150));
    }

    public void updateUtilizadores(Utilizador u) {
        lock.lock();
        utilizadores.put(u.getNome(),u);
        lock.unlock();
    }

    public void updateReservas(Reserva r){
        lock.lock();
        if(r.getCodigo() > this.lastidReserva) this.lastidReserva = r.getCodigo();
        reservas.put(r.getCodigo(),r);
        System.out.println(r.getCodigo());
        lock.unlock();
    }
    public void removeReserva(String codReserva){
        lock.lock();
        reservas.remove(codReserva);
        lock.unlock();
    }
    //TODO: verificar antes de put se ja existe esse id
    public void updateVoos(Voo v){
        lock.lock();
        if(v.getId() > this.lastidVoo) this.lastidVoo = v.getId();
        voos.put(v.getId(),v);
        lock.unlock();
    }
    public int existsVoo(String origem,String destino){
        for(Voo v : voos.values()){
            if(v.getOrigem().equals(origem) && v.getDestino().equals(destino)){
                return v.getId();
            }

        }
        return -1;
    }

    public VoosList getVoos () {
        try{
            lock.lock();
            VoosList ret = new VoosList();
            ret.addAll(voos.values());
            return ret;
        }
        finally {
            lock.unlock();
        }
    }

    public int getLastidVoo() {
        return lastidVoo;
    }
    public int getLastidReserva(){
        return lastidReserva;
    }
}

class Handler implements Runnable {
    private Socket socket;
    private VoosManager manager;
    private DataInputStream dis;
    private DataOutputStream dos;

    public Handler (Socket socket, VoosManager manager){
        this.socket = socket;
        this.manager = manager;
        try{
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    public void run() {
            try{
                boolean finish = false;
                while(!finish) {
                    String command = dis.readUTF();
                    System.out.println("comando recebido: "+command);
                    switch (command){
                        case "voos" ->{
                            VoosList voos = manager.getVoos();
                            voos.serialize(dos);
                        }
                        case "reservas" ->{

                        }
                        case "registo" ->{}
                        case "login" ->{}
                        case "addvoo" ->{ //TODO: apenas admin pode
                            boolean validoOD = true;
                            boolean validoC = true;
                            String origem = dis.readUTF();
                            String destino = dis.readUTF();
                            String Scapacidade = dis.readUTF();
                            StringBuilder sb;
                            sb = new StringBuilder();
                            if(origem.equals(destino)){
                                validoOD = false;
                            }
                            try{
                                int capacidade = Integer.parseInt(Scapacidade);
                                int id = manager.getLastidVoo()+1;
                                if(capacidade < 100 || capacidade > 250){
                                    validoC = false;
                                }
                                if(validoC && validoOD) {
                                    manager.updateVoos(new Voo(id, origem, destino, capacidade));
                                    sb.append("O voo ").append(origem).append(" -> ").append(destino).append(" com a capacidade de ").append(capacidade).append(" passageiros, foi registado com o id: ").append(id).append(".");
                                }
                                else if(!validoC){
                                    sb.append("Erro ao registar voo: ").append(capacidade).append(" não é uma capacidade válida, experimente [100-250]");
                                }
                                else
                                    sb.append("Erro ao registar voo: Origem e destino inválidos");
                            }
                            catch (NumberFormatException e){
                                sb.append("Erro ao registar voo: ").append(Scapacidade).append(" não é uma capacidade válida");
                            }
                            finally {
                                dos.writeUTF(sb.toString());
                                dos.flush();
                            }
                        }
                        case "encerra" ->{}
                        case "reserva" ->{
                            String[] viagem = dis.readUTF().split(";");
                            String[] datas = dis.readUTF().split(";");
                            boolean valido = true;
                            List<Integer> idsVoos = new ArrayList<>();
                            List<LocalDate> datasVoos = new ArrayList<>();
                            if(viagem.length>=2){
                                for(int i = 0;i< viagem.length-1 && valido;i++){
                                    int id;
                                    if((id = manager.existsVoo(viagem[i],viagem[i+1]))!=-1 ){
                                        idsVoos.add(id);
                                    }else {
                                        valido = false;
                                    }

                                    for(String d : datas){
                                        datasVoos.add(LocalDate.parse(d));
                                    }
                                }
                                //sistema está a escolher a primeira data do intervalo
                                manager.updateReservas(new Reserva(manager.getLastidReserva(),idsVoos,datasVoos.get(0)));

                            }


                        }
                        case "cancela" ->{
                            String codReserva = dis.readUTF();
                            manager.removeReserva(codReserva);
                            dos.writeUTF("Reserva "+codReserva+" cancelada");

                        }
                        case "quit" -> {
                            finish = true;
                            dis.close();
                            dos.close();
                            socket.close();
                        }
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
        VoosManager manager = new VoosManager();

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new Handler(socket, manager));
            worker.start();
        }
    }

}
