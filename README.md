### [Hangar page](https://hangar.papermc.io/atikiNBTW/VelocityCoolList)
### [Modrinth page](https://modrinth.com/plugin/velocitycoollist)

VelocityCoolList is a simple and easy-to-use plugin for Minecraft Velocity servers that allows you to create a whitelist based on nicknames.

## Commands and permissions
The main command is ```/vclist```, it shows you information about VelocityCoolList, below are its arguments:
| Argument| Description                               | Permission    |
|---------|-------------------------------------------|---------------|
| enable  | Enables whitelist.                        | vclist.admin  |
| disable | Disables whitelist.                       | vclist.admin  |
| add     | Add player to the whitelist.              | vclist.manage |
| remove  | Remove player from the whitelist.         | vclist.manage |
| list    | Gives you a list of whitelisted players.  | vclist.manage |
| reload  | Reload plugin.                            | vclist.admin  |
| clear   | Clears the whitelist.                     | vclist.manage |
| status  | Get the status of the plugin.             | vclist.admin  |

Aliases: ```/vcl```, ```/velocitycoollist```

## Colors and formatting
This plugin supports MiniMessage modern formatting, which allows you to make gradients and many other things.
Here is what this formatting supports, its documentation is in config and [here](https://docs.advntr.dev/minimessage/format.html#standard-tags)!

![picture1](https://docs.advntr.dev/_images/rainbow_1.png) ![picture2](https://docs.advntr.dev/_images/newline_1.png) ![picture3](https://docs.advntr.dev/_images/insertion_1.png)

## Configuration file
All the configuration is in the config.yml file, here it is:
```
# Format - Minimessage, documetation - https://docs.advntr.dev/minimessage/format.html#standard-tags

# Enable or disable Velocity Cool whitelist!
enabled: true

# Enable or disable autoupdate
autoupdate: true

# Set the prefix of the plugin!
prefix: "<gradient:#5e4fa2:#f79459>VelocityCoolList</gradient> <red><bold>>>></red>"

# DANGEROUS: Here you can enable of disable the whitelist clear command, be careful!
enable_clear_command: false

# Do not touch
config_version: 1
```
