    package com.switchfully.apps.eurder_db.domain;

    import jakarta.persistence.*;

    import java.util.ArrayList;
    import java.util.List;

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
        @Column(name = "status", nullable = false)
        private EurderStatus status = EurderStatus.CART;

        public Eurder() {
        }
        public Eurder(Long memberId) {
            this.memberId = memberId;
        }

        public double calculateEurderPrice() {
           return this.itemGroups.stream()
                    .mapToDouble(ItemGroup::calculateCurrentSubtotalPrice)
                    .sum();
        }

        public double calculateEurderPriceFinalized() {
            return this.getItemGroups().stream()
                    .mapToDouble(ItemGroup::getTotalPriceAtEurderDate)
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

        public void setStatusFinalized() {
            this.status = EurderStatus.FINALIZED;
        }

        public ItemGroup addItemGroup(ItemGroup itemGroup) {
            if(this.status == EurderStatus.CART) {
                this.itemGroups.add(itemGroup);
                return itemGroup;
            } else {
                throw new IllegalArgumentException("Cannot add ItemGroup to finalized Eurder");
            }
        }

        public List<ItemGroup> removeItemGroup(ItemGroup itemGroup) {
            if(this.status == EurderStatus.CART) {
                this.itemGroups.remove(itemGroup);
                return this.itemGroups;
            }else {
                throw new IllegalArgumentException("Cannot remove ItemGroup from finalized Eurder");
            }

        }

        @Override
        public String toString() {
            return this.id+" memberId:"+this.memberId+"\n"+this.itemGroups+"\n"+calculateEurderPrice()+" "+this.status;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Eurder eurder = (Eurder) o;
            return this.id.equals(eurder.id);
        }
    }