import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class ReservasList extends ArrayList<Reserva> {

    /**
     * Serialize da lista de reservas.
     * @param out Data output stream.
     */
    public void serialize (DataOutputStream out) throws IOException {
        out.writeInt(this.size());
        for(Reserva r : this)
            r.serialize(out);
    }

    /**
     * Deserialize da lista de reservas.
     * @param in Data input stream.
     * @return lista de reservas.
     */
    public static ReservasList deserialize (DataInputStream in) throws IOException {
        ReservasList cl = new ReservasList();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            cl.add(Reserva.deserialize(in));
        }
        return cl;
    }

    /**
     * Método toString da lista de reservas.
     * @return lista de reservas no formato String.
     */
    public String toString(){
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("*** Reservas ***\n");
        for(Reserva r : this){
            sb.append(r).append("\n");
        }
        return sb.toString();
    }

}
