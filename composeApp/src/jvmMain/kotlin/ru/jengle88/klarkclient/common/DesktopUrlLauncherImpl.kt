package ru.jengle88.klarkclient.common

import java.awt.Desktop
import java.net.URI

class DesktopUrlLauncherImpl : UrlLauncher {
    override fun open(url: String) {
        check(
            Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE),
        ) {
            "Desktop is not supported"
        }

        Desktop.getDesktop().browse(URI(url))
    }
}
