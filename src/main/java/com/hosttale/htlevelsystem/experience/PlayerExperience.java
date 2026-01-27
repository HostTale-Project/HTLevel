package com.hosttale.htlevelsystem.experience;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

/**
 * Persistent representation of a player's experience.
 * Stored once per player; level is derived from the experience value using the configured formula.
 */
@DatabaseTable(tableName = "player_experience")
public final class PlayerExperience {

    @DatabaseField(id = true, columnName = "player_uuid", dataType = DataType.UUID)
    private UUID playerId;

    @DatabaseField(canBeNull = false, columnName = "experience")
    private long experience;

    @DatabaseField(canBeNull = false, columnName = "created_at", dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @DatabaseField(canBeNull = false, columnName = "updated_at", dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    // ORMLite requires a no-arg constructor
    PlayerExperience() {
    }

    public PlayerExperience(UUID playerId, long experience) {
        this.playerId = playerId;
        this.experience = Math.max(0, experience);
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getExperience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = Math.max(0, experience);
        touch();
    }

    public void addExperience(long delta) {
        this.experience = Math.max(0, this.experience + delta);
        touch();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void touch() {
        this.updatedAt = new Date();
    }
}
