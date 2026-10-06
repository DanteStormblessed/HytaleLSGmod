package dev.hytalemodding.models;

public class PlayerAttributePoints {
    private int id_players;
    private String player_name;
    private int id_attributes;
    private String attribute_name;
    private int balance_ledger;
    private int snapshot_points;
    private int diff_ledger_minus_snapshot;

    public int getPlayerId() {
        return id_players;
    }

    public String getPlayerName() {
        return player_name;
    }

    public int getAttributeId() {
        return id_attributes;
    }

    public String getAttributeName() {
        return attribute_name;
    }

    public int getBalanceLedger() {
        return balance_ledger;
    }

    public int getSnapshotPoints() {
        return snapshot_points;
    }

    public int getDiffLedgerMinusSnapshot() {
        return diff_ledger_minus_snapshot;
    }
}
