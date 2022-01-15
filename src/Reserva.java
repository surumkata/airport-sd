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

    /**
     * Construtor da Reserva.
     * @param origem origem da reserva.
     * @param destino destino da reserva.
     * @param codigo codigo da reserva.
     * @param viagem lista dos identificadores de voos que fazem a viagem.
     * @param data data da reserva.
     * @param utilizador nome do utilizador da reserva.
     */
    public Reserva(String origem, String destino, int codigo, List<String> viagem, LocalDate data, String utilizador) {
        this.origem = origem;
        this.destino = destino;
        this.codigo = codigo;
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
        this.utilizador = utilizador;
    }

    /**
     * Get viagem.
     * @return lista de identificadores de todos os voos que fazem a viagem.
     */
    public List<String> getViagem(){
        return new ArrayList<>(this.viagem);
    }

    /**
     * Get Destino.
     * @return destino da reserva.
     */
    public String getDestino() {
        return destino;
    }

    /**
     * Get Origem.
     * @return origem da reserva.
     */
    public String getOrigem() {
        return origem;
    }

    /**
     * Get codigo.
     * @return codigo da reserva.
     */
    public int getCodigo() {
        return codigo;
    }

    /**
     * Get nome do utilizador.
     * @return nome do utilizador da reserva.
     */
    public String getUtilizador(){
        return utilizador;}

    public LocalDate getData() {
        return data;
    }

    /**
     * Serialize da reserva.
     * @param out Data output stream.
     */
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

    /**
     * Deserialize da reserva.
     * @param in Data input stream.
     * @return Reserva reserva.
     */
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

    /**
     * Método toString da reserva.
     * @return Reserva sobre o formato String.
     */
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

    /**
     * Método clone da reserva.
     * @return clone da reserva.
     */
    public Reserva clone(){
        return new Reserva(this.origem,this.destino,this.codigo,this.viagem,this.data,this.utilizador);
    }

}
