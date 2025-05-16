package com.switchfully.apps.eurder_db.domain;

public enum MemberRole {
    ADMIN(0),
    CUSTOMER(1);

    private final int rank;
    MemberRole(int rank) {
        this.rank = rank;
    }
    public int getRank() {
        return rank;
    }
}
