# Fix: Player Names Not Displaying on Bungeecord Plan

## Problem
プレイヤー名がBungeecord側のPlanに正しく表示されていない問題が発生していました。

Player names were not being displayed correctly on the Bungeecord side of Plan. Instead, players would appear as UUID fragments (e.g., "12345678...") in the consecutive login day rankings.

## Root Cause
The issue was in `MySqlStorage.updatePlayerName()` which used a simple `UPDATE` statement:

```java
String sql = "UPDATE " + tableName + " SET player_name = ? WHERE uuid = ?";
```

**The problem**: This UPDATE statement only works if the player row already exists in the database. 

**What happens**:
1. A new player joins a Bukkit server
2. `EventListener.onPlayerJoin()` is called
3. `updatePlayerName()` is called FIRST to save the player name
4. The UPDATE statement executes but finds no matching row (player is new)
5. The UPDATE fails silently (no row affected)
6. The player name is never stored in the database
7. Later when Bungeecord queries the database, it finds no player_name and falls back to UUID fragment

## Solution
Changed `updatePlayerName()` to use the `INSERT ... ON DUPLICATE KEY UPDATE` pattern, similar to how `setCumulative()` works:

```java
String sql = "INSERT INTO " + tableName + " (uuid, player_name) VALUES (?, ?) " +
        "ON DUPLICATE KEY UPDATE player_name = ?";
```

**How this works**:
1. Tries to INSERT a new row with uuid and player_name
2. If the row already exists (duplicate key), it UPDATEs the player_name instead
3. This ensures the player name is always stored, whether it's a new player or existing player

## Technical Details

### Before (Broken)
```sql
UPDATE player_data SET player_name = 'PlayerName' WHERE uuid = '12345-67890-...'
-- If uuid doesn't exist: 0 rows affected, fails silently
```

### After (Fixed)
```sql
INSERT INTO player_data (uuid, player_name) VALUES ('12345-67890-...', 'PlayerName')
ON DUPLICATE KEY UPDATE player_name = 'PlayerName'
-- If uuid doesn't exist: creates new row with player_name
-- If uuid exists: updates player_name
```

## Impact
- **Fixes**: Player names now display correctly in Bungeecord Plan rankings
- **Scope**: Only affects MySQL storage (used for Bungeecord integration)
- **Backward Compatible**: Works with existing data, no migration needed
- **Safe**: Uses prepared statements, maintains existing security measures

## Testing
After this fix:
1. New players joining Bukkit servers will have their names stored immediately
2. Existing players will have their names updated on next login
3. Bungeecord Plan will display actual player names instead of UUID fragments
4. The ranking table will show: "PlayerName" instead of "12345678..."
