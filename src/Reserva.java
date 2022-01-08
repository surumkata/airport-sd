import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String utilizador;
    private int codigo;
    private List<String> viagem; //lista de ids de voos
    private LocalDate data;

    public Reserva(List<String> viagem,LocalDate data, String utilizador){
        this.codigo = -1; //sem codigo atribuido ainda
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
        this.utilizador = utilizador;
    }

    public Reserva(int codigo, List<String> viagem, LocalDate data, String utilizador) {
        this.codigo = codigo;
        this.viagem = new ArrayList<>(viagem);
        this.data = data;
        this.utilizador = utilizador;
    }

    public List<String> getViagem(){
        return new ArrayList<>(this.viagem);
    }
    public int getCodigo() {
        return codigo;
    }

    public String getUtilizador(){
        return utilizador;}

    public void setCodigo(int cod){

        this.codigo = cod;
    }

    public LocalDate getData() {
        return data;
    }

    public Reserva clone(){
        return new Reserva(this.codigo,this.viagem,this.data,this.utilizador);
    }

}
