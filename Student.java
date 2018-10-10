public class Student extends User {
    private String email;

    public Student(){
        super();
        email = "unknown";
    }

    public Student(String name, String id, String email){
        super(name, id);
        if(email.length() < 10){
            System.out.println("Fail to set the email");
        }else{
            this.email = email;
        }
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        if(email.length() < 10){
            System.out.println("Error setting the email");
            System.exit(0);
        }else{
            this.email = email;
        }
    }
}