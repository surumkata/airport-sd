import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String utilizador;
    private int codigo;
    private List<Integer> viagem; //lista de ids de voos
    private LocalDate data;

    public Reserva(int codigo,List<Integer> viagem,LocalDate data, String utilizador){
        this.codigo = codigo;
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
        this.utilizador = utilizador;
    }

    public int getCodigo() {
        return codigo;
    }

    public LocalDate getData() {
        return data;
    }
}
