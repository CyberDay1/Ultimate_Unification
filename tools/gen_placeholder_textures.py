#!/usr/bin/env python3
"""Generate reports for missing textures and optionally write 1×1 PNG placeholders."""

import argparse
import base64
import json
from pathlib import Path
from typing import Iterable, List

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src" / "main" / "resources"
MODID = "unifyworks"

# 1x1 fully opaque white PNG encoded as base64 (kept inline to avoid shipping binaries)
PNG_1x1 = base64.b64decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII="
)


def iter_targets(materials: Iterable[dict]) -> Iterable[tuple[str, Path]]:
    textures_root = RESOURCES / "assets" / MODID / "textures"
    for mat in materials:
        if not mat.get("unify", False):
            continue
        name = mat["name"]
        kind = mat.get("kind", "metal")
        base = f"{name}_{'ingot' if kind == 'metal' else 'gem'}"
        yield f"item/{base}.png", textures_root / "item" / f"{base}.png"
        yield f"item/{name}_nugget.png", textures_root / "item" / f"{name}_nugget.png"
        yield f"block/{name}_block.png", textures_root / "block" / f"{name}_block.png"


def write_placeholder(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(PNG_1x1)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--write",
        action="store_true",
        help="Actually write placeholder PNGs for any missing textures.",
    )
    args = parser.parse_args()

    mats_path = RESOURCES / "data" / MODID / "unify" / "materials.json"
    materials = json.loads(mats_path.read_text("utf-8")).get("materials", [])

    missing: List[str] = []
    created: List[str] = []

    for rel_path, fs_path in iter_targets(materials):
        if fs_path.exists():
            continue
        missing.append(rel_path)
        if args.write:
            write_placeholder(fs_path)
            created.append(rel_path)

    report = {
        "mode": "write" if args.write else "dry-run",
        "missing": sorted(missing),
    }
    if args.write:
        report["created"] = sorted(created)

    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
