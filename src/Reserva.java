import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String utilizador;
    private int codigo;
    private List<Integer> viagem; //lista de ids de voos
    private LocalDateTime data;

    public Reserva(int codigo,List<Integer> viagem,LocalDateTime data, String utilizador){
        this.codigo = codigo;
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
        this.utilizador = utilizador;
    }

    public int getCodigo() {
        return codigo;
    }

    public LocalDateTime getData() {
        return data;
    }
}
