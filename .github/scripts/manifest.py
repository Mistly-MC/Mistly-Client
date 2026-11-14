"""Copies the jars of every Minecraft version to one folder and writes misty-client.json there.

misty-client.json, read by the Mistly launcher:
    {"version": "1.0.2", "jars": [{"file": "misty-client-1.0.2+mc26.1.2.jar",
      "minecraft": ["26.1", "26.1.1", "26.1.2"], "sha1": "..."}]}
"minecraft" comes from mod.mc_releases in stonecutter.properties.toml (else the jar's own version).
"""
import hashlib
import json
import shutil
import sys
import tomllib
from pathlib import Path

out = Path(sys.argv[1])
out.mkdir(parents=True, exist_ok=True)
props = tomllib.loads(Path("stonecutter.properties.toml").read_text(encoding="utf-8"))

jars = []
for jar in sorted(Path("versions").glob("*/build/libs/misty-client-*.jar")):
    if jar.name.endswith("-sources.jar") or "+mc" not in jar.name:
        continue
    mc = jar.stem.split("+mc", 1)[1]
    releases = props.get(mc, {}).get("mod", {}).get("mc_releases", [mc])
    shutil.copy2(jar, out / jar.name)
    jars.append({"file": jar.name, "minecraft": releases, "sha1": hashlib.sha1(jar.read_bytes()).hexdigest()})

if not jars:
    sys.exit("no jar in versions/*/build/libs")
manifest = {"version": props["mod"]["version"], "jars": jars}
(out / "misty-client.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
print(json.dumps(manifest, indent=2))
