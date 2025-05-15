package webapi.dto;

public class ItemGroupDtoOutput {
    private Long id;
    private String itemName;
    private String itemDescription;
    private int quantity;
    private double subtotalPrice;
    private Long eurderId;

    public ItemGroupDtoOutput(Long id, String itemName, String itemDescription, int quantity, double subtotalPrice, Long eurderId) {
        this.id = id;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.subtotalPrice = subtotalPrice;
        this.eurderId = eurderId;
    }

    public Long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotalPrice() {
        return subtotalPrice;
    }

    public Long getEurderId() {
        return eurderId;
    }

    @Override
    public String toString() {
        return this.id + " " + this.itemName + " " + this.itemDescription + " " + this.quantity + " " + this.subtotalPrice+ " " + this.eurderId;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemGroupDtoOutput that = (ItemGroupDtoOutput) o;
        return this.id.equals(that.id) &&
                this.itemName.equals(that.itemName) &&
                this.itemDescription.equals(that.itemDescription) &&
                this.quantity==that.quantity &&
                this.subtotalPrice==that.subtotalPrice &&
                this.eurderId==that.eurderId;
    }
}
