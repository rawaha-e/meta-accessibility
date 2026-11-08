SUMMARY = "Configuration file parser library"
DESCRIPTION = "dot.conf parses Apache-style configuration files. It is \
used by Speech Dispatcher."
HOMEPAGE = "https://github.com/williamh/dotconf"
SECTION = "libs"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=155b66f2dc258f05886f9886a60fd870"

SRC_URI = "git://github.com/williamh/dotconf.git;protocol=https;branch=1.4.x;tag=v${PV}"
SRCREV = "7c3e0515c29efce45de6aae29befb2ab4396bd60"

inherit autotools pkgconfig
