package galacy.galacyhcf.models;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import galacy.galacyhcf.GalacyHCF;
import galacy.galacyhcf.providers.MySQL;
import galacy.galacyhcf.providers.SQLStatements;
import galacy.galacyhcf.utils.Utils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Faction {
    public MySQL mysql;

    public int id;
    public Date createdAt;
    public Date updatedAt;
    public String name;
    public int balance;
    public double dtr;
    public double maxDtr;
    public String home;
    public String leaderId;

    public Faction(MySQL mysql, int factionId) {
        this.mysql = mysql;
        String sql = SQLStatements.factionById.replace("$id", String.valueOf(factionId));
        try {
            ResultSet result = mysql.query(sql);
            if (result.next()) {
                id = factionId;
                createdAt = result.getDate("created_at");
                updatedAt = result.getDate("updated_at");
                name = result.getString("name");
                balance = result.getInt("balance");
                dtr = Math.round(result.getDouble("dtr") * 100.0) / 100.0;
                maxDtr = result.getDouble("max_dtr");
                home = result.getString("home");
                leaderId = result.getString("leader_id");
            } else {
                GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Couldn't find the faction.");
            }
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues finding faction by id: " + e);
        }
    }

    public Faction(MySQL mysql, String factionName) {
        this.mysql = mysql;
        String sql = SQLStatements.factionByName.replace("$name", factionName);
        try {
            ResultSet result = mysql.query(sql);
            if (result.next()) {
                id = result.getInt("id");
                createdAt = result.getDate("created_at");
                updatedAt = result.getDate("updated_at");
                name = factionName;
                balance = result.getInt("balance");
                dtr = Math.round(result.getDouble("dtr") * 100.0) / 100.0;
                maxDtr = result.getDouble("max_dtr");
                home = result.getString("home");
                leaderId = result.getString("leader_id");
            } else {
                GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Couldn't find the faction.");
            }
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues finding faction by name: " + e);
        }
    }

    public Faction(MySQL mysql, ResultSet result) {
        this.mysql = mysql;
        try {
            id = result.getInt("id");
            createdAt = result.getDate("created_at");
            updatedAt = result.getDate("updated_at");
            name = result.getString("name");
            balance = result.getInt("balance");
            dtr = result.getDouble("dtr");
            maxDtr = result.getDouble("max_dtr");
            home = result.getString("home");
            leaderId = result.getString("leader_id");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String leaderName() {
        try {
            ResultSet result = mysql.query(SQLStatements.playerByFactionId.replace("$faction_id", String.valueOf(id)));
            if (result.next()) {
                return result.getString("username");
            } else {
                return "ERROR";
            }
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues getting leader name: " + e);
        }

        return "ERROR";
    }

    public void disband() {
        try {
            mysql.exec(SQLStatements.disbandFactionByName.replace("$name", name));
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues deleting faction by name: " + e);
        }

        String currentTime = Utils.dateFormat.format(new java.util.Date());
        try {
            mysql.exec(SQLStatements.removeAllMembersById.
                    replace("$faction_id", String.valueOf(id)).
                    replace("$updated_at", currentTime));
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues removing members after disband: " + e);
        }

        for (GPlayer player : onlineMembers()) {
            player.factionId = 0;
        }

        GalacyHCF.claimsManager.deleteFactionClaims(id);
    }

    public List<GPlayer> onlineMembers() {
        List<GPlayer> players = new ArrayList<>();

        for (Player player : GalacyHCF.instance.getServer().getOnlinePlayers().values()) {
            if (player instanceof GPlayer) {
                if (((GPlayer) player).factionId == id) players.add((GPlayer) player);
            }
        }

        return players;
    }

    public void updateLeader(GPlayer player) {
        String currentTime = Utils.dateFormat.format(new java.util.Date());
        String sql = SQLStatements.updateLeaderById.
                replace("$leader_id", player.xuid).
                replace("$updated_at", currentTime).
                replace("$id", String.valueOf(id));
        try {
            mysql.exec(sql);
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues deleting faction by name: " + e);
        }
    }

    public void updateBalance(int newBalance) {
        try {
            GalacyHCF.mysql.exec(SQLStatements.updateFactionBalanceById.
                    replace("$balance", String.valueOf(newBalance)).
                    replace("$id", String.valueOf(id)).
                    replace("$updated_at", Utils.dateFormat.format(new java.util.Date())));
            balance = newBalance;
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues updating faction balance: " + e);
        }
    }

    public ArrayList<String> members() {
        ArrayList<String> members = new ArrayList<>();

        try {
            ResultSet results = GalacyHCF.mysql.query(SQLStatements.factionMembersById.
                    replace("$faction_id", String.valueOf(id)));
            while (results.next()) {
                members.add(results.getString("username"));
            }
            results.close();
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues getting faction members: " + e);
        }

        return members;
    }

    public void updateHome(int x, int y, int z) {
        home = x + "," + y + "," + z;
        try {
            GalacyHCF.mysql.exec(SQLStatements.updateHomeById.
                    replace("$id", String.valueOf(id)).
                    replace("$home", home).
                    replace("$updated_at", Utils.dateFormat.format(new java.util.Date())));
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues updating faction balance: " + e);
        }
    }

    public double maxDtr() {
        int count = members().toArray().length;
        if (count > 5)
            return 6.01;
        return count + 0.01;
    }

    public void updateDtr(double dtr) {
        try {
            GalacyHCF.mysql.exec(SQLStatements.updateDtrById.
                    replace("$id", String.valueOf(id)).
                    replace("$dtr", String.valueOf(dtr)).
                    replace("$updated_at", Utils.dateFormat.format(new java.util.Date())));
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues updating faction dtr: " + e);
        }
    }

    public void updateMaxDtr() {
        try {
            maxDtr = maxDtr();
            if (dtr > maxDtr) updateDtr(maxDtr);
            GalacyHCF.mysql.exec(SQLStatements.updateMaxDtrById.
                    replace("$id", String.valueOf(id)).
                    replace("$max_dtr", String.valueOf(maxDtr)).
                    replace("$updated_at", Utils.dateFormat.format(new java.util.Date())));
        } catch (SQLException e) {
            GalacyHCF.instance.getLogger().info(TextFormat.RED + "[MySQL]: Had issues updating faction max dtr: " + e);
        }
    }
}
