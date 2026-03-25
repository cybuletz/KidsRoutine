package com.kidsroutine.feature.world.data

import com.kidsroutine.core.model.WorldTheme

// ─────────────────────────────────────────────────────────────────────────────
// 500-level world progression table
// Structure: 10 Zones × 50 levels each
// Each zone has a theme, unique node titles, emojis, and a boss every 10 levels
// XP curve: early levels fast (50 XP each), scaling to 500 XP per level at end
// ─────────────────────────────────────────────────────────────────────────────

data class LevelDefinition(
    val level: Int,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val requiredXp: Int,   // cumulative XP needed to unlock this node
    val rewardXp: Int,
    val theme: WorldTheme,
    val isBoss: Boolean
)

object WorldLevelData {

    // XP required to unlock level N (1-indexed, cumulative)
    // Levels 1-50:   50 XP each  → max 2,500
    // Levels 51-100: 80 XP each  → max 6,500
    // Levels 101-150:120 XP each → max 12,500
    // Levels 151-200:160 XP each → max 20,500
    // Levels 201-250:200 XP each → max 30,500
    // Levels 251-300:250 XP each → max 43,000
    // Levels 301-350:300 XP each → max 58,000
    // Levels 351-400:350 XP each → max 75,500
    // Levels 401-450:400 XP each → max 95,500
    // Levels 451-500:500 XP each → max 120,500
    fun xpForLevel(level: Int): Int {
        val perLevel = when {
            level <= 50  -> 50
            level <= 100 -> 80
            level <= 150 -> 120
            level <= 200 -> 160
            level <= 250 -> 200
            level <= 300 -> 250
            level <= 350 -> 300
            level <= 400 -> 350
            level <= 450 -> 400
            else         -> 500
        }
        // Sum of all previous tiers + remainder of current tier
        val tiers = listOf(
            50 to 50, 100 to 80, 150 to 120, 200 to 160,
            250 to 200, 300 to 250, 350 to 300, 400 to 350,
            450 to 400, 500 to 500
        )
        var cumulative = 0
        for ((cap, xp) in tiers) {
            val prevCap = if (cap == 50) 0 else tiers[tiers.indexOfFirst { it.first == cap } - 1].first
            if (level > cap) {
                cumulative += (cap - prevCap) * xp
            } else {
                cumulative += (level - prevCap) * xp
                break
            }
        }
        return cumulative
    }

    private val zone1 = listOf(  // JUNGLE — levels 1-50
        "Seed Sprout" to "🌱", "Mushroom Grove" to "🍄", "Butterfly Meadow" to "🦋",
        "Cherry Blossom" to "🌸", "Vine Bridge" to "🌿", "Fox Den" to "🦊",
        "Ancient Oak" to "🌳", "Leaf Canopy" to "🍃", "Deer Trail" to "🦌",
        "Jungle Entrance" to "🌴", "Parrot Perch" to "🦜", "Leopard Ledge" to "🐆",
        "Orchid Path" to "🌺", "Banana Falls" to "🍌", "Ape Sanctuary" to "🦧",
        "Fern Hollow" to "🌿", "Python Ridge" to "🐍", "Morpho Wing" to "🦋",
        "Croco Swamp" to "🐊", "Golden Pollen" to "💛", "Mango Summit" to "🥭",
        "Piranha Pool" to "🐟", "Toucan Peak" to "🦜", "Night Bloomer" to "🌙",
        "Glowing Spores" to "✨", "Thunder Root" to "⚡", "Tribe Camp" to "🏕️",
        "Ritual Stone" to "🗿", "Lost Temple" to "🏛️", "Idol Chamber" to "🗽",
        "Torch Hall" to "🔦", "Fossil Wall" to "🦕", "Amber Trap" to "🟡",
        "Bone Maze" to "💀", "Crystal Fern" to "💎", "Echo Cave" to "🌀",
        "Poison Dart" to "💚", "Hidden Shrine" to "🙏", "Jungle River" to "🏞️",
        "Waterfall Mist" to "💧", "Fire Orchid" to "🌺", "Jaguar Run" to "🐆",
        "Emerald Lake" to "🟢", "Monkey Bridge" to "🐒", "Storm Clearing" to "⛈️",
        "Moonlit Glade" to "🌕", "Spirit Tree" to "🌲", "Ancient Gate" to "🚪",
        "Jungle Summit" to "🏔️", "Jungle Dragon" to "🐉"
    )

    private val zone2 = listOf(  // OCEAN — levels 51-100
        "Shallow Reef" to "🐠", "Crab Shore" to "🦀", "Octopus Cove" to "🐙",
        "Wave Break" to "🌊", "Dolphin Bay" to "🐬", "Hammerhead Pass" to "🦈",
        "Whale Song" to "🐋", "Coral Garden" to "🪸", "Puffer Fish" to "🐡",
        "Starfish Beach" to "⭐", "Jellyfish Bloom" to "🪼", "Sea Turtle Path" to "🐢",
        "Sunken Ship" to "⚓", "Treasure Hold" to "💰", "Pearl Oyster" to "🦪",
        "Manta Ray" to "🌊", "Anglerfish Deep" to "🐟", "Hydrothermal Vent" to "♨️",
        "Abyssal Zone" to "🌑", "Sea Dragon" to "🐉", "Tidal Cave" to "🌊",
        "Phospho Lake" to "💙", "Blue Whale Road" to "🐋", "Ice Shelf" to "🧊",
        "Kelp Forest" to "🌿", "Electric Eel" to "⚡", "Sea Witch" to "🧙",
        "Poseidon Arch" to "🏛️", "Current Spiral" to "🌀", "Narwhal Keep" to "🦄",
        "Sunken City" to "🏙️", "Bubble Trench" to "🫧", "Deep Glow" to "💡",
        "Abyss Door" to "🚪", "Cold Current" to "❄️", "Lava Flow" to "🔥",
        "Basalt Pillar" to "🪨", "Magma Vent" to "🌋", "Storm Surge" to "⛈️",
        "Rogue Wave" to "🌊", "Kraken Arm" to "🦑", "Siren Rock" to "🎵",
        "Whirlpool" to "🌀", "Poseidon Gate" to "⚡", "Lost Atlantis" to "🏛️",
        "Golden Trident" to "🔱", "Neptune Throne" to "👑", "Tide Caller" to "🌊",
        "Ocean Summit" to "🏔️", "Sea Kraken" to "🦑"
    )

    private val zone3 = listOf(  // SPACE — levels 101-150
        "Launch Pad" to "🚀", "Asteroid Belt" to "☄️", "Moon Base" to "🌙",
        "Saturn Rings" to "🪐", "Alien Greeting" to "👾", "Saucer Route" to "🛸",
        "Nebula Cloud" to "🌌", "Shooting Star" to "💫", "Observatory" to "🔭",
        "Red Giant" to "🔴", "Binary Stars" to "✨", "Black Dwarf" to "⚫",
        "Pulsar Beat" to "💓", "Quasar Flash" to "⚡", "Dark Matter" to "🌑",
        "Wormhole Edge" to "🌀", "Supernova" to "💥", "Event Horizon" to "🕳️",
        "Neutron Star" to "💫", "Cosmic Ray" to "☢️", "Gravity Well" to "🌊",
        "Space Whale" to "🐋", "Star Forge" to "⚒️", "Planet Nursery" to "🌍",
        "Comet Trail" to "☄️", "Alien Ruins" to "🏛️", "Zero Gravity" to "🌀",
        "Ion Storm" to "⚡", "Photon Barrier" to "🛡️", "Plasma Field" to "🔥",
        "Space Station" to "🛸", "Docking Bay" to "⚓", "Moon Garden" to "🌸",
        "Titan Shore" to "🟠", "Europa Ice" to "🧊", "Ganymede Rock" to "🪨",
        "Io Volcano" to "🌋", "Jupiter Storm" to "🌪️", "Uranus Ring" to "🔵",
        "Neptune Gale" to "🌬️", "Pluto Field" to "⚫", "Kuiper Belt" to "🪐",
        "Oort Cloud" to "🌌", "Proxima Path" to "⭐", "Andromeda Call" to "📡",
        "Milky Core" to "🌟", "Galactic Rim" to "🌌", "Star Map" to "🗺️",
        "Cosmos Gate" to "🚪", "Space Dragon" to "🐉"
    )

    private val zone4 = listOf(  // VOLCANO — levels 151-200
        "Lava Shore" to "🔥", "Ash Plain" to "💨", "Magma Pit" to "🌋",
        "Fire Drake" to "🐲", "Obsidian Wall" to "⬛", "Sulfur Lake" to "💛",
        "Flame Geyser" to "♨️", "Cinder Path" to "🪨", "Molten Core" to "🔴",
        "Lava Tube" to "🕳️", "Eruption Point" to "🌋", "Fire Orchid" to "🌺",
        "Char Forest" to "🌲", "Smoke Signal" to "💨", "Ember Field" to "⭐",
        "Pyroclast" to "💥", "Tephra Rain" to "🌧️", "Magma Chamber" to "🔥",
        "Caldera Rim" to "🌋", "Dragon Nest" to "🥚", "Lava Bridge" to "🌉",
        "Fire Salamander" to "🦎", "Hellhound Pass" to "🐕", "Inferno Gate" to "🚪",
        "Phoenix Nest" to "🦅", "Rebirth Flame" to "🔥", "Titan Forge" to "⚒️",
        "Iron Rain" to "⛈️", "Metal Storm" to "🌪️", "Steel Keep" to "🏰",
        "Volcano Summit" to "🏔️", "Fire River" to "🌊", "Ash Crown" to "👑",
        "Magma Throne" to "🪑", "Obsidian Blade" to "⚔️", "Flame Whip" to "🌀",
        "Igneous Cave" to "🕳️", "Petrified Wood" to "🪵", "Basalt Throne" to "🪑",
        "Lava Kraken" to "🦑", "Fire Whale" to "🐋", "Erupt Dance" to "💃",
        "Cinder Crown" to "👑", "Ash Summit" to "🏔️", "Flame Keep" to "🏰",
        "Magma Dragon" to "🐲", "Fire Core" to "🔥", "Volcano Heart" to "❤️‍🔥",
        "Lava Summit" to "🌋", "Volcano Dragon" to "🐉"
    )

    private val zone5 = listOf(  // ARCTIC — levels 201-250
        "Frost Shore" to "❄️", "Penguin March" to "🐧", "Seal Floe" to "🦭",
        "Ice Shelf" to "🧊", "Blizzard Pass" to "🌨️", "Wolf Pack" to "🐺",
        "Arctic Fox" to "🦊", "Snow Storm" to "❄️", "Glacier Cave" to "🏔️",
        "Polar Bear" to "🐻‍❄️", "Aurora Watch" to "🌌", "Ice Crystal" to "💎",
        "Frozen Lake" to "🌊", "Snow Leopard" to "🐆", "Tundra Trail" to "🏕️",
        "Mammoth Path" to "🐘", "Ice Fishing" to "🎣", "Whale Breath" to "🐋",
        "Walrus Rock" to "🪨", "Narwhal Sea" to "🦄", "Cryo Lab" to "🧪",
        "Ice Fortress" to "🏰", "Blizzard King" to "👑", "Frozen Throne" to "🪑",
        "Snow Giant" to "🗿", "Ice Dragon" to "🐲", "Crystal Spire" to "🏙️",
        "Avalanche" to "🌨️", "Crevasse" to "🕳️", "Yeti Lair" to "🦣",
        "Snowflake Core" to "❄️", "Permafrost" to "🌑", "Ice Armor" to "🛡️",
        "Frozen Sword" to "⚔️", "Arctic Crown" to "👑", "Cold Breath" to "💨",
        "Iceberg Fleet" to "🚢", "Frost Wyrm" to "🐲", "Rime Veil" to "🌫️",
        "Snow Summit" to "🏔️", "Crystal Keep" to "🏰", "Polar Gate" to "🚪",
        "Blizzard Core" to "❄️", "Ice Heart" to "💙", "Frozen River" to "🌊",
        "Arctic Light" to "🌟", "Snow Crown" to "👑", "Glacier Peak" to "🏔️",
        "Arctic Summit" to "🏔️", "Ice Bear King" to "🐻‍❄️"
    )

    private val zone6 = listOf(  // NEON_CITY — levels 251-300
        "Neon Street" to "🌃", "Holo Sign" to "📺", "Laser Gate" to "⚡",
        "Cyber Market" to "🏪", "Bot Factory" to "🤖", "Data Towers" to "🗼",
        "Glitch Zone" to "💻", "Pixel Park" to "🎮", "Neon Highway" to "🛣️",
        "Circuit Bridge" to "🌉", "Code Temple" to "🏛️", "Server Farm" to "💾",
        "Holo Arena" to "🏟️", "Net Plaza" to "🌐", "Virtual Sky" to "☁️",
        "Drone Swarm" to "🚁", "Power Grid" to "⚡", "Night Market" to "🌙",
        "Rooftop Rave" to "🎵", "Skyline View" to "🌆", "Mech District" to "🤖",
        "Plasma Road" to "🔥", "Hologram Wall" to "🖼️", "Signal Tower" to "📡",
        "AR Garden" to "🌸", "Neural Bridge" to "🧠", "Quantum Gate" to "🚪",
        "Chip Forest" to "🌲", "Memory Lane" to "💭", "Sync Hub" to "🔄",
        "Cyber Dojo" to "⚔️", "Nano Swarm" to "🐝", "Firewall" to "🔥",
        "Upload Spire" to "🗼", "Download Rain" to "🌧️", "Reboot Room" to "🔄",
        "Zero Day" to "🕳️", "Exploit Edge" to "⚡", "Root Access" to "🔑",
        "Admin Tower" to "🏰", "Kernel Core" to "💻", "Stack Overflow" to "🌊",
        "Infinite Loop" to "🔄", "Byte Bazaar" to "🏪", "Pixel Summit" to "🏔️",
        "Neon Crown" to "👑", "Cyber Heart" to "💜", "Code Dragon" to "🐉",
        "Matrix Edge" to "🌐", "System Boss" to "🤖"
    )

    private val zone7 = listOf(  // CRYSTAL — levels 301-350
        "Quartz Cave" to "💎", "Amethyst Path" to "💜", "Sapphire Lake" to "💙",
        "Ruby Ridge" to "❤️", "Emerald Glade" to "💚", "Diamond Spire" to "💎",
        "Opal Hollow" to "🌈", "Topaz Trail" to "💛", "Onyx Gate" to "⬛",
        "Garnet Keep" to "🔴", "Crystal Forest" to "✨", "Prism Hall" to "🌀",
        "Light Refract" to "🌟", "Rainbow Arch" to "🌈", "Spectrum Peak" to "🏔️",
        "Facet Falls" to "💧", "Geo Node" to "🪨", "Mineral Maze" to "🌀",
        "Gem Market" to "🏪", "Jewel Throne" to "👑", "Crystal Snake" to "🐍",
        "Prism Dragon" to "🐲", "Facet Guard" to "🛡️", "Shard Storm" to "⚡",
        "Crystal Rain" to "🌧️", "Gem Forge" to "⚒️", "Quartz Core" to "💎",
        "Selenite Pillar" to "🏛️", "Calcite Cave" to "🕳️", "Azurite Sea" to "🌊",
        "Malachite Marsh" to "🌿", "Pyrite Field" to "⭐", "Obsidian Keep" to "🏰",
        "Turquoise Bay" to "🌊", "Jade Temple" to "🏛️", "Pearl Gate" to "🚪",
        "Crystal Crown" to "👑", "Gem Heart" to "💎", "Prism Summit" to "🏔️",
        "Crystal Core" to "✨", "Facet King" to "👑", "Jewel Dragon" to "🐲",
        "Shard Boss" to "💎", "Gem Dragon" to "🐉", "Crystal Boss" to "💎",
        "Rainbow Core" to "🌈", "Light Dragon" to "🐲", "Spectrum Boss" to "🌟",
        "Crystal Summit" to "🏔️", "Crystal Dragon" to "🐉"
    )

    private val zone8 = listOf(  // CLOUD — levels 351-400
        "Nimbus Gate" to "☁️", "Cirrus Path" to "🌤️", "Cumulus Keep" to "⛅",
        "Rain Cloud" to "🌧️", "Thunder Head" to "⛈️", "Lightning Rod" to "⚡",
        "Sky Garden" to "🌸", "Wind Rider" to "🪶", "Storm Eye" to "🌪️",
        "Cloud Castle" to "🏰", "Air Throne" to "🪑", "Sky Market" to "🏪",
        "Wind Temple" to "🏛️", "Gale Bridge" to "🌉", "Typhoon Edge" to "🌀",
        "Zephyr Trail" to "💨", "Monsoon Bay" to "🌊", "Fog Forest" to "🌫️",
        "Mist Lake" to "💧", "Cloud Dragon" to "🐲", "Updraft Tower" to "🗼",
        "Jet Stream" to "✈️", "Cloud Forge" to "⚒️", "Sky Armor" to "🛡️",
        "Storm Shield" to "🛡️", "Thunder Blade" to "⚔️", "Wind Crown" to "👑",
        "Sky Heart" to "💙", "Cloud Core" to "☁️", "Storm Summit" to "🏔️",
        "Sky Gate" to "🚪", "Cloud Crown" to "👑", "Wind Dragon" to "🐲",
        "Storm Heart" to "⚡", "Sky Summit" to "🏔️", "Nimbus King" to "👑",
        "Thunder Core" to "⚡", "Gale Boss" to "🌪️", "Storm Dragon" to "🐲",
        "Sky Dragon" to "🐉", "Cloud Gate" to "☁️", "Air Dragon" to "🐲",
        "Heaven's Edge" to "✨", "Celestial Path" to "🌟", "Divine Arch" to "🏛️",
        "Angelic Keep" to "👼", "Cloud Summit" to "🏔️", "Sky Crown" to "👑",
        "Heaven Gate" to "🚪", "Storm Eagle" to "🦅"
    )

    private val zone9 = listOf(  // DESERT — levels 401-450
        "Dune Shore" to "🏜️", "Camel Trail" to "🐪", "Scorpion Pass" to "🦂",
        "Sand Castle" to "🏺", "Cactus Grove" to "🌵", "Sun Blazer" to "☀️",
        "Hawk Ridge" to "🦅", "Conch Path" to "🐚", "Stone Giant" to "🗿",
        "Amber Keep" to "🟡", "Mirage Lake" to "💧", "Oasis Garden" to "🌴",
        "Pyramid Base" to "🔺", "Sphinx Gate" to "🦁", "Pharaoh Hall" to "👑",
        "Sarcophagus" to "⬛", "Mummy Walk" to "🧟", "Jackal Path" to "🐺",
        "Anubis Gate" to "⚖️", "Ra Shrine" to "☀️", "Desert Storm" to "🌪️",
        "Sand Serpent" to "🐍", "Dune Dragon" to "🐲", "Heat Core" to "🔥",
        "Sun Forge" to "⚒️", "Desert Armor" to "🛡️", "Sand Shield" to "🛡️",
        "Mirage Blade" to "⚔️", "Desert Crown" to "👑", "Sand Heart" to "💛",
        "Dune Core" to "🏜️", "Desert Summit" to "🏔️", "Sun Gate" to "🚪",
        "Sand Crown" to "👑", "Desert Dragon" to "🐲", "Heat Boss" to "🔥",
        "Sun Dragon" to "🐲", "Desert Boss" to "🏜️", "Pharaoh Boss" to "👑",
        "Desert Gate" to "🚪", "Oasis Dragon" to "🐲", "Sand Dragon" to "🐉",
        "Sun Summit" to "🏔️", "Desert King" to "👑", "Sands of Time" to "⏳",
        "Lost City" to "🏛️", "Desert Realm" to "🌟", "Golden Dunes" to "✨",
        "Desert Summit" to "🏔️", "Sand Serpent" to "🐍"
    )

    private val zone10 = listOf( // COSMOS — levels 451-500
        "Void Shore" to "🌌", "Star Stream" to "🌠", "Comet Chase" to "☄️",
        "Planet Forge" to "🪐", "Star Birth" to "⭐", "Constellation" to "🌟",
        "Telescope Peak" to "🔭", "Moon Haven" to "🌙", "Meteor Shower" to "💥",
        "Galactic Gate" to "🚪", "Supernova Path" to "💫", "Cosmos Keep" to "🏰",
        "Dark Energy" to "🌑", "Space-Time" to "🌀", "Singularity" to "🕳️",
        "Big Bang Echo" to "💥", "Multiverse" to "🌌", "Dimension Rift" to "🌀",
        "Quantum Realm" to "⚛️", "Cosmic Dragon" to "🐲", "Star Throne" to "👑",
        "Nova Crown" to "👑", "Galaxy Heart" to "💜", "Universe Core" to "🌌",
        "Cosmos Summit" to "🏔️", "Stellar Forge" to "⚒️", "Space Armor" to "🛡️",
        "Cosmic Shield" to "🛡️", "Star Blade" to "⚔️", "Galaxy Crown" to "👑",
        "Cosmos Heart" to "💜", "Universe Gate" to "🚪", "Cosmic Crown" to "👑",
        "Infinity Edge" to "♾️", "Eternal Path" to "🌟", "Final Frontier" to "🚀",
        "Omega Gate" to "🚪", "Alpha Summit" to "🏔️", "Cosmic Core" to "🌌",
        "Universe Boss" to "🌌", "Star Dragon" to "🐲", "Galaxy Dragon" to "🐉",
        "Cosmos Dragon" to "🐲", "Final Boss" to "👑", "Universe Dragon" to "🐉",
        "Omega Dragon" to "🐲", "The Last Star" to "⭐", "Eternal Flame" to "🔥",
        "Cosmic Summit" to "🏔️", "Cosmic Ruler" to "👑"
    )

    private val allZones = listOf(
        Triple(zone1, WorldTheme.JUNGLE, "Enchanted Jungle"),
        Triple(zone2, WorldTheme.OCEAN, "Deep Ocean"),
        Triple(zone3, WorldTheme.SPACE, "Outer Space"),
        Triple(zone4, WorldTheme.VOLCANO, "Volcano Island"),
        Triple(zone5, WorldTheme.ARCTIC, "Arctic Kingdom"),
        Triple(zone6, WorldTheme.NEON_CITY, "Neon City"),
        Triple(zone7, WorldTheme.CRYSTAL, "Crystal Caves"),
        Triple(zone8, WorldTheme.CLOUD, "Sky Citadel"),
        Triple(zone9, WorldTheme.DESERT, "Ancient Desert"),
        Triple(zone10, WorldTheme.COSMOS, "The Cosmos")
    )

    val ALL_LEVELS: List<LevelDefinition> by lazy {
        val list = mutableListOf<LevelDefinition>()
        allZones.forEachIndexed { zoneIndex, (nodes, theme, _) ->
            nodes.forEachIndexed { nodeIndex, (title, emoji) ->
                val level = zoneIndex * 50 + nodeIndex + 1
                val isBoss = (nodeIndex + 1) % 10 == 0
                list.add(
                    LevelDefinition(
                        level       = level,
                        title       = title,
                        subtitle    = subtitleFor(theme, nodeIndex, isBoss),
                        emoji       = emoji,
                        requiredXp  = xpForLevel(level),
                        rewardXp    = if (isBoss) 200 else 50 + (zoneIndex * 10),
                        theme       = theme,
                        isBoss      = isBoss
                    )
                )
            }
        }
        list
    }

    private fun subtitleFor(theme: WorldTheme, index: Int, isBoss: Boolean): String {
        if (isBoss) return when (theme) {
            WorldTheme.JUNGLE   -> "Defeat the guardian of the jungle!"
            WorldTheme.OCEAN    -> "Face the terror of the deep!"
            WorldTheme.SPACE    -> "Battle the cosmic overlord!"
            WorldTheme.VOLCANO  -> "Survive the volcano dragon!"
            WorldTheme.ARCTIC   -> "Conquer the ice giant!"
            WorldTheme.NEON_CITY-> "Hack the system boss!"
            WorldTheme.CRYSTAL  -> "Shatter the crystal dragon!"
            WorldTheme.CLOUD    -> "Soar past the storm eagle!"
            WorldTheme.DESERT   -> "Outlast the desert king!"
            WorldTheme.COSMOS   -> "Become the cosmic ruler!"
        }
        val subtitles = when (theme) {
            WorldTheme.JUNGLE   -> listOf("Explore deeper into the jungle", "The trees grow thicker here", "Strange sounds echo all around", "Ancient secrets await discovery", "The path winds through dense foliage", "Rare creatures cross your trail", "The heat rises as you push on", "Local tribes have passed this way", "A sacred space — tread carefully", "You feel the jungle watching you")
            WorldTheme.OCEAN    -> listOf("The water is clear and inviting", "Colorful fish dart past you", "The current pulls you forward", "Bioluminescent creatures glow", "The pressure builds as you descend", "Strange shapes move in the dark", "Ancient ruins lie half-buried", "The cold of the deep hits you", "Only the bravest dive this far", "The abyss has no end in sight")
            WorldTheme.SPACE    -> listOf("Gravity loosens its grip", "Stars stretch into infinity", "Your ship hums with energy", "Unknown signals reach your sensors", "The silence of space is vast", "A new planet comes into view", "Your oxygen holds — press on", "Cosmic dust swirls around you", "You are truly alone out here", "The universe reveals its scale")
            WorldTheme.VOLCANO  -> listOf("The ground trembles beneath you", "Heat waves blur your vision", "Lava oozes between the rocks", "The air smells of sulfur", "Your boots crunch on ash", "A deep rumble shakes the earth", "The glow of magma lights your way", "Rivers of fire flow below", "The summit is almost within reach", "The volcano stirs with fury")
            WorldTheme.ARCTIC   -> listOf("The cold bites through your gear", "Footprints in fresh snow lead ahead", "The aurora dances overhead", "Ice cracks beneath your feet", "A blizzard stirs on the horizon", "Silence blankets the tundra", "Your breath turns to mist", "The ice fortress looms ahead", "Only the strongest survive here", "The storm reaches a fierce peak")
            WorldTheme.NEON_CITY-> listOf("Hologram ads flash around you", "The streets hum with electricity", "Data streams flow like rivers", "Robots go about their routines", "You jack into the city's network", "A glitch distorts reality briefly", "The skyline pulses with light", "Underground data markets thrum", "You trace a signal to its source", "The system is watching your moves")
            WorldTheme.CRYSTAL  -> listOf("The crystals hum a gentle tone", "Light refracts into rainbows", "The air tastes sweet and pure", "Rare gems line the cave walls", "The deeper you go the brighter it gets", "Crystals vibrate at your touch", "A prism chamber opens before you", "The resonance grows stronger", "Every surface reflects your form", "The crystal dragon stirs within")
            WorldTheme.CLOUD    -> listOf("The sky opens wide around you", "Wind carries you effortlessly", "Cloud cities float in the distance", "Updrafts lift your spirit", "The storm is far below now", "Silver linings edge every cloud", "A rainbow marks the path forward", "Thunder rolls at the horizon", "You are walking among the clouds", "The sky has no ceiling here")
            WorldTheme.DESERT   -> listOf("The sun beats down without mercy", "Sand shifts under each step", "A mirage shimmers ahead", "Ancient ruins break the dunes", "The heat tests your endurance", "A desert wind sweeps across", "Buried tombs hint at past glory", "Scarabs skitter across the sand", "The desert holds its secrets close", "A great sandstorm is gathering")
            WorldTheme.COSMOS   -> listOf("The cosmos stretches without limit", "Every star is a new world", "Time bends at the edge of space", "Quantum fields ripple around you", "Dimensions overlap here", "The multiverse whispers to you", "Ancient starlight guides your path", "The fabric of reality is thin", "You touch the edge of everything", "The universe holds its breath")
        }
        return subtitles[index % subtitles.size]
    }
}
