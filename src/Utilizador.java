public class Utilizador {
    private String nome;
    private String password;
    private boolean adminPermission;

    /**
     * Construtor do Utilizador.
     * @param nome nome do utilizador.
     * @param password password do utilizador.
     * @param adminPermission true se for um utilizador admin, false caso contrário.
     */
    public Utilizador(String nome,String password,boolean adminPermission){
        this.nome = nome;
        this.password = password;
        this.adminPermission = adminPermission;
    }

    /**
     * Get do nome do utilizador.
     * @return nome do utilizador.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Get da password do utilizador.
     * @return password do utilizador.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Verifica se o utilizador é admin.
     * @return true se for admin, false caso contrário.
     */
    public boolean isAdmin() {
        return adminPermission;
    }

}
