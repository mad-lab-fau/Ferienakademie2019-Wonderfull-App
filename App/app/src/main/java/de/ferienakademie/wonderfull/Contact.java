package de.ferienakademie.wonderfull;

public class Contact {

    private String name;
    private String phone;
    private int id;

    Contact(){}

    Contact(String name, String phone, int id){
        this.name = name;
        this.phone = phone;
        this.id = id;
    }


    public String getName(){
        return name;
    }

    public String getPhone(){
        return phone;
    }

    public int getId(){
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public void setId(int id) {
        this.id = id;
    }
}
