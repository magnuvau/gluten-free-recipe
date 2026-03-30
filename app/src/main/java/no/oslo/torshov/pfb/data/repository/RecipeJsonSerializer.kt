package no.oslo.torshov.pfb.data.repository

import no.oslo.torshov.pfb.data.model.Recipe
import no.oslo.torshov.pfb.data.model.RecipeCategory
import no.oslo.torshov.pfb.data.model.RecipeExperience
import org.json.JSONArray
import org.json.JSONObject

object RecipeJsonSerializer {

    private const val VERSION = 1

    fun toJson(recipes: List<Recipe>): String {
        val root = JSONObject()
        root.put("version", VERSION)
        val array = JSONArray()
        for (recipe in recipes) {
            array.put(JSONObject().apply {
                put("name", recipe.name)
                put("emoji", recipe.emoji)
                put("category", recipe.category)
                put("ingredients", JSONArray(recipe.ingredients))
                put("steps", JSONArray(recipe.steps))
                put("tips", JSONArray(recipe.tips))
                put("commonMistakes", JSONArray(recipe.commonMistakes))
            })
        }
        root.put("recipes", array)
        return root.toString(2)
    }

    fun fromJson(json: String): List<Recipe> {
        val array = JSONObject(json).getJSONArray("recipes")
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            Recipe(
                name = obj.getString("name"),
                emoji = obj.optString("emoji", ""),
                category = obj.optString("category", RecipeCategory.OTHER),
                ingredients = obj.getJSONArray("ingredients").toMutableStringList(),
                steps = obj.getJSONArray("steps").toMutableStringList(),
                tips = obj.getJSONArray("tips").toMutableStringList(),
                commonMistakes = obj.getJSONArray("commonMistakes").toMutableStringList()
            )
        }
    }

    fun isSyncJson(json: String): Boolean =
        runCatching { JSONObject(json).optBoolean("sync", false) }.getOrDefault(false)

    fun toSyncJson(
        recipes: List<Recipe>,
        experiencesByRecipeId: Map<Long, List<RecipeExperience>>
    ): String {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("sync", true)
        val array = JSONArray()
        for (recipe in recipes) {
            array.put(JSONObject().apply {
                put("name", recipe.name)
                put("emoji", recipe.emoji)
                put("category", recipe.category)
                put("tested", recipe.tested)
                put("favourite", recipe.favourite)
                put("ingredients", JSONArray(recipe.ingredients))
                put("steps", JSONArray(recipe.steps))
                put("tips", JSONArray(recipe.tips))
                put("commonMistakes", JSONArray(recipe.commonMistakes))
                val expArray = JSONArray()
                experiencesByRecipeId[recipe.id]?.forEach { exp ->
                    expArray.put(JSONObject().apply {
                        put("date", exp.date)
                        put("note", exp.note)
                    })
                }
                put("experiences", expArray)
            })
        }
        root.put("recipes", array)
        return root.toString(2)
    }

    data class SyncData(
        val recipes: List<Recipe>,
        val experiencesByRecipeName: Map<String, List<Pair<String, String>>> // name -> [(date, note)]
    )

    fun fromSyncJson(json: String): SyncData {
        val array = JSONObject(json).getJSONArray("recipes")
        val recipes = mutableListOf<Recipe>()
        val experiencesByRecipeName = mutableMapOf<String, List<Pair<String, String>>>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.getString("name")
            recipes.add(Recipe(
                name = name,
                emoji = obj.optString("emoji", ""),
                category = obj.optString("category", RecipeCategory.OTHER),
                tested = obj.optBoolean("tested", false),
                favourite = obj.optBoolean("favourite", false),
                ingredients = obj.getJSONArray("ingredients").toMutableStringList(),
                steps = obj.getJSONArray("steps").toMutableStringList(),
                tips = obj.getJSONArray("tips").toMutableStringList(),
                commonMistakes = obj.getJSONArray("commonMistakes").toMutableStringList()
            ))
            val expArray = obj.optJSONArray("experiences") ?: JSONArray()
            experiencesByRecipeName[name] = List(expArray.length()) { j ->
                val e = expArray.getJSONObject(j)
                Pair(e.getString("date"), e.optString("note", ""))
            }
        }
        return SyncData(recipes, experiencesByRecipeName)
    }

    private fun JSONArray.toMutableStringList(): MutableList<String> =
        MutableList(length()) { getString(it) }
}
