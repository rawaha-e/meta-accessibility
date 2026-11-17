# meta-accessibility

A small Yocto layer for building images that can talk and drive a braille
display.

So far it has been built and tested only for `qemux86-64` with systemd and
pulseaudio, on the `wrynose` release series.

## What's in it

- **espeak-ng**: text-to-speech engine
- **pcaudiolib**: audio output library used by espeak-ng
- **speech-dispatcher**: speech API that apps talk to
- **dotconf**: config file parser used by speech-dispatcher
- **brltty**: braille display driver and console screen reader, with BrlAPI
- **liblouis**: braille translator and translation tables

Planned, not in the layer yet:

- **orca**: screen reader for GTK desktops (WIP)

None of these have an up-to-date recipe in the main OpenEmbedded layers,
which is why this layer exists.

## Depends on

- openembedded-core

Some optional speech-dispatcher features need more layers:

- `python` in `PACKAGECONFIG` needs meta-oe (for `pyxdg`)
- `libao` in `PACKAGECONFIG` needs meta-multimedia

## Use it

```sh
bitbake-layers add-layer ../meta-accessibility
```

In `local.conf`:

```
IMAGE_INSTALL:append = " espeak-ng speech-dispatcher brltty"
```

Test it in QEMU:

```sh
runqemu qemux86-64 nographic slirp snapshot
espeak-ng "hello"
spd-say "hello"
echo "the quick brown fox" | brltty-ctb -c louis:en-ueb-g2.ctb
```

With systemd, brltty starts by itself (through udev) when a braille display
is plugged in, and the `/etc/brlapi.key` that BrlAPI clients authenticate
with is generated on first boot. Users who run BrlAPI clients such as Orca
need to be in the `brlapi` group. Without systemd nothing starts brltty:
run `brltty-genkey` once and start `brltty` from your own init script.

Optional brltty packages: `brltty-scripts` (bash helper scripts such as
`brltty-setcaps`), `brltty-x11` (xbrlapi and the XWindow driver, when `x11`
is in `DISTRO_FEATURES`) and `brltty-vstp` (VisioBraille file transfer,
GPLv2+).

The liblouis command line tools (`lou_translate` and others) are in the
`liblouis-tools` package.

## Maintainer

Rawaha El Houssayni <rawaha.elhoussayni@gmail.com>

## License

MIT for the layer itself. Each program keeps its own license.
