package com.example.data.model

data class ChefIdea(
    val id: String,
    val title: String,
    val emoji: String,
    val time: String,
    val difficulty: String, // Fácil, Media, Tradicional
    val mealTypes: Set<RecipeMealType> = setOf(RecipeMealType.DESAYUNO, RecipeMealType.ALMUERZO, RecipeMealType.CENA),
    val matchedIngredients: List<String>,
    val optionalAdditions: List<String>,
    val chefTips: List<String>,
    val detailedInstructions: List<String>
)

object ChefIdeasKnowledgeBase {

    fun generateIdeasFor(
        selectedProductNames: List<String>,
        mealTypeFilter: RecipeMealType? = null
    ): List<ChefIdea> {
        val normalized = selectedProductNames.map { it.lowercase().trim() }
        val results = mutableListOf<ChefIdea>()

        val has = { keyword: String ->
            normalized.any { it.contains(keyword) }
        }

        // 1. Mangú Tradicional con Los Tres Golpes (Desayuno y Cena)
        if (has("platano") || has("plátano")) {
            val matched = mutableListOf<String>()
            if (has("platano") || has("plátano")) matched.add("Plátano")
            if (has("huevo")) matched.add("Huevos")
            if (has("queso")) matched.add("Queso")
            if (has("cebolla")) matched.add("Cebolla")
            if (has("aceite") || has("mantequilla")) matched.add("Aceite/Mantequilla")

            results.add(
                ChefIdea(
                    id = "idea_mangu",
                    title = "Mangú Criollo con Cebollita Encurtida",
                    emoji = "🍌",
                    time = "30 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                    matchedIngredients = matched,
                    optionalAdditions = listOf("Salami frito", "Aguacate", "Vinagre blanco"),
                    chefTips = listOf(
                        "🔥 El secreto de la suavidad: Guarda media taza del agua caliente donde hirvieron los plátanos y añade 2 cucharadas de mantequilla bien fría o un chorrito de agua con hielo al majar.",
                        "🧅 Para la cebolla perfecta: Córtala en aros finos, déjala reposar 10 minutos con vinagre blanco y una pizca de sal, luego saltéala en aceite bien caliente solo 2 minutos para que quede crujiente y rosada.",
                        "🥑 Para el queso frito: Pásalo rápidamente por harina seca antes de freír en aceite bien caliente para que dore por fuera sin derretirse por completo."
                    ),
                    detailedInstructions = listOf(
                        "Pela 4 plátanos verdes, córtalos en trozos medianos y retira la vena central.",
                        "Colócalos en una olla con abundante agua y una cucharadita de sal. Hierve a fuego medio-alto por 20-25 minutos hasta que un tenedor los atraviese con facilidad.",
                        "Saca los plátanos y colócalos en un tazón hondo. Agrega mantequilla y comienza a majar con un majador de papas.",
                        "Agrega gradualmente pequeñas porciones del agua de la cocción reservada (y unas gotas de agua con hielo) mientras majas hasta lograr una textura aterciopelada y sin grumos.",
                        "En una sartén pequeña, calienta dos cucharadas de aceite. Sofríe los aros de cebolla con su vinagre por 2 minutos hasta ablandar ligeramente.",
                        "Sirve el mangú en montículo caliente, corona con la cebolla tibia por encima y acompaña con huevo revuelto o queso frito dorado."
                    )
                )
            )
        }

        // 2. Desayuno Express de Avena Cremosa o Harina con Canela
        if (has("avena") || has("harina") || has("leche") || has("guineo")) {
            val matched = mutableListOf<String>()
            if (has("avena")) matched.add("Avena")
            if (has("harina")) matched.add("Harina de maíz")
            if (has("leche")) matched.add("Leche")
            if (has("guineo")) matched.add("Guineo maduro")
            if (has("canela")) matched.add("Canela")

            results.add(
                ChefIdea(
                    id = "idea_avena_desayuno",
                    title = "Desayuno Cremoso de Avena y Canela Criolla",
                    emoji = "🥣",
                    time = "12 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.DESAYUNO),
                    matchedIngredients = matched.ifEmpty { listOf("Avena/Leche") },
                    optionalAdditions = listOf("Canela en rama", "Cáscara de limón", "Guineo en rodajas"),
                    chefTips = listOf(
                        "🍋 Aroma de abuela: Pon un trocito pequeño de cáscara de limón verde junto a la canela mientras hierve la leche; transforma el sabor por completo.",
                        "🧂 Una pizca de sal realza el dulce natural sin necesidad de añadir demasiada azúcar."
                    ),
                    detailedInstructions = listOf(
                        "Calienta 2 tazas de leche o mezcla de leche y agua en una olla mediana con una astilla de canela.",
                        "Cuando empiece a humear, agrega 1 taza de avena en hojuelas y una pizca de sal.",
                        "Cocina a fuego bajo moviendo de forma circular durante 6 minutos hasta lograr una crema suave.",
                        "Retira del fuego, endulza al gusto, deja reposar 2 minutos y corona con rodajas de guineo si tienes."
                    )
                )
            )
        }

        // 3. Desayuno / Cena Rápida: Huevos con Queso y Pan o Yuca
        if (has("huevo") || has("queso") || has("pan") || has("yuca")) {
            val matched = mutableListOf<String>()
            if (has("huevo")) matched.add("Huevos")
            if (has("queso")) matched.add("Queso")
            if (has("pan")) matched.add("Pan")
            if (has("yuca")) matched.add("Yuca")
            if (has("cebolla")) matched.add("Cebolla")

            results.add(
                ChefIdea(
                    id = "idea_desayuno_cena_huevos",
                    title = if (has("yuca")) "Yuca Caliente con Huevos y Queso Dorado" else "Tostadas o Sándwich Criollo de Huevo y Queso",
                    emoji = "🍳",
                    time = if (has("yuca")) "25 min" else "12 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.DESAYUNO, RecipeMealType.CENA),
                    matchedIngredients = matched,
                    optionalAdditions = listOf("Tomate en rodajas", "Aguacate", "Mantequilla"),
                    chefTips = listOf(
                        "🍳 Huevos jugosos: Retira los huevos del fuego un instante antes de que estén completamente secos; el calor residual termina de cuajarlos suaves.",
                        "🧀 Queso perfecto: Si usas queso de freír dominicano, sécalo bien con servilleta antes de llevarlo a la sartén bien caliente."
                    ),
                    detailedInstructions = listOf(
                        if (has("yuca")) "Pela y hierve la yuca con sal por 20 minutos hasta que esté suave y escurre." else "Corta y tuesta el pan en sartén con un toque de mantequilla o aceite.",
                        "En una sartén fríe las lonjas de queso hasta que doren ligeramente por fuera.",
                        "Prepara los huevos revueltos o estrellados con cebolla picadita y una pizca de sal.",
                        "Sirve caliente combinando el queso dorado, el huevo y tu acompañamiento favorito."
                    )
                )
            )
        }

        // 4. La Bandera Dominicana (Almuerzo)
        if (has("arroz") && (has("habichuela") || has("frijol") || has("guandul"))) {
            val matched = mutableListOf("Arroz", "Habichuelas/Guandules")
            if (has("pollo") || has("carne")) matched.add("Pollo/Carne")
            if (has("ajo")) matched.add("Ajo")
            if (has("cebolla")) matched.add("Cebolla")

            results.add(
                ChefIdea(
                    id = "idea_bandera",
                    title = "La Bandera Dominicana con Concón Dorado",
                    emoji = "🍛",
                    time = "50 min",
                    difficulty = "Tradicional",
                    mealTypes = setOf(RecipeMealType.ALMUERZO),
                    matchedIngredients = matched,
                    optionalAdditions = listOf("Cilantro", "Pasta de tomate", "Orégano seco tostado"),
                    chefTips = listOf(
                        "🍚 Truco del concón perfecto: Cuando el arroz seque, no revuelvas el fondo. Deja fuego muy bajo 15 minutos tapado. Antes de apagar, sube el fuego a medio por 3 minutos con unas gotas de aceite en los bordes.",
                        "🫘 Espesura natural de las habichuelas: Saca 4 cucharadas de habichuelas cocidas con un poco de caldo, májalas con un tenedor y devuélvelas a la olla. Creará una salsa untuosa sin maicena.",
                        "🍗 Sellado con azúcar: En una olla de hierro, agrega una cucharadita de azúcar al aceite caliente hasta que caramelice a color ámbar oscuro, luego echa el pollo sazonado para obtener ese color moreno brillante característico."
                    ),
                    detailedInstructions = listOf(
                        "Lava el arroz con agua fría. En un caldero sofríe ajo triturado en dos cucharadas de aceite, agrega agua (proporción 1.5 tazas de agua por taza de arroz) y sal.",
                        "Cuando hierva a borbotones, añade el arroz, remueve una vez y cocina destapado a fuego medio hasta que absorba todo el líquido.",
                        "Haz una montañita con el arroz, tapa herméticamente y baja el fuego al mínimo por 20 minutos.",
                        "Para las habichuelas: sofríe cebolla picada, ajo, ají y un toque de orégano en aceite. Vierte las habichuelas con su caldo y deja hervir 15 minutos a fuego medio hasta que espese a gusto.",
                        "Si tienes pollo, sazónalo previamente con ajo, sal, orégano y limón. Dóralo en aceite caliente y guísalo con salsa de tomate por 20 minutos.",
                        "Emplata el arroz blanco con las habichuelas bañándolo y el pollo guisado al lado con su jugo."
                    )
                )
            )
        }

        // 5. Tostones Crocantes con Mojo de Ajo Criollo (Cena y Almuerzo)
        if (has("platano") || has("plátano")) {
            results.add(
                ChefIdea(
                    id = "idea_tostones",
                    title = "Tostones Crujientes con Mojo de Ajo y Limón",
                    emoji = "🟡",
                    time = "20 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.ALMUERZO),
                    matchedIngredients = listOf("Plátano verde", "Aceite", "Ajo"),
                    optionalAdditions = listOf("Limón", "Sal marina", "Queso frito"),
                    chefTips = listOf(
                        "🧊 Doble fritura y salmuera: El secreto dominicano es pasar el plátano aplastado 2 segundos por agua fría con sal y ajo antes de la segunda fritura. El vapor interno los deja suaves adentro y crocantes afuera.",
                        "🌡️ Temperatura: La primera fritura debe ser a fuego medio (160°C) para cocer el plátano por dentro sin dorar. La segunda fritura debe ser a fuego alto (190°C) para dorar en 60 segundos."
                    ),
                    detailedInstructions = listOf(
                        "Pela los plátanos verdes y córtalos en rodajas diagonales de 2 a 3 cm de grosor.",
                        "Calienta abundante aceite en una sartén honda a fuego medio.",
                        "Coloca las rodajas y fríelas durante 3 a 4 minutos por lado hasta que estén tiernas al pincharlas con un tenedor, pero aún amarillas.",
                        "Retira sobre papel absorbente y aplasta con una tostonera o la base de una taza forrada con una funda plástica.",
                        "Prepara un tazón con agua fría, 2 dientes de ajo machacados y sal. Pasa cada tostón 1 segundo y regresa de inmediato al aceite muy caliente.",
                        "Fríe por 1-2 minutos hasta que adquieran un color dorado crujiente. Escurre y espolvorea sal inmediatamente."
                    )
                )
            )
        }

        // 6. Pollo Guisado al Estilo Campestre (Almuerzo)
        if (has("pollo") || has("pechuga") || has("carne")) {
            val matched = mutableListOf("Pollo/Carne")
            if (has("cebolla")) matched.add("Cebolla")
            if (has("ajo")) matched.add("Ajo")
            if (has("tomate")) matched.add("Tomate")
            if (has("aceite")) matched.add("Aceite")

            results.add(
                ChefIdea(
                    id = "idea_pollo_guisado",
                    title = "Pollo Guisado en Reducción Criolla",
                    emoji = "🍗",
                    time = "40 min",
                    difficulty = "Media",
                    mealTypes = setOf(RecipeMealType.ALMUERZO),
                    matchedIngredients = matched,
                    optionalAdditions = listOf("Orégano", "Limón o naranja agria", "Ají gustoso"),
                    chefTips = listOf(
                        "🍋 Limpieza y marinado: Lava el pollo con jugo de limón o naranja agria. Marina al menos 20 minutos con ajo majado, sal y orégano frotado entre las palmas.",
                        "🍯 Caramelizado auténtico con azúcar: El secreto de las abuelas dominicanas es poner media cucharadita de azúcar en el aceite caliente hasta que caramelice en color ámbar; de inmediato se coloca el pollo para dorar y sellar con ese color dorado caribeño inconfundible.",
                        "💧 Agua caliente: Nunca agregues agua fría al guisado; usa siempre agua caliente o caldo para no cortar la cocción."
                    ),
                    detailedInstructions = listOf(
                        "Corta el pollo en presas limpias y sazona con sal, ajo majado y orégano seco.",
                        "En un caldero grande, calienta 2 cucharadas de aceite a fuego medio-alto.",
                        "Agrega las piezas de pollo y dóralas uniformemente por ambos lados durante 8-10 minutos.",
                        "Añade la cebolla picada en juliana, tomate en cubos y ajo adicional. Sofríe 3 minutos hasta que los vegetales suelten aroma.",
                        "Agrega media taza de agua caliente, tapa el caldero y cocina a fuego medio durante 25 minutos.",
                        "Destapa los últimos 5 minutos para que la salsa espese y tome consistencia untuosa. Sirve con arroz o tostones."
                    )
                )
            )
        }

        // 7. Pastelón Rápido de Plátano Maduro (Almuerzo y Cena)
        if ((has("platano") || has("plátano")) && (has("huevo") || has("queso") || has("carne") || has("molida"))) {
            results.add(
                ChefIdea(
                    id = "idea_pastelon",
                    title = "Pastelón Dulce y Salado de Plátano Maduro",
                    emoji = "🥧",
                    time = "45 min",
                    difficulty = "Media",
                    mealTypes = setOf(RecipeMealType.ALMUERZO, RecipeMealType.CENA),
                    matchedIngredients = listOf("Plátano maduro", "Huevo/Queso"),
                    optionalAdditions = listOf("Carne molida o atún", "Mantequilla", "Canela"),
                    chefTips = listOf(
                        "🍌 El plátano debe tener la piel bien manchada o casi negra; mientras más maduro, más dulce y suave quedará el contraste con el queso salado.",
                        "🥚 Capa aglutinante: Batir un huevo con una cucharadita de mantequilla derretida y verterlo sobre la capa superior para que gratine dorado y mantenga la forma al cortar."
                    ),
                    detailedInstructions = listOf(
                        "Corta 4 plátanos maduros en láminas finas a lo largo.",
                        "Dóralos en una sartén con un poco de aceite o mantequilla hasta que caramelicen.",
                        "En un molde para horno o sartén antiadherente, coloca una base de láminas de plátano.",
                        "Añade un relleno de carne molida sazonada, atún o huevo revuelto con vegetales.",
                        "Cubre con queso rallado y coloca otra capa de plátano maduro por encima.",
                        "Baña con huevo batido y cocina tapado a fuego bajo por 15 minutos o en horno a 180°C hasta gratinar."
                    )
                )
            )
        }

        // 8. Tortilla Española Criolla con Papas y Huevos (Cena y Desayuno)
        if (has("papa") || has("batata") || has("huevo")) {
            results.add(
                ChefIdea(
                    id = "idea_tortilla",
                    title = "Tortilla Criolla de Papas y Huevos",
                    emoji = "🥔",
                    time = "25 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.DESAYUNO),
                    matchedIngredients = listOf("Papa", "Huevos", "Cebolla"),
                    optionalAdditions = listOf("Queso rallado", "Pimienta", "Aceite de oliva"),
                    chefTips = listOf(
                        "🍳 El secreto del cuajado jugoso: Cocina las papas a fuego lento en abundante aceite (pochadas, no fritas crujientes). Escúrrelas calientes y mézclalas con el huevo batido; déjalas reposar 5 minutos antes de llevar a la sartén para que absorban el huevo.",
                        "🔄 La vuelta sin miedo: Usa un plato llano más grande que la sartén, engrásalo ligeramente y voltea con un movimiento firme y seguro."
                    ),
                    detailedInstructions = listOf(
                        "Pela 2 papas medianas y córtalas en rodajas finas de 3 mm.",
                        "Pica media cebolla en juliana fina.",
                        "Pocha las papas y la cebolla en aceite a fuego suave durante 12-15 minutos hasta que estén muy tiernas.",
                        "Bate 4 a 5 huevos en un tazón con una pizca de sal. Añade las papas escurridas y mezcla.",
                        "En una sartén antiadherente con una cucharadita de aceite caliente, vierte la mezcla a fuego medio-bajo.",
                        "Cocina 4 minutos moviendo los bordes con espátula. Da la vuelta con un plato y cocina 3 minutos más."
                    )
                )
            )
        }

        // 9. Espaguetis a la Criolla con Salsa Cremosa (Cena y Almuerzo)
        if (has("pasta") || has("espagueti") || has("fideo")) {
            results.add(
                ChefIdea(
                    id = "idea_espagueti",
                    title = "Espaguetis Rojos al Estilo Dominicano",
                    emoji = "🍝",
                    time = "20 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.CENA, RecipeMealType.ALMUERZO),
                    matchedIngredients = listOf("Pasta/Espagueti", "Tomate/Salsa", "Ajo"),
                    optionalAdditions = listOf("Queso parmesano o cheddar", "Leche evaporada", "Orégano"),
                    chefTips = listOf(
                        "🧀 Toque colmadero: Los tradicionales espaguetis dominicanos llevan salsa de tomate sofrita con ajo, orégano y un chorrito de leche evaporada o crema de leche con queso fundido.",
                        "🍝 Al dente: Hierve la pasta 2 minutos menos de lo que dice el empaque y termina de cocerla directamente dentro de la salsa para que absorba todo el sabor."
                    ),
                    detailedInstructions = listOf(
                        "Hierve 3 litros de agua con abundante sal. Cocina los espaguetis hasta que estén al dente.",
                        "En otra sartén, sofríe 2 dientes de ajo picados en dos cucharadas de aceite.",
                        "Agrega tomate triturado, una pizca de orégano, sal y pimienta. Cocina 6 minutos a fuego medio.",
                        "Si tienes queso o leche, agrégalos para suavizar la acidez de la salsa.",
                        "Pasa la pasta escurrida directamente a la salsa con media taza del agua de cocción. Revuelve 2 minutos y sirve caliente."
                    )
                )
            )
        }

        // 10. Cena Ligera de Atún o Crema de Auyama
        if (has("atun") || has("atún") || has("auyama")) {
            results.add(
                ChefIdea(
                    id = "idea_cena_ligera",
                    title = if (has("auyama")) "Crema Dorada de Auyama con Queso" else "Ensalada Fresca de Atún con Limón",
                    emoji = if (has("auyama")) "🎃" else "🥗",
                    time = if (has("auyama")) "25 min" else "10 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.CENA),
                    matchedIngredients = if (has("auyama")) listOf("Auyama", "Cebolla/Ajo") else listOf("Atún", "Tomate/Cebolla"),
                    optionalAdditions = listOf("Pan tostado", "Galletas saladas", "Limón"),
                    chefTips = listOf(
                        "🌙 Cena ligera y digestiva: Ideal para la noche porque llena sin caer pesada.",
                        "🌿 Un toque de orégano o limón fresco al final eleva cualquier preparación nocturna."
                    ),
                    detailedInstructions = if (has("auyama")) {
                        listOf(
                            "Pela y corta la auyama en cubos y sofríela 2 minutos con ajo y cebolla.",
                            "Cubre con agua y sal, hierve 18 minutos hasta que esté blanda.",
                            "Licúa con un chorrito de leche y sirve caliente con cuadritos de queso."
                        )
                    } else {
                        listOf(
                            "Escurre el atún en lata y desmenúzalo en un tazón.",
                            "Agrega tomate, cebolla picadita y cilantro si tienes.",
                            "Aliña con limón, sal y un poco de aceite; acompaña con pan o galletas."
                        )
                    }
                )
            )
        }

        // 11. Sopa Criolla Reconfortante con Pollo y Víveres (Almuerzo y Cena)
        if (has("yuca") || has("auyama") || has("papa") || has("fideo") || has("pollo")) {
            results.add(
                ChefIdea(
                    id = "idea_sopa",
                    title = "Sopa Levantamuertos de Víveres y Pollo",
                    emoji = "🍲",
                    time = "40 min",
                    difficulty = "Media",
                    mealTypes = setOf(RecipeMealType.ALMUERZO, RecipeMealType.CENA),
                    matchedIngredients = listOf("Pollo/Víveres", "Cebolla", "Ajo"),
                    optionalAdditions = listOf("Cilantro", "Limón", "Fideos finos"),
                    chefTips = listOf(
                        "🎃 Color dorado natural: Licúa un trozo de auyama cocida con el caldo y devuélvelo a la olla; le dará ese tono ámbar radiante y cuerpo aterciopelado a la sopa.",
                        "🌿 Cilantro al final: Añade el ramito de cilantro en los últimos 3 minutos de cocción para que mantenga sus aceites esenciales frescos."
                    ),
                    detailedInstructions = listOf(
                        "En una olla honda, sofríe ajo y cebolla picada con trozos de pollo.",
                        "Cubre con 6 tazas de agua y deja hervir 15 minutos.",
                        "Añade la yuca, plátano o papa en cubos medianos y la auyama.",
                        "Cocina 15 minutos más hasta que los víveres estén tiernos.",
                        "Añade los fideos y el cilantro picado, rectifica de sal y cocina 5 minutos finales.",
                        "Sirve humeante con unas gotas de limón fresco."
                    )
                )
            )
        }

        val filteredByMeal = if (mealTypeFilter != null) {
            results.filter { mealTypeFilter in it.mealTypes }
        } else {
            results
        }

        if (filteredByMeal.isNotEmpty()) {
            return filteredByMeal
        }

        // Fallback específico según Desayuno / Cena / General
        return listOf(
            when (mealTypeFilter) {
                RecipeMealType.DESAYUNO -> ChefIdea(
                    id = "idea_fallback_desayuno",
                    title = "Desayuno Criollo Express con lo de Casa",
                    emoji = "🌅",
                    time = "15 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.DESAYUNO),
                    matchedIngredients = selectedProductNames.take(4),
                    optionalAdditions = listOf("Huevos", "Queso", "Pan o plátano"),
                    chefTips = listOf(
                        "🌅 Combina un carbohidrato tibio (pan tostado, avena, yuca o plátano) con una proteína rápida (huevo o queso).",
                        "☕ Acompaña con café, chocolate caliente o un batido natural."
                    ),
                    detailedInstructions = listOf(
                        "Revisa tus alimentos base seleccionados y elige el carbohidrato principal (plátano, pan, yuca o avena).",
                        "Prepara el acompañamiento proteico en sartén con una pizca de sal y poco aceite.",
                        "Sirve todo caliente recién hecho para empezar el día con energía."
                    )
                )
                RecipeMealType.CENA -> ChefIdea(
                    id = "idea_fallback_cena",
                    title = "Cena Ligera Dominicana de Despensa",
                    emoji = "🌙",
                    time = "20 min",
                    difficulty = "Fácil",
                    mealTypes = setOf(RecipeMealType.CENA),
                    matchedIngredients = selectedProductNames.take(4),
                    optionalAdditions = listOf("Cebolla encendida", "Limón", "Queso"),
                    chefTips = listOf(
                        "🌙 Por la noche, hervir víveres o saltear vegetales con huevo/queso es rápido, económico y ligero.",
                        "🧅 Una cebollita pasada por aceite y vinagre realza cualquier cena sencilla."
                    ),
                    detailedInstructions = listOf(
                        "Corta en trozos uniformes los víveres o ingredientes seleccionados.",
                        "Hierve o saltea con ajo, cebolla y una pizca de sal y orégano.",
                        "Sirve tibio con limón o un trozo de queso."
                    )
                )
                else -> ChefIdea(
                    id = "idea_salteado_express",
                    title = "Salteado Criollo Rápido de la Despensa",
                    emoji = "🍳",
                    time = "15 min",
                    difficulty = "Fácil",
                    matchedIngredients = selectedProductNames.take(4),
                    optionalAdditions = listOf("Ajo", "Aceite", "Sazón criollo"),
                    chefTips = listOf(
                        "🔥 Fuego vivo: Saltea en sartén bien caliente con poco aceite para sellar aromas.",
                        "🌿 Condimenta en capas: Sal y ajo al inicio, hierbas y pimienta al final."
                    ),
                    detailedInstructions = listOf(
                        "Corta los ingredientes seleccionados en tamaños homogéneos.",
                        "Calienta 2 cucharadas de aceite en sartén amplia.",
                        "Saltea primero los ingredientes más duros por 5 minutos.",
                        "Añade los ingredientes suaves, sazona al gusto con sal y orégano.",
                        "Sirve caliente como plato principal o guarnición."
                    )
                )
            }
        )
    }
}
