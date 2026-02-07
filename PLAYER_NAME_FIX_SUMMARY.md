# Summary: Fix for Player Name Display Issue on Bungeecord Plan

## Issue Resolved
**Problem**: Bungeecord側のPlanに正しくプレイヤー名が表示されていません  
**English**: Player names were not displaying correctly on the Bungeecord side of Plan

## What Was Wrong

### Symptom
In the Bungeecord Plan network page, the consecutive login day rankings showed UUID fragments like "12345678..." instead of actual player names like "PlayerName".

### Root Cause Analysis
The bug was in the `MySqlStorage.updatePlayerName()` method:

**Original Code (Broken)**:
```java
String sql = "UPDATE " + tableName + " SET player_name = ? WHERE uuid = ?";
```

**The Problem**:
1. When a player joins a Bukkit server, `EventListener.onPlayerJoin()` calls `updatePlayerName()` 
2. For new players, no row exists in the database yet
3. The UPDATE statement executes but matches 0 rows
4. MySQL returns success but updates 0 rows (silent failure)
5. Player name is never stored
6. Bungeecord Plan can't find the name and falls back to UUID fragment

### Why It Happened
The `updatePlayerName()` method was added in v1.5.0 to support Bungeecord Plan display, but it was implemented as a simple UPDATE instead of using the INSERT/UPDATE pattern used by other methods like `setCumulative()`.

## The Fix

### Changed Code
```java
// INSERT ... ON DUPLICATE KEY UPDATE を使用して、行が存在しない場合は作成、存在する場合は更新
String sql = "INSERT INTO " + tableName + " (uuid, player_name) VALUES (?, ?) " +
        "ON DUPLICATE KEY UPDATE player_name = ?";
```

### How It Works Now
1. Tries to INSERT a new row with UUID and player_name
2. If UUID already exists (DUPLICATE KEY), updates the player_name instead
3. Always succeeds in storing the player name, whether player is new or existing

### SQL Behavior
```sql
-- New player scenario
INSERT INTO player_data (uuid, player_name) VALUES ('uuid-here', 'PlayerName')
-- Result: New row created with player_name

-- Existing player scenario  
INSERT INTO player_data (uuid, player_name) VALUES ('uuid-here', 'NewName')
ON DUPLICATE KEY UPDATE player_name = 'NewName'
-- Result: Existing row updated with new player_name
```

## Files Modified
- `src/main/java/me/kubota6646/loginbonus/storage/MySqlStorage.java`
  - Modified `updatePlayerName()` method (lines 416-430)
  - Changed from UPDATE to INSERT ON DUPLICATE KEY UPDATE

## Impact Assessment

### Positive Impact
✅ Player names now display correctly in Bungeecord Plan rankings  
✅ Works for both new and existing players  
✅ No manual database migration needed  
✅ Backward compatible with existing data  

### Risk Assessment
🟢 **Low Risk**
- Only affects MySQL storage (used for Bungeecord)
- Uses same pattern as existing `setCumulative()` method
- Well-tested SQL pattern in MySQL
- Uses prepared statements (secure)
- Synchronized method (thread-safe)

### Testing Scenarios
1. **New Player**: Join Bukkit server → Name stored immediately → Shows in Bungeecord Plan
2. **Existing Player**: Re-join Bukkit server → Name updated → Shows in Bungeecord Plan  
3. **Name Change**: Player changes name → Updated on next join → Shows new name
4. **Offline Players**: Historical data remains accessible via UUID

## Verification Steps
To verify the fix is working:

1. **On Bukkit Server**:
   - Player joins
   - Check logs: "MySQLデータベースに接続しました"
   - No errors about player name update

2. **In MySQL Database**:
   ```sql
   SELECT uuid, player_name FROM player_data;
   ```
   Should show actual player names, not NULL values

3. **On Bungeecord Plan**:
   - Navigate to Network page
   - Check LoginBonus section
   - Consecutive login ranking should show player names
   - Not UUID fragments

## Related Files
- `FIX_PLAYER_NAME_DISPLAY.md` - Detailed technical explanation
- `VERSION_1.5.0_CHANGES.md` - Version changelog
- `IMPLEMENTATION_SUMMARY.md` - Overall implementation docs

## Commit
- Commit: 8fc7a04
- Message: "Fix player name display on Bungeecord Plan by using INSERT ON DUPLICATE KEY UPDATE"
- Branch: copilot/add-login-streak-display-bungeecord
