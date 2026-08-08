#!/usr/bin/env python3
from __future__ import annotations

import argparse
import base64
import difflib
import json
import urllib.request
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable

import yaml


API_URL = "https://api.hypixel.net/resources/skyblock/items"
ITEMS_ROOT = Path("skyblock/items")
GENERATED_DIR = ITEMS_ROOT / "generated"
GENERATED_FILE = GENERATED_DIR / "hypixel_api_items.yml"

MATERIAL_ALIASES = {
    "LEAVES": "OAK_LEAVES",
    "RAW_FISH": "COD",
    "SKULL_ITEM": "PLAYER_HEAD",
    "INK_SACK": "INK_SAC",
    "GOLD_AXE": "GOLDEN_AXE",
    "GOLD_BOOTS": "GOLDEN_BOOTS",
    "GOLD_CHESTPLATE": "GOLDEN_CHESTPLATE",
    "GOLD_HELMET": "GOLDEN_HELMET",
    "GOLD_HOE": "GOLDEN_HOE",
    "GOLD_LEGGINGS": "GOLDEN_LEGGINGS",
    "GOLD_PICKAXE": "GOLDEN_PICKAXE",
    "GOLD_SPADE": "GOLDEN_SHOVEL",
    "GOLD_SWORD": "GOLDEN_SWORD",
    "IRON_SPADE": "IRON_SHOVEL",
    "DIAMOND_SPADE": "DIAMOND_SHOVEL",
    "STONE_SPADE": "STONE_SHOVEL",
    "WOOD_SPADE": "WOODEN_SHOVEL",
    "WOOD_AXE": "WOODEN_AXE",
    "WOOD_HOE": "WOODEN_HOE",
    "WOOD_PICKAXE": "WOODEN_PICKAXE",
    "WOOD_SWORD": "WOODEN_SWORD",
}

STAT_ALIASES = {
    "ABILITY_DAMAGE_PERCENT": "ability_damage",
    "DAMAGE": "damage",
    "STRENGTH": "strength",
    "CRIT_CHANCE": "critical_chance",
    "CRIT_DAMAGE": "critical_damage",
    "ATTACK_SPEED": "bonus_attack_speed",
    "HEALTH": "health",
    "DEFENSE": "defense",
    "SPEED": "speed",
    "WALK_SPEED": "speed",
    "INTELLIGENCE": "intelligence",
    "MAGIC_FIND": "magic_find",
    "PET_LUCK": "pet_luck",
    "FEROCITY": "ferocity",
    "ABILITY_DAMAGE": "ability_damage",
    "WEAPON_ABILITY_DAMAGE": "ability_damage",
    "MINING_SPEED": "mining_speed",
    "MINING_FORTUNE": "mining_fortune",
    "FARMING_FORTUNE": "farming_fortune",
    "FORAGING_FORTUNE": "foraging_fortune",
    "FISHING_SPEED": "fishing_speed",
    "SEA_CREATURE_CHANCE": "sea_creature_chance",
}

STANDARD_CATEGORY_TYPES = {
    "SWORD": "SWORD",
    "BOW": "BOW",
    "HELMET": "HELMET",
    "CHESTPLATE": "CHESTPLATE",
    "LEGGINGS": "LEGGINGS",
    "BOOTS": "BOOTS",
}

UNSUPPORTED_TOP_LEVEL_KEYS = {
    "color",
    "dungeon_item_conversion_cost",
    "experience",
    "generator",
    "glowing",
    "soulbound",
    "upgrade_costs",
}


@dataclass(frozen=True)
class ApiData:
    last_updated: int | None
    items: list[dict[str, Any]]


class IndentedDumper(yaml.SafeDumper):
    def increase_indent(self, flow: bool = False, indentless: bool = False) -> None:
        return super().increase_indent(flow, False)


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    if getattr(args, "yes", False):
        args.apply = True
    root = Path(args.root)
    api_data = load_api_data(args)

    if args.command == "inspect-fields":
        inspect_fields(api_data.items)
        return 0

    if args.command == "generate":
        generated = build_generated_document(api_data, root, args.limit)
        write_or_preview(GENERATED_FILE, generated, args.apply, args.interactive, args.yes, root)
        return 0

    if args.command == "update-attribute-shards":
        update_attribute_shards(root, args.neu_items, args.apply, args.interactive, args.yes)
        return 0

    known = load_known_item_locations(root / ITEMS_ROOT)
    if args.command == "update-sell-prices":
        update_known_items(
            root=root,
            api_items=api_data.items,
            known_locations=known,
            updater=update_sellable_component,
            field_name="npc sell prices",
            apply=args.apply,
            interactive=args.interactive,
            yes=args.yes,
        )
        return 0

    if args.command == "update-known":
        update_known_items(
            root=root,
            api_items=api_data.items,
            known_locations=known,
            updater=update_known_metadata,
            field_name="material/rarity/head texture/item model metadata",
            apply=args.apply,
            interactive=args.interactive,
            yes=args.yes,
        )
        return 0

    parser.error(f"Unknown command {args.command}")
    return 2


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=".", help="Repository root. Defaults to current directory.")
    parser.add_argument("--api-url", default=API_URL, help="Hypixel item resource URL.")
    parser.add_argument("--api-json", type=Path, help="Use a saved API response instead of fetching.")
    parser.add_argument("--timeout", type=float, default=30.0, help="HTTP timeout in seconds.")

    sub = parser.add_subparsers(dest="command", required=True)

    inspect = sub.add_parser("inspect-fields", help="Print unique top-level API fields and nested component-like fields.")
    inspect.add_argument("--sample", type=int, default=0, help="Only inspect the first N API items after loading.")

    generate = sub.add_parser("generate", help="Generate missing API-backed sample items into items/generated.")
    generate.add_argument("--limit", type=int, default=0, help="Limit generated missing items, useful for reviewing samples.")
    add_write_flags(generate)

    sell = sub.add_parser("update-sell-prices", help="Update SELLABLE values for known item ids with npc_sell_price.")
    add_write_flags(sell)

    known = sub.add_parser("update-known", help="Update safe API metadata on known ids: material, rarity, head texture, and item_model.")
    add_write_flags(known)

    shards = sub.add_parser("update-attribute-shards", help="Update shard materials, heads and rarities from NEU's captured Hypixel item data.")
    shards.add_argument("--neu-items", type=Path, required=True, help="Path to the NotEnoughUpdates-REPO items directory.")
    add_write_flags(shards)

    return parser


def add_write_flags(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--apply", action="store_true", help="Write accepted changes. Without this, only diffs are shown.")
    parser.add_argument("--interactive", action="store_true", help="Prompt for each changed file.")
    parser.add_argument("--yes", action="store_true", help="Accept all changed files without prompting.")


def load_api_data(args: argparse.Namespace) -> ApiData:
    if args.api_json:
        raw = json.loads(args.api_json.read_text(encoding="utf-8"))
    else:
        request = urllib.request.Request(args.api_url, headers={"User-Agent": "HypixelSkyBlockItemConfigUpdater/1.0"})
        with urllib.request.urlopen(request, timeout=args.timeout) as response:
            raw = json.loads(response.read().decode("utf-8"))

    if not raw.get("success", False):
        raise SystemExit("Hypixel item resource did not report success=true")
    items = raw.get("items")
    if not isinstance(items, list):
        raise SystemExit("Hypixel item resource did not contain an items list")
    if getattr(args, "sample", 0):
        items = items[: args.sample]
    return ApiData(last_updated=raw.get("lastUpdated"), items=items)


def inspect_fields(items: Iterable[dict[str, Any]]) -> None:
    top_level: dict[str, int] = {}
    nested: dict[str, set[str]] = {}
    categories: set[str] = set()
    stat_keys: set[str] = set()
    salvage_types: set[str] = set()

    for item in items:
        for key, value in item.items():
            top_level[key] = top_level.get(key, 0) + 1
            if isinstance(value, dict):
                nested.setdefault(key, set()).update(value.keys())
        if isinstance(item.get("category"), str):
            categories.add(item["category"])
        if isinstance(item.get("stats"), dict):
            stat_keys.update(item["stats"].keys())
        for salvage in item.get("salvages") or []:
            if isinstance(salvage, dict) and isinstance(salvage.get("type"), str):
                salvage_types.add(salvage["type"])

    print("Top-level fields:")
    for key, count in sorted(top_level.items()):
        print(f"  {key}: {count}")
    print("\nNested fields:")
    for key, fields in sorted(nested.items()):
        print(f"  {key}: {', '.join(sorted(fields))}")
    print("\nCategories:")
    print("  " + ", ".join(sorted(categories)))
    print("\nStat keys:")
    print("  " + ", ".join(sorted(stat_keys)))
    print("\nSalvage types:")
    print("  " + ", ".join(sorted(salvage_types)))


def build_generated_document(api_data: ApiData, root: Path, limit: int) -> str:
    known_ids = set(load_known_item_locations(root / ITEMS_ROOT))
    generated: list[dict[str, Any]] = []

    for api_item in api_data.items:
        item_id = api_item.get("id")
        if not isinstance(item_id, str) or item_id in known_ids:
            continue
        generated_item = translate_api_item(api_item)
        if generated_item:
            generated.append(generated_item)
        if limit and len(generated) >= limit:
            break

    document = {
        "items": generated,
    }
    header = [
        "# Generated from Hypixel's partial SkyBlock item resource.",
        "# Do not treat this file as full source-of-truth item data.",
    ]
    if api_data.last_updated is not None:
        header.append(f"# API lastUpdated: {api_data.last_updated}")
    return "\n".join(header) + "\n" + dump_yaml(document)


def translate_api_item(api_item: dict[str, Any]) -> dict[str, Any] | None:
    item_id = api_item.get("id")
    if not isinstance(item_id, str) or not item_id:
        return None

    translated: dict[str, Any] = {
        "id": item_id,
        "material": normalize_material(api_item.get("material")),
    }
    if isinstance(api_item.get("tier"), str):
        translated["rarity"] = api_item["tier"]

    lore = translate_lore(api_item.get("description"))
    if lore:
        translated["lore"] = lore

    stats = translate_stats(api_item.get("stats"))
    if stats:
        translated["default_statistics"] = stats

    components = translate_components(api_item)
    if components:
        translated["components"] = components

    unsupported = sorted(key for key in api_item if key in UNSUPPORTED_TOP_LEVEL_KEYS)
    if unsupported:
        translated["_api_unsupported_fields"] = unsupported

    return translated


def normalize_material(material: Any) -> str:
    if not isinstance(material, str) or not material:
        return "BARRIER"
    normalized = MATERIAL_ALIASES.get(material.upper(), material.upper())
    return normalized


def translate_lore(description: Any) -> list[str]:
    if not isinstance(description, str) or not description:
        return []
    return description.splitlines()


def translate_stats(stats: Any) -> dict[str, float]:
    if not isinstance(stats, dict):
        return {}
    translated: dict[str, float] = {}
    for key, value in stats.items():
        if not isinstance(value, (int, float)):
            continue
        normalized = STAT_ALIASES.get(str(key).upper(), str(key).lower())
        translated[normalized] = float(value)
    return translated


def translate_components(api_item: dict[str, Any]) -> list[dict[str, Any]]:
    components: list[dict[str, Any]] = []

    sell_price = api_item.get("npc_sell_price")
    if isinstance(sell_price, (int, float)):
        components.append({"id": "SELLABLE", "value": float(sell_price)})

    texture = extract_texture_hash(api_item.get("skin"))
    if texture:
        components.append({"id": "SKULL_HEAD", "texture": texture})

    item_model = api_item.get("item_model")
    if isinstance(item_model, str) and item_model:
        components.append({"id": "ITEM_MODEL", "item_model": item_model})

    category = api_item.get("category")
    if isinstance(category, str):
        if category in STANDARD_CATEGORY_TYPES:
            components.append({"id": "STANDARD_ITEM", "standard_item_type": STANDARD_CATEGORY_TYPES[category]})
        if category == "ACCESSORY":
            components.append({"id": "ACCESSORY"})

    museum = translate_museum(api_item.get("museum_data"))
    if museum:
        components.append(museum)

    return components


def translate_museum(museum_data: Any) -> dict[str, Any] | None:
    if not isinstance(museum_data, dict):
        return None
    component: dict[str, Any] = {"id": "MUSEUM"}
    if isinstance(museum_data.get("category"), str):
        component["museum_category"] = museum_data["category"]
    if isinstance(museum_data.get("game_stage"), str):
        component["game_stage"] = museum_data["game_stage"]
    if isinstance(museum_data.get("donation_xp"), int):
        component["donation_xp"] = museum_data["donation_xp"]
    if isinstance(museum_data.get("parent"), dict) and museum_data["parent"]:
        component["parent"] = museum_data["parent"]
    if isinstance(museum_data.get("mapped_item_ids"), list):
        component["mapped_item_ids"] = museum_data["mapped_item_ids"]
    return component if len(component) > 1 else None


def extract_texture_hash(skin: Any) -> str | None:
    if not isinstance(skin, dict):
        return None
    value = skin.get("value")
    if not isinstance(value, str) or not value:
        return None
    try:
        decoded = base64.b64decode(value).decode("utf-8")
        texture_json = json.loads(decoded)
        url = texture_json["textures"]["SKIN"]["url"]
    except Exception:
        return None
    if not isinstance(url, str):
        return None
    return url.rstrip("/").split("/")[-1]


def load_known_item_locations(items_root: Path) -> dict[str, tuple[Path, int]]:
    locations: dict[str, tuple[Path, int]] = {}
    for path in sorted(items_root.rglob("*.yml")) + sorted(items_root.rglob("*.yaml")):
        data = load_yaml_file(path)
        for index, item in enumerate(data.get("items") or []):
            if isinstance(item, dict) and isinstance(item.get("id"), str):
                locations[item["id"]] = (path, index)
    return locations


def update_known_items(
    root: Path,
    api_items: list[dict[str, Any]],
    known_locations: dict[str, tuple[Path, int]],
    updater: Any,
    field_name: str,
    apply: bool,
    interactive: bool,
    yes: bool,
) -> None:
    files: dict[Path, dict[str, Any]] = {}
    changed_files: set[Path] = set()
    changed_items = 0

    for api_item in api_items:
        item_id = api_item.get("id")
        if not isinstance(item_id, str) or item_id not in known_locations:
            continue
        path, index = known_locations[item_id]
        data = files.setdefault(path, load_yaml_file(path))
        items = data.get("items") or []
        if index >= len(items) or not isinstance(items[index], dict):
            continue
        if updater(items[index], api_item):
            changed_files.add(path)
            changed_items += 1

    print(f"Prepared {changed_items} {field_name} changes across {len(changed_files)} files.")
    for path in sorted(changed_files):
        new_text = dump_yaml(files[path])
        write_or_preview(path, new_text, apply, interactive, yes, root)


def update_sellable_component(item: dict[str, Any], api_item: dict[str, Any]) -> bool:
    sell_price = api_item.get("npc_sell_price")
    if not isinstance(sell_price, (int, float)):
        return False
    components = ensure_components(item)
    sellable = next((component for component in components if component.get("id") == "SELLABLE"), None)
    if sellable is None:
        components.append({"id": "SELLABLE", "value": float(sell_price)})
        return True
    if float(sellable.get("value", 0.0)) == float(sell_price):
        return False
    sellable["value"] = float(sell_price)
    return True


def update_known_metadata(item: dict[str, Any], api_item: dict[str, Any]) -> bool:
    changed = False
    material = normalize_material(api_item.get("material"))
    if item.get("material") != material:
        item["material"] = material
        changed = True
    tier = api_item.get("tier")
    if isinstance(tier, str) and item.get("rarity") != tier:
        item["rarity"] = tier
        changed = True

    texture = extract_texture_hash(api_item.get("skin"))
    if texture:
        components = ensure_components(item)
        skull = next((component for component in components if component.get("id") == "SKULL_HEAD"), None)
        if skull is None:
            components.append({"id": "SKULL_HEAD", "texture": texture})
            changed = True
        elif skull.get("texture") != texture:
            skull["texture"] = texture
            changed = True

    item_model = api_item.get("item_model")
    if isinstance(item_model, str) and item_model:
        components = ensure_components(item)
        model = next((component for component in components if component.get("id") == "ITEM_MODEL"), None)
        if model is None:
            components.append({"id": "ITEM_MODEL", "item_model": item_model})
            changed = True
        elif model.get("item_model") != item_model:
            model["item_model"] = item_model
            changed = True
    else:
        components = item.get("components")
        if isinstance(components, list):
            without_models = [component for component in components
                              if not isinstance(component, dict) or component.get("id") != "ITEM_MODEL"]
            if len(without_models) != len(components):
                item["components"] = without_models
                changed = True

    return changed


def update_attribute_shards(root: Path, neu_items: Path, apply: bool, interactive: bool, yes: bool) -> None:
    """Update dynamic Attribute Shards, which the public item API exposes only as ATTRIBUTE_SHARD."""
    target = ITEMS_ROOT / "hunting/attribute_shards.yml"
    document = load_yaml_file(root / target)
    by_name: dict[str, dict[str, Any]] = {}
    for path in neu_items.glob("ATTRIBUTE_SHARD_*.json"):
        value = json.loads(path.read_text(encoding="utf-8"))
        display_name = strip_minecraft_formatting(value.get("displayname", ""))
        if display_name.endswith(" Shard"):
            by_name[normalize_shard_name(display_name[:-6])] = value

    changed = 0
    for item in document.get("items") or []:
        if not isinstance(item, dict) or not str(item.get("id", "")).startswith("SHARD_"):
            continue
        neu = by_name.get(normalize_shard_name(str(item["id"])[6:].replace("_", " ")))
        if neu is None:
            continue
        before = json.dumps(item, sort_keys=True)
        item["material"] = normalize_minecraft_item(neu.get("itemid"))
        lore = [strip_minecraft_formatting(line) for line in neu.get("lore") or []]
        rarity = next((name for name in ("LEGENDARY", "EPIC", "RARE", "UNCOMMON", "COMMON")
                       if any(name in line for line in reversed(lore))), None)
        if rarity:
            item["rarity"] = rarity
        components = [component for component in ensure_components(item) if component.get("id") not in {"ITEM_MODEL", "SKULL_HEAD"}]
        texture = extract_neu_texture(neu.get("nbttag"))
        if texture:
            components.append({"id": "SKULL_HEAD", "texture": texture})
        item["components"] = components
        changed += before != json.dumps(item, sort_keys=True)
    print(f"Prepared {changed} Attribute Shard metadata changes from {len(by_name)} NEU captures.")
    write_or_preview(target, dump_yaml(document), apply, interactive, yes, root)


def strip_minecraft_formatting(value: str) -> str:
    return re.sub(r"\u00a7.", "", value)


def normalize_shard_name(value: str) -> str:
    return value.upper().replace("SPECTER", "SPECTRE")


def normalize_minecraft_item(value: Any) -> str:
    name = str(value or "minecraft:paper").removeprefix("minecraft:").upper()
    return "PLAYER_HEAD" if name in {"SKULL", "PLAYER_HEAD"} else MATERIAL_ALIASES.get(name, name)


def extract_neu_texture(nbt: Any) -> str | None:
    if not isinstance(nbt, str):
        return None
    match = re.search(r'Value:"([A-Za-z0-9+/=]+)"', nbt)
    if match is None:
        return None
    try:
        payload = json.loads(base64.b64decode(match.group(1)).decode("utf-8"))
        return payload["textures"]["SKIN"]["url"].rstrip("/").split("/")[-1]
    except Exception:
        return None


def ensure_components(item: dict[str, Any]) -> list[dict[str, Any]]:
    components = item.get("components")
    if not isinstance(components, list):
        components = []
        item["components"] = components
    return components


def load_yaml_file(path: Path) -> dict[str, Any]:
    if not path.exists():
        return {"items": []}
    with path.open("r", encoding="utf-8") as file:
        loaded = yaml.safe_load(file) or {}
    if not isinstance(loaded, dict):
        return {"items": []}
    return loaded


def dump_yaml(data: dict[str, Any]) -> str:
    text = yaml.dump(data, Dumper=IndentedDumper, sort_keys=False, allow_unicode=False, width=120)
    return text if text.endswith("\n") else text + "\n"


def write_or_preview(path: Path, new_text: str, apply: bool, interactive: bool, yes: bool, root: Path) -> None:
    absolute = root / path if not path.is_absolute() else path
    old_text = absolute.read_text(encoding="utf-8") if absolute.exists() else ""
    if old_text == new_text:
        print(f"No changes: {path}")
        return

    diff = "".join(
        difflib.unified_diff(
            old_text.splitlines(keepends=True),
            new_text.splitlines(keepends=True),
            fromfile=str(path),
            tofile=str(path),
        )
    )
    print(diff)

    if not apply:
        return
    if interactive and not yes:
        answer = input(f"Apply changes to {path}? [y/N] ").strip().lower()
        if answer not in {"y", "yes"}:
            print(f"Skipped: {path}")
            return
    elif not yes and interactive:
        return

    absolute.parent.mkdir(parents=True, exist_ok=True)
    absolute.write_text(new_text, encoding="utf-8")
    print(f"Wrote: {path}")


if __name__ == "__main__":
    raise SystemExit(main())
