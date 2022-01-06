import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String utilizador;
    private int codigo;
    private List<Integer> viagem; //lista de ids de voos
    private LocalDate data;

    public Reserva(List<Integer> viagem,LocalDate data, String utilizador){
        this.codigo = -1; //sem codigo atribuido ainda
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
        this.utilizador = utilizador;
    }
    public List<Integer> getViagem(){
        return this.viagem;
    }
    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int cod){this.codigo = cod;}

    public LocalDate getData() {
        return data;
    }
}
