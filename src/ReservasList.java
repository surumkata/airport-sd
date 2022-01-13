import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class ReservasList extends ArrayList<Reserva> {

    public void serialize (DataOutputStream out) throws IOException {
        out.writeInt(this.size());
        for(Reserva r : this)
            r.serialize(out);
    }
    public static ReservasList deserialize (DataInputStream in) throws IOException {
        ReservasList cl = new ReservasList();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            cl.add(Reserva.deserialize(in));
        }
        return cl;
    }

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
