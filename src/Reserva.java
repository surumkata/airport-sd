import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String utilizador;
    private int codigo;
    private List<Integer> viagem; //lista de ids de voos
    private LocalDate data;

    public Reserva(int codigo,List<Integer> viagem,LocalDate data){
        this.codigo = codigo;
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
    }

    public int getCodigo() {
        return codigo;
    }

    public LocalDate getData() {
        return data;
    }
}
