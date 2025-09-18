# Attribution and License Information

## Original Project

This project (ZKyNet VPN) is based on **WG Tunnel** by Zane Schepke.

- **Original Project**: [WG Tunnel](https://github.com/zaneschepke/wgtunnel)
- **Original Author**: Zane Schepke
- **Original Copyright**: Copyright (c) 2023-2025 Zane Schepke
- **License**: MIT License

## ZKyNet VPN Modifications

ZKyNet VPN is a customized fork that adds server integration and management capabilities to the original WG Tunnel application.

### Key Modifications Made

#### New Files Added
- `app/src/main/java/com/zaneschepke/wireguardautotunnel/data/service/ZKyNetVpnService.kt` - ZKyNet server configuration management
- `app/src/main/java/com/zaneschepke/wireguardautotunnel/data/model/ZKyNetServerConfig.kt` - Server configuration data model
- `app/src/main/java/com/zaneschepke/wireguardautotunnel/ui/screens/main/ConnectScreen.kt` - Custom connection interface
- `app/src/main/java/com/zaneschepke/wireguardautotunnel/ui/screens/main/components/ZKyNetServerList.kt` - Server selection UI component
- `CLAUDE.md` - Project documentation and development instructions
- `.claude/settings.local.json` - Claude AI configuration
- Various configuration and documentation files

#### Existing Files Modified
- `app/src/main/java/com/zaneschepke/wireguardautotunnel/viewmodel/AppViewModel.kt` - Enhanced with ZKyNet server integration
- `app/build.gradle.kts` - Build configuration updates for ZKyNet features
- `app/src/main/AndroidManifest.xml` - Manifest updates for new services and activities
- Various UI screens and components - Modified to support ZKyNet server functionality

#### Core Functionality Changes
1. **Server Management**: Added ZKyNet-specific server configuration and management
2. **User Interface**: Replaced default tunnel-adding flow with per-server VPN toggles
3. **API Integration**: Implemented automatic config download and caching for production servers
4. **Custom UI Components**: New server selection and connection interface

## License Compliance

ZKyNet VPN maintains full compliance with the MIT License of the original WG Tunnel project:

- ✅ Original copyright notice preserved in LICENSE file
- ✅ Original license text included in LICENSE file
- ✅ Attribution provided in README.md and this ATTRIBUTION.md file
- ✅ Copyright headers added to new ZKyNet-specific files
- ✅ No original copyright notices removed or altered

## Third-Party Acknowledgments

TORUS VPN inherits and maintains acknowledgments to the following upstream projects:

- **WireGuard** - Jason A. Donenfeld (https://github.com/WireGuard/wireguard-android)
- **AmneziaWG** - Amnezia Team (https://github.com/amnezia-vpn/amneziawg-android)
- All contributors to the original WG Tunnel project

## Copyright Information

- **Original WG Tunnel Code**: Copyright (c) 2023-2025 Zane Schepke
- **TORUS VPN Modifications**: Copyright (c) 2025 TheTorusProject
- **License**: MIT License (see LICENSE file for full text)

---

This attribution file ensures transparency about the relationship between TORUS VPN and the original WG Tunnel project, maintaining compliance with the MIT License requirements.