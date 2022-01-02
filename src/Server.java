import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class VoosManager {
    private HashMap<String,Utilizador> utilizadores;
    private HashMap<String,Reserva> reservas;
    private HashMap<Integer,Voo> voos;
    private ReentrantLock lock;
    private int lastid;

    public VoosManager() {
        lock = new ReentrantLock();
        utilizadores = new HashMap<>();
        reservas = new HashMap<>();
        voos = new HashMap<>();
        //pre população
        int lastid = 0;
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
        reservas.put(r.getCodigo(),r);
        lock.unlock();
    }
    //TODO: verificar antes de put se ja existe esse id
    public void updateVoos(Voo v){
        lock.lock();
        if(v.getId() > this.lastid) this.lastid = v.getId();
        voos.put(v.getId(),v);
        lock.unlock();
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

    public int getLastid() {
        return lastid;
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
                                int id = manager.getLastid()+1;
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
                        case "reserva" ->{}
                        case "cancela" ->{}
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
