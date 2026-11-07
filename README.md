# meta-accessibility

A small Yocto layer for building images that can talk and drive a braille
display.

> Work in progress. Not usable yet.

## What's in it

- **espeak-ng**: text-to-speech (work in progress)
- **speech-dispatcher**: speech API that apps talk to (work in progress)
- **brltty**: braille displays and console screen reader (work in progress)
- **liblouis**: braille translation tables (work in progress)
- **orca**: screen reader for GTK desktops (optional) (work in progress)

None of these have an up-to-date recipe in the main OpenEmbedded layers,
which is why this layer exists.

## Depends on

- openembedded-core (work in progress)
- meta-oe (work in progress)
- meta-python (only for orca) (work in progress)

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
runqemu qemux86-64 nographic audio
espeak-ng "hello"
spd-say "hello"
```

## License

MIT for the layer itself. Each program keeps its own license.
