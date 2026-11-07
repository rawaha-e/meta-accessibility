SUMMARY = "Portable C audio library"
DESCRIPTION = "Small audio output library used by eSpeak NG."
HOMEPAGE = "https://github.com/espeak-ng/pcaudiolib"
SECTION = "libs"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "git://github.com/espeak-ng/pcaudiolib.git;protocol=https;branch=master"
SRCREV = "e907571bad7dee296fb56a409a0f7c5a0c57d38a"

# Include paths are relative to the source dir, so build in-tree.
inherit autotools-brokensep pkgconfig

PACKAGECONFIG ??= "alsa ${@bb.utils.filter('DISTRO_FEATURES', 'pulseaudio', d)}"
PACKAGECONFIG[alsa] = "--with-alsa,--without-alsa,alsa-lib"
PACKAGECONFIG[pulseaudio] = "--with-pulseaudio,--without-pulseaudio,pulseaudio"

EXTRA_OECONF = "--without-oss --without-qsa --without-coreaudio"

# Same as upstream autogen.sh: automake expects these files.
do_configure:prepend() {
    mkdir -p ${S}/m4
    ln -sf ChangeLog.md ${S}/NEWS
    ln -sf README.md ${S}/README
}
