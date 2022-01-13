import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String origem;
    private String destino;
    private String utilizador;
    private int codigo;
    private List<String> viagem; //lista de ids de voos
    private LocalDate data;

    public Reserva(String origem, String destino, int codigo, List<String> viagem, LocalDate data, String utilizador) {
        this.origem = origem;
        this.destino = destino;
        this.codigo = codigo;
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
        this.utilizador = utilizador;
    }

    public List<String> getViagem(){
        return new ArrayList<>(this.viagem);
    }

    public String getDestino() {
        return destino;
    }

    public String getOrigem() {
        return origem;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getUtilizador(){
        return utilizador;}

    public LocalDate getData() {
        return data;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(this.origem);
        out.writeUTF(this.destino);
        out.writeUTF(this.utilizador);
        out.writeInt(this.codigo);
        out.writeInt(viagem.size());
        viagem.forEach(k -> {
            try {
                out.writeUTF(k);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        out.writeUTF(this.data.toString());
    }

    public static Reserva deserialize(DataInputStream in) throws IOException {
        String origem = in.readUTF();
        String destino = in.readUTF();
        String utilizador = in.readUTF();
        int codigo = in.readInt();
        int viagemSize = in.readInt();
        List<String> viagem = new ArrayList<>();
        for(int i = 0; i < viagemSize; i++){
            viagem.add(in.readUTF());
        }
        LocalDate data = LocalDate.parse(in.readUTF());
        return new Reserva(origem,destino,codigo,viagem,data,utilizador);
    }

    public String toString(){
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("Código: [").append(codigo).append("]; Viagem: ").append(origem).append("->").append(destino).append(" Voos: { ");
        for(String id : viagem){
            sb.append("[").append(id).append("] ");
        }
        sb.append("}");
        return sb.toString();
    }

    public Reserva clone(){
        return new Reserva(this.origem,this.destino,this.codigo,this.viagem,this.data,this.utilizador);
    }

}
