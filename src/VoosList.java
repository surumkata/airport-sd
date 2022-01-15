import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class VoosList extends ArrayList<Voo> {

    /**
     * Serialize da lista de voos.
     * @param out data output stream.
     */
    public void serialize (DataOutputStream out) throws IOException {
        out.writeInt(this.size());
        for(Voo i : this)
            i.serialize(out);
    }

    /**
     * Deserialize da lista de voos.
     * @param in Data input stream.
     * @return lista de voos.
     * @throws IOException
     */
    public static VoosList deserialize (DataInputStream in) throws IOException {
        VoosList cl = new VoosList();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            cl.add(Voo.deserialize(in));
        }
        return cl;
    }

    /**
     * Método toString da lista de voos.
     * @return lista de voos no formato String.
     */
    public String toString(){
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("*** Voos ***\n");
        for(Voo i : this){
            sb.append(i).append("\n");
        }
        return sb.toString();
    }

}
