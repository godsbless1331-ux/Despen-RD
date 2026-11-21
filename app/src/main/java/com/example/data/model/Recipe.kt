package com.example.data.model

enum class RecipeMealType(val id: String, val displayName: String, val emoji: String) {
    DESAYUNO("desayuno", "Desayuno", "🌅"),
    ALMUERZO("almuerzo", "Almuerzo", "☀️"),
    CENA("cena", "Cena", "🌙")
}

data class RecipeIngredientSpec(
    val colmadoKey: String,
    val baseQuantityFor4: Double // Quantity needed for 4 people
)

data class RecipeCostLine(
    val name: String,
    val quantityDisplay: String,
    val cost: Double,
    val haveInPantry: Boolean
)

data class RecipeCostResult(
    val totalCost: Double,
    val missingCost: Double,
    val lines: List<RecipeCostLine>
)

data class EvaluatedRecipe(
    val recipe: Recipe,
    val matches: List<Boolean>,
    val matchCount: Int,
    val proportion: Double,
    val costResult: RecipeCostResult
)

data class Recipe(
    val id: Int,
    val name: String,
    val emoji: String,
    val time: String,
    val mealTypes: Set<RecipeMealType> = setOf(RecipeMealType.ALMUERZO),
    val ingredientPatterns: List<String>, // e.g. "arroz", "habichuela|frijol"
    val ingredientsSpecs: List<RecipeIngredientSpec>,
    val includesCondiments: Boolean,
    val instructions: List<String>
) {
    fun hasIngredient(pattern: String, pantryItems: List<PantryItem>): Boolean {
        val alternatives = pattern.split("|").map { it.trim().lowercase() }
        return pantryItems.any { item ->
            if (item.cat != Category.ALIMENTO || item.qty <= 0) return@any false
            val n = item.name.lowercase().trim()
            alternatives.any { alt -> n.contains(alt) }
        }
    }

    fun calculateCost(servings: Int, customPrices: Map<String, Double>, pantryItems: List<PantryItem>): RecipeCostResult {
        val factor = servings / 4.0
        var total = 0.0
        var missing = 0.0
        val lines = mutableListOf<RecipeCostLine>()

        ingredientsSpecs.forEachIndexed { index, spec ->
            val colmadoItem = ColmadoDefaults.ITEMS.find { it.key == spec.colmadoKey }
            val unitPrice = customPrices[spec.colmadoKey] ?: colmadoItem?.defaultPrice ?: 50.0
            val neededQty = spec.baseQuantityFor4 * factor
            val lineCost = unitPrice * neededQty
            val pattern = ingredientPatterns.getOrNull(index) ?: spec.colmadoKey
            val have = hasIngredient(pattern, pantryItems)

            total += lineCost
            if (!have) missing += lineCost

            lines.add(
                RecipeCostLine(
                    name = colmadoItem?.displayName ?: spec.colmadoKey.replaceFirstChar { it.uppercase() },
                    quantityDisplay = ColmadoDefaults.formatQuantity(neededQty, colmadoItem?.unit ?: "ud"),
                    cost = lineCost,
                    haveInPantry = have
                )
            )
        }

        if (includesCondiments) {
            val condItem = ColmadoDefaults.ITEMS.find { it.key == "cond" }
            val condPrice = customPrices["cond"] ?: condItem?.defaultPrice ?: 25.0
            val condQty = Math.max(1.0, Math.round(factor).toDouble())
            val condCost = condPrice * condQty
            total += condCost
            missing += condCost // Treat seasoning as a small purchase if not tracked
            lines.add(
                RecipeCostLine(
                    name = condItem?.displayName ?: "Sal, orégano y sazón",
                    quantityDisplay = "1 paquetico",
                    cost = condCost,
                    haveInPantry = false
                )
            )
        }

        return RecipeCostResult(totalCost = total, missingCost = missing, lines = lines)
    }

    companion object {
        val ALL: List<Recipe> = listOf(
            Recipe(
                id = 0,
                name = "Arroz con habichuelas",
                emoji = "🍛",
                time = "50 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("arroz", "habichuela|frijol", "ajo", "cebolla", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("arroz", 1.0),
                    RecipeIngredientSpec("habichuela", 0.5),
                    RecipeIngredientSpec("ajo", 1.0),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("aceite", 0.1)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Si tus habichuelas son secas, remójalas la noche anterior y hiérvelas hasta que ablanden.",
                    "Sofríe cebolla y ajo picados en aceite 2 minutos, agrega las habichuelas con agua y sal; deja hervir 15 minutos.",
                    "En otra olla sofríe el arroz con un chorrito de aceite y sal.",
                    "Agrega agua (que cubra dos dedos), tapa y cocina a fuego bajo hasta que seque y quede suelto.",
                    "Sirve el arroz con las habichuelas encima."
                )
            ),
            Recipe(
                id = 1,
                name = "Mangú",
                emoji = "🍌",
                time = "30 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                ingredientPatterns = listOf("plátano|platano", "cebolla", "aceite|mantequilla", "huevo|queso"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("platano", 3.0),
                    RecipeIngredientSpec("cebolla", 0.4),
                    RecipeIngredientSpec("aceite", 0.05),
                    RecipeIngredientSpec("huevo", 4.0)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela los plátanos verdes y hiérvelos en agua con sal hasta que estén blandos (20 minutos).",
                    "Escurre guardando un poco del agua caliente y májalos con aceite o mantequilla y unas gotas de agua fría.",
                    "Sofríe cebolla en rodajas con un toque de vinagre y sal durante 2 minutos.",
                    "Sirve el mangú caliente con la cebolla encima, acompañado de huevo o queso frito."
                )
            ),
            Recipe(
                id = 2,
                name = "Moro de guandules",
                emoji = "🥘",
                time = "50 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("arroz", "guandul", "cebolla", "ajo", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("arroz", 1.0),
                    RecipeIngredientSpec("guandul", 0.5),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("ajo", 1.0),
                    RecipeIngredientSpec("aceite", 0.1)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Sofríe ajo y cebolla picados en aceite.",
                    "Agrega los guandules cocidos con su caldo y sazona con sal.",
                    "Añade el arroz lavado y agua suficiente (que cubra dos dedos).",
                    "Cuando hierva, tapa y baja el fuego; cocina hasta que el arroz esté suelto."
                )
            ),
            Recipe(
                id = 3,
                name = "Pollo guisado",
                emoji = "🍗",
                time = "45 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("pollo|pechuga|muslo", "cebolla", "ajo", "tomate", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("pollo", 2.0),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("ajo", 1.0),
                    RecipeIngredientSpec("tomate", 0.4),
                    RecipeIngredientSpec("aceite", 0.1)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Sazona el pollo con ajo, sal y un poco de limón y déjalo reposar 15 minutos.",
                    "Dora las presas en aceite caliente por ambos lados y resérvalas.",
                    "Sofríe cebolla, ajo y tomate; devuelve el pollo y agrega media taza de agua.",
                    "Tapa y cocina a fuego medio 25 minutos hasta que esté tierno y la salsa espese."
                )
            ),
            Recipe(
                id = 4,
                name = "Sancocho sencillo",
                emoji = "🍲",
                time = "1 h 30",
                mealTypes = setOf(RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("pollo|carne", "yuca", "plátano|platano", "auyama|ñame", "cilantro"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("pollo", 1.5),
                    RecipeIngredientSpec("yuca", 1.5),
                    RecipeIngredientSpec("platano", 2.0),
                    RecipeIngredientSpec("auyama", 1.0),
                    RecipeIngredientSpec("cilantro", 1.0)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Sazona la carne con ajo y sal; dórala en la olla con un poco de aceite.",
                    "Agrega agua y cocina 30 minutos hasta que ablande.",
                    "Añade yuca, plátano y auyama en trozos y cocina 25 minutos más.",
                    "Agrega cilantro picado, ajusta de sal y sirve caliente con arroz."
                )
            ),
            Recipe(
                id = 5,
                name = "Pastelón de plátano maduro",
                emoji = "🥧",
                time = "1 h 15",
                mealTypes = setOf(RecipeMealType.ALMUERZO, RecipeMealType.CENA),
                ingredientPatterns = listOf("plátano|platano", "carne molida|molida", "queso", "huevo", "cebolla"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("platano", 4.0),
                    RecipeIngredientSpec("molida", 1.0),
                    RecipeIngredientSpec("queso", 0.25),
                    RecipeIngredientSpec("huevo", 3.0),
                    RecipeIngredientSpec("cebolla", 0.3)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Corta los plátanos maduros en láminas y fríelos hasta dorar.",
                    "Sofríe la carne molida con cebolla, ajo y sazón hasta que cocine.",
                    "En un molde arma una capa de plátano, otra de carne y queso; repite.",
                    "Cubre con huevo batido y hornea a 180 °C unos 30 minutos hasta dorar."
                )
            ),
            Recipe(
                id = 6,
                name = "Huevos revueltos con vegetales",
                emoji = "🍳",
                time = "10 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                ingredientPatterns = listOf("huevo", "cebolla", "tomate", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("huevo", 6.0),
                    RecipeIngredientSpec("cebolla", 0.2),
                    RecipeIngredientSpec("tomate", 0.25),
                    RecipeIngredientSpec("aceite", 0.05)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pica la cebolla y el tomate en cubitos pequeños.",
                    "Sofríelos en un poco de aceite 3 minutos hasta que suavicen.",
                    "Bate los huevos con una pizca de sal y viértelos en la sartén.",
                    "Revuelve suavemente a fuego bajo hasta que cuajen jugosos y sirve de inmediato con pan o víveres."
                )
            ),
            Recipe(
                id = 7,
                name = "Espagueti con salsa de tomate",
                emoji = "🍝",
                time = "25 min",
                mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("espagueti|spaghetti|pasta", "tomate", "ajo", "queso", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("pasta", 1.0),
                    RecipeIngredientSpec("tomate", 0.75),
                    RecipeIngredientSpec("ajo", 1.0),
                    RecipeIngredientSpec("queso", 0.15),
                    RecipeIngredientSpec("aceite", 0.05)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Hierve el espagueti en agua con sal según el tiempo del paquete y escúrrelo.",
                    "Sofríe ajo en aceite, agrega tomate picado o salsa y cocina 10 minutos.",
                    "Mezcla la pasta con la salsa.",
                    "Sirve caliente con queso rallado por encima."
                )
            ),
            Recipe(
                id = 8,
                name = "Avena caliente",
                emoji = "🥣",
                time = "10 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO),
                ingredientPatterns = listOf("avena", "leche", "canela", "azúcar|azucar"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("avena", 0.5),
                    RecipeIngredientSpec("leche", 1.0),
                    RecipeIngredientSpec("canela", 1.0),
                    RecipeIngredientSpec("azucar", 0.15)
                ),
                includesCondiments = false,
                instructions = listOf(
                    "Calienta la leche en una ollita con una astilla o pizca de canela.",
                    "Agrega la avena y revuelve constantemente a fuego bajo por 5 a 7 minutos hasta espesar.",
                    "Endulza al gusto con azúcar o miel y deja reposar 2 minutos.",
                    "Sirve tibia para un desayuno nutritivo y rápido."
                )
            ),
            Recipe(
                id = 9,
                name = "Tostones",
                emoji = "🟡",
                time = "25 min",
                mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("plátano|platano", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("platano", 3.0),
                    RecipeIngredientSpec("aceite", 0.3)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela los plátanos verdes y córtalos en rodajas gruesas diagonales.",
                    "Fríelos 3 minutos por lado a fuego medio sin que se doren mucho.",
                    "Aplástalos con un plato o tostonera y pásalos 1 segundo por agua con sal y ajo.",
                    "Vuelve a freírlos en aceite bien caliente hasta dorar crujientes y sírvelos calientes."
                )
            ),
            Recipe(
                id = 10,
                name = "Sopa de pollo con fideos",
                emoji = "🍜",
                time = "50 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO, RecipeMealType.CENA),
                ingredientPatterns = listOf("pollo|pechuga", "fideo|pasta", "zanahoria|papa", "cebolla", "cilantro"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("pollo", 1.5),
                    RecipeIngredientSpec("fideo", 1.0),
                    RecipeIngredientSpec("papa", 0.5),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("cilantro", 1.0)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Hierve el pollo con cebolla y sal hasta que esté tierno (30 minutos).",
                    "Saca el pollo, desmenúzalo y devuélvelo al caldo.",
                    "Agrega zanahoria o papa en cubitos y cocina 10 minutos.",
                    "Añade los fideos y cocina 5 minutos más; termina con cilantro fresco."
                )
            ),
            Recipe(
                id = 11,
                name = "Tortilla de papa",
                emoji = "🥔",
                time = "30 min",
                mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.DESAYUNO),
                ingredientPatterns = listOf("papa", "huevo", "cebolla", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("papa", 1.5),
                    RecipeIngredientSpec("huevo", 5.0),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("aceite", 0.15)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela y corta las papas en láminas finas; fríelas a fuego medio-bajo con la cebolla hasta que ablanden.",
                    "Bate los huevos con sal y mézclalos con las papas escurridas dejando reposar 3 minutos.",
                    "Cuaja la mezcla en una sartén antiadherente a fuego bajo durante 4 minutos.",
                    "Voltea con ayuda de un plato llano y cocina 3 minutos más hasta dorar."
                )
            ),
            Recipe(
                id = 12,
                name = "Ensalada de atún",
                emoji = "🥗",
                time = "10 min",
                mealTypes = setOf(RecipeMealType.CENA),
                ingredientPatterns = listOf("atún|atun", "tomate", "cebolla", "limón|limon"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("atun", 2.0),
                    RecipeIngredientSpec("tomate", 0.4),
                    RecipeIngredientSpec("cebolla", 0.25),
                    RecipeIngredientSpec("limon", 2.0)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Escurre bien las latas de atún y desmenúzalo en un tazón.",
                    "Agrega tomate y cebolla picados en cubitos pequeños.",
                    "Aliña con jugo de limón fresco, sal, pimienta y un chorrito de aceite.",
                    "Sirve fresca sola, con galletas saladas, casabe o en pan tostado."
                )
            ),
            Recipe(
                id = 13,
                name = "Puré de papa",
                emoji = "🥣",
                time = "30 min",
                mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("papa|batata", "leche|mantequilla"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("papa", 1.5),
                    RecipeIngredientSpec("leche", 0.25)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela las papas, córtalas en trozos y hiérvelas 20 minutos hasta que estén muy blandas.",
                    "Escúrrelas y májalas bien en caliente.",
                    "Agrega leche tibia o mantequilla y sal; mezcla hasta que quede suave y cremoso.",
                    "Sirve caliente como cena ligera o acompañamiento."
                )
            ),
            Recipe(
                id = 14,
                name = "Arroz blanco",
                emoji = "🍚",
                time = "25 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("arroz", "aceite", "ajo"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("arroz", 1.0),
                    RecipeIngredientSpec("aceite", 0.05),
                    RecipeIngredientSpec("ajo", 0.5)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Lava el arroz hasta que el agua salga casi clara.",
                    "Sofríe un diente de ajo en aceite, agrega el arroz y revuelve 1 minuto.",
                    "Añade agua (dos dedos por encima del arroz) y sal.",
                    "Cuando hierva y se seque, tapa y baja el fuego 15 minutos. Suelta con un tenedor."
                )
            ),
            Recipe(
                id = 15,
                name = "Habichuelas guisadas",
                emoji = "🫘",
                time = "45 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("habichuela|frijol", "cebolla", "ajo", "tomate|salsa", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("habichuela", 0.75),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("ajo", 1.0),
                    RecipeIngredientSpec("tomate", 0.3),
                    RecipeIngredientSpec("aceite", 0.05)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Si son secas, remójalas la noche anterior y hiérvelas hasta que ablanden.",
                    "Sofríe cebolla, ajo y tomate en aceite.",
                    "Agrega las habichuelas con su caldo, sal y orégano.",
                    "Cocina a fuego medio 15 minutos hasta que espese; si quieres, aplasta unas pocas."
                )
            ),
            Recipe(
                id = 16,
                name = "Yuca hervida con cebolla",
                emoji = "🥔",
                time = "35 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                ingredientPatterns = listOf("yuca", "cebolla", "aceite", "limón|limon"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("yuca", 2.0),
                    RecipeIngredientSpec("cebolla", 0.4),
                    RecipeIngredientSpec("aceite", 0.05),
                    RecipeIngredientSpec("limon", 1.0)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela la yuca, quita la vena central dura y córtala en trozos medianos.",
                    "Hiérvela en abundante agua con sal de 20 a 25 minutos hasta que esté suave al pincharla.",
                    "Sofríe cebolla en rodajas con aceite, sal y unas gotas de limón o vinagre.",
                    "Sirve la yuca bien caliente con la cebolla encima, ideal con queso o huevo."
                )
            ),
            Recipe(
                id = 17,
                name = "Arroz con pollo",
                emoji = "🍗",
                time = "55 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO),
                ingredientPatterns = listOf("arroz", "pollo|pechuga|muslo", "cebolla", "ajo", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("arroz", 1.0),
                    RecipeIngredientSpec("pollo", 2.0),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("ajo", 1.0),
                    RecipeIngredientSpec("aceite", 0.1)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Sazona el pollo con ajo y sal; dóralo en la olla y resérvalo.",
                    "Sofríe cebolla y ajo, agrega el arroz y revuelve 1 minuto.",
                    "Devuelve el pollo, añade agua caliente (dos dedos) y sal.",
                    "Tapa y cocina a fuego bajo 20 a 25 minutos hasta que el arroz esté suelto."
                )
            ),
            Recipe(
                id = 18,
                name = "Pescado frito",
                emoji = "🐟",
                time = "25 min",
                mealTypes = setOf(RecipeMealType.ALMUERZO, RecipeMealType.CENA),
                ingredientPatterns = listOf("pescado", "limón|limon", "ajo", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("pescado", 2.0),
                    RecipeIngredientSpec("limon", 2.0),
                    RecipeIngredientSpec("ajo", 1.0),
                    RecipeIngredientSpec("aceite", 0.3)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Limpia el pescado y haz cortes superficiales.",
                    "Sazónalo con limón, ajo y sal; déjalo reposar 15 minutos.",
                    "Fríelo en aceite bien caliente 5 a 6 minutos por lado.",
                    "Escúrrelo en papel y sírvelo con tostones o ensalada."
                )
            ),
            Recipe(
                id = 19,
                name = "Sándwich de huevo y queso",
                emoji = "🥪",
                time = "10 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                ingredientPatterns = listOf("pan", "huevo", "queso"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("pan", 4.0),
                    RecipeIngredientSpec("huevo", 4.0),
                    RecipeIngredientSpec("queso", 0.25)
                ),
                includesCondiments = false,
                instructions = listOf(
                    "Fríe o revuelve los huevos en una sartén antiadherente con una pizca de sal.",
                    "Tuesta las rebanadas de pan en sartén o tostadora hasta que queden crujientes.",
                    "Arma el sándwich colocando el huevo caliente y una buena lonja de queso encima.",
                    "Presiona 1 minuto en la sartén tapada para que el queso se derrita bien."
                )
            ),
            Recipe(
                id = 20,
                name = "Batido de guineo con leche",
                emoji = "🥤",
                time = "5 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO),
                ingredientPatterns = listOf("guineo|banana", "leche", "azúcar|azucar|miel"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("guineo", 4.0),
                    RecipeIngredientSpec("leche", 1.0),
                    RecipeIngredientSpec("azucar", 0.1)
                ),
                includesCondiments = false,
                instructions = listOf(
                    "Pela los guineos maduros y pártelos en trozos.",
                    "Colócalos en la licuadora con leche bien fría, hielo al gusto y un toque de azúcar o miel.",
                    "Licúa 45 segundos hasta obtener una textura espumosa y cremosa; sirve de inmediato."
                )
            ),
            // Nuevas recetas específicas de Desayuno y Cena dominicanos
            Recipe(
                id = 21,
                name = "Mangú con Los Tres Golpes",
                emoji = "🇩🇴",
                time = "35 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                ingredientPatterns = listOf("plátano|platano", "huevo", "queso", "cebolla", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("platano", 4.0),
                    RecipeIngredientSpec("huevo", 4.0),
                    RecipeIngredientSpec("queso", 0.5),
                    RecipeIngredientSpec("cebolla", 0.5),
                    RecipeIngredientSpec("aceite", 0.1)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela los plátanos verdes, córtalos en trozos y hiérvelos con sal 20 minutos hasta que estén bien suaves.",
                    "En una sartén fríe el queso de freír cortado en lonjas hasta dorar por ambos lados y reserva.",
                    "En la misma sartén fríe o revuelve los huevos al gusto (y salami si tienes en nevera).",
                    "Sofríe los aros de cebolla roja con una cucharada de vinagre y sal durante 2 minutos.",
                    "Maja los plátanos calientes con mantequilla o aceite y un chorrito de agua fría hasta quedar suaves.",
                    "Sirve el mangú coronado con la cebolla encendida, el queso frito y los huevos al lado."
                )
            ),
            Recipe(
                id = 22,
                name = "Harina de maíz dulce con canela",
                emoji = "🥣",
                time = "15 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO),
                ingredientPatterns = listOf("harina", "leche", "canela", "azúcar|azucar"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("harina", 0.5),
                    RecipeIngredientSpec("leche", 1.0),
                    RecipeIngredientSpec("canela", 1.0),
                    RecipeIngredientSpec("azucar", 0.2)
                ),
                includesCondiments = false,
                instructions = listOf(
                    "Disuelve la harina de maíz en 1 taza de leche fría para evitar que se formen grumos.",
                    "Calienta el resto de la leche con una astilla de canela y una pizca de sal.",
                    "Vierte la mezcla de harina poco a poco removiendo sin parar con cuchara de madera.",
                    "Cocina a fuego bajo de 8 a 10 minutos hasta que espese cremosa, endulza al gusto y sirve tibia."
                )
            ),
            Recipe(
                id = 23,
                name = "Tostadas criollas con huevo, queso y tomate",
                emoji = "🍞",
                time = "12 min",
                mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                ingredientPatterns = listOf("pan", "huevo", "queso", "tomate"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("pan", 4.0),
                    RecipeIngredientSpec("huevo", 4.0),
                    RecipeIngredientSpec("queso", 0.25),
                    RecipeIngredientSpec("tomate", 0.3)
                ),
                includesCondiments = false,
                instructions = listOf(
                    "Corta el pan en rebanadas y tuéstalo en una sartén caliente con un poquito de mantequilla o aceite.",
                    "Prepara los huevos estrellados o revueltos con una pizca de sal.",
                    "Coloca sobre cada tostada una lonja de queso, rodajas frescas de tomate y el huevo caliente encima.",
                    "Sirve enseguida acompañado de café, avena o jugo natural."
                )
            ),
            Recipe(
                id = 24,
                name = "Guineítos verdes hervidos con cebolla y huevo",
                emoji = "🍌",
                time = "25 min",
                mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.DESAYUNO),
                ingredientPatterns = listOf("guineo", "huevo", "cebolla", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("guineo", 6.0),
                    RecipeIngredientSpec("huevo", 4.0),
                    RecipeIngredientSpec("cebolla", 0.3),
                    RecipeIngredientSpec("aceite", 0.05)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Lava los guineos verdes, hazles un corte longitudinal en la cáscara y hiérvelos con sal por 15 minutos.",
                    "Pélalos con cuidado mientras estén calientes (salen muy fácil después de hervir).",
                    "Sofríe cebolla en rodajas finas con aceite, sal y un toque de vinagre o limón.",
                    "Acompaña los guineítos con huevos hervidos o revueltos y báñalos con el escabeche de cebolla."
                )
            ),
            Recipe(
                id = 25,
                name = "Crema criolla de auyama",
                emoji = "🎃",
                time = "30 min",
                mealTypes = setOf(RecipeMealType.CENA),
                ingredientPatterns = listOf("auyama", "cebolla", "ajo", "leche", "queso"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("auyama", 1.5),
                    RecipeIngredientSpec("cebolla", 0.25),
                    RecipeIngredientSpec("ajo", 0.5),
                    RecipeIngredientSpec("leche", 0.35),
                    RecipeIngredientSpec("queso", 0.15)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela y corta la auyama en cubos medianos.",
                    "En una olla sofríe cebolla y ajo picados con un chorrito de aceite por 2 minutos.",
                    "Agrega la auyama, cubre con agua, pon una pizca de sal y hierve 18 minutos hasta que ablande.",
                    "Licúa la auyama con parte de su caldo y un chorrito de leche hasta lograr una crema suave.",
                    "Sirve caliente con cubitos de queso o pan tostado para una cena ligera y reconfortante."
                )
            ),
            Recipe(
                id = 26,
                name = "Batata hervida o asada con queso",
                emoji = "🍠",
                time = "30 min",
                mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.DESAYUNO),
                ingredientPatterns = listOf("batata|papa", "queso", "cebolla", "aceite"),
                ingredientsSpecs = listOf(
                    RecipeIngredientSpec("batata", 2.0),
                    RecipeIngredientSpec("queso", 0.35),
                    RecipeIngredientSpec("cebolla", 0.25),
                    RecipeIngredientSpec("aceite", 0.05)
                ),
                includesCondiments = true,
                instructions = listOf(
                    "Pela las batatas, córtalas en ruedas gruesas y hiérvelas en agua con una pizca de sal por 20 minutos.",
                    "Escúrrelas bien cuando un tenedor entre suave.",
                    "Dora el queso en una sartén y sofríe cebolla en aros con un toque de aceite.",
                    "Sirve la batata caliente acompañada del queso dorado y la cebolla."
                )
            )
        )
    }
}
