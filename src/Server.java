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
    private HashMap<String,Voo> voos;
    private ReentrantLock lock;

    public VoosManager() {
        lock = new ReentrantLock();
        utilizadores = new HashMap<>();
        reservas = new HashMap<>();
        voos = new HashMap<>();
        //pre população
        updateVoos(new Voo("1","Porto","Lisboa",150));
        updateVoos(new Voo("2","Madrid","Lisboa",150));
        updateVoos(new Voo("3","Lisboa","Tokyo",150));
        updateVoos(new Voo("4","Barcelona","Paris",150));
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
    public void updateVoos(Voo v){
        lock.lock();
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
}

class Handler implements Runnable {
    private Socket socket;
    private VoosManager manager;

    public Handler (Socket socket, VoosManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    // @TODO
    @Override
    public void run() {
        VoosList voos = manager.getVoos();
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            voos.serialize(dos);
            dos.flush();
            dos.close();
        } catch (IOException e) {
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
