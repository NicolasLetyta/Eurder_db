package com.switchfully.apps.eurder_db.webapi.dto;

public class MemberDtoInput {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private AddressDtoInput addressInput;
    public MemberDtoInput(String firstName, String lastName, String email, String password, String phone, AddressDtoInput addressInput) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.addressInput = addressInput;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public AddressDtoInput getAddressInput() {
        return addressInput;
    }
}
