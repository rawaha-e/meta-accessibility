SUMMARY = "Common interface to speech synthesizers"
DESCRIPTION = "Speech Dispatcher is a device independent layer for speech \
synthesis. Applications such as screen readers talk to it through a socket \
(SSIP), and it forwards the text to an output module such as eSpeak NG."
HOMEPAGE = "https://github.com/brailcom/speechd"
SECTION = "sound"
# Server and modules are GPLv2+, the C client library LGPLv2.1+, and the
# module helper library (libspeechd_module) BSD-2-Clause.
LICENSE = "GPL-2.0-or-later & LGPL-2.1-or-later & BSD-2-Clause"
LIC_FILES_CHKSUM = "file://COPYING.GPL-2;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
                    file://COPYING.LGPL;md5=4fbd65380cdd255951079008b364516c \
                    file://src/modules/module_main.c;beginline=4;endline=26;md5=24750de17f9ca1100b201ec780f48af9"

SRC_URI = "https://github.com/brailcom/speechd/releases/download/${PV}/${BP}.tar.gz \
           file://0001-modules-don-t-reset-LDFLAGS.patch \
           "
SRC_URI[sha256sum] = "b14a5238d287d2dcce4dd42bbd66ca65fa228e7e683708267f7b34036f7ba4b4"

UPSTREAM_CHECK_URI = "https://github.com/brailcom/speechd/releases"

DEPENDS = "glib-2.0 dotconf libsndfile1 libtool"

inherit autotools gettext pkgconfig \
    ${@bb.utils.contains('PACKAGECONFIG', 'python', 'python3native', '', d)}

PACKAGECONFIG ??= "espeak-ng alsa \
    ${@bb.utils.filter('DISTRO_FEATURES', 'pulseaudio pipewire systemd', d)}"

PACKAGECONFIG[espeak-ng] = "--with-espeak-ng,--without-espeak-ng,espeak-ng"
PACKAGECONFIG[alsa] = "--with-alsa,--without-alsa,alsa-lib"
PACKAGECONFIG[pulseaudio] = "--with-pulse,--without-pulse,pulseaudio"
PACKAGECONFIG[pipewire] = "--with-pipewire,--without-pipewire,pipewire"
PACKAGECONFIG[libao] = "--with-libao,--without-libao,libao"
# Python bindings (speechd module) and spd-conf. Orca needs these.
PACKAGECONFIG[python] = "--enable-python,--disable-python"
PACKAGECONFIG[systemd] = "--with-systemdsystemunitdir=${systemd_system_unitdir} \
                          --with-systemduserunitdir=${systemd_user_unitdir}, \
                          --without-systemdsystemunitdir --without-systemduserunitdir, \
                          systemd"

# The other output modules need synthesizers that have no recipe here.
# ESPEAK= stops configure from picking up a host espeak to regenerate the
# dummy module's wav file.
EXTRA_OECONF = "--disable-doc --without-espeak --without-flite \
                --without-ibmtts --without-voxin --without-ivona \
                --without-pico --without-baratinoo --without-kali \
                --without-oss --without-nas \
                --with-default-audio-method=${SPD_AUDIO_METHODS} \
                ac_cv_prog_ESPEAK= "

# Configure sets the default to only the first audio method it finds (pulse
# when the distro has it), which can't work on images without a sound server.
# The server accepts a comma-separated list and tries each in order, so give
# it every enabled method.
def spd_audio_methods(d):
    pc = d.getVar('PACKAGECONFIG').split()
    methods = [m for m, opt in (('pipewire', 'pipewire'), ('pulse', 'pulseaudio'),
                                ('alsa', 'alsa'), ('libao', 'libao')) if opt in pc]
    return ','.join(methods)

SPD_AUDIO_METHODS = "${@spd_audio_methods(d)}"

do_install:append() {
    # Audio output plugins are loaded with dlopen, the .la files aren't used.
    rm -f ${D}${libdir}/speech-dispatcher/*.la
}

PACKAGES =+ "libspeechd libspeechd-module ${PN}-python"

FILES:libspeechd = "${libdir}/libspeechd.so.*"
FILES:libspeechd-module = "${libdir}/libspeechd_module.so.*"
FILES:${PN}-python = "${libdir}/python3*/site-packages ${bindir}/spd-conf"
RDEPENDS:${PN}-python = "${@bb.utils.contains('PACKAGECONFIG', 'python', 'python3-core pyxdg', '', d)}"

FILES:${PN} += " \
    ${libdir}/speech-dispatcher/*.so \
    ${libexecdir}/speech-dispatcher-modules \
    ${datadir}/speech-dispatcher \
    ${datadir}/sounds/speech-dispatcher \
    ${systemd_system_unitdir} \
    ${systemd_user_unitdir} \
"
