"""Copies the jars of every Minecraft version to one folder and writes misty-client.json there.

misty-client.json, read by the Mistly launcher:
    {"version": "1.0.2",
     "jars": [{"file": "misty-client-1.0.2+mc26.1.2.jar", "minecraft": ["26.1", "26.1.1", "26.1.2"], "sha1": "..."}],
     "newer_java": [{"file": "misty-client-1.0.2+mc1.16.5.jar", "minecraft": ["1.16.5"], "java": 17, "sha1": "..."}],
     "forge": [{"file": "misty-client-1.0.2+mc1.8.9.jar", "minecraft": ["1.8.9"], "loader": "forge", "sha1": "..."}]}
"jars" are the Fabric jars, the ones the launcher installs; "minecraft" comes from mod.mc_releases in
stonecutter.properties.toml (else the jar's own version). "newer_java" lists the Fabric jars of the
versions Mojang starts with an older Java than the mod needs (mod.java in stonecutter.properties.toml):
apart, so that a launcher starting them with Mojang's Java never installs them. "forge" lists the
Forge jars (forge-<mc>/) apart, so that a launcher reading "jars" only never takes them for Fabric ones.
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


def entry(jar: Path, releases: list, **extra) -> dict:
    shutil.copy2(jar, out / jar.name)
    return {"file": jar.name, "minecraft": releases, **extra, "sha1": hashlib.sha1(jar.read_bytes()).hexdigest()}


def built(pattern: str) -> list:
    return [jar for jar in sorted(Path(".").glob(pattern)) if not jar.name.endswith("-sources.jar") and "+mc" in jar.name]


jars = []
newer_java = []
for jar in built("versions/*/build/libs/misty-client-*.jar"):
    mc = jar.stem.split("+mc", 1)[1]
    mod = props.get(mc, {}).get("mod", {})
    releases = mod.get("mc_releases", [mc])
    if "java" in mod:
        newer_java.append(entry(jar, releases, java=mod["java"]))
    else:
        jars.append(entry(jar, releases))

forge = [entry(jar, [jar.stem.split("+mc", 1)[1]], loader="forge") for jar in built("forge-*/build/libs/misty-client-*.jar")]

if not jars:
    sys.exit("no jar in versions/*/build/libs")
manifest = {"version": props["mod"]["version"], "jars": jars, "newer_java": newer_java, "forge": forge}
(out / "misty-client.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
print(json.dumps(manifest, indent=2))
