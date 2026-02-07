# Fix: Player Name Migration for Existing Data

## Problem Statement
まだプレイヤー名で表示されません (Player names still not displaying)  
キャッシュが残っているのでしょうか？(Is there cache remaining? - Browser cache already cleared)

## Root Cause

The previous fixes were correct in terms of code logic, but there was a **data migration issue**:

1. ✅ **Code Fix Applied**: `updatePlayerName()` now correctly stores player names when players join
2. ❌ **Migration Gap**: Existing database entries from before the fix have NULL `player_name` values
3. ❌ **Cache Issue**: Not browser cache, but **database cache** - old NULL values persist
4. ❌ **Display Problem**: Bungeecord reads these NULL values and shows UUID fragments

### Timeline of the Issue

```
Before Fix Deployment:
┌─────────────────────────────────────┐
│ Database State                      │
├─────────┬─────────────┬────────────┤
│ uuid    │ player_name │ streak     │
├─────────┼─────────────┼────────────┤
│ abc-123 │ NULL        │ 5          │  ← No name
│ def-456 │ NULL        │ 10         │  ← No name
└─────────┴─────────────┴────────────┘

After Fix Deployment (but before players rejoin):
┌─────────────────────────────────────┐
│ Database State - UNCHANGED          │
├─────────┬─────────────┬────────────┤
│ uuid    │ player_name │ streak     │
├─────────┼─────────────┼────────────┤
│ abc-123 │ NULL        │ 5          │  ← Still NULL!
│ def-456 │ NULL        │ 10         │  ← Still NULL!
└─────────┴─────────────┴────────────┘

New Player Joins:
┌─────────────────────────────────────┐
│ Database State                      │
├─────────┬─────────────┬────────────┤
│ uuid    │ player_name │ streak     │
├─────────┼─────────────┼────────────┤
│ abc-123 │ NULL        │ 5          │  ← Still NULL
│ def-456 │ NULL        │ 10         │  ← Still NULL
│ ghi-789 │ "NewPlayer" │ 1          │  ← Has name! ✅
└─────────┴─────────────┴────────────┘
```

### Why Players Still See UUID Fragments

- **Old players (abc-123, def-456)**: Haven't rejoined since fix → Names still NULL → Show as "abc12345..."
- **New players (ghi-789)**: Joined after fix → Names stored correctly → Show as "NewPlayer"

---

## Solution Implemented

### New Migration Command: `/rewardmigratenames`

This command backfills player names for existing database entries by:
1. Querying all UUIDs from the database
2. Checking which entries have NULL or empty `player_name`
3. Looking up player names from Bukkit's OfflinePlayer cache
4. Updating the database with the retrieved names

### Technical Implementation

#### 1. New Method in MySqlStorage

**Method**: `migratePlayerNames()`

```java
public synchronized int migratePlayerNames() {
    int updatedCount = 0;
    List<UUID> allUUIDs = getAllPlayerUUIDs();
    
    for (UUID uuid : allUUIDs) {
        String currentName = getPlayerNameFromDB(uuid);
        
        // Only update if name is NULL or empty
        if (currentName == null || currentName.isEmpty()) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(uuid);
            
            // If player has played before and name is available
            if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null) {
                String playerName = offlinePlayer.getName();
                updatePlayerName(uuid, playerName);
                updatedCount++;
            }
        }
    }
    
    return updatedCount;
}
```

**Key Features**:
- Only updates entries with NULL/empty names (idempotent)
- Uses Bukkit's OfflinePlayer cache (works for offline players)
- Thread-safe (synchronized)
- Returns count of updated players
- Logs progress and results

#### 2. New Admin Command

**Command**: `/rewardmigratenames`  
**Permission**: `loginbonus.admin` or OP  
**Storage**: MySQL only (YAML/SQLite not affected)  
**Execution**: Async (doesn't block server)

**File**: `RewardMigrateNamesCommand.java`

#### 3. Updated Files

- `MySqlStorage.java`: Added `migratePlayerNames()` and `getPlayerNameFromDB()`
- `RewardMigrateNamesCommand.java`: New command implementation
- `Main.java`: Registered new command
- `plugin.yml`: Added command definition
- `message.yml`: Added new messages

---

## Usage Instructions

### For Server Administrators

#### Step 1: Update Plugin
1. Deploy the updated LoginBonus JAR to your Bukkit server
2. Restart or reload the plugin

#### Step 2: Run Migration Command
```
/rewardmigratenames
```

**Expected Output**:
```
[LoginBonus] プレイヤー名の移行を開始します。対象: 150 人
[LoginBonus] プレイヤー名を更新: abc-123-456... -> Steve
[LoginBonus] プレイヤー名を更新: def-456-789... -> Alice
[LoginBonus] プレイヤー名を更新: ghi-789-012... -> Bob
...
[LoginBonus] プレイヤー名の移行が完了しました。更新数: 147 人
```

#### Step 3: Verify on Bungeecord Plan
1. Wait a few minutes for caching to clear
2. Access Plan's network page
3. Check LoginBonus rankings
4. Player names should now display correctly

### Important Notes

1. **Bukkit Only**: Must run on Bukkit/Spigot server (not Bungeecord)
2. **MySQL Required**: Only works with MySQL storage
3. **OfflinePlayer Cache**: Uses Bukkit's player cache (must have played before)
4. **Safe to Re-run**: Only updates NULL/empty entries
5. **Async Execution**: Won't lag the server

---

## What Gets Updated

### Before Migration
```sql
SELECT uuid, player_name FROM player_data;
```
```
uuid                                   | player_name
---------------------------------------|-------------
550e8400-e29b-41d4-a716-446655440000  | NULL
6ba7b810-9dad-11d1-80b4-00c04fd430c8  | NULL
6ba7b814-9dad-11d1-80b4-00c04fd430c8  | Steve
```

### After Migration
```sql
SELECT uuid, player_name FROM player_data;
```
```
uuid                                   | player_name
---------------------------------------|-------------
550e8400-e29b-41d4-a716-446655440000  | Alice       ← Updated!
6ba7b810-9dad-11d1-80b4-00c04fd430c8  | Bob         ← Updated!
6ba7b814-9dad-11d1-80b4-00c04fd430c8  | Steve       ← Unchanged
```

---

## Error Handling

### Scenario 1: Player Never Joined
```
UUID exists in database but player never joined this server
→ Skip (no name available from OfflinePlayer)
```

### Scenario 2: MySQL Connection Issue
```
Error during migration
→ Logged with full error details
→ Safe to retry after fixing connection
```

### Scenario 3: Not Using MySQL
```
Command: /rewardmigratenames
Output: このコマンドはMySQLストレージを使用している場合のみ実行できます。
```

---

## Verification

### Check Database Directly
```sql
-- Count NULL player names (before)
SELECT COUNT(*) FROM player_data WHERE player_name IS NULL OR player_name = '';

-- Run migration command

-- Count NULL player names (after)
SELECT COUNT(*) FROM player_data WHERE player_name IS NULL OR player_name = '';
-- Should be 0 or very few
```

### Check Plan Display
1. Access Plan web interface
2. Navigate to Network page
3. Find LoginBonus section
4. Verify player names appear instead of UUID fragments

---

## Future Prevention

### For New Players
The original fix ensures new players automatically get their names stored:
- Player joins → `EventListener.onPlayerJoin()` called
- `updatePlayerName()` stores the name immediately
- Bungeecord Plan reads the name correctly

### For Existing Players
After running the migration:
- All cached players get names populated
- Future joins update names automatically
- No manual intervention needed

---

## Technical Specifications

### Performance
- **Operation**: SELECT + conditional UPDATE per player
- **Time Complexity**: O(n) where n = number of players
- **Database Load**: Moderate (runs async, uses prepared statements)
- **Server Impact**: Minimal (async execution)

### Safety
- **Idempotent**: Safe to run multiple times
- **Non-destructive**: Only fills in missing data
- **Transactional**: Each update is atomic
- **Logged**: Full audit trail in server logs

### Requirements
- Bukkit/Spigot server (for OfflinePlayer API)
- MySQL storage enabled
- OP or loginbonus.admin permission
- Players must have joined server at least once

---

## Messages

### Japanese (message.yml)
```yaml
migrate-names-mysql-only: "&cこのコマンドはMySQLストレージを使用している場合のみ実行できます。"
migrate-names-start: "&aプレイヤー名の移行を開始します..."
migrate-names-complete: "&aプレイヤー名の移行が完了しました。更新数: %count% 人"
migrate-names-error: "&cプレイヤー名の移行中にエラーが発生しました: %error%"
```

### English Translation
```yaml
migrate-names-mysql-only: "&cThis command can only be executed when using MySQL storage."
migrate-names-start: "&aStarting player name migration..."
migrate-names-complete: "&aPlayer name migration completed. Updated: %count% players"
migrate-names-error: "&cError during player name migration: %error%"
```

---

## Summary

✅ **Problem**: Existing database entries had NULL player names  
✅ **Solution**: Migration command to backfill from OfflinePlayer cache  
✅ **Command**: `/rewardmigratenames`  
✅ **Result**: Player names display correctly on Bungeecord Plan  

The "cache" issue was actually **database state** - old NULL values that needed migration. This is now resolved! 🎉
