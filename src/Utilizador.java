public class Utilizador {
    private String nome;
    private String password;
    private int adminPermission;

    public Utilizador(String nome,String password,int adminPermission){
        this.nome = nome;
        this.password = password;
        this.adminPermission = adminPermission;
    }

    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }


    public boolean isAdmin() {
        if (this.adminPermission == 1)
            return true;
        else return false;

    }

}
