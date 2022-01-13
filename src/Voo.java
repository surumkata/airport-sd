import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Voo {
    private String id;
    private final String origem;
    private final String destino;
    private final int capacidade;
    private Map<LocalDate,Integer> lotacoes;
    ReentrantLock lockLotacao = new ReentrantLock();


    public Voo(String id, String origem,String destino,int capacidade,Map<LocalDate,Integer> lotacoes){
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
        this.lotacoes = new HashMap<>(lotacoes);
    }

    public Voo(Voo v) {
        this.id = v.id;
        this.origem = v.origem;
        this.destino = v.destino;
        this.capacidade = v.capacidade;
        this.lotacoes = new HashMap<>(v.lotacoes);
    }

    public void addPassageiro(LocalDate data){
        lockLotacao.lock();
        if(lotacoes.containsKey(data)){
            int lot = lotacoes.get(data)+1;
            lotacoes.put(data,lot);
        }
        else{
            lotacoes.put(data,1);
        }
        lockLotacao.unlock();
    }

    public void removePassageiro(LocalDate data){
        lockLotacao.lock();
        if(lotacoes.containsKey(data)){
            int lot = lotacoes.get(data)-1;
            lotacoes.put(data,lot);
            if(lotacoes.get(data) == 0)
                lotacoes.remove(data);
        }
        lockLotacao.unlock();
    }

    public int getLotacao(LocalDate data) {
        try{
            lockLotacao.lock();
            if(lotacoes.containsKey(data))
                return lotacoes.get(data);
            return 0;
        }
        finally {
            lockLotacao.unlock();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id){this.id = id;}

    public int getCapacidade() {
        return capacidade;
    }

    public String getDestino() {
        return destino;
    }

    public String getOrigem() {
        return origem;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(this.id);
        out.writeUTF(this.origem);
        out.writeUTF(this.destino);
        out.writeInt(this.capacidade);
        out.writeInt(this.lotacoes.size());
        lotacoes.forEach((k,v) -> {
            try{
                out.writeUTF(k.toString());
                out.writeInt(v);
            }
            catch(IOException ignored){}
        });

    }

    public static Voo deserialize(DataInputStream in) throws IOException {
        String id = in.readUTF();
        String origem = in.readUTF();
        String destino = in.readUTF();
        int capacidade = in.readInt();
        int sizeLotacoes = in.readInt();
        Map<LocalDate,Integer> lotacoes = new HashMap<>();
        for(int i = 0; i < sizeLotacoes; i++){
            try{
                lotacoes.put(LocalDate.parse(in.readUTF()),in.readInt());
            }
            catch(DateTimeParseException ignored){}
        }
        return new Voo(id, origem, destino, capacidade,lotacoes);
    }

    public String toString(){
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append(this.origem).append("->").append(this.destino).append(" { ");
        lotacoes.forEach((k,v) -> {
            sb.append("[").append(k.toString()).append(": ");
            sb.append(v).append("/").append(capacidade).append("] ");
        });
        sb.append("}.");
        return sb.toString();
    }

    public Voo clone(){
        return new Voo(this);
    }
}
