package com.example.data.model

data class BabyGuideItem(
    val pattern: String,
    val title: String,
    val why: String,
    val steps: List<String>,
    val tip: String
)

object BabyGuides {
    val ALL: List<BabyGuideItem> = listOf(
        BabyGuideItem(
            pattern = "panalera|bolso|mochila",
            title = "Pañalera lista para salir",
            why = "Tu kit para salir con el bebé sin olvidar nada.",
            steps = listOf(
                "Lleva 3 o 4 pañales, toallitas, un cambiador y una muda de ropa extra.",
                "Agrega biberón o fórmula medida, y un babero si ya come.",
                "Incluye una bolsita para la ropa sucia y los pañales usados.",
                "Al volver a casa, repón lo que usaste."
            ),
            tip = "Vuelve a llenarla de una vez para no olvidar nada al salir."
        ),
        BabyGuideItem(
            pattern = "toallit",
            title = "Toallitas húmedas",
            why = "Para limpiar al cambiar el pañal o las manitas.",
            steps = listOf(
                "Saca una toallita y cierra el paquete para que no se seque.",
                "Limpia siempre de adelante hacia atrás.",
                "Usa una toallita nueva por cada pasada si hay mucha suciedad.",
                "Deja secar la piel un momento antes de poner el pañal limpio."
            ),
            tip = "Si la piel está irritada, usa agua tibia y algodón. No tires las toallitas al inodoro."
        ),
        BabyGuideItem(
            pattern = "panal",
            title = "Pañales",
            why = "Para mantener al bebé seco y cómodo, de día y de noche.",
            steps = listOf(
                "Revisa la talla según el peso que indica el paquete.",
                "Cambia el pañal cada 2 o 3 horas o apenas esté sucio.",
                "Abre el pañal limpio, levanta las piernitas y colócalo debajo, con las tiras hacia atrás.",
                "Cierra las tiras ajustadas pero cómodas (debe caber un dedo) y acomoda los bordes hacia afuera.",
                "En recién nacidos, dobla el borde del pañal para dejar libre el ombligo."
            ),
            tip = "Si se escapa seguido o deja marcas rojas, probablemente toca subir de talla."
        ),
        BabyGuideItem(
            pattern = "biberon|tetina",
            title = "Biberones",
            why = "Para dar leche materna extraída o fórmula.",
            steps = listOf(
                "Esteriliza el biberón y la tetina antes del primer uso.",
                "Lávalos con agua tibia y jabón después de cada toma, con un cepillo especial.",
                "Prepara la leche y prueba unas gotas en tu muñeca: debe estar tibia, no caliente.",
                "Da el biberón con el bebé semisentado, inclinándolo para que la tetina esté siempre llena.",
                "Hazlo eructar a la mitad y al final de la toma."
            ),
            tip = "Desecha la leche que sobre después de una hora y cambia la tetina si se agrieta."
        ),
        BabyGuideItem(
            pattern = "formula|leche en polvo|leche infantil",
            title = "Fórmula infantil",
            why = "Leche en polvo que se prepara para el biberón.",
            steps = listOf(
                "Lávate las manos y usa un biberón limpio.",
                "Mide primero el agua y luego agrega el polvo con la cucharita de la lata, a ras.",
                "Mezcla suavemente hasta que no queden grumos.",
                "Comprueba la temperatura en tu muñeca antes de darle."
            ),
            tip = "Sigue la etiqueta de tu fórmula y las indicaciones de tu pediatra. No agregues más polvo del indicado."
        ),
        BabyGuideItem(
            pattern = "papilla|cereal|compota|colado",
            title = "Papillas y compotas",
            why = "Primeros alimentos cuando el pediatra lo autoriza.",
            steps = listOf(
                "Empieza solo cuando el pediatra lo indique (generalmente hacia los 6 meses).",
                "Siéntalo erguido y usa una cuchara pequeña y blanda.",
                "Ofrece un alimento nuevo a la vez y espera unos días antes de probar otro.",
                "No agregues sal ni azúcar, y no des miel antes del año."
            ),
            tip = "Nunca lo dejes solo mientras come."
        ),
        BabyGuideItem(
            pattern = "chupete|chupon",
            title = "Chupetes",
            why = "Ayuda a calmar al bebé.",
            steps = listOf(
                "Lávalo con agua tibia y jabón antes de usarlo y esterilízalo de vez en cuando.",
                "Elige la talla según la edad que indica el empaque.",
                "No lo cuelgues con cordones largos del cuello ni lo endulces.",
                "Revísalo a diario: si está pegajoso o agrietado, cámbialo."
            ),
            tip = "Úsalo para calmar, no como sustituto de una toma."
        ),
        BabyGuideItem(
            pattern = "mordedor",
            title = "Mordedores",
            why = "Alivian las encías cuando salen los dientes.",
            steps = listOf(
                "Lávalo antes de usarlo.",
                "Para aliviar las encías, enfríalo en la nevera (no en el congelador).",
                "Entrégalo y quédate cerca mientras lo muerde.",
                "Deséchalo si se rompe o se desprenden piezas."
            ),
            tip = "Ideal cuando salen los primeros dientes."
        ),
        BabyGuideItem(
            pattern = "babero",
            title = "Baberos",
            why = "Protegen la ropa al comer o babear.",
            steps = listOf(
                "Colócalo antes de empezar a comer.",
                "Ajusta el cierre sin apretar el cuello.",
                "Después de comer, límpialo y lávalo."
            ),
            tip = "Reemplázalo si tiene piezas sueltas o broches flojos."
        ),
        BabyGuideItem(
            pattern = "esteriliz",
            title = "Esterilizador",
            why = "Deja biberones y piezas libres de gérmenes.",
            steps = listOf(
                "Lava primero los biberones y piezas.",
                "Pon agua hasta el nivel indicado en el manual.",
                "Coloca las piezas boca abajo y enciéndelo.",
                "Deja enfriar antes de abrir y seca con un paño limpio."
            ),
            tip = "Vacía el agua y seca el aparato después de cada uso."
        ),
        BabyGuideItem(
            pattern = "champu|shampoo|jabon|bano|banera|talco|colonia|aceite",
            title = "Baño e higiene",
            why = "Rutina de limpieza suave para la piel del bebé.",
            steps = listOf(
                "Prepara el agua tibia (unos 37 °C) y pruébala con tu codo.",
                "Usa poco jabón o champú especial para bebé.",
                "Sostén siempre al bebé con una mano; nunca lo dejes solo en la bañera.",
                "Lava de arriba hacia abajo, sin olvidar los pliegues.",
                "Sécalo con palmaditas, sobre todo cuello, axilas e ingles."
            ),
            tip = "Un baño corto, de 5 a 10 minutos, es suficiente."
        ),
        BabyGuideItem(
            pattern = "crema|pomada|oxido",
            title = "Cremas y pomadas",
            why = "Protegen e hidratan la piel, sobre todo en la zona del pañal.",
            steps = listOf(
                "Limpia y seca bien la piel antes de aplicar.",
                "Pon una capa fina, sin frotar fuerte.",
                "Prueba primero en una zona pequeña."
            ),
            tip = "Si el enrojecimiento no mejora en unos días, consulta al pediatra."
        ),
        BabyGuideItem(
            pattern = "termometro",
            title = "Termómetro",
            why = "Para medir la temperatura con seguridad.",
            steps = listOf(
                "Límpialo antes y después de usarlo.",
                "Colócalo según su tipo (axila, oído o frente) y espera la señal.",
                "Anota la hora y la temperatura."
            ),
            tip = "Si es menor de 3 meses y tiene fiebre (38 °C o más), llama al pediatra de inmediato."
        ),
        BabyGuideItem(
            pattern = "cuna|manta|sabana|colchon",
            title = "Sueño seguro",
            why = "Cuna y ropa de cama para dormir sin riesgos.",
            steps = listOf(
                "Acuéstalo siempre boca arriba sobre una superficie firme.",
                "Deja la cuna libre de almohadas, peluches y mantas sueltas.",
                "Usa sábana ajustada y ropa cómoda para la temperatura del cuarto."
            ),
            tip = "Es la forma más segura de que duerma bien."
        ),
        BabyGuideItem(
            pattern = "cuchara|plato|vaso|tazon",
            title = "Utensilios de comida",
            why = "Para empezar a comer solito.",
            steps = listOf(
                "Lávalos con agua tibia y jabón antes del primer uso.",
                "Elige cucharas suaves y de punta redondeada.",
                "Ofrece porciones pequeñas y deja que explore.",
                "Lávalos después de cada uso."
            ),
            tip = "Los vasos con asas ayudan a practicar a partir de los 6 meses."
        ),
        BabyGuideItem(
            pattern = "ropa|body|pijama|media|gorro|zapat",
            title = "Ropa de bebé",
            why = "Ropa cómoda para cada clima.",
            steps = listOf(
                "Lávala antes del primer uso con jabón suave.",
                "Elige tallas holgadas y tela de algodón.",
                "Viste al bebé por capas, según el clima."
            ),
            tip = "Revisa etiquetas y botones para que no le molesten."
        ),
        BabyGuideItem(
            pattern = "zzz",
            title = "Uso general",
            why = "Guía básica para cualquier artículo.",
            steps = listOf(
                "Lee las instrucciones del empaque antes de usarlo.",
                "Lávalo y mantenlo limpio.",
                "Supervisa siempre al bebé cuando lo use."
            ),
            tip = "Ante cualquier duda, consulta a tu pediatra."
        )
    )

    fun findGuideForItem(name: String, sub: String): BabyGuideItem {
        val n = name.lowercase().trim()
        val match = ALL.find { item ->
            val regex = Regex(item.pattern)
            regex.containsMatchIn(n)
        }
        if (match != null) return match

        val subMatch = when (sub) {
            "panales" -> "Pañales"
            "toallitas" -> "Toallitas húmedas"
            "alimentacion" -> "Biberones"
            "bano" -> "Baño e higiene"
            "utensilios" -> "Utensilios de comida"
            "ropa" -> "Ropa de bebé"
            else -> null
        }
        if (subMatch != null) {
            val byTitle = ALL.find { it.title == subMatch }
            if (byTitle != null) return byTitle
        }

        return ALL.last()
    }
}
