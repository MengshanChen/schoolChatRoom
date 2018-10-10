public class User {
    private String name;
    private String userId;

	public User() {
        name = "unknown";
        userId = "unknown";
    }
    
    public User(String name, String id) {
        this.name = name;
        userId = id;
	}
    
    public String getName(){
        return name;
    }

    public String getId(){
        return userId;
    }

    public void setName(String userName){
        if(userName == null){
            System.out.println("Error setting your name");
            System.exit(0);
        }else{
            name = userName;
        }
    }

    public void setId(String id){
        if(userId == null){
            System.out.println("Error setting your id");
            System.exit(0);
        }else{
            userId = id;
        }
    }

    public String toString(){
        return (getId() + ":" +getName());
    }
    
}