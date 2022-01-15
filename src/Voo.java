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


    /**
     * Construtor do voo.
     * @param id id do voo.
     * @param origem origem do voo.
     * @param destino destino do voo.
     * @param capacidade capacicade do voo.
     * @param lotacoes map das lotacoes por datas do voo.
     */
    public Voo(String id, String origem,String destino,int capacidade,Map<LocalDate,Integer> lotacoes){
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
        this.lotacoes = new HashMap<>(lotacoes);
    }

    /**
     * Construtor clone do voo.
     * @param v Voo
     */
    public Voo(Voo v) {
        this.id = v.id;
        this.origem = v.origem;
        this.destino = v.destino;
        this.capacidade = v.capacidade;
        this.lotacoes = new HashMap<>(v.lotacoes);
    }

    /**
     * Adiciona um passageiro ao voo.
     * @param data data do voo.
     */
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

    /**
     * Remove um passageiro ao voo.
     * @param data data do voo.
     */
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

    /**
     * Get da lotacao do voo.
     * @param data data do voo.
     * @return lotacao do voo.
     */
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

    /**
     * Get id do voo.
     * @return id do voo.
     */
    public String getId() {
        return id;
    }

    /**
     * Set id do voo.
     * @param id do voo.
     */
    public void setId(String id){this.id = id;}

    /**
     * Get capacidade do voo.
     * @return capacidade do voo.
     */
    public int getCapacidade() {
        return capacidade;
    }

    /**
     * Get destino do voo.
     * @return destino do voo.
     */
    public String getDestino() {
        return destino;
    }

    /**
     * Get origem do voo.
     * @return origem do voo.
     */
    public String getOrigem() {
        return origem;
    }

    /**
     * Serialize do voo.
     * @param out Data output stream
     */
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

    /**
     * Deserialize do voo.
     * @param in Data input stream
     * @return Voo voo.
     */
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

    /**
     * Método toString do voo.
     * @return voo em formato String.
     */
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

    /**
     * Método clone do voo.
     * @return voo clonado.
     */
    public Voo clone(){
        return new Voo(this);
    }
}
