import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String utilizador;
    private String codigo;
    private List<String> viagem; //lista de ids de voos
    private LocalDateTime data;

    public Reserva(String codigo,List<String> viagem,LocalDateTime data){
        this.codigo = codigo;
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getData() {
        return data;
    }
}
