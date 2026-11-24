package com.example.data.knowledge

import com.example.data.model.OrganicKnowledgeItem

object OrganicKnowledgeBase {

    val supportedCrops: List<String> = listOf(
        "Tomato",
        "Rice",
        "Maize",
        "Groundnut",
        "Brinjal",
        "Chilli",
        "Cotton",
        "Banana",
        "Mango",
        "Coconut",
        "Sugarcane",
        "Neem"
    )

    val knowledgeItems: List<OrganicKnowledgeItem> = listOf(
        OrganicKnowledgeItem(
            id = "neem_oil",
            title = "Neem Oil (Cold-Pressed Emulsion)",
            category = "Botanical Crop Protection",
            summary = "Broad-spectrum botanical antifeedant and growth regulator derived from Azadirachta indica seeds, effective against soft-bodied sap-sucking pests and early foliar fungal pressure.",
            preparationAndUsage = listOf(
                "Mix 3–5 ml of cold-pressed neem oil with 1–2 ml of mild liquid soap or soapnut extract in 1 liter of clean water.",
                "Agitate thoroughly until a uniform milky emulsion forms before loading into a clean sprayer.",
                "Apply as a fine mist covering both upper and lower leaf surfaces during early morning or late evening."
            ),
            targetCrops = listOf("Tomato", "Brinjal", "Chilli", "Cotton", "Banana", "Mango"),
            targetIssues = listOf("Aphids", "Whiteflies", "Thrips", "Mites", "Powdery mildew suppression"),
            safetyPrecautions = listOf(
                "Do not spray during peak midday sunlight to prevent leaf scorch.",
                "Test on 2–3 leaves 24 hours before full-field application on tender seedlings.",
                "Avoid spraying directly onto open flowers during peak bee foraging hours."
            )
        ),
        OrganicKnowledgeItem(
            id = "neem_preparations",
            title = "Neem Seed Kernel Extract (NSKE 5%)",
            category = "Neem-Based Preparations",
            summary = "Traditional farm-prepared botanical extract offering concentrated azadirachtin activity for caterpillars, beetles, and leaf-eating larvae.",
            preparationAndUsage = listOf(
                "Soak 50 g of finely pounded dried neem seed kernels in 1 liter of water overnight (12 hours) inside a muslin cloth.",
                "Squeeze the cloth firmly and filter the milky extract through fine mesh to prevent sprayer nozzle clogging.",
                "Add a gentle natural wetting agent before foliar spraying at 7–10 day intervals when pests are observed."
            ),
            targetCrops = listOf("Cotton", "Maize", "Groundnut", "Chilli", "Brinjal", "Rice"),
            targetIssues = listOf("Leaf folders", "Pod borers", "Bollworms", "Cutworms", "Hoppers"),
            safetyPrecautions = listOf(
                "Use freshly prepared extract within 24 hours for maximum potency.",
                "Keep away from pond water or fish habitats when rinsing spray equipment.",
                "Wear gloves and wash hands after handling crushed kernels."
            )
        ),
        OrganicKnowledgeItem(
            id = "garlic_chilli",
            title = "Garlic-Chilli Botanical Spray",
            category = "Natural Pest Repellent",
            summary = "Pungent sulfur- and capsaicin-rich repellent spray that deters chewing caterpillars, aphids, and stem borers without leaving persistent synthetic residues.",
            preparationAndUsage = listOf(
                "Crush 50 g of fresh garlic cloves and 25 g of hot green chillies into a fine paste.",
                "Steep in 1 liter of warm water for 12–24 hours, strain through double-layer muslin cloth, and dilute 1:10 with clean water before spraying.",
                "Apply along new shoots and leaf undersides every 7 days during active pest pressure."
            ),
            targetCrops = listOf("Tomato", "Chilli", "Brinjal", "Maize", "Groundnut"),
            targetIssues = listOf("Aphids", "Caterpillars", "Thrips", "Flea beetles"),
            safetyPrecautions = listOf(
                "Wear protective eyewear, mask, and gloves while grinding chillies and spraying.",
                "Always dilute properly; concentrated paste can burn tender foliage.",
                "Wash harvested produce thoroughly with clean water before consumption."
            )
        ),
        OrganicKnowledgeItem(
            id = "panchagavya",
            title = "Panchagavya (Fermented Bio-Enhancer)",
            category = "Organic Plant Nutrition",
            summary = "Traditional fermented formulation supporting beneficial phyllosphere microbes, root vigor, and crop resilience under biotic and abiotic stress.",
            preparationAndUsage = listOf(
                "Dilute 30 ml of mature, well-filtered Panchagavya per liter of water (3% solution) for foliar spraying.",
                "For root drenching, apply through irrigation channels at recommended agronomic intervals during vegetative and flowering stages.",
                "Stir mature stock gently twice daily and filter carefully before spraying."
            ),
            targetCrops = listOf("Banana", "Mango", "Coconut", "Rice", "Sugarcane", "Tomato"),
            targetIssues = listOf("Weak vegetative vigor", "Post-stress recovery", "Microbial soil support"),
            safetyPrecautions = listOf(
                "Ensure complete fermentation (21–30 days) before field application.",
                "Never exceed 3% concentration on young vegetable foliage.",
                "Store in shaded, breathable earthen or food-grade containers away from direct heat."
            )
        ),
        OrganicKnowledgeItem(
            id = "jeevamrutham",
            title = "Jeevamrutham (Microbial Soil Inoculant)",
            category = "Soil Biology & Fertility",
            summary = "Liquid microbial culture fermented from cow dung, cow urine, jaggery, pulse flour, and native undisturbed soil to stimulate earthworm activity and nutrient cycling.",
            preparationAndUsage = listOf(
                "Ferment fresh cow dung, cow urine, jaggery, gram flour, and a handful of native topsoil in shaded water for 48–72 hours while stirring twice daily.",
                "Apply as a soil drench near the root zone or mix into irrigation water once every 15 days.",
                "Use within 7 days of preparation while microbial activity is at peak."
            ),
            targetCrops = listOf("Sugarcane", "Banana", "Rice", "Maize", "Coconut", "Mango", "Groundnut"),
            targetIssues = listOf("Low soil microbial activity", "Compacted soil", "Slow organic matter breakdown"),
            safetyPrecautions = listOf(
                "Apply to moist soil—never pour concentrated culture onto bone-dry soil under hot sun.",
                "Filter thoroughly if routing through micro-irrigation to prevent emitter clogging.",
                "Keep fermentation drums covered with breathable cloth to prevent mosquito breeding."
            )
        ),
        OrganicKnowledgeItem(
            id = "compost",
            title = "Well-Decomposed Farm Compost",
            category = "Soil Organic Matter",
            summary = "Stabilized aerobic humus produced from crop residues and livestock manure that improves soil aggregation, infiltration, and root aeration.",
            preparationAndUsage = listOf(
                "Layer carbon-rich dry crop residues with nitrogen-rich green biomass and manure, keeping the heap moist and turning every 2–3 weeks.",
                "Confirm maturity when the compost smells earthy, feels crumbly, and has cooled to ambient temperature.",
                "Incorporate evenly into topsoil 2–3 weeks before sowing or apply as side-dressing around perennial basins."
            ),
            targetCrops = listOf("Tomato", "Brinjal", "Chilli", "Maize", "Banana", "Mango", "Sugarcane"),
            targetIssues = listOf("Crusting soils", "Poor drainage or low water retention", "Low organic carbon"),
            safetyPrecautions = listOf(
                "Avoid applying fresh, hot, immature manure directly near roots as it can cause ammonia burn and invite soil pests.",
                "Do not compost diseased plant material showing severe viral or soil-borne wilt symptoms."
            )
        ),
        OrganicKnowledgeItem(
            id = "vermicompost",
            title = "Vermicompost (Earthworm Castings)",
            category = "Soil Conditioning & Nurseries",
            summary = "Nutrient-rich, fine-textured bio-humus processed by epigeic earthworms (Eisenia fetida), ideal for nursery beds, vegetable rows, and perennial root zones.",
            preparationAndUsage = listOf(
                "Mix into nursery potting media or apply along planting furrows prior to transplanting.",
                "Side-dress around established tomato, chilli, brinjal, or banana plants and cover lightly with mulch.",
                "Keep bed moisture balanced so beneficial microbes remain active after application."
            ),
            targetCrops = listOf("Tomato", "Chilli", "Brinjal", "Banana", "Groundnut", "Mango"),
            targetIssues = listOf("Poor seedling establishment", "Nutrient-depleted topsoil", "Root zone stress"),
            safetyPrecautions = listOf(
                "Do not expose stored vermicompost bags to direct sun or drying winds.",
                "Avoid mixing directly with harsh synthetic fungicides or strong alkaline materials."
            )
        ),
        OrganicKnowledgeItem(
            id = "neem_cake",
            title = "Neem Cake (Soil Amendment)",
            category = "Root-Zone Protection",
            summary = "De-oiled neem seed press cake that enriches organic matter, slows nitrogen loss, and suppresses root-knot nematodes and soil-dwelling grubs.",
            preparationAndUsage = listOf(
                "Incorporate powdered neem cake into the soil during final land preparation or pit filling before planting.",
                "Blend with compost or vermicompost around banana, coconut, mango, or vegetable root basins.",
                "Water lightly after soil incorporation to activate gentle release."
            ),
            targetCrops = listOf("Tomato", "Brinjal", "Banana", "Coconut", "Mango", "Groundnut", "Sugarcane"),
            targetIssues = listOf("Root-knot nematodes", "White grubs", "Termites", "Rapid fertilizer leaching"),
            safetyPrecautions = listOf(
                "Avoid placing excessive quantities directly touching delicate germinating seeds.",
                "Store in a dry, ventilated area to prevent fungal molding during storage."
            )
        ),
        OrganicKnowledgeItem(
            id = "natural_pest_management",
            title = "Natural & Integrated Pest Management (IPM)",
            category = "Ecological Pest Control",
            summary = "Preventive ecosystem strategy combining physical traps, beneficial predators, trap crops, and targeted botanical sprays before infestations escalate.",
            preparationAndUsage = listOf(
                "Install yellow sticky cards for whiteflies/aphids and blue sticky traps for thrips in vegetable plots.",
                "Conserve natural enemies such as ladybird beetles, lacewings, spiders, and parasitoid wasps by planting flowering border crops (marigold, cowpea).",
                "Remove and safely dispose of severely infested leaves or bored fruits early to break pest life cycles."
            ),
            targetCrops = listOf("Cotton", "Tomato", "Chilli", "Brinjal", "Maize", "Rice"),
            targetIssues = listOf("Sap-sucking outbreaks", "Fruit & shoot borers", "Pest resistance"),
            safetyPrecautions = listOf(
                "Inspect fields weekly before choosing any spray intervention.",
                "Never mix incompatible homemade or chemical formulations in the same tank."
            )
        ),
        OrganicKnowledgeItem(
            id = "sustainable_crop_protection",
            title = "Sustainable Crop & Disease Protection",
            category = "Preventive Agronomy",
            summary = "Cultural and biological practices that minimize fungal and bacterial outbreaks through sanitation, spacing, crop rotation, and Trichoderma/Pseudomonas bio-agents.",
            preparationAndUsage = listOf(
                "Rotate unrelated crop families (e.g., rotate Solanaceae like tomato/brinjal/chilli with legumes like groundnut or cereals like maize).",
                "Maintain proper plant spacing and morning root-zone watering so foliage dries quickly and soil splash is minimized.",
                "Apply organic mulch around stems to block soil-borne fungal spores from splashing onto lower leaves."
            ),
            targetCrops = listOf("Tomato", "Rice", "Maize", "Groundnut", "Brinjal", "Chilli", "Banana"),
            targetIssues = listOf("Early blight", "Bacterial leaf spot", "Wilt", "Damping-off"),
            safetyPrecautions = listOf(
                "Sanitize pruning shears between diseased and healthy plants.",
                "Never compost infected plant debris back onto the same crop field."
            )
        )
    )

    fun searchKnowledge(query: String): List<OrganicKnowledgeItem> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return knowledgeItems
        return knowledgeItems.filter { item ->
            item.title.lowercase().contains(q) ||
                item.category.lowercase().contains(q) ||
                item.summary.lowercase().contains(q) ||
                item.targetCrops.any { it.lowercase().contains(q) } ||
                item.targetIssues.any { it.lowercase().contains(q) }
        }
    }
}
