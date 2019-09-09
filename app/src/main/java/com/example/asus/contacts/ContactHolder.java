package com.example.asus.contacts;

public class ContactHolder {

    public int id;
    public byte[] image;
    public String name, number;

    public ContactHolder(byte[] image, String name, String number) {
        this.image = image;
        this.name = name;
        this.number = number;
    }

    public ContactHolder( int id, byte[] image,String name, String number) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.number = number;
    }
}
