# Build Fix: Brigadier Dependency Resolution

## Problem
The build was failing with the following error:
```
Could not find com.mojang:brigadier:1.2.9.
Required by:
    project : > net.md-5:bungeecord-api:1.20-R0.2 > net.md-5:bungeecord-protocol:1.20-R0.2
```

## Root Cause
When we added Bungeecord API support in version 1.5.0, we introduced a new dependency:
```gradle
compileOnly 'net.md-5:bungeecord-api:1.20-R0.2'
```

The Bungeecord API has a transitive dependency on `com.mojang:brigadier:1.2.9`, which is a Mojang library used for command parsing in Minecraft. This library is hosted in the Minecraft Libraries repository, which was not included in our repository list.

## Solution
Added the Minecraft Libraries repository to `build.gradle`:
```gradle
repositories {
    mavenCentral()
    maven { url = 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/' }
    maven { url = 'https://oss.sonatype.org/content/repositories/snapshots/' }
    maven { url = 'https://jitpack.io' }
    maven { url = 'https://libraries.minecraft.net/' }  // Added for brigadier
}
```

## Why This Works
The Minecraft Libraries repository (`https://libraries.minecraft.net/`) is the official Maven repository for Mojang libraries, including:
- brigadier (command parsing)
- authlib (authentication)
- datafixerupper (data migration)
- Other Minecraft-related libraries

By adding this repository, Gradle can now resolve the brigadier dependency that Bungeecord API requires.

## Testing
After applying this fix, the build should complete successfully. The brigadier library will be downloaded from:
`https://libraries.minecraft.net/com/mojang/brigadier/1.2.9/`

## Impact
- **Scope**: Minimal change - only adds a repository URL
- **Risk**: Low - only affects dependency resolution, no code changes
- **Compatibility**: Fully backward compatible
- **Dependencies**: Allows Bungeecord API to work correctly
