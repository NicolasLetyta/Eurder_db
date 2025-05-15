package webapi.dto;

public class AddressDtoOutput {
    private Long id;
    private String street;
    private String streetNumber;
    private String postalCode;
    private String city;
    private String country;
    public AddressDtoOutput(Long id, String street, String streetNumber, String postalCode, String city, String country) {
        this.id = id;
        this.street = street;
        this.streetNumber = streetNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public Long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return this.id+" "+this.street+" "+this.streetNumber+" "+this.postalCode+" "+this.city+" "+this.country;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressDtoOutput that = (AddressDtoOutput) o;
        return this.id.equals(that.id) &&
                this.street.equals(that.street) &&
                this.streetNumber.equals(that.streetNumber) &&
                this.postalCode.equals(that.postalCode) &&
                this.city.equals(that.city) &&
                this.country.equals(that.country);
    }
}
