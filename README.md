# Spring Config Visualizer – Felhasználói Kézikönyv

A **Spring Config Visualizer** egy Eclipse IDE bővítmény, amely megkönnyíti a Spring Framework `@Configuration` és `@Import` annotációi által alkotott függőségi hálózatok feltérképezését, vizualizációját és validációját. Kifejezetten a többmodulos (multi-module) projektarchitektúrák átláthatóságát támogatja.

---

## Fő funkciók

* **Gyors indítás helyi menüből:** Közvetlen elemzés indítása bármely `*Config.java` fájlból.
* **Kétirányú feltárás:** Irányított (lefelé történő) vagy teljes workspace-szintű (fel- és lefele) bejárás.
* **Multi-moduláris támogatás:** A modulok közötti függőségeket is automatikusan felkutatja a Workspace-en belül.
* **Dupla nézet (Gráf és Fa):** Vizuális hálózati gráf (Eclipse Zest) és kinyitható struktúra fa.
* **In-memory validáció:**
* Cirkuláris `@Import` hivatkozások kiszűrése.
* Duplikált / felülírt `@Bean` deklarációk detektálása a hierarchiában.


* **Kódnavigáció:** Dupla kattintással az érintett `.java` forrásfájl azonnal megnyílik a szerkesztőben.

---

## Használati útmutató

### 1. Elemzés indítása

1. A **Package Explorer** vagy **Project Explorer** nézetben kattints jobb gombbal egy `*Config.java` fájlra.
2. Válassza a **Show config tree** opciót a helyi menüből.

### 2. Felfedezési mód kiválasztása

A megjelenő ablakban válaszd ki a kívánt elemzési hatókört:

| Mód | Leírás |
| --- | --- |
| **Show below** *(Lefelé feltárás)* | A kiválasztott fájlból kiindulva rekurzívan bejárja az `@Import({...})` annotációkban megadott `@Configuration` osztályokat. |
| **Show in workspace** *(Teljes bejárás)* | Végrehajtja a lefelé feltárást, majd átvizsgálja a Workspace összes konfigurációját, és megkeresi azokat is, amelyek importálják a kiválasztott osztályt (felfelé mutató lánc). |

> **Megjegyzés:** A keresés kizárólag a Workspace-ben található `.java` forrásfájlokra terjed ki, a fordított külső JAR/AAR függőségeket a rendszer nem vizsgálja. (Még)

### 3. Az eredmények értelmezése (ViewPart)

Az elemzés eredménye a **Spring Config Visualizer** dedikált nézetben jelenik meg (minden új elemzés felülírja a korábbit).

#### Nézetváltás és opciók

A jobb felső sarokban található kapcsolókkal az alábbi lehetőségek érhetők el:

* **Graph view:** Zest-alapú vizuális hálózati gráf (irányított nyilakkal).
* **Structured view:** Fa struktúra (Tree View), ahol a kiválasztott konfiguráció a gyökér.
* **Show details (Ki/Be kapcsolható):** Be kapcsolásakor a bejegyzések mellett megjelenik a státuszuk: `[OK]`, `[CIRCULAR]`, vagy `[INVALID_BEAN]`.

#### Bean Részletező Panel

Ha a **Show bean details** opció be van kapcsolva, a nézet alján megjelenik a **Bean Panel**. Bármelyik csomópontra kattintva láthatóvá válnak az abban deklarált Spring bean-ek.

---

## Validáció és Színezési Szabályok

A bővítmény az alábbi jelzésekkel segít a hibák gyors azonosításában:

```
[ Normál konfiguráció / Bean ]   --->  Zöld / Alapértelmezett szín
[ Cirkuláris függőség ]          --->  Piros (Csomópont és érintett útvonal)
[ Duplikált / Hibás Bean ]       --->  Piros (Konfigurációs útvonal és a Bean neve)

```

1. **Cirkuláris Függőség (`[CIRCULAR]`):** Kizárólag az a csomópont és útvonal vált pirosra, amely közvetlenül részese a körkörös `@Import` hivatkozásnak.
2. **Bean Duplikáció (`[INVALID_BEAN]`):** Ha egy szülő konfigurációban deklarált bean újra deklarálásra kerül egy alatta lévő, behúzott konfigurációban, a rendszer hibaként kezeli. Az alsó panelben a konfliktusban érintett bean **piros** színnel jelenik meg.

---

## Projektstruktúra és Komponensek

Fejlesztőknek és karbantartóknak szóló áttekintés a modulokról:

```
src/main/
├── core/                       # Maglogika és kódábrázolás
│   ├── GraphBuilderService.java    # Gráfépítés és import-hálózat összefűzése
│   ├── SpringConfigAstVisitor.java # AST elemző (@Configuration, @Import, @Bean)
│   └── WorkspaceConfigSearcher.java# JDT Search Engine integráció
├── model/                      # Adatmodell
│   ├── BeanModel.java              # Bean deklarációk és validációs állapot
│   ├── ConfigGraph.java / Node / Edge # Gráf adatszerkezetek
├── validation/                 # Statikus validáció
│   ├── CycleDetector.java          # Körkörös importok kiszűrése
│   └── BeanValidator.java          # Duplikált bean-ek ellenőrzése
└── ui/                         # Eclipse UI komponensek
    ├── dialogs/DiscoveryModeDialog.java # Módválasztó ablak
    ├── handlers/Handlers.java           # Context menu eseménykezelő
    ├── views/SpringConfigViewPart.java  # Fő nézet és vezérlők
    └── views/components/                # ZestGraph és StructuredTree nézetek

```
