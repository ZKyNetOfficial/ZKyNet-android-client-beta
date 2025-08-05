<h1 align="center">
TORUS VPN
</h1>

<div align="center">

A customized Android VPN client based on WG Tunnel, featuring TORUS server integration
with support for [WireGuard](https://www.wireguard.com/) and [AmneziaWG](https://docs.amnezia.org/documentation/amnezia-wg/) protocols
<br />
<br />
TORUS VPN - Customized VPN Client with Server Integration

</div>

<br/>

<div align="center">

<!-- TORUS VPN - Custom VPN Client -->

</div>

<div align="center">

<!-- Community links removed for TORUS VPN custom implementation -->
</div>

<details open="open">
<summary>Table of Contents</summary>

- [About](#about)
- [Acknowledgements](#acknowledgements)
- [Screenshots](#screenshots)
- [Features](#features)
- [Building](#building)
- [Translation](#translation)
- [Contributing](#contributing)

</details>

<div style="text-align: left;">

## About
TORUS VPN is a customized fork of [WG Tunnel](https://github.com/zaneschepke/wgtunnel) by Zane Schepke, modified to include TORUS server integration and management capabilities. This app combines the robust VPN functionality of WG Tunnel with custom server configuration management, supporting both [WireGuard](https://www.wireguard.com/) and [AmneziaWG](https://docs.amnezia.org/documentation/amnezia-wg/) protocols.

### Key TORUS Customizations
- Integrated TORUS server management via `TorusVpnService`
- Per-server VPN toggles replacing default tunnel-adding flow
- Hardcoded test server configuration support
- Automatic config download and caching for production servers
- Custom UI with server selection interface

</div>

<div style="text-align: left;">

## Attribution and Acknowledgements

**Original Project**: This project is based on [WG Tunnel](https://github.com/zaneschepke/wgtunnel) by **Zane Schepke**, licensed under the MIT License. We are grateful for the excellent foundation provided by the original WG Tunnel project.

**Upstream Acknowledgements**:
- [WireGuard](https://www.wireguard.com/) - Jason A. Donenfeld (https://github.com/WireGuard/wireguard-android)
- [AmneziaWG](https://docs.amnezia.org/documentation/amnezia-wg/) - Amnezia Team (https://github.com/amnezia-vpn/amneziawg-android)
- All contributors to the original WG Tunnel project

## Screenshots

</div>
<div style="display: flex; flex-wrap: wrap; justify-content: left; gap: 10px;">
 <img label="Main" src="fastlane/metadata/android/en-US/images/phoneScreenshots/main_screen.png" width="200" />
 <img label="Settings" src="fastlane/metadata/android/en-US/images/phoneScreenshots/settings_screen.png" width="200" />
  <img label="Auto" src="fastlane/metadata/android/en-US/images/phoneScreenshots/auto_screen.png" width="200" />
  <img label="Config" src="fastlane/metadata/android/en-US/images/phoneScreenshots/config_screen.png" width="200" />
</div>

<div style="text-align: left;">

## Features

### TORUS-Specific Features
* **TORUS Server Integration**: Built-in server management and configuration
* **Per-Server VPN Toggles**: Individual server connection controls
* **Test Server Support**: Hardcoded test server configuration (peer1.conf)
* **Automatic Config Management**: Download and cache production server configs
* **Custom Server Selection UI**: Dedicated interface for server browsing

### Inherited from WG Tunnel
* Support for both WireGuard and AmneziaWG protocols
* Auto-tunnel based on Wi-Fi SSID, ethernet, or mobile data
* Split tunneling by application with search
* Support for kernel and userspace modes
* Amnezia support for userspace mode for DPI/censorship protection
* Always-On VPN support
* Quick tile support for tunnel toggling
* In-app VPN kill switch with LAN bypass
* Battery preservation measures

## Building

```sh
git clone [your-torus-vpn-repository]
cd torus-vpn-source
```

```sh
./gradlew assembleDebug
```

**Note**: This is a customized version of WG Tunnel. For the original project, visit [zaneschepke/wgtunnel](https://github.com/zaneschepke/wgtunnel).

## Translation

TORUS VPN inherits the translation infrastructure from the original WG Tunnel project. For translation contributions, please refer to the [original WG Tunnel project](https://github.com/zaneschepke/wgtunnel) on Weblate.

## License and Legal

TORUS VPN is based on [WG Tunnel](https://github.com/zaneschepke/wgtunnel) by Zane Schepke and is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

**Original Project**: [WG Tunnel](https://github.com/zaneschepke/wgtunnel) - Copyright © 2023-2025 Zane Schepke  
**TORUS Modifications**: Copyright © 2025 TheTorusProject
