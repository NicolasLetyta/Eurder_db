package webapi.dto;

public class ItemDtoOutput {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    public ItemDtoOutput(Long id, String name, String description, double price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public String toString() {
        return this.id + ": " + this.name + ", " + this.description + ", " + this.price+ ", " + this.stock;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDtoOutput that = (ItemDtoOutput) o;
        return this.id.longValue() == that.id &&
                this.name.equals(that.name) &&
                this.description.equals(that.description) &&
                this.price == that.price &&
                this.stock == that.stock;
    }
}
