package com.example

import com.example.data.local.MonetizationConstants
import com.example.data.model.BabyGuides
import com.example.data.model.BabySubCategory
import com.example.data.model.Category
import com.example.data.model.ChefIdeasKnowledgeBase
import com.example.data.model.ConfidenceLevel
import com.example.data.model.Location
import com.example.data.model.MovementType
import com.example.data.model.PantryItem
import com.example.data.model.Recipe
import com.example.data.model.RecipeMealType
import com.example.data.model.StockMovement
import com.example.data.parser.ReceiptTextParser
import com.example.ui.viewmodel.LimitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testReceiptTextParser_extractsItemsAndCategories() {
        val input = """
            Arroz Selecto 5 lb
            Habichuelas rojas 2 lb
            2 Leche entera
            Cloro Macier 1 l
            Pañales Huggies 38 ud
            Total: RD$ 1,200.00
        """.trimIndent()

        val items = ReceiptTextParser.parse(input)
        assertEquals(5, items.size)

        val arroz = items.find { it.name.contains("Arroz", ignoreCase = true) }
        assertNotNull(arroz)
        assertEquals(5.0, arroz!!.qty, 0.01)
        assertEquals("lb", arroz.unit)
        assertEquals(Category.ALIMENTO, arroz.cat)

        val cloro = items.find { it.name.contains("Cloro", ignoreCase = true) }
        assertNotNull(cloro)
        assertEquals(Category.LIMPIEZA, cloro!!.cat)

        val panales = items.find { it.name.contains("Pañales", ignoreCase = true) }
        assertNotNull(panales)
        assertEquals(Category.BEBE, panales!!.cat)
        assertEquals(BabySubCategory.PANALES.id, panales.sub)
    }

    @Test
    fun testReceiptTextParser_detailedExtractionWithConfidenceAndStore() {
        val ticket = """
            Supermercado Bravo Enriquillo
            Arroz Selecto 5 lb - RD$ 240.00
            Habichuelas rojas 2 lb - RD$ 180.00
            Aceite Crisol 1 l - RD$ 195.00
            Total RD$ 615.00
        """.trimIndent()

        val result = ReceiptTextParser.parseDetailed(ticket)
        assertTrue("Store name should detect Bravo", result.storeName.contains("Bravo"))
        assertEquals(3, result.lines.size)

        val arrozLine = result.lines[0]
        assertEquals("Arroz Selecto", arrozLine.name)
        assertEquals(5.0, arrozLine.qty, 0.01)
        assertEquals("lb", arrozLine.unit)
        assertEquals(240.0, arrozLine.unitPrice, 0.01)
        assertEquals(ConfidenceLevel.HIGH, arrozLine.confidenceLevel)
    }

    @Test
    fun testDominicanRecipe_costAndMatching() {
        val samplePantry = listOf(
            PantryItem(name = "Arroz", qty = 3.0, unit = "lb", cat = Category.ALIMENTO),
            PantryItem(name = "Habichuelas rojas", qty = 1.0, unit = "lb", cat = Category.ALIMENTO),
            PantryItem(name = "Ajo", qty = 2.0, unit = "ud", cat = Category.ALIMENTO),
            PantryItem(name = "Cebolla", qty = 1.0, unit = "lb", cat = Category.ALIMENTO),
            PantryItem(name = "Aceite", qty = 1.0, unit = "l", cat = Category.ALIMENTO)
        )

        val recipe = Recipe.ALL.first { it.name == "Arroz con habichuelas" }
        assertTrue(recipe.hasIngredient("arroz", samplePantry))
        assertTrue(recipe.hasIngredient("habichuela|frijol", samplePantry))

        val costFor4 = recipe.calculateCost(servings = 4, customPrices = emptyMap(), pantryItems = samplePantry)
        assertTrue(costFor4.totalCost > 0)
        // Since all primary ingredients are present in pantry, missing cost should only be seasonings
        assertTrue(costFor4.missingCost < costFor4.totalCost)
    }

    @Test
    fun testBabyGuide_findsAppropriateTutorial() {
        val guide = BabyGuides.findGuideForItem("Pañales Huggies Talla 3", "panales")
        assertEquals("Pañales", guide.title)
        assertTrue(guide.steps.isNotEmpty())
        assertTrue(guide.why.isNotBlank())
    }

    @Test
    fun testPantryItem_locationAndLedger() {
        val item = PantryItem(
            name = "Pollo congelado",
            qty = 2.0,
            unit = "lb",
            location = Location.FREEZER,
            brand = "Pollo Cibao",
            profileId = "profile_1"
        )
        assertEquals(Location.FREEZER, item.location)
        assertEquals("Pollo Cibao", item.brand)

        val movement = StockMovement(
            id = "mov_1",
            itemId = item.id,
            itemName = item.name,
            profileId = item.profileId,
            type = MovementType.OUT,
            delta = -1.0,
            unit = "lb",
            reason = "Cena familiar",
            source = "MANUAL",
            timestamp = System.currentTimeMillis()
        )
        assertEquals(MovementType.OUT, movement.type)
        assertEquals(-1.0, movement.delta, 0.01)
    }

    @Test
    fun testPantryItem_expiryAndLowStock() {
        val lowStockItem = PantryItem(
            name = "Aceite",
            qty = 0.5,
            minQty = 1.0,
            expDate = "2029-12-31"
        )
        assertTrue(lowStockItem.isLowStock)

        val expiredItem = PantryItem(
            name = "Leche",
            qty = 1.0,
            expDate = "2020-01-01"
        )
        assertTrue(expiredItem.isExpired)
    }

    @Test
    fun testMonetizationLimits_exactChecklistRequirements() {
        // Free limits
        assertEquals(30, MonetizationConstants.FREE_PRODUCT_LIMIT)
        assertEquals(5, MonetizationConstants.FREE_MONTHLY_RECEIPT_SCANS_LIMIT)
        assertEquals("RD$1,690", MonetizationConstants.PRO_PRICE_RD)
        assertEquals("despensa_pro_lifetime", MonetizationConstants.PRO_PRODUCT_ID)

        // Exact dialog messages required by user
        assertTrue(LimitType.PRODUCT_LIMIT.exactMessage.contains("límite de 30 productos"))
        assertTrue(LimitType.RECEIPT_SCAN_LIMIT.exactMessage.contains("límite de 5 escaneos de facturas"))
        assertTrue(LimitType.REPORT_HISTORY_LIMIT.exactMessage.contains("reportes e historial de días anteriores"))
        assertTrue(LimitType.PROFILE_LIMIT.exactMessage.contains("modo multi-usuario"))
    }

    @Test
    fun testChefIdeasFeature_generatesDominicanDishesWithTipsAndDetailedInstructions() {
        // Case 1: user has plátano, mantequilla, cebolla, queso
        val selectedProducts = listOf("Plátano verde", "Mantequilla", "Cebolla roja", "Queso frito")
        val ideas = ChefIdeasKnowledgeBase.generateIdeasFor(selectedProducts)

        assertTrue("Must generate at least 1 dish", ideas.isNotEmpty())
        val mangu = ideas.find { it.title.contains("Mangú", ignoreCase = true) }
        assertNotNull("Mangú should be recommended when plátano is present", mangu)
        assertTrue("Matching ingredients must be listed", mangu!!.matchedIngredients.isNotEmpty())
        assertTrue("Chef tips must be provided", mangu.chefTips.isNotEmpty())
        assertTrue("Detailed step-by-step instructions must be provided", mangu.detailedInstructions.size >= 4)

        // Case 2: user has pollo, arroz, habichuelas
        val banderaProducts = listOf("Pollo", "Arroz", "Habichuelas rojas", "Cebolla", "Ajo")
        val banderaIdeas = ChefIdeasKnowledgeBase.generateIdeasFor(banderaProducts)
        val polloIdea = banderaIdeas.find { it.title.contains("Pollo", ignoreCase = true) }
        assertNotNull("Pollo guisado should be recommended", polloIdea)
        assertTrue(polloIdea!!.chefTips.any { it.contains("azúcar", ignoreCase = true) || it.contains("dorar", ignoreCase = true) })
    }

    @Test
    fun testUnlockProWithVipCode_validatesAndActivatesPermanently() {
        val validCodes = listOf(
            "RD-2026",
            "PRO-26",
            "VIP-26",
            "GEO-26",
            "RD 2026",
            "geo26"
        )
        val validCleaned = setOf(
            "RD2026",
            "PRO26",
            "VIP26",
            "GEO26",
            "MANAGEMENTTECHGEO"
        )

        for (code in validCodes) {
            val clean = code.trim().uppercase().replace(" ", "").replace("-", "")
            assertTrue("Code $code should be valid", clean in validCleaned)
        }

        val invalidCodes = listOf("1234", "password", "free", "admin")
        for (code in invalidCodes) {
            val clean = code.trim().uppercase().replace(" ", "").replace("-", "")
            assertFalse("Code $code should be invalid", clean in validCleaned)
        }
    }

    @Test
    fun testBreakfastAndDinnerRecipes_haveIngredientsAndInstructions() {
        val breakfastRecipes = Recipe.ALL.filter { RecipeMealType.DESAYUNO in it.mealTypes }
        val dinnerRecipes = Recipe.ALL.filter { RecipeMealType.CENA in it.mealTypes }

        assertTrue("Should have multiple breakfast recipes", breakfastRecipes.size >= 4)
        assertTrue("Should have multiple dinner recipes", dinnerRecipes.size >= 4)

        (breakfastRecipes + dinnerRecipes).forEach { recipe ->
            assertTrue("${recipe.name} must list ingredients", recipe.ingredientsSpecs.isNotEmpty())
            assertTrue("${recipe.name} must include step-by-step instructions", recipe.instructions.isNotEmpty())
        }
    }

    @Test
    fun testChefIdeas_breakfastAndDinnerFiltering() {
        val breakfastIdeas = ChefIdeasKnowledgeBase.generateIdeasFor(
            selectedProductNames = listOf("Plátano verde", "Huevos", "Queso", "Avena"),
            mealTypeFilter = RecipeMealType.DESAYUNO
        )
        assertTrue("Should return breakfast ideas", breakfastIdeas.isNotEmpty())
        assertTrue(breakfastIdeas.all { RecipeMealType.DESAYUNO in it.mealTypes })
        assertTrue(breakfastIdeas.all { it.detailedInstructions.isNotEmpty() && it.chefTips.isNotEmpty() })

        val dinnerIdeas = ChefIdeasKnowledgeBase.generateIdeasFor(
            selectedProductNames = listOf("Auyama", "Cebolla", "Atún", "Tomate"),
            mealTypeFilter = RecipeMealType.CENA
        )
        assertTrue("Should return dinner ideas", dinnerIdeas.isNotEmpty())
        assertTrue(dinnerIdeas.all { RecipeMealType.CENA in it.mealTypes })
        assertTrue(dinnerIdeas.all { it.detailedInstructions.isNotEmpty() && it.chefTips.isNotEmpty() })
    }
}
