package domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "eurder")
public class Eurder{

    @Id
    @SequenceGenerator(sequenceName = "eurder_seq", name = "eurder_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eurder_seq")
    private Long id;

    @OneToMany(mappedBy = "eurder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemGroup> itemGroups = new ArrayList<>();

    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EurderStatus status;

    @Transient
    private double eurderPrice;

    public Eurder() {
    }
    public Eurder(List<ItemGroup> itemGroups, Long memberId) {
        this.itemGroups = itemGroups;
        this.memberId = memberId;
        calculateEurderPrice();
    }

    @PrePersist
    @PreUpdate
    private void calculateEurderPrice() {
        this.eurderPrice = this.itemGroups.stream()
                .mapToDouble(ItemGroup::getSubtotalPrice)
                .sum();
    }

    public long getId() {
        return id;
    }

    public List<ItemGroup> getItemGroups() {
        return itemGroups;
    }

    public Long getMemberId() {
        return memberId;
    }
    public EurderStatus getStatus() {
        return status;
    }
    public double getEurderPrice() {
        return eurderPrice;
    }

    public ItemGroup addItemGroup(ItemGroup itemGroup) {
        this.itemGroups.add(itemGroup);
        return itemGroup;
    }

    public List<ItemGroup> removeItemGroup(ItemGroup itemGroup) {
        this.itemGroups.remove(itemGroup);
        return this.itemGroups;
    }

    @Override
    public String toString() {
        return this.id+"\n"+this.itemGroups+"\n"+this.eurderPrice+" "+this.status;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Eurder eurder = (Eurder) o;
        return this.id.equals(eurder.id);
    }
}