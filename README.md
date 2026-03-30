# Gluten-Free Recipe

An Android app for managing gluten-free recipes, built with Kotlin and MVVM architecture.

## Features

- **Recipe management** — add, edit, rename and delete recipes
- **Ingredients & steps** — inline editing with long-press drag-to-reorder
- **Categories** — filter recipes by type (Bread, Flatbread, Cakes, Cookies, Buns, Rolls, Scones, Muffins, Waffles, Pancakes, Other); localised based on device language
- **Sorted recipes** — recipes sorted alphabetically, emojis in names ignored when sorting
- **Thickener tabs** — separate views for recipes with (E400–E499) and without thickeners
- **Tips & common mistakes** — freetext tabs per recipe
- **Tested tracking** — long-press a recipe to mark it as tested; shown with a checkmark icon
- **Erfaringer (Experiences)** — log notes per bake with date; accessible from recipe menu or recipe list icon; marking a recipe as tested is automatic when the first experience is added
- **Calendar** — browse experiences by date; tap a day to see all experiences registered that day
- **Export / Import** — share recipes as JSON or PDF
- **Delete all** — clear all recipes at once (with confirmation)
- **Localisation** — Norwegian (Bokmål) and English, based on device language
- **Bundled recipes** — 21 pre-loaded recipes, sorted by category, restored automatically if deleted

## Tech Stack

- Kotlin
- MVVM (ViewModel + LiveData)
- Room (local database, v6)
- ViewPager2 + TabLayout
- Material Design 3
- KSP (Kotlin Symbol Processing)
- kizitonwose/calendar-view 2.5.0

## Package Structure

```
no.oslo.torshov.pfb
├── data
│   ├── db          # Room database, DAOs, migrations, type converters
│   ├── model       # Entity classes (Recipe, DayNote, RecipeCategory, RecipeExperience)
│   └── repository  # RecipeRepository, RecipeJsonSerializer, RecipePdfExporter
└── ui
    ├── adapter     # RecyclerView and ViewPager adapters
    ├── fragment    # List, freetext, calendar fragments
    ├── viewmodel   # MainViewModel, RecipeDetailViewModel
    └── (root)      # MainActivity, RecipeDetailActivity, ExperiencesActivity,
                    # DateExperiencesActivity, CalendarActivity
```

## Recipe JSON Format

Recipes can be imported/exported as JSON:

```json
{
  "version": 1,
  "recipes": [
    {
      "name": "Recipe name",
      "category": "bread",
      "ingredients": ["ingredient 1", "ingredient 2"],
      "steps": ["step 1", "step 2"],
      "tips": ["tip 1"],
      "commonMistakes": ["common mistake 1"]
    }
  ]
}
```

> **Note:** `category` is stored as a stable English key (e.g. `bread`, `cakes`, `waffles`). Display names are resolved from string resources based on device locale.

## Bundled Recipes

Recipes in `app/src/main/res/raw/bundled_recipes.json` are automatically loaded on launch. A recipe is only added if no existing recipe has the same name and ingredients, so user edits are never overwritten.

## License

See [LICENSE](LICENSE).
