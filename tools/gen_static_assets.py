#!/usr/bin/env python3
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"


def jwrite(path: Path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")


def ensure_lang(lang_path: Path):
    lang_path.parent.mkdir(parents=True, exist_ok=True)
    if not lang_path.exists():
        with open(lang_path, "w", encoding="utf-8") as f:
            f.write("{}\n")
    with open(lang_path, "r", encoding="utf-8") as f:
        try:
            return json.load(f)
        except Exception:
            return {}


def save_lang(lang_path: Path, data: dict):
    lang_path.parent.mkdir(parents=True, exist_ok=True)
    with open(lang_path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def title_case_id(s: str) -> str:
    return " ".join(p.capitalize() for p in s.split("_"))


def copy_if_exists(src: Path, dst: Path):
    if src.exists():
        obj = json.loads(src.read_text(encoding="utf-8"))
        jwrite(dst, obj)


def ore_drop_item(modid: str, material: dict) -> str:
    kind = material.get("kind", "metal")
    name = material["name"]
    if kind == "metal":
        return f"{modid}:raw_{name}"
    return f"{modid}:{name}_gem"


def default_ore_loot(modid: str, ore_id: str, drop_item: str) -> dict:
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:alternatives",
                        "children": [
                            {
                                "type": "minecraft:item",
                                "name": f"{modid}:{ore_id}",
                                "conditions": [
                                    {
                                        "condition": "minecraft:match_tool",
                                        "predicate": {
                                            "enchantments": [
                                                {
                                                    "enchantment": "minecraft:silk_touch",
                                                    "levels": {"min": 1},
                                                }
                                            ]
                                        },
                                    }
                                ],
                            },
                            {
                                "type": "minecraft:item",
                                "name": drop_item,
                                "functions": [
                                    {"function": "minecraft:explosion_decay"},
                                    {
                                        "function": "minecraft:apply_bonus",
                                        "enchantment": "minecraft:fortune",
                                        "formula": "minecraft:ore_drops",
                                    },
                                ],
                            },
                        ],
                    }
                ],
            }
        ],
    }


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--modid", default="unifyworks")
    args = ap.parse_args()
    modid = args.modid

    mats_path = RES / "data" / modid / "unify" / "materials.json"
    data = json.loads(mats_path.read_text(encoding="utf-8"))
    mats = [m for m in data.get("materials", []) if m.get("unify", False)]

    # language file
    lang_path = RES / "assets" / modid / "lang" / "en_us.json"
    lang = ensure_lang(lang_path)

    # Helpers for tags
    def tag_path(ns, kind, family, name):
        if kind == "items":
            return RES / "data" / ns / "tags" / "items" / family / f"{name}.json"
        if kind == "blocks":
            return RES / "data" / ns / "tags" / "blocks" / family / f"{name}.json"
        raise ValueError("kind must be items or blocks")

    for m in mats:
        name = m["name"]
        kind = m.get("kind", "metal")  # metal, gem, etc.
        base_suffix = "ingot" if kind == "metal" else "gem"
        base_id = f"{name}_{base_suffix}"
        nug_id = f"{name}_nugget"
        blk_id = f"{name}_block"

        # Item models
        jwrite(
            RES / "assets" / modid / "models" / "item" / f"{base_id}.json",
            {"parent": "minecraft:item/generated", "textures": {"layer0": f"{modid}:item/{base_id}"}},
        )
        jwrite(
            RES / "assets" / modid / "models" / "item" / f"{nug_id}.json",
            {"parent": "minecraft:item/generated", "textures": {"layer0": f"{modid}:item/{nug_id}"}},
        )

        # Storage block models + blockstates
        jwrite(
            RES / "assets" / modid / "models" / "block" / f"{blk_id}.json",
            {"parent": "minecraft:block/cube_all", "textures": {"all": f"{modid}:block/{blk_id}"}},
        )
        jwrite(
            RES / "assets" / modid / "blockstates" / f"{blk_id}.json",
            {"variants": {"": {"model": f"{modid}:block/{blk_id}"}}},
        )
        # Block item model
        jwrite(
            RES / "assets" / modid / "models" / "item" / f"{blk_id}.json",
            {"parent": f"{modid}:block/{blk_id}"},
        )

        # Loot table for storage block
        jwrite(
            RES / "data" / modid / "loot_tables" / "blocks" / f"{blk_id}.json",
            {
                "type": "minecraft:block",
                "pools": [
                    {
                        "rolls": 1,
                        "entries": [{"type": "minecraft:item", "name": f"{modid}:{blk_id}"}],
                        "conditions": [{"condition": "minecraft:survives_explosion"}],
                    }
                ],
            },
        )

        # Language keys
        base_title = title_case_id(name) + (" Ingot" if kind == "metal" else " Gem")
        nug_title = title_case_id(name) + " Nugget"
        blk_title = title_case_id(name) + " Block"
        lang.setdefault(f"item.{modid}.{base_id}", base_title)
        lang.setdefault(f"item.{modid}.{nug_id}", nug_title)
        lang.setdefault(f"block.{modid}.{blk_id}", blk_title)

        # Tags: base + nuggets
        fam = "ingots" if kind == "metal" else "gems"
        jwrite(tag_path("forge", "items", fam, name), {"replace": False, "values": [f"{modid}:{base_id}"]})
        jwrite(tag_path("c", "items", fam, name), {"replace": False, "values": [f"{modid}:{base_id}"]})
        jwrite(tag_path("forge", "items", "nuggets", name), {"replace": False, "values": [f"{modid}:{nug_id}"]})
        jwrite(tag_path("c", "items", "nuggets", name), {"replace": False, "values": [f"{modid}:{nug_id}"]})

        # Tags: storage blocks (items + blocks)
        jwrite(
            tag_path("forge", "items", "storage_blocks", name),
            {"replace": False, "values": [f"{modid}:{blk_id}"]},
        )
        jwrite(
            tag_path("c", "items", "storage_blocks", name),
            {"replace": False, "values": [f"{modid}:{blk_id}"]},
        )
        jwrite(
            tag_path("forge", "blocks", "storage_blocks", name),
            {"replace": False, "values": [f"{modid}:{blk_id}"]},
        )
        jwrite(
            tag_path("c", "blocks", "storage_blocks", name),
            {"replace": False, "values": [f"{modid}:{blk_id}"]},
        )

        # Ore bridge tags (items + blocks) to internal helper tags
        jwrite(
            RES / "data" / "forge" / "tags" / "blocks" / "ores" / f"{name}.json",
            {"replace": False, "values": [f"#{modid}:{name}_ores_self"]},
        )
        jwrite(
            RES / "data" / "c" / "tags" / "blocks" / "ores" / f"{name}.json",
            {"replace": False, "values": [f"#{modid}:{name}_ores_self"]},
        )
        jwrite(
            RES / "data" / "forge" / "tags" / "items" / "ores" / f"{name}.json",
            {"replace": False, "values": [f"#{modid}:{name}_ores_items_self"]},
        )
        jwrite(
            RES / "data" / "c" / "tags" / "items" / "ores" / f"{name}.json",
            {"replace": False, "values": [f"#{modid}:{name}_ores_items_self"]},
        )

        # Ensure helper tags exist
        jwrite(
            RES / "data" / modid / "tags" / "blocks" / f"{name}_ores_self.json",
            {"replace": False, "values": [f"{modid}:{name}_ore", f"{modid}:deepslate_{name}_ore"]},
        )
        jwrite(
            RES / "data" / modid / "tags" / "items" / f"{name}_ores_items_self.json",
            {"replace": False, "values": [f"{modid}:{name}_ore", f"{modid}:deepslate_{name}_ore"]},
        )

        if name == "quartz":
            # add netherrack variant to helper tags
            for suffix in ["blocks", "items"]:
                p = RES / "data" / modid / "tags" / suffix / f"{name}_ores{'_items' if suffix == 'items' else ''}_self.json"
                obj = json.loads(p.read_text(encoding="utf-8"))
                vals = set(obj.get("values", []))
                vals.add(f"{modid}:netherrack_quartz_ore")
                obj["values"] = sorted(vals)
                jwrite(p, obj)

        # Ensure ore aliases (block models + loot tables)
        drop_item = ore_drop_item(modid, m)
        for variant in ("", "deepslate_"):
            ore_id = f"{variant}{name}_ore"
            bstate = RES / "assets" / modid / "blockstates" / f"{ore_id}.json"
            if bstate.exists():
                # Block model alias at root
                jwrite(
                    RES / "assets" / modid / "models" / "block" / f"{ore_id}.json",
                    {"parent": "minecraft:block/cube_all", "textures": {"all": f"{modid}:block/ore/{ore_id}"}},
                )
                # Copy loot table if stored in legacy directory, otherwise create default
                legacy_lt = RES / "data" / modid / "loot_table" / "blocks" / f"{ore_id}.json"
                new_lt = RES / "data" / modid / "loot_tables" / "blocks" / f"{ore_id}.json"
                if legacy_lt.exists():
                    copy_if_exists(legacy_lt, new_lt)
                elif not new_lt.exists():
                    jwrite(new_lt, default_ore_loot(modid, ore_id, drop_item))

    # Save language updates
    save_lang(lang_path, lang)

    print("Static resource generation complete.")


if __name__ == "__main__":
    main()
