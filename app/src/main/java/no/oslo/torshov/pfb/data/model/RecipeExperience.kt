package no.oslo.torshov.pfb.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_experiences",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("recipeId")]
)
data class RecipeExperience(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val date: String,   // "YYYY-MM-DD"
    val note: String = ""
)
