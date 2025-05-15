package webapi.dto;

import domain.MemberRole;

public class MemberDtoOutput {
    private Long id;
    private String fullName;
    private String email;
    private MemberRole role;
    private AddressDtoOutput address;

    public MemberDtoOutput(Long id, String fullName, String email, MemberRole role, AddressDtoOutput address) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public AddressDtoOutput getAddress() {
        return address;
    }
    public MemberRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return this.id + " " + this.fullName + " " + this.email + " " + this.role + " " + this.address.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberDtoOutput that = (MemberDtoOutput) o;
        return this.id.equals(that.id) &&
                this.fullName.equals(that.fullName) &&
                this.email.equals(that.email) &&
                this.role.equals(that.role) &&
                this.address.equals(that.address);
    }
}
