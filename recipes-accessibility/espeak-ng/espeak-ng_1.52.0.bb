SUMMARY = "Compact multilingual text-to-speech synthesizer"
DESCRIPTION = "eSpeak NG is a formant speech synthesizer supporting more \
than a hundred languages and accents."
HOMEPAGE = "https://github.com/espeak-ng/espeak-ng"
SECTION = "sound"
LICENSE = "GPL-3.0-or-later & Unicode-DFS-2016"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504 \
                    file://COPYING.UCD;md5=9bc437a2583c6de495e5c4acd7c2c787"

SRC_URI = "git://github.com/espeak-ng/espeak-ng.git;protocol=https;branch=master"
SRCREV = "4870adfa25b1a32b4361592f1be8a40337c58d6c"

# The Makefile uses paths relative to the source dir, so build in-tree.
inherit autotools-brokensep pkgconfig

# The voice data is compiled by running espeak-ng itself. The target binary
# can't run on the build host, so the native build compiles it instead.
DEPENDS:append:class-target = " espeak-ng-native"

PACKAGECONFIG ??= "pcaudiolib"
PACKAGECONFIG:class-native = ""
PACKAGECONFIG[pcaudiolib] = "--with-pcaudiolib,--without-pcaudiolib,pcaudiolib"

# mbrola needs external voices, sonic has no recipe yet (only matters for
# speech rates above ~450 words per minute).
EXTRA_OECONF = "--disable-rpath --without-mbrola --without-sonic"

# Same as upstream autogen.sh: automake expects these files.
do_configure:prepend() {
    mkdir -p ${S}/m4
    touch ${S}/AUTHORS
    ln -sf ChangeLog.md ${S}/NEWS
    ln -sf README.md ${S}/README
}

do_compile:class-target() {
    oe_runmake src/espeak-ng src/speak-ng

    # Create the dictionary helper files make would otherwise touch later,
    # which would make the copied data look out of date.
    oe_runmake $(grep -o 'dictsource/[a-z0-9-]*_\(extra\|emoji\)' Makefile | sort -u)

    cp -r ${STAGING_DATADIR_NATIVE}/espeak-ng-data/. ${S}/espeak-ng-data/
    touch ${S}/phsource/phonemes.stamp
    find ${S}/espeak-ng-data -type f -exec touch {} +

    oe_runmake
}

do_install:append() {
    rm -rf ${D}${datadir}/vim
    # Upstream installs the data with "cp -p", which keeps the build user.
    chown -R root:root ${D}${datadir}/espeak-ng-data
}

FILES:${PN} += "${datadir}/espeak-ng-data"

BBCLASSEXTEND = "native"
