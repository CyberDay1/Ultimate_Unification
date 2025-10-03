#!/usr/bin/env python3
import sys, hashlib, base64, json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PNG_1x1_BYTES = base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=")
PNG_1x1_SHA256 = hashlib.sha256(PNG_1x1_BYTES).hexdigest()

violations = []
for p in ROOT.rglob("*.png"):
    if any(part in {"build", ".gradle", ".git"} for part in p.parts):
        continue
    try:
        data = p.read_bytes()
    except Exception:
        continue
    if len(data) <= 100 and hashlib.sha256(data).hexdigest() == PNG_1x1_SHA256:
        violations.append(str(p.relative_to(ROOT)))
    if p.name.endswith("_placeholder.png"):
        violations.append(str(p.relative_to(ROOT)))

if violations:
    print("Placeholder PNGs detected (do not commit):")
    print(json.dumps(violations, indent=2))
    sys.exit(2)
print("No placeholder PNGs detected.")
