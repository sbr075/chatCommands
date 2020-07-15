# chatCommands
Welcome to chatCommands. This is a spigot plugin for the latest minecraft version (1.16.1)
This plugin aims to implement several features which users and moderators can take advantage off.
The mod, as well as this description, is in the early stages of development and bugs are a given.
If you got any complaints or tips feel free to let me know. 
#### If you decide to test this out and find any bugs please report them immediately

## How to use
### To be added

## Planned features
 1. Cooldown on commands
    - Helpful way for server administrators to prevent players from abusing commands
 2. Broadcast - **Implemented**
    - Allows server administrators, or players with the right permissions, to broadcast a message to the whole server. This can be done once or set to a timed interval
 3. Moderator utilities
    - Allows server administrators more control of the server.
      1. Warns / warn logs
      2. Timeouts
      3. Player warn info
 4. Chat system
    - Chat modes
      1. Whisper, local, shout, global and mute
    - Group/parties
      1. Roles in groups
      2. Groups are permanent, parties are not
 5. Titles **Implemented**
    1. Titles for better permission management
 6. Logs
    1. This feature is still uncertain if it'll make it into the final cut
 ### More to come in the future
 ### NB! Not all features are 100% guaranteed to make it in. I am working on this as a hobby but will try to include them all and more!

## Changelog
### Version 0.4.3
- General
  - Fixed /<label> help command
  - Updated "commands.yml" to include missing commands
- Broadcast
  - Removal of broadcast will now cancel the task properly
  - Fixed broadcast command "edit add runtime"
- Emote
  - Fixed "emotes.yml" by removing unintended "commands" tab
  - Rewrote a part of command "/emote use"
    - Should now be more optimized
    - Includes more warnings to guide user if something goes wrong
  - Removed old permission add/remove from emote commands
- Title
  - Fixes
    - Fixed add parent to support multiple parents
    - Fixed removal of title
    - Fixed changing of color of title
    - Fixed removal of perms from titles
  - Addon(s)
    - Added new command "/title default <name>"
      - Adds the abiity to designate new default title
#### Update info
- Update 0.4.3 is a Hotfix update fixing some minor and major bugs with the plugin. This update brings no new content with it unfortunately.
### Version 0.4.2
- Added multi-parent support for titles
- Added proper title removal, will now set players with title to be removed to a default set title
- Fixed help list
- Updated onJoin system to now work with new title permission system
#### Known issues with 0.4.2
- Broadcast removal doesn't fully remove broadcast (gets remade)
- Title remove doesn't set default title
### Version 0.4.1
- In this version I've added all commands that are going to be accessible with /title. This is however not a full release seeing as the code is messy and missing some extras i.e. multiple parent title support. A full changelog will be released with 0.5.0
- Added all commands with basic functionality for title feature
#### Known issues with 0.4.1
- Help list is broken
- Old onJoin system needs updating to fit with the new permission system
### Version 0.4.0
- Update 0.4.0 brings with it a new set of functions accessible with /broadcast. It allows administrators, or players with the correct permissions, to send a broadcast to the whole server either once or at a timed interval for a set duration of time.
  - New commands
    1. Send
       - **COMMAND**: /broadcast send <message>
         - Broadcasts a message once to the whole server. 
         - I.e. */broadcast send this is a message*
    2. Add
       - **COMMAND**: /broadcast add <name> <interval (s, m or h)> <duration (s, m or h)> <message>
       - Broadcasts a message to the whole server at a set interval for the duration given. 
       - I.e. */broadcast add test 10s 100s this is a message*
    3. Edit
       - **COMMAND**: /broadcast edit <name> <add runtime/message> <new value>
         - Add runtime
           - This can either be negative (remove runtime) or positve (add more runtime)
         - Message
           Changes the broadcast message
         - I.e */broadcast edit test add runtime -10s*
            - **NB!** If you remove more time than duration given it will remove the broadcast entirely
    4. Remove
       - **COMMAND**: /broadcast remove <name>
       - Removes a running broadcast
       - I.e. */broadcast remove test*
    5. List
       - **COMMAND**: /broadcast list
       - Lists all running broadcasts
    6. Info
       - **COMMAND**: /broadcast info <name>
       - Shows information about a specifc running broadcast
       - I.e. */broadcast info test*
 - Server administrator information
   - You can configure how many total broadcasts are allowed at once and the minimum delay interval in the broadcast.yml file. 
   - **NB!** Do not touch the "current_broadcasts" tab since it's used to keep track of how many broadcasts are currently running
 
 - Extra information
   - Anything you see here is subject to change since this is an early version of this system. The functionlity of the implemented functions will remain the same but the code will be cleaned up or optimized for better performance.
   - There are 100% hidden bugs that I haven't seen while testing. I'll test this more thoroughly as I clean the code.

#### Planned changes
- Before I start on the adding the next feature to this plugin I will spend time on cleaing up the code. I'm also planning on adding a better interface, and/or command execution, to make it easier for the user to use these commands.
       
### Version 0.4.0-alpha
- Updated commands.yml to include new planned commands
- Updated sectioning and sorted code
- Updated "help" function
- Added async task scheduling to broadcast so several can run at a time
### Version 0.3.6-alpha
- Started work on organization before adding/refining features
  #### Known issues
  - Command "/emote list <num>" doesn't work
  
### Version 0.3.5
- Fixed editing, now it works as intended
- Added permissions so server admin/OPs can choose who can do what

### Version 0.3
- Added the ability to create, edit and delete emotes

### Version 0.2
- Rebranded 'chatEmotes' to 'chatCommands'
- Added more base emotes

### Version 0.1
- Released a test version, chatEmotes, with one emote
