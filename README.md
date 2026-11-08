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

Planned, not in the layer yet:

- **brltty**: braille displays and console screen reader (WIP)
- **liblouis**: braille translation tables (WIP)
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
IMAGE_INSTALL:append = " espeak-ng speech-dispatcher"
```

Test it in QEMU:

```sh
runqemu qemux86-64 nographic slirp snapshot
espeak-ng "hello"
spd-say "hello"
```

## License

MIT for the layer itself. Each program keeps its own license.
