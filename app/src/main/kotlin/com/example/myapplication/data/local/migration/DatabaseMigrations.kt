package com.example.myapplication.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4
    )

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 添加账户表的archived字段
            database.execSQL("""
                ALTER TABLE accounts 
                ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0
            """)

            // 添加分类表的icon字段
            database.execSQL("""
                ALTER TABLE categories 
                ADD COLUMN icon TEXT NOT NULL DEFAULT '📝'
            """)
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建预算表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    categoryId INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    startDate TEXT NOT NULL,
                    endDate TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL,
                    FOREIGN KEY (categoryId) REFERENCES categories (id)
                    ON DELETE CASCADE
                )
            """)

            // 创建预算使用记录表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS budget_usages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    budgetId INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    FOREIGN KEY (budgetId) REFERENCES budgets (id)
                    ON DELETE CASCADE
                )
            """)

            // 添加索引
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_budgets_categoryId 
                ON budgets (categoryId)
            """)
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_budget_usages_budgetId 
                ON budget_usages (budgetId)
            """)
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建标签表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS tags (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    color INTEGER NOT NULL,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL
                )
            """)

            // 创建交易-标签关联表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS transaction_tags (
                    transactionId INTEGER NOT NULL,
                    tagId INTEGER NOT NULL,
                    PRIMARY KEY (transactionId, tagId),
                    FOREIGN KEY (transactionId) REFERENCES transactions (id)
                    ON DELETE CASCADE,
                    FOREIGN KEY (tagId) REFERENCES tags (id)
                    ON DELETE CASCADE
                )
            """)

            // 添加索引
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_tags_name 
                ON tags (name)
            """)
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_transaction_tags_transactionId 
                ON transaction_tags (transactionId)
            """)
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_transaction_tags_tagId 
                ON transaction_tags (tagId)
            """)
        }
    }

    // 数据库验证
    fun validateDatabase(database: SupportSQLiteDatabase) {
        validateTables(database)
        validateColumns(database)
        validateIndexes(database)
        validateForeignKeys(database)
    }

    private fun validateTables(database: SupportSQLiteDatabase) {
        val requiredTables = listOf(
            "accounts",
            "categories",
            "transactions",
            "budgets",
            "budget_usages",
            "tags",
            "transaction_tags"
        )

        val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table'")
        val existingTables = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                existingTables.add(it.getString(0))
            }
        }

        val missingTables = requiredTables - existingTables.toSet()
        if (missingTables.isNotEmpty()) {
            throw IllegalStateException("Missing tables: $missingTables")
        }
    }

    private fun validateColumns(database: SupportSQLiteDatabase) {
        // 验证accounts表的列
        validateTableColumns(
            database,
            "accounts",
            listOf(
                "id",
                "name",
                "type",
                "balance",
                "isArchived",
                "createdAt",
                "updatedAt"
            )
        )

        // 验证categories表的列
        validateTableColumns(
            database,
            "categories",
            listOf(
                "id",
                "name",
                "type",
                "icon",
                "parentId",
                "createdAt",
                "updatedAt"
            )
        )

        // 验证transactions表的列
        validateTableColumns(
            database,
            "transactions",
            listOf(
                "id",
                "type",
                "amount",
                "accountId",
                "categoryId",
                "date",
                "note",
                "createdAt",
                "updatedAt"
            )
        )
    }

    private fun validateTableColumns(
        database: SupportSQLiteDatabase,
        table: String,
        requiredColumns: List<String>
    ) {
        val cursor = database.query("PRAGMA table_info($table)")
        val existingColumns = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                existingColumns.add(it.getString(1))
            }
        }

        val missingColumns = requiredColumns - existingColumns.toSet()
        if (missingColumns.isNotEmpty()) {
            throw IllegalStateException("Missing columns in $table: $missingColumns")
        }
    }

    private fun validateIndexes(database: SupportSQLiteDatabase) {
        val requiredIndexes = listOf(
            "index_budgets_categoryId",
            "index_budget_usages_budgetId",
            "index_tags_name",
            "index_transaction_tags_transactionId",
            "index_transaction_tags_tagId"
        )

        val cursor = database.query("SELECT name FROM sqlite_master WHERE type='index'")
        val existingIndexes = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                existingIndexes.add(it.getString(0))
            }
        }

        val missingIndexes = requiredIndexes - existingIndexes.toSet()
        if (missingIndexes.isNotEmpty()) {
            throw IllegalStateException("Missing indexes: $missingIndexes")
        }
    }

    private fun validateForeignKeys(database: SupportSQLiteDatabase) {
        // 验证外键约束
        database.execSQL("PRAGMA foreign_key_check")
    }
} 