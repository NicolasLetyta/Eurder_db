package repository;

import java.util.List;

public interface EurderProjected {
    public Long getId();

    default List<ItemGroupProjected> getItemGroupsProjected() {

    }
}
