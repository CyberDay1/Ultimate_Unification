#!/usr/bin/env python3
import json, sys, os
root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
r = lambda *p: os.path.join(root, *p)

def read_json(p):
    with open(p, "r", encoding="utf-8") as f:
        return json.load(f)

errors = []

# Load materials spec
mat_path = r("src","main","resources","data","unifyworks","unify","materials.json")
data = read_json(mat_path)
ver = data.get("version", None)
if ver not in (3,4):
    errors.append(f"materials.json version {ver} not in (3,4)")
materials = [m for m in data.get("materials", []) if m.get("unify", False)]
names = [m["name"] for m in materials]
kinds = {m["name"]: m.get("kind","metal") for m in materials}

# Helper checks
def assert_file(p, desc):
    if not os.path.exists(p):
        errors.append(f"Missing {desc}: {os.path.relpath(p, root)}")

def has_lang_key(key):
    lang = r("src","main","resources","assets","unifyworks","lang","en_us.json")
    if not os.path.exists(lang):
        errors.append("Missing language file: assets/unifyworks/lang/en_us.json")
        return False
    with open(lang,"r",encoding="utf-8") as f:
        try:
            j = json.load(f)
        except Exception as e:
            errors.append(f"Invalid JSON in lang file: {e}")
            return False
    return key in j

# Item/block model + lang presence
for n in names:
    base = f"{n}_{'ingot' if kinds[n]=='metal' else 'gem'}"
    nug  = f"{n}_nugget"
    blk  = f"{n}_block"
    # item models
    assert_file(r("src","main","resources","assets","unifyworks","models","item", f"{base}.json"), f"item model {base}")
    assert_file(r("src","main","resources","assets","unifyworks","models","item", f"{nug}.json"), f"item model {nug}")
    # block model + blockstate
    assert_file(r("src","main","resources","assets","unifyworks","models","block", f"{blk}.json"), f"block model {blk}")
    assert_file(r("src","main","resources","assets","unifyworks","blockstates", f"{blk}.json"), f"blockstate {blk}")
    # loot table (storage block)
    assert_file(r("src","main","resources","data","unifyworks","loot_tables","blocks", f"{blk}.json"), f"loot table {blk}")
    # lang keys
    for k in [f"item.unifyworks.{base}", f"item.unifyworks.{nug}", f"block.unifyworks.{blk}"]:
        if not has_lang_key(k):
            errors.append(f"Missing lang key: {k}")

# Ore assets if present
for n in names:
    for variant in ["", "deepslate_"]:
        ore_id = f"{variant}{n}_ore"
        bstate = r("src","main","resources","assets","unifyworks","blockstates", f"{ore_id}.json")
        if os.path.exists(bstate):
            # paired model, loot, tags
            assert_file(
                r("src", "main", "resources", "assets", "unifyworks", "models", "block", f"{ore_id}.json"),
                f"block model {ore_id}",
            )
            assert_file(
                r("src", "main", "resources", "data", "unifyworks", "loot_tables", "blocks", f"{ore_id}.json"),
                f"loot table {ore_id}",
            )
            # needs_*_tool placement inferred by tags; just check at least one needs_* file mentions it
            tool_dir = r("src","main","resources","data","minecraft","tags","blocks")
            found = False
            if os.path.isdir(tool_dir):
                for f in os.listdir(tool_dir):
                    if not f.startswith("needs_") or not f.endswith("_tool.json"): continue
                    j = read_json(os.path.join(tool_dir,f))
                    vals = j.get("values", [])
                    if any(isinstance(v,str) and v.endswith(f":{ore_id}") for v in vals):
                        found = True; break
            if not found:
                errors.append(f"{ore_id} not present in any needs_*_tool tag JSON")

# Tag coverage check: forge + c for base and nuggets
def check_tag(ns, fam, name):
    path = r("src","main","resources","data",ns,"tags","items",fam,f"{name}.json")
    if not os.path.exists(path):
        errors.append(f"Missing tag: {ns}:{fam}/{name}")

for n in names:
    fam = "ingots" if kinds[n]=="metal" else "gems"
    check_tag("forge", fam, n)
    check_tag("c", fam, n)
    check_tag("forge","nuggets", n)
    check_tag("c","nuggets", n)
    # storage blocks items
    for ns in ["forge","c"]:
        p = r("src","main","resources","data",ns,"tags","items","storage_blocks", f"{n}.json")
        if not os.path.exists(p):
            errors.append(f"Missing tag: {ns}:storage_blocks/{n}")

# Report
if errors:
    print("QA scan found issues:")
    for e in errors: print(" -", e)
    sys.exit(2)
print("QA scan passed.")
