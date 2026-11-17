SUMMARY = "Braille display driver and console screen reader"
DESCRIPTION = "BRLTTY is a background process which gives a blind person \
access to the Linux console by driving a refreshable braille display. It \
can also speak the screen through a speech synthesizer, and offers an API \
(BrlAPI) that other screen readers such as Orca use to reach the display."
HOMEPAGE = "https://brltty.app"
BUGTRACKER = "https://github.com/brltty/brltty/issues"
SECTION = "console/utils"
# Everything is LGPLv2.1+ except vstp, the VisioBraille file transfer tool,
# which is GPLv2+.
LICENSE = "LGPL-2.1-or-later & GPL-2.0-or-later"
LICENSE:${PN} = "LGPL-2.1-or-later"
LICENSE:libbrlapi = "LGPL-2.1-or-later"
LICENSE:${PN}-tables = "LGPL-2.1-or-later"
LICENSE:${PN}-scripts = "LGPL-2.1-or-later"
LICENSE:${PN}-x11 = "LGPL-2.1-or-later"
LICENSE:${PN}-vstp = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE-LGPL;md5=4bf661c1e3793e55c8d1051bc5e0ae21 \
                    file://README;beginline=13;endline=17;md5=c5155d440c4f7a14048646dfe1113342 \
                    file://Drivers/Braille/VisioBraille/vstp_main.c;beginline=6;endline=18;md5=f3bdc88d2b7d25a713183c005607a2d0"

SRC_URI = "https://brltty.app/archive/${BP}.tar.xz \
           file://0001-configure-find-eSpeak-NG-with-pkg-config.patch \
           file://0002-brltty-genkey-try-the-next-method-when-one-fails.patch \
           file://brlapi-key.service \
           file://brltty-brlapi-key.conf \
           "
SRC_URI[sha256sum] = "cf4619ce1471690e4ecc5544ee6289cef6c5bdf5855a1ac8dcc63f5beedf108d"

UPSTREAM_CHECK_URI = "https://brltty.app/download.html"

# The makefiles take paths relative to the source dir and don't use automake.
inherit autotools-brokensep pkgconfig gettext useradd systemd

# libpcre2 has no configure switch and is used when found.
DEPENDS = "ncurses libpcre2"

PACKAGECONFIG ??= "liblouis espeak-ng speechd expat \
    ${@bb.utils.filter('DISTRO_FEATURES', 'alsa bluetooth systemd x11', d)}"

PACKAGECONFIG[liblouis] = "--enable-liblouis,--disable-liblouis,liblouis"
PACKAGECONFIG[expat] = "--enable-expat,--disable-expat,expat"
PACKAGECONFIG[icu] = "--enable-icu,--disable-icu,icu"
PACKAGECONFIG[polkit] = "--enable-polkit,--disable-polkit,polkit"
PACKAGECONFIG[gpm] = "--enable-gpm,--disable-gpm,gpm"
# xbrlapi and the XWindow braille driver.
PACKAGECONFIG[x11] = "--enable-x,--disable-x,libx11 libxext libxtst libxfixes libxt libxaw"
# These have no configure switch, they are used when found.
PACKAGECONFIG[alsa] = ",,alsa-lib"
PACKAGECONFIG[bluetooth] = ",,bluez5 dbus"
# tclsh generates the USB device udev rules at install time.
PACKAGECONFIG[systemd] = ",,systemd tcl-native"
PACKAGECONFIG[espeak-ng] = "--with-espeak_ng,--without-espeak_ng,espeak-ng"
PACKAGECONFIG[speechd] = "--with-speechd,--without-speechd,speech-dispatcher"

# ncursesw would be preferred, but configure looks for its header in an
# ncursesw/ include subdirectory, which the ncurses recipe doesn't have.
EXTRA_OECONF = " \
    --disable-stripping \
    --disable-emacs-bindings --disable-java-bindings --disable-lisp-bindings \
    --disable-lua-bindings --disable-ocaml-bindings --disable-python-bindings \
    --disable-tcl-bindings \
    --without-libbraille --without-espeak --without-flite --without-mikropuhe \
    --without-swift --without-theta \
    --with-curses=ncurses \
"

# Upstream's own installation root variable instead of DESTDIR.
EXTRA_OEMAKE = "INSTALL_ROOT=${D}"

do_install() {
    oe_runmake install
    if ${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'true', 'false', d)}; then
        oe_runmake install-systemd install-udev

        # The useradd class creates the user and groups. Upstream's
        # sysusers.d file would define them a second time.
        rm -r ${D}${nonarch_libdir}/sysusers.d

        install -d ${D}${systemd_system_unitdir}/brltty@.service.d
        install -m 0644 ${UNPACKDIR}/brlapi-key.service ${D}${systemd_system_unitdir}
        sed -i -e 's|@bindir@|${bindir}|g' -e 's|@sysconfdir@|${sysconfdir}|g' \
            ${D}${systemd_system_unitdir}/brlapi-key.service
        install -m 0644 ${UNPACKDIR}/brltty-brlapi-key.conf \
            ${D}${systemd_system_unitdir}/brltty@.service.d/brlapi-key.conf
    fi
    oe_runmake install-messages

    # "make install" doesn't install the configuration file, only the
    # documented template that configure generates.
    install -m 0644 ${S}/Documents/brltty.conf ${D}${sysconfdir}/brltty.conf

    # This contraction table is a Python program run as an external
    # translator, for LaTeX documents. Not worth pulling in Python for.
    rm -f ${D}${sysconfdir}/brltty/Contraction/latex-access.ctb
}

# The brltty units are templates started by the udev rules when a braille
# display is plugged in, so only the BrlAPI key generation is enabled. It
# runs on first boot, so every device gets its own key instead of one baked
# into the image. Users who run BrlAPI clients (such as Orca) need to be in
# the brlapi group to read it.
SYSTEMD_SERVICE:${PN} = "brlapi-key.service"

# The same user and groups as upstream's Autostart/Systemd/sysusers, which
# the systemd unit's SupplementaryGroups= also lists. root is for USB
# devices and /dev/uinput. pulse-access isn't created by the pulseaudio
# recipe, but the unit fails to start if the group doesn't exist.
USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "--system brlapi; --system pulse-access"
USERADD_PARAM:${PN} = "--system --home-dir /var/lib/brltty --no-create-home \
                       --shell /bin/false --user-group \
                       --groups tty,dialout,root,audio,pulse-access,input,brlapi \
                       brltty"

PACKAGES =+ "libbrlapi ${PN}-tables ${PN}-scripts ${PN}-vstp ${PN}-x11"

FILES:libbrlapi = "${libdir}/libbrlapi.so.*"
FILES:${PN}-tables = "${sysconfdir}/brltty"
FILES:${PN}-scripts = " \
    ${bindir}/brltty-mkuser \
    ${bindir}/brltty-setcaps \
    ${bindir}/brltty-term \
    ${bindir}/brltty-tmux \
    ${bindir}/brltty-ttysize \
    ${bindir}/brltty-prologue.bash \
"
FILES:${PN}-vstp = "${bindir}/vstp"
FILES:${PN}-x11 = " \
    ${bindir}/xbrlapi \
    ${libdir}/brltty/libbrlttybxw.so \
    ${sysconfdir}/X11/Xsession.d \
    ${datadir}/gdm \
"
FILES:${PN} += " \
    ${libdir}/brltty \
    ${libexecdir}/brltty \
    ${systemd_system_unitdir} \
    ${nonarch_base_libdir}/udev \
    ${nonarch_libdir}/tmpfiles.d \
"

# systemd-wrapper, which the unit runs, is a bash script.
RDEPENDS:${PN} += "${PN}-tables \
    ${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'bash', '', d)}"
RDEPENDS:${PN}-scripts = "bash"

CONFFILES:${PN} = "${sysconfdir}/brltty.conf"
