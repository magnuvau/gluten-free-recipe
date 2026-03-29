package no.oslo.torshov.pfb.data.db

import androidx.room.*
import no.oslo.torshov.pfb.data.model.RecipeExperience

@Dao
interface RecipeExperienceDao {

    @Query("SELECT * FROM recipe_experiences WHERE recipeId = :recipeId ORDER BY date DESC")
    suspend fun getForRecipe(recipeId: Long): List<RecipeExperience>

    @Query("SELECT DISTINCT recipeId FROM recipe_experiences")
    suspend fun getRecipeIdsWithExperiences(): List<Long>

    @Query("SELECT DISTINCT date FROM recipe_experiences")
    suspend fun getAllExperienceDates(): List<String>

    @Query("SELECT * FROM recipe_experiences WHERE date = :date ORDER BY recipeId")
    suspend fun getByDate(date: String): List<RecipeExperience>

    @Insert
    suspend fun insert(experience: RecipeExperience)

    @Update
    suspend fun update(experience: RecipeExperience)

    @Delete
    suspend fun delete(experience: RecipeExperience)
}
