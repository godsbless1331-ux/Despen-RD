(() => {
  const STORAGE_KEY = "despensa_pro_web_v1";

  const CATEGORIES = {
    alimento: { label: "Alimentos", emoji: "🥗" },
    bebida: { label: "Bebidas", emoji: "🥤" },
    limpieza: { label: "Limpieza", emoji: "🧽" },
    higiene: { label: "Higiene", emoji: "🧴" },
    bebe: { label: "Bebé", emoji: "👶" }
  };

  const LOCATIONS = {
    pantry: { label: "Despensa", emoji: "📦" },
    fridge: { label: "Nevera", emoji: "❄️" },
    freezer: { label: "Congelador", emoji: "🧊" }
  };

  const RECIPES = [
    {
      id: 21,
      name: "Mangú con Los Tres Golpes",
      emoji: "🇩🇴",
      time: "35 min",
      meals: ["desayuno", "cena"],
      ingredients: [
        { key: "platano", text: "4 plátanos verdes", price: 100 },
        { key: "huevo", text: "4 huevos", price: 40 },
        { key: "queso", text: "1/2 lb queso de freír", price: 120 },
        { key: "cebolla", text: "1 cebolla roja", price: 28 },
        { key: "aceite", text: "Aceite o mantequilla", price: 20 }
      ],
      steps: [
        "Pela los plátanos verdes, córtalos en trozos y hiérvelos con sal 20 minutos hasta que estén bien suaves.",
        "Fríe el queso en lonjas hasta dorar por ambos lados y reserva.",
        "Fríe o revuelve los huevos al gusto en la misma sartén.",
        "Sofríe los aros de cebolla roja con una cucharada de vinagre y sal durante 2 minutos.",
        "Maja los plátanos calientes con mantequilla o aceite y un chorrito de agua fría hasta lograr textura suave.",
        "Sirve el mangú con la cebolla encendida, el queso frito y los huevos."
      ]
    },
    {
      id: 22,
      name: "Harina de maíz dulce con canela",
      emoji: "🥣",
      time: "15 min",
      meals: ["desayuno"],
      ingredients: [
        { key: "harina", text: "1/2 lb harina de maíz", price: 25 },
        { key: "leche", text: "1 litro de leche", price: 85 },
        { key: "canela", text: "1 astilla de canela", price: 15 },
        { key: "azucar", text: "Azúcar al gusto", price: 15 }
      ],
      steps: [
        "Disuelve la harina de maíz en 1 taza de leche fría para evitar grumos.",
        "Calienta el resto de la leche con la astilla de canela y una pizca de sal.",
        "Vierte la mezcla poco a poco removiendo sin parar con cuchara de madera.",
        "Cocina a fuego bajo de 8 a 10 minutos hasta que espese cremosa, endulza y sirve tibia."
      ]
    },
    {
      id: 23,
      name: "Tostadas criollas con huevo, queso y tomate",
      emoji: "🍞",
      time: "12 min",
      meals: ["desayuno", "cena"],
      ingredients: [
        { key: "pan", text: "4 rebanadas de pan", price: 60 },
        { key: "huevo", text: "4 huevos", price: 40 },
        { key: "queso", text: "1/4 lb queso", price: 60 },
        { key: "tomate", text: "1 tomate maduro", price: 20 }
      ],
      steps: [
        "Tuesta las rebanadas de pan en sartén con un poquito de mantequilla o aceite.",
        "Prepara los huevos estrellados o revueltos con una pizca de sal.",
        "Coloca queso, rodajas frescas de tomate y el huevo caliente sobre cada tostada.",
        "Sirve de inmediato con café, avena o jugo."
      ]
    },
    {
      id: 20,
      name: "Batido de guineo con leche",
      emoji: "🥤",
      time: "5 min",
      meals: ["desayuno"],
      ingredients: [
        { key: "guineo", text: "4 guineos maduros", price: 32 },
        { key: "leche", text: "1 litro de leche fría", price: 85 },
        { key: "azucar", text: "Azúcar o miel al gusto", price: 10 }
      ],
      steps: [
        "Pela los guineos maduros y pártelos en trozos.",
        "Colócalos en la licuadora con leche fría, hielo y azúcar o miel al gusto.",
        "Licúa 45 segundos hasta lograr textura espumosa y sirve de inmediato."
      ]
    },
    {
      id: 24,
      name: "Guineítos verdes hervidos con cebolla y huevo",
      emoji: "🍌",
      time: "25 min",
      meals: ["cena", "desayuno"],
      ingredients: [
        { key: "guineo", text: "6 guineos verdes", price: 48 },
        { key: "huevo", text: "4 huevos", price: 40 },
        { key: "cebolla", text: "1 cebolla mediana", price: 22 },
        { key: "aceite", text: "Aceite y limón", price: 15 }
      ],
      steps: [
        "Lava los guineos verdes, haz un corte longitudinal en la cáscara y hiérvelos con sal 15 minutos.",
        "Pélalos mientras estén calientes.",
        "Sofríe cebolla en rodajas finas con aceite, sal y unas gotas de limón o vinagre.",
        "Sirve con huevos hervidos o revueltos y el escabeche de cebolla por encima."
      ]
    },
    {
      id: 25,
      name: "Crema criolla de auyama",
      emoji: "🎃",
      time: "30 min",
      meals: ["cena"],
      ingredients: [
        { key: "auyama", text: "1.5 lb auyama", price: 52 },
        { key: "cebolla", text: "1 cebolla pequeña", price: 18 },
        { key: "ajo", text: "2 dientes de ajo", price: 12 },
        { key: "leche", text: "1 taza de leche", price: 30 },
        { key: "queso", text: "Queso en cubitos", price: 38 }
      ],
      steps: [
        "Pela y corta la auyama en cubos medianos.",
        "Sofríe cebolla y ajo picados con un chorrito de aceite 2 minutos.",
        "Agrega la auyama, cubre con agua, pon sal y hierve 18 minutos hasta ablandar.",
        "Licúa con parte del caldo y un chorrito de leche hasta lograr una crema suave.",
        "Sirve caliente con cubitos de queso para una cena ligera."
      ]
    },
    {
      id: 26,
      name: "Batata hervida o asada con queso",
      emoji: "🍠",
      time: "30 min",
      meals: ["cena", "desayuno"],
      ingredients: [
        { key: "batata", text: "2 lb batata", price: 70 },
        { key: "queso", text: "1/3 lb queso de freír", price: 84 },
        { key: "cebolla", text: "1 cebolla", price: 18 },
        { key: "aceite", text: "Aceite para saltear", price: 12 }
      ],
      steps: [
        "Pela las batatas, córtalas en ruedas gruesas y hiérvelas en agua con sal 20 minutos.",
        "Escúrrelas bien cuando estén suaves al pinchar.",
        "Dora el queso en sartén y sofríe aros de cebolla en aceite.",
        "Sirve la batata caliente con el queso dorado y cebolla."
      ]
    },
    {
      id: 0,
      name: "Arroz con habichuelas (La Bandera)",
      emoji: "🍛",
      time: "50 min",
      meals: ["almuerzo"],
      ingredients: [
        { key: "arroz", text: "1 lb arroz", price: 42 },
        { key: "habichuela", text: "1/2 lb habichuelas rojas", price: 42 },
        { key: "ajo", text: "Ajo majado", price: 15 },
        { key: "cebolla", text: "Cebolla", price: 20 },
        { key: "aceite", text: "Aceite", price: 20 }
      ],
      steps: [
        "Si las habichuelas son secas, remójalas y hiérvelas hasta que ablanden.",
        "Sofríe cebolla y ajo 2 minutos, agrega las habichuelas con su caldo y hierve 15 minutos.",
        "En otra olla sofríe el arroz con aceite y sal, agrega agua que cubra dos dedos.",
        "Tapa y cocina a fuego bajo hasta que quede suelto y sirve."
      ]
    },
    {
      id: 3,
      name: "Pollo guisado criollo",
      emoji: "🍗",
      time: "45 min",
      meals: ["almuerzo"],
      ingredients: [
        { key: "pollo", text: "2 lb pollo", price: 180 },
        { key: "cebolla", text: "Cebolla", price: 20 },
        { key: "ajo", text: "Ajo", price: 15 },
        { key: "tomate", text: "Tomate", price: 20 },
        { key: "aceite", text: "Aceite", price: 20 }
      ],
      steps: [
        "Sazona el pollo con ajo, sal y limón 15 minutos.",
        "Dora las presas en aceite caliente por ambos lados.",
        "Agrega cebolla, ajo, tomate y media taza de agua.",
        "Tapa y cocina a fuego medio 25 minutos hasta que espese la salsa."
      ]
    }
  ];

  const DEFAULT_STATE = {
    pantryName: "Despensa RD",
    monthlyBudgetRD: 18000,
    colorTheme: "esmeralda",
    mode: "light",
    items: [
      { id: "i1", name: "Arroz Selecto", qty: 5, unit: "lb", cat: "alimento", location: "pantry", minQty: 2, expDate: "2027-06-30" },
      { id: "i2", name: "Plátanos verdes", qty: 6, unit: "ud", cat: "alimento", location: "pantry", minQty: 2, expDate: "" },
      { id: "i3", name: "Huevos criollos", qty: 12, unit: "ud", cat: "alimento", location: "fridge", minQty: 4, expDate: "" },
      { id: "i4", name: "Queso de freír", qty: 1, unit: "lb", cat: "alimento", location: "fridge", minQty: 0.5, expDate: "" },
      { id: "i5", name: "Leche entera", qty: 1, unit: "l", cat: "bebida", location: "fridge", minQty: 1, expDate: "" },
      { id: "i6", name: "Cebolla roja", qty: 1, unit: "lb", cat: "alimento", location: "pantry", minQty: 0.5, expDate: "" }
    ],
    shopping: [
      { id: "s1", name: "Habichuelas rojas", qty: 2, unit: "lb", checked: false },
      { id: "s2", name: "Auyama", qty: 1.5, unit: "lb", checked: false }
    ],
    expenses: 1705,
    mealPlan: []
  };

  let state = loadState();
  let activeTab = "home";
  let searchQuery = "";
  let locFilter = "all";
  let mealFilter = "all";
  let readyOnlyFilter = false;
  let chefMealFilter = "desayuno";

  function loadState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return structuredClone(DEFAULT_STATE);
      return { ...structuredClone(DEFAULT_STATE), ...JSON.parse(raw) };
    } catch {
      return structuredClone(DEFAULT_STATE);
    }
  }

  function saveState() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }

  function applyTheme() {
    document.body.dataset.colorTheme = state.colorTheme || "esmeralda";
    document.body.dataset.mode = state.mode || "light";
  }

  function esc(str) {
    return String(str ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function toast(msg) {
    const el = document.getElementById("toast");
    el.textContent = msg;
    el.classList.remove("hidden");
    clearTimeout(toast._t);
    toast._t = setTimeout(() => el.classList.add("hidden"), 2600);
  }

  function hasIngredient(key) {
    return state.items.some(i => i.qty > 0 && i.name.toLowerCase().includes(key.toLowerCase()));
  }

  function renderHome() {
    const lowStock = state.items.filter(i => i.minQty > 0 && i.qty <= i.minQty);
    const pct = state.monthlyBudgetRD > 0 ? Math.min(100, Math.round((state.expenses / state.monthlyBudgetRD) * 100)) : 0;

    return `
      <div class="hero-3d-wrap">
        <div class="hero-3d-plinth"></div>
        <section class="hero-3d-face">
          <div class="hero-top">
            <div>
              <div class="hero-badge">✨ CENTRO DEL HOGAR 3D</div>
              <h1 class="hero-title">${esc(state.pantryName || "Despensa RD")}</h1>
              <p class="hero-desc">El sistema operativo de tu hogar en la web. Controla inventario, desayuno, cena y lista de compras.</p>
            </div>
            <div class="cube-pedestal">
              <div class="cube-3d">🛒</div>
            </div>
          </div>
          <div class="hero-actions">
            <button class="push-btn-3d push-white" type="button" onclick="window.__app.openBatchModal()">📋 Pegar Lista</button>
            <button class="push-btn-3d push-glass" type="button" onclick="window.__app.openProductModal()">➕ Manual</button>
            <button class="push-btn-3d push-cream" type="button" onclick="window.__app.goTab('cooking')">✨ Cocina</button>
          </div>
        </section>
      </div>

      <section class="card">
        <h3>🍳 ¿Qué cocinamos hoy?</h3>
        <p class="muted">Recetas con ingredientes exactos y cómo se hace paso a paso.</p>
        <div class="grid-2" style="margin-top:10px">
          <button class="btn secondary" type="button" onclick="window.__app.openCookingMeal('desayuno')">🌅 ¿Qué hago de Desayuno?</button>
          <button class="btn primary" type="button" onclick="window.__app.openCookingMeal('cena')">🌙 ¿Qué hago de Cena?</button>
        </div>
      </section>

      <section class="card">
        <div class="row" style="justify-content:space-between">
          <div>
            <h3>💰 Presupuesto del Hogar</h3>
            <p class="muted">Gastado este mes: RD$ ${state.expenses.toLocaleString("en-US")}</p>
          </div>
          <strong>RD$ ${Number(state.monthlyBudgetRD).toLocaleString("en-US")}</strong>
        </div>
        <div class="progress-track"><div class="progress-fill" style="width:${pct}%"></div></div>
        <div class="muted">${pct}% utilizado</div>
      </section>

      ${lowStock.length ? `
      <section class="card">
        <h3>📉 Bajos de stock (${lowStock.length})</h3>
        ${lowStock.map(item => `
          <div class="pantry-item">
            <div>
              <strong>${esc(item.name)}</strong>
              <div class="muted">Quedan ${item.qty} ${esc(item.unit)} (mín: ${item.minQty})</div>
            </div>
            <button class="btn primary" type="button" onclick="window.__app.addShopping('${esc(item.name)}', ${Math.max(1, item.minQty)}, '${esc(item.unit)}')">+ Lista</button>
          </div>
        `).join("")}
      </section>` : ""}
    `;
  }

  function renderPantry() {
    const filtered = state.items.filter(item => {
      const qOk = !searchQuery || item.name.toLowerCase().includes(searchQuery.toLowerCase());
      const lOk = locFilter === "all" || item.location === locFilter;
      return qOk && lOk;
    });

    return `
      <section class="card">
        <div class="row" style="justify-content:space-between">
          <div>
            <h2>📦 Inventario (${state.items.length})</h2>
            <div class="muted">Sube productos manuales o pega listas completas</div>
          </div>
          <div class="row">
            <button class="btn primary" type="button" onclick="window.__app.openProductModal()">➕ Subir manual</button>
            <button class="btn" type="button" onclick="window.__app.openBatchModal()">📋 Pegar texto</button>
          </div>
        </div>
        <div style="margin-top:12px">
          <input id="pantry-search" placeholder="Buscar producto..." value="${esc(searchQuery)}">
        </div>
        <div class="chips" style="margin-top:8px">
          <button class="chip ${locFilter === "all" ? "active" : ""}" onclick="window.__app.setLoc('all')">Todas</button>
          ${Object.entries(LOCATIONS).map(([k, v]) => `
            <button class="chip ${locFilter === k ? "active" : ""}" onclick="window.__app.setLoc('${k}')">${v.emoji} ${v.label}</button>
          `).join("")}
        </div>
        ${filtered.length === 0 ? `<p class="muted">No hay productos aquí. Pulsa "Subir manual" para agregar uno.</p>` : filtered.map(item => {
          const cat = CATEGORIES[item.cat] || CATEGORIES.alimento;
          const loc = LOCATIONS[item.location] || LOCATIONS.pantry;
          return `
            <div class="pantry-item">
              <div>
                <strong>${cat.emoji} ${esc(item.name)}</strong>
                <div class="muted">${loc.emoji} ${loc.label}${item.expDate ? ` · Vence ${esc(item.expDate)}` : ""}</div>
              </div>
              <div class="qty-controls">
                <button class="sq-btn" type="button" onclick="window.__app.changeQty('${item.id}', -1)">-</button>
                <strong>${item.qty} ${esc(item.unit)}</strong>
                <button class="sq-btn" type="button" onclick="window.__app.changeQty('${item.id}', 1)">+</button>
                <button class="sq-btn" type="button" onclick="window.__app.openProductModal('${item.id}')">✏️</button>
              </div>
            </div>
          `;
        }).join("")}
      </section>
    `;
  }

  function renderCooking() {
    const evaluated = RECIPES.map(r => {
      const lines = r.ingredients.map(ing => ({ ...ing, have: hasIngredient(ing.key) }));
      const haveCount = lines.filter(l => l.have).length;
      const missingCost = lines.filter(l => !l.have).reduce((sum, l) => sum + l.price, 0);
      return { ...r, lines, haveCount, missingCost };
    }).filter(r => {
      const mOk = mealFilter === "all" || r.meals.includes(mealFilter);
      const rOk = !readyOnlyFilter || r.haveCount === r.lines.length;
      return mOk && rOk;
    }).sort((a, b) => (b.haveCount / b.lines.length) - (a.haveCount / a.lines.length));

    return `
      <section class="card">
        <h2>🍳 Cocina · Desayuno, Almuerzo y Cena</h2>
        <p class="muted">Elige el momento del día para ver recetas con sus ingredientes y preparación detallada.</p>
        <div class="chips" style="margin-top:8px">
          <button class="chip ${mealFilter === "all" ? "active" : ""}" onclick="window.__app.setMeal('all')">🍽️ Todas</button>
          <button class="chip ${mealFilter === "desayuno" ? "active" : ""}" onclick="window.__app.setMeal('desayuno')">🌅 Desayuno</button>
          <button class="chip ${mealFilter === "cena" ? "active" : ""}" onclick="window.__app.setMeal('cena')">🌙 Cena</button>
          <button class="chip ${mealFilter === "almuerzo" ? "active" : ""}" onclick="window.__app.setMeal('almuerzo')">☀️ Almuerzo</button>
          <button class="chip ${readyOnlyFilter ? "active" : ""}" onclick="window.__app.toggleReady()">✅ Listas con lo que tengo</button>
        </div>
      </section>

      ${evaluated.map(r => `
        <article class="card">
          <div class="row" style="justify-content:space-between">
            <div>
              <h3>${r.emoji} ${esc(r.name)}</h3>
              <div>
                ${r.meals.map(m => `<span class="badge">${m === "desayuno" ? "🌅 Desayuno" : m === "cena" ? "🌙 Cena" : "☀️ Almuerzo"}</span>`).join("")}
                <span class="badge">⏱️ ${esc(r.time)}</span>
              </div>
            </div>
            <span class="badge ${r.haveCount === r.lines.length ? "ok" : "warn"}">${r.haveCount}/${r.lines.length} en despensa</span>
          </div>
          <p style="margin:12px 0 4px"><strong>Ingredientes (4 personas):</strong></p>
          <ul style="margin:0;padding-left:18px">
            ${r.lines.map(l => `<li>${l.have ? "✅" : "🛒"} ${esc(l.text)}</li>`).join("")}
          </ul>
          <p style="margin:12px 0 4px"><strong>Cómo se hace (Paso a paso):</strong></p>
          <ol style="margin:0;padding-left:18px">
            ${r.steps.map(s => `<li style="margin-bottom:4px">${esc(s)}</li>`).join("")}
          </ol>
          <div class="row" style="margin-top:12px;justify-content:space-between">
            <span class="muted">${r.missingCost > 0 ? `Faltantes aprox: RD$ ${r.missingCost}` : "¡Tienes todo listo para cocinar!"}</span>
            ${r.missingCost > 0 ? `<button class="btn primary" type="button" onclick="window.__app.addMissingFromRecipe(${r.id})">Añadir faltantes a lista</button>` : ""}
          </div>
        </article>
      `).join("")}
    `;
  }

  function renderShopping() {
    const pending = state.shopping.filter(s => !s.checked);
    const done = state.shopping.filter(s => s.checked);
    return `
      <section class="card">
        <h2>🛒 Lista de Compras</h2>
        <form id="shop-form" class="row" style="margin-top:10px">
          <input id="shop-name" placeholder="Ej. Aceite, pan, jabón..." style="flex:2;min-width:160px" required>
          <input id="shop-qty" type="number" step="0.5" value="1" style="flex:0.7;min-width:75px">
          <button class="btn primary" type="submit">Añadir</button>
        </form>
        <div style="margin-top:12px">
          ${pending.length === 0 ? `<p class="muted">No hay pendientes por comprar.</p>` : pending.map(s => `
            <div class="pantry-item">
              <label style="margin:0;display:flex;align-items:center;gap:8px;cursor:pointer">
                <input type="checkbox" style="width:auto" onchange="window.__app.toggleShop('${s.id}')">
                <span><strong>${esc(s.name)}</strong> · ${s.qty} ${esc(s.unit)}</span>
              </label>
              <div class="row">
                <button class="btn" type="button" onclick="window.__app.moveToPantry('${s.id}')">A despensa</button>
                <button class="sq-btn" type="button" onclick="window.__app.deleteShop('${s.id}')">🗑️</button>
              </div>
            </div>
          `).join("")}
        </div>
        ${done.length ? `<h3 style="margin-top:16px">Comprados (${done.length})</h3>` + done.map(s => `
          <div class="pantry-item" style="opacity:0.65">
            <label style="margin:0;display:flex;align-items:center;gap:8px;cursor:pointer">
              <input type="checkbox" checked style="width:auto" onchange="window.__app.toggleShop('${s.id}')">
              <s>${esc(s.name)} (${s.qty} ${esc(s.unit)})</s>
            </label>
            <button class="sq-btn" type="button" onclick="window.__app.deleteShop('${s.id}')">🗑️</button>
          </div>
        `).join("") : ""}
      </section>
    `;
  }

  function renderHousehold() {
    const themes = [
      { id: "esmeralda", name: "Esmeralda" },
      { id: "oceano", name: "Océano" },
      { id: "atardecer", name: "Atardecer" },
      { id: "violeta", name: "Violeta" }
    ];
    return `
      <section class="card">
        <h2>⚙️ Hogar & Configuración Web</h2>
        <label>Nombre de tu despensa
          <input id="cfg-name" value="${esc(state.pantryName)}">
        </label>
        <label>Presupuesto mensual (RD$)
          <input id="cfg-budget" type="number" value="${state.monthlyBudgetRD}">
        </label>
        <button class="btn primary" style="margin-top:10px" type="button" onclick="window.__app.saveConfig()">Guardar ajustes</button>
      </section>

      <section class="card">
        <h3>🎨 Tema visual</h3>
        <div class="chips">
          ${themes.map(t => `
            <button class="chip ${state.colorTheme === t.id ? "active" : ""}" onclick="window.__app.setTheme('${t.id}')">${t.name}</button>
          `).join("")}
        </div>
        <button class="btn" type="button" onclick="window.__app.toggleMode()">Alternar Claro / Oscuro (${state.mode === "dark" ? "Oscuro" : "Claro"})</button>
      </section>

      <section class="card">
        <h3>💾 Respaldo JSON (Compatible con la App Android)</h3>
        <p class="muted">Exporta o importa tus datos entre el teléfono Android y este sitio web.</p>
        <div class="row" style="margin-top:10px">
          <button class="btn primary" type="button" onclick="window.__app.exportJson()">Copiar respaldo JSON</button>
          <button class="btn" type="button" onclick="window.__app.openImportModal()">Importar JSON</button>
          <button class="btn danger" type="button" onclick="window.__app.resetDemo()">Restablecer datos</button>
        </div>
      </section>
    `;
  }

  function render() {
    applyTheme();
    document.querySelectorAll(".dock-btn").forEach(btn => {
      btn.classList.toggle("active", btn.dataset.tab === activeTab);
    });
    const main = document.getElementById("app-main");
    if (activeTab === "home") main.innerHTML = renderHome();
    else if (activeTab === "pantry") main.innerHTML = renderPantry();
    else if (activeTab === "cooking") main.innerHTML = renderCooking();
    else if (activeTab === "shopping") main.innerHTML = renderShopping();
    else if (activeTab === "household") main.innerHTML = renderHousehold();

    const searchInput = document.getElementById("pantry-search");
    if (searchInput) {
      searchInput.addEventListener("input", e => {
        searchQuery = e.target.value;
        render();
        const refocused = document.getElementById("pantry-search");
        if (refocused) {
          refocused.focus();
          refocused.setSelectionRange(searchQuery.length, searchQuery.length);
        }
      });
    }

    const shopForm = document.getElementById("shop-form");
    if (shopForm) {
      shopForm.addEventListener("submit", e => {
        e.preventDefault();
        const name = document.getElementById("shop-name").value.trim();
        const qty = parseFloat(document.getElementById("shop-qty").value) || 1;
        if (!name) return;
        window.__app.addShopping(name, qty, "ud");
      });
    }
  }

  function showModal(html) {
    document.getElementById("modal-card").innerHTML = html;
    document.getElementById("modal-backdrop").classList.remove("hidden");
  }

  function closeModal() {
    document.getElementById("modal-backdrop").classList.add("hidden");
  }

  document.getElementById("modal-backdrop").addEventListener("click", e => {
    if (e.target.id === "modal-backdrop") closeModal();
  });

  document.querySelectorAll(".dock-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      activeTab = btn.dataset.tab;
      render();
      window.scrollTo({ top: 0, behavior: "smooth" });
    });
  });

  document.getElementById("fab-add").addEventListener("click", () => window.__app.openProductModal());
  document.getElementById("theme-mode-btn").addEventListener("click", () => window.__app.toggleMode());

  window.__app = {
    goTab(tab) { activeTab = tab; render(); },
    openCookingMeal(meal) { mealFilter = meal; activeTab = "cooking"; render(); },
    setLoc(loc) { locFilter = loc; render(); },
    setMeal(meal) { mealFilter = meal; render(); },
    toggleReady() { readyOnlyFilter = !readyOnlyFilter; render(); },
    setTheme(t) { state.colorTheme = t; saveState(); render(); },
    toggleMode() { state.mode = state.mode === "dark" ? "light" : "dark"; saveState(); render(); },
    saveConfig() {
      state.pantryName = document.getElementById("cfg-name").value.trim() || "Despensa RD";
      state.monthlyBudgetRD = parseFloat(document.getElementById("cfg-budget").value) || 18000;
      saveState();
      toast("Ajustes guardados");
      render();
    },
    changeQty(id, delta) {
      const it = state.items.find(x => x.id === id);
      if (!it) return;
      it.qty = Math.max(0, Math.round((it.qty + delta) * 100) / 100);
      saveState();
      render();
    },
    addShopping(name, qty, unit) {
      state.shopping.unshift({ id: "s_" + Date.now() + Math.random(), name, qty, unit: unit || "ud", checked: false });
      saveState();
      toast("Añadido a la lista: " + name);
      render();
    },
    addMissingFromRecipe(recipeId) {
      const r = RECIPES.find(x => x.id === recipeId);
      if (!r) return;
      let count = 0;
      r.ingredients.forEach(ing => {
        if (!hasIngredient(ing.key)) {
          state.shopping.unshift({ id: "s_" + Date.now() + Math.random(), name: ing.text, qty: 1, unit: "ud", checked: false });
          count++;
        }
      });
      saveState();
      toast(count + " ingredientes faltantes añadidos a tu lista");
    },
    toggleShop(id) {
      const s = state.shopping.find(x => x.id === id);
      if (s) s.checked = !s.checked;
      saveState();
      render();
    },
    deleteShop(id) {
      state.shopping = state.shopping.filter(x => x.id !== id);
      saveState();
      render();
    },
    moveToPantry(id) {
      const s = state.shopping.find(x => x.id === id);
      if (!s) return;
      state.items.unshift({ id: "i_" + Date.now(), name: s.name, qty: s.qty, unit: s.unit, cat: "alimento", location: "pantry", minQty: 1, expDate: "" });
      state.shopping = state.shopping.filter(x => x.id !== id);
      saveState();
      toast("Movido a tu despensa");
      render();
    },
    openProductModal(editId) {
      const item = editId ? state.items.find(x => x.id === editId) : null;
      showModal(`
        <h2>${item ? "Editar producto" : "Subir producto manual"}</h2>
        <form id="prod-form">
          <label>Nombre
            <input id="pf-name" required value="${esc(item ? item.name : "")}" placeholder="Ej. Arroz, huevos, cloro...">
          </label>
          <div class="grid-2">
            <label>Cantidad
              <input id="pf-qty" type="number" step="0.5" value="${item ? item.qty : 1}">
            </label>
            <label>Unidad
              <select id="pf-unit">
                ${["ud", "lb", "kg", "l", "paq", "bot"].map(u => `<option ${item && item.unit === u ? "selected" : ""}>${u}</option>`).join("")}
              </select>
            </label>
          </div>
          <div class="grid-2">
            <label>Categoría
              <select id="pf-cat">
                ${Object.entries(CATEGORIES).map(([k, v]) => `<option value="${k}" ${item && item.cat === k ? "selected" : ""}>${v.emoji} ${v.label}</option>`).join("")}
              </select>
            </label>
            <label>Ubicación
              <select id="pf-loc">
                ${Object.entries(LOCATIONS).map(([k, v]) => `<option value="${k}" ${item && item.location === k ? "selected" : ""}>${v.emoji} ${v.label}</option>`).join("")}
              </select>
            </label>
          </div>
          <div class="grid-2">
            <label>Mínimo aviso
              <input id="pf-min" type="number" step="0.5" value="${item ? item.minQty : 1}">
            </label>
            <label>Vencimiento (opcional)
              <input id="pf-exp" type="date" value="${esc(item ? item.expDate : "")}">
            </label>
          </div>
          <div class="row" style="margin-top:16px;justify-content:flex-end">
            ${item ? `<button class="btn danger" type="button" onclick="window.__app.deleteProduct('${item.id}')">Eliminar</button>` : ""}
            <button class="btn" type="button" onclick="window.__app.closeModal()">Cancelar</button>
            <button class="btn primary" type="submit">Guardar</button>
          </div>
        </form>
      `);
      document.getElementById("prod-form").addEventListener("submit", e => {
        e.preventDefault();
        const data = {
          id: item ? item.id : "i_" + Date.now(),
          name: document.getElementById("pf-name").value.trim(),
          qty: parseFloat(document.getElementById("pf-qty").value) || 1,
          unit: document.getElementById("pf-unit").value,
          cat: document.getElementById("pf-cat").value,
          location: document.getElementById("pf-loc").value,
          minQty: parseFloat(document.getElementById("pf-min").value) || 0,
          expDate: document.getElementById("pf-exp").value
        };
        if (!data.name) return;
        if (item) {
          const idx = state.items.findIndex(x => x.id === item.id);
          if (idx >= 0) state.items[idx] = data;
        } else {
          state.items.unshift(data);
        }
        saveState();
        closeModal();
        toast("Guardado en despensa");
        render();
      });
    },
    deleteProduct(id) {
      state.items = state.items.filter(x => x.id !== id);
      saveState();
      closeModal();
      toast("Producto eliminado");
      render();
    },
    openBatchModal() {
      showModal(`
        <h2>📋 Pegar lista o factura</h2>
        <p class="muted">Escribe o pega un producto por línea (ej. "Arroz 5 lb", "2 Leche", "Huevos 12 ud").</p>
        <textarea id="batch-text" rows="6" placeholder="Arroz Selecto 5 lb&#10;Huevos 12 ud&#10;Leche 2 l"></textarea>
        <div class="row" style="margin-top:12px;justify-content:flex-end">
          <button class="btn" type="button" onclick="window.__app.closeModal()">Cancelar</button>
          <button class="btn primary" type="button" onclick="window.__app.importBatch()">Agregar productos</button>
        </div>
      `);
    },
    importBatch() {
      const lines = (document.getElementById("batch-text").value || "").split(/\r?\n/).map(l => l.trim()).filter(Boolean);
      let added = 0;
      lines.forEach(line => {
        const m = line.match(/^(\d+(?:[.,]\d+)?)\s+(.+)$/) || line.match(/^(.+?)\s+(\d+(?:[.,]\d+)?)\s*(lb|kg|ud|l|paq|bot)?$/i);
        let name = line, qty = 1, unit = "ud";
        if (m && !isNaN(parseFloat(m[1]))) {
          qty = parseFloat(m[1].replace(",", "."));
          name = m[2].trim();
        } else if (m && !isNaN(parseFloat(m[2]))) {
          name = m[1].trim();
          qty = parseFloat(m[2].replace(",", "."));
          unit = (m[3] || "ud").toLowerCase();
        }
        state.items.unshift({ id: "i_" + Date.now() + Math.random(), name, qty, unit, cat: "alimento", location: "pantry", minQty: 1, expDate: "" });
        added++;
      });
      saveState();
      closeModal();
      toast(added + " productos agregados");
      activeTab = "pantry";
      render();
    },
    exportJson() {
      const json = JSON.stringify(state, null, 2);
      navigator.clipboard?.writeText(json);
      toast("Respaldo JSON copiado al portapapeles");
    },
    openImportModal() {
      showModal(`
        <h2>Importar respaldo JSON</h2>
        <textarea id="imp-json" rows="6" placeholder="Pega el JSON aquí..."></textarea>
        <div class="row" style="margin-top:12px;justify-content:flex-end">
          <button class="btn" type="button" onclick="window.__app.closeModal()">Cancelar</button>
          <button class="btn primary" type="button" onclick="window.__app.doImportJson()">Restaurar</button>
        </div>
      `);
    },
    doImportJson() {
      try {
        const parsed = JSON.parse(document.getElementById("imp-json").value);
        if (Array.isArray(parsed.items)) {
          state = { ...structuredClone(DEFAULT_STATE), ...parsed };
          saveState();
          closeModal();
          toast("Respaldo restaurado");
          render();
        } else {
          toast("JSON inválido");
        }
      } catch {
        toast("Error leyendo JSON");
      }
    },
    resetDemo() {
      state = structuredClone(DEFAULT_STATE);
      saveState();
      toast("Datos restablecidos");
      render();
    },
    closeModal
  };

  render();
})();
