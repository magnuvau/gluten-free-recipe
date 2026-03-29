package no.oslo.torshov.pfb.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import no.oslo.torshov.pfb.data.model.DayNote
import no.oslo.torshov.pfb.data.model.Recipe
import no.oslo.torshov.pfb.data.model.RecipeCategory
import no.oslo.torshov.pfb.data.model.RecipeExperience

@Database(entities = [Recipe::class, DayNote::class, RecipeExperience::class], version = 5, exportSchema = false)
@TypeConverters(RecipeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun dayNoteDao(): DayNoteDao
    abstract fun recipeExperienceDao(): RecipeExperienceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `day_notes` (`date` TEXT NOT NULL, `text` TEXT NOT NULL, PRIMARY KEY(`date`))"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recipes ADD COLUMN category TEXT NOT NULL DEFAULT '${RecipeCategory.OTHER}'"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recipes ADD COLUMN tested INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `recipe_experiences` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `recipeId` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `note` TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON DELETE CASCADE
                    )"""
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_recipe_experiences_recipeId` ON `recipe_experiences` (`recipeId`)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { INSTANCE = it }
            }
    }
}
