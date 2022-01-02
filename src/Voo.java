import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Voo {
    private int id;
    private String origem;
    private String destino;
    private int capacidade;


    public Voo(int id,String origem,String destino,int capacidade){
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
    }

    public int getId() {
        return id;
    }

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
        out.writeInt(this.id);
        out.writeUTF(this.origem);
        out.writeUTF(this.destino);
        out.writeInt(this.capacidade);
    }

    public static Voo deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        String origem = in.readUTF();
        String destino = in.readUTF();
        int capacidade = in.readInt();

        return new Voo(id,origem, destino, capacidade);
    }

    public String toString(){
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append(this.origem).append("->").append(this.destino);
        return sb.toString();
    }
/*
    public Voo clone(){
        return new Voo(this.id,this.origem,this.destino,this.capacidade);
    }*/
}
