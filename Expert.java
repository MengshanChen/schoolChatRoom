public class Expert extends User {
    private String password;

    public Expert(){
        super();
        password = "unknown";
    }

    public Expert(String name, String id, String password){
        super(name, id);
        this.password = password;
    }

    public String getDepartment(){
        return password;
    }

    public void setDepartment(String password){
        this.password = password;
    }
}