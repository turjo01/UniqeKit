# UniqueKits - The Ultimate Minecraft Kit Plugin

[![Version](https://img.shields.io/badge/version-1.0.0-brightgreen.svg)](https://github.com/turjo/uniquekits)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.4-blue.svg)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/java-17+-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## 🌟 Features

### ✨ **Core Features**
- **Beautiful GUI Interface** - Stunning, customizable kit selection menus
- **Advanced Kit System** - Comprehensive kit management with items, commands, and effects
- **Smart Cooldown System** - Flexible cooldown management with bypass permissions
- **Economy Integration** - Full Vault support for kit pricing
- **Permission System** - Granular permission control for all features
- **Multi-Language Support** - Fully translatable with beautiful color formatting

### 🎨 **Visual Excellence**
- **MiniMessage Support** - Modern text formatting with gradients and effects
- **Animated GUIs** - Smooth transitions and loading animations
- **Custom Icons** - Fully customizable kit icons with model data support
- **Progress Bars** - Visual cooldown and progress indicators
- **Particle Effects** - Eye-catching particle effects for kit claims

### 🔧 **Advanced Management**
- **In-Game Editor** - Drag-and-drop kit creation and editing
- **Import/Export** - Import kits from EssentialsX and other plugins
- **Auto-Give System** - Automatic kit distribution on join/respawn
- **Virtual Storage** - Overflow protection with virtual item storage
- **Statistics Tracking** - Comprehensive usage statistics and analytics

### 🔌 **Plugin Integration**
- **Vault** - Economy and permission integration
- **PlaceholderAPI** - Rich placeholder support
- **EssentialsX** - Kit import functionality
- **Adventure API** - Modern text components and formatting

## 📋 Requirements

- **Minecraft Version**: 1.21.4+
- **Java Version**: 17+
- **Dependencies**: Vault (required)
- **Soft Dependencies**: PlaceholderAPI, EssentialsX

## 🚀 Installation

1. Download the latest release from [Releases](https://github.com/turjo/uniquekits/releases)
2. Place the JAR file in your server's `plugins` folder
3. Install Vault and an economy plugin (like EssentialsX)
4. Restart your server
5. Configure the plugin in `plugins/UniqueKits/config.yml`

## 📖 Commands

### Player Commands
- `/kit` - Open the kit selection GUI
- `/kit <name>` - Claim a specific kit
- `/kit list` - View all available kits
- `/kit preview <name>` - Preview kit contents
- `/kit cooldowns` - Check your cooldowns
- `/kit stats` - View your kit statistics

### Admin Commands
- `/uk create <name>` - Create a new kit
- `/uk edit <name>` - Open the kit editor
- `/uk delete <name>` - Delete a kit
- `/uk give <player> <kit>` - Give a kit to a player
- `/uk import` - Import kits from EssentialsX
- `/uk reload` - Reload the plugin
- `/uk stats <player>` - View player statistics

## 🔑 Permissions

### Basic Permissions
- `uniquekits.use` - Basic plugin usage (default: true)
- `uniquekits.kit.use` - Use kit commands (default: true)
- `uniquekits.admin` - Full admin access (default: op)

### Kit Permissions
- `uniquekits.kit.*` - Access to all kits
- `uniquekits.kit.<name>` - Access to specific kit

### Bypass Permissions
- `uniquekits.bypass.cooldown` - Bypass cooldowns
- `uniquekits.bypass.permission` - Bypass kit permissions
- `uniquekits.bypass.world` - Bypass world restrictions

## 🎨 Configuration

The plugin comes with extensive configuration options:

### Main Config (`config.yml`)
```yaml
settings:
  language: "en"
  check-updates: true
  virtual-storage: true

gui:
  update-interval: 20
  animations:
    enabled: true
    speed: 2

hooks:
  vault:
    enabled: true
  placeholderapi:
    enabled: true
```

### Kit Configuration (`kits.yml`)
```yaml
kits:
  starter:
    name: "&a&lStarter Kit"
    description: "&7A basic kit for new players"
    icon: "CHEST"
    cooldown: 1800000  # 30 minutes
    cost: 0
    items:
      - type: STONE_SWORD
      - type: BREAD
        amount: 16
```

## 🌍 Placeholders

UniqueKits provides rich PlaceholderAPI integration:

- `%uniquekits_total_kits%` - Total number of kits
- `%uniquekits_available_kits%` - Available kits for player
- `%uniquekits_used_kits%` - Total kits used by player
- `%uniquekits_kit_cooldown_<kit>%` - Cooldown remaining for kit
- `%uniquekits_most_used_kit%` - Player's most used kit

## 🎯 Kit Features

### Advanced Kit Options
- **Items & Equipment** - Full inventory support with enchantments
- **Potion Effects** - Apply temporary or permanent effects
- **Commands** - Execute console or player commands
- **Cooldowns** - Flexible cooldown system
- **Costs** - Economy integration for paid kits
- **Permissions** - Per-kit permission requirements
- **World Restrictions** - Limit kits to specific worlds
- **One-time Use** - Kits that can only be claimed once
- **Auto-give** - Automatic distribution on join/respawn

### Kit Requirements
- **Level Requirements** - Minimum player level
- **Money Requirements** - Minimum balance needed
- **Permission Requirements** - Required permissions
- **Custom Requirements** - Extensible requirement system

## 🔧 Development

### Building from Source
```bash
git clone https://github.com/turjo/uniquekits.git
cd uniquekits
mvn clean package
```

### API Usage
```java
// Get the UniqueKits API
UniqueKits plugin = UniqueKits.getInstance();
KitManager kitManager = plugin.getKitManager();

// Give a kit to a player
Kit kit = kitManager.getKit("starter");
kitManager.giveKit(player, kit, false);
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Discord**: [Join our Discord](https://discord.gg/uniquekits)
- **Issues**: [GitHub Issues](https://github.com/turjo/uniquekits/issues)
- **Wiki**: [Documentation](https://github.com/turjo/uniquekits/wiki)

## 🙏 Acknowledgments

- **Spigot Team** - For the amazing Minecraft server platform
- **Adventure Team** - For the modern text component system
- **Vault Team** - For the economy and permission abstraction
- **PlaceholderAPI** - For the placeholder system
- **Community** - For feedback and suggestions

---

<div align="center">
  <h3>Made with ❤️ by Turjo</h3>
  <p>If you find this plugin useful, please consider giving it a ⭐ on GitHub!</p>
</div>