<h1 align="center">
ZKyNet VPN
</h1>

<div align="center">

A customized Android VPN client based on WG Tunnel, featuring ZKyNet server integration
with support for [WireGuard](https://www.wireguard.com/) and [AmneziaWG](https://docs.amnezia.org/documentation/amnezia-wg/) protocols
<br />
<br />
ZKyNet VPN - Customized VPN Client with Server Integration

</div>

<br/>

<div align="center">

<!-- ZKyNet VPN - Custom VPN Client -->

</div>

<div align="center">

<!-- Community links removed for ZKyNet VPN custom implementation -->
</div>

<details open="open">
<summary>Table of Contents</summary>

- [About](#about)
- [Acknowledgements](#acknowledgements)
- [Features](#features)
- [Building](#building)
- [Translation](#translation)
- [Contributing](#contributing)

</details>

<div style="text-align: left;">

## About
ZKyNet VPN is a customized fork of [WG Tunnel](https://github.com/zaneschepke/wgtunnel) by Zane Schepke, modified to include ZKyNet server integration and management capabilities. This app combines the robust VPN functionality of WG Tunnel with custom server configuration management, supporting both [WireGuard](https://www.wireguard.com/) and [AmneziaWG](https://docs.amnezia.org/documentation/amnezia-wg/) protocols.

### Key ZKyNet Customizations
- Integrated ZKyNet server management via `ZKyNetVpnService`
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

</div>

<div style="text-align: left;">

## Features

### ZKyNet-Specific Features
* **ZKyNet Server Integration**: Built-in server management and configuration
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
git clone https://github.com/ZKyNet/zkynet-android-client-beta.git
cd zkynet-vpn-source
```

```sh
./gradlew assembleDebug
```

**Note**: This is a customized version of WG Tunnel. For the original project, visit [zaneschepke/wgtunnel](https://github.com/zaneschepke/wgtunnel).

## Translation

ZKyNet VPN inherits the translation infrastructure from the original WG Tunnel project. For translation contributions, please refer to the [original WG Tunnel project](https://github.com/zaneschepke/wgtunnel) on Weblate.

## License and Legal

ZKyNet VPN is based on [WG Tunnel](https://github.com/zaneschepke/wgtunnel) by Zane Schepke and is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

**Original Project**: [WG Tunnel](https://github.com/zaneschepke/wgtunnel) - Copyright © 2023-2025 Zane Schepke  
**ZKyNet Modifications**: Copyright © 2025 ZKyNet
