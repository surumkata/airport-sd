import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class FramedConnection implements AutoCloseable {
    private ReentrantLock outLock = new ReentrantLock();
    private ReentrantLock inLock = new ReentrantLock();;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public FramedConnection(Socket socket) throws IOException {;
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());

    }
    public void send(byte[] data) throws IOException {
        this.outLock.lock();
        this.out.writeInt(data.length);
        this.out.write(data);
        this.out.flush();
        this.outLock.unlock();    }

    public byte[] receive() throws IOException {
        try{
            this.inLock.lock();
            byte[] res = new byte[this.in.readInt()];
            this.in.readFully(res);
            return res;
        }
        finally {
            this.inLock.unlock();
        }
    }
    public void close() throws IOException {
        this.outLock.lock();
        this.inLock.lock();

        this.out.close();
        this.in.close();
        this.socket.close();

        this.outLock.unlock();
        this.inLock.unlock();
    }
}
