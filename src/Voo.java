import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Voo {
    private int id;
    private final String origem;
    private final String destino;
    private final int capacidade;
    private int lotacao;


    public Voo(int id, String origem,String destino,int capacidade,int lotacao){
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
        this.lotacao = lotacao;
    }

    public void addPassageiro(){
        if(lotacao < capacidade){
            lotacao++;
        }
    }

    public int getLotacao() {
        return lotacao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id){this.id = id;}

    public int getCapacidade() {
        return capacidade;
    }

    public String getDestino() {
        return destino;
    }

    public String getOrigem() {
        return origem;
    }

    public void addLotacao(int lotacao){
        this.lotacao += lotacao;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(this.id);
        out.writeUTF(this.origem);
        out.writeUTF(this.destino);
        out.writeInt(this.capacidade);
        out.writeInt(this.lotacao);
    }

    public static Voo deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        String origem = in.readUTF();
        String destino = in.readUTF();
        int capacidade = in.readInt();
        int lotacao = in.readInt();

        return new Voo(id, origem, destino, capacidade,lotacao);
    }

    public String toString(){
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append(this.origem).append("->").append(this.destino).append(" [").append(lotacao).append("/").append(capacidade).append("]");
        return sb.toString();
    }
/*
    public Voo clone(){
        return new Voo(this.id,this.origem,this.destino,this.capacidade);
    }*/
}
