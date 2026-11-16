SUMMARY = "Braille translator and back-translator"
DESCRIPTION = "Liblouis translates text to and from contracted and \
uncontracted braille, and formats it for braille displays and embossers. \
It ships translation tables for many languages and is used by BRLTTY and \
Orca."
HOMEPAGE = "https://liblouis.io"
BUGTRACKER = "https://github.com/liblouis/liblouis/issues"
SECTION = "libs"
# The library and tables are LGPLv2.1+, the command line tools GPLv3+.
LICENSE = "LGPL-2.1-or-later & GPL-3.0-or-later"
LICENSE:${PN} = "LGPL-2.1-or-later"
LICENSE:${PN}-tools = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504 \
                    file://COPYING.LESSER;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "https://github.com/liblouis/liblouis/releases/download/v${PV}/${BP}.tar.gz"
SRC_URI[sha256sum] = "629fa8cb0dfd9ad457c5bf47a42f0953b673e62c8ad6b1d03ddc4e2bd20008f1"

UPSTREAM_CHECK_URI = "https://github.com/liblouis/liblouis/releases"
UPSTREAM_CHECK_REGEX = "releases/tag/v(?P<pver>\d+(\.\d+)+)"

inherit autotools pkgconfig

# libyaml is only used by lou_checkyaml, a tool for running the YAML test
# suite. The Python bindings aren't installed by "make install".
EXTRA_OECONF = "--without-yaml"

PACKAGES =+ "${PN}-tools"

FILES:${PN}-tools = "${bindir}"
FILES:${PN} += "${datadir}/liblouis"

BBCLASSEXTEND = "native"
