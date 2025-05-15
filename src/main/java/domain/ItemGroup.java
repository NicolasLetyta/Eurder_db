package domain;

import jakarta.persistence.*;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "item_group")
public class ItemGroup {
    @Id
    @SequenceGenerator(sequenceName = "item_group_seq", name = "item_group_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_group_seq")
    private Long id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "shipping_date", nullable = false)
    private LocalDate shippingDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "total_price_eurder_date")
    private double totalPriceAtEurderDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "eurder_id", nullable = false)
    private Eurder eurder;

    public ItemGroup() {
    }
    public ItemGroup(int quantity, Item item, Eurder eurder) {
        this.quantity = quantity;
        this.item = item;
        this.eurder = eurder;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public Item getItem() {
        return item;
    }

    public Eurder getEurder() {
        return eurder;
    }

    public double getTotalPriceAtEurderDate() {
        return totalPriceAtEurderDate;
    }

    public void setTotalPriceAtEurderDate(double totalPriceAtEurderDate) {
        this.totalPriceAtEurderDate = totalPriceAtEurderDate;
    }

    public double calculateCurrentSubtotalPrice() {
        return this.item.getPrice() * this.quantity;
    }

    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }

    @Override
    public String toString() {
        return this.item.getName() +" "+this.eurder.getId()+ " " + this.quantity + " " + this.shippingDate + " " + calculateCurrentSubtotalPrice();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemGroup itemGroup = (ItemGroup) o;
        return this.id.equals(itemGroup.id);
    }

}
