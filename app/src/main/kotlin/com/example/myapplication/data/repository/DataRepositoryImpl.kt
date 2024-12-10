package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.BudgetDao
import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.DataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) : DataRepository {

    private val exportProgress = MutableStateFlow(0)
    private val importProgress = MutableStateFlow(0)

    override suspend fun exportToCSV(uri: Uri): Result<Unit> {
        return try {
            val data = getAllData().getOrThrow()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    // Write transactions
                    writer.write("Transactions\n")
                    writer.write("id,amount,note,categoryId,accountId,date,type,tags,location,createdAt,updatedAt\n")
                    data.transactions.forEachIndexed { index, transaction ->
                        writer.write("${transaction.id},${transaction.amount},${transaction.note},${transaction.categoryId},${transaction.accountId},${transaction.date},${transaction.type},${transaction.tags.joinToString("|")},${transaction.location},${transaction.createdAt},${transaction.updatedAt}\n")
                        exportProgress.value = (index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
                    }

                    // Write accounts
                    writer.write("\nAccounts\n")
                    writer.write("id,name,type,balance,currency,icon,color,note,isArchived,createdAt,updatedAt\n")
                    data.accounts.forEachIndexed { index, account ->
                        writer.write("${account.id},${account.name},${account.type},${account.balance},${account.currency},${account.icon},${account.color},${account.note},${account.isArchived},${account.createdAt},${account.updatedAt}\n")
                        exportProgress.value = (data.transactions.size + index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
                    }

                    // Write categories
                    writer.write("\nCategories\n")
                    writer.write("id,name,type,icon,color,parentId,order,isArchived,createdAt,updatedAt\n")
                    data.categories.forEachIndexed { index, category ->
                        writer.write("${category.id},${category.name},${category.type},${category.icon},${category.color},${category.parentId},${category.order},${category.isArchived},${category.createdAt},${category.updatedAt}\n")
                        exportProgress.value = (data.transactions.size + data.accounts.size + index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
                    }

                    // Write budgets
                    writer.write("\nBudgets\n")
                    writer.write("id,name,amount,categoryId,startDate,endDate,notifyThreshold,isEnabled,isRepeating,repeatInterval,createdAt,updatedAt\n")
                    data.budgets.forEachIndexed { index, budget ->
                        writer.write("${budget.id},${budget.name},${budget.amount},${budget.categoryId},${budget.startDate},${budget.endDate},${budget.notifyThreshold},${budget.isEnabled},${budget.isRepeating},${budget.repeatInterval},${budget.createdAt},${budget.updatedAt}\n")
                        exportProgress.value = (data.transactions.size + data.accounts.size + data.categories.size + index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
                    }
                }
            } ?: throw ExportError.FileCreationError("Failed to create output stream")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ExportError.WriteError(e.message ?: "Unknown error"))
        } finally {
            exportProgress.value = 0
        }
    }

    override suspend fun importFromCSV(uri: Uri): Result<ImportResult> {
        return try {
            var currentSection = ""
            val transactions = mutableListOf<Transaction>()
            val accounts = mutableListOf<Account>()
            val categories = mutableListOf<Category>()
            val budgets = mutableListOf<Budget>()
            var totalLines = 0

            // First pass: count total lines
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    while (reader.readLine() != null) {
                        totalLines++
                    }
                }
            }

            // Second pass: parse data
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var currentLine = 0
                    reader.forEachLine { line ->
                        currentLine++
                        importProgress.value = currentLine * 100 / totalLines

                        when {
                            line.startsWith("Transactions") -> currentSection = "transactions"
                            line.startsWith("Accounts") -> currentSection = "accounts"
                            line.startsWith("Categories") -> currentSection = "categories"
                            line.startsWith("Budgets") -> currentSection = "budgets"
                            line.contains(",") -> {
                                val values = line.split(",")
                                when (currentSection) {
                                    "transactions" -> if (values.size >= 11) {
                                        transactions.add(parseTransaction(values))
                                    }
                                    "accounts" -> if (values.size >= 11) {
                                        accounts.add(parseAccount(values))
                                    }
                                    "categories" -> if (values.size >= 10) {
                                        categories.add(parseCategory(values))
                                    }
                                    "budgets" -> if (values.size >= 12) {
                                        budgets.add(parseBudget(values))
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: throw ImportError.InvalidFormat("Failed to read input stream")

            val data = ExportData(
                transactions = transactions,
                accounts = accounts,
                categories = categories,
                budgets = budgets
            )

            importData(data).getOrThrow()
        } catch (e: Exception) {
            Result.failure(ImportError.InvalidFormat(e.message ?: "Unknown error"))
        } finally {
            importProgress.value = 0
        }
    }

    override suspend fun exportToExcel(uri: Uri): Result<Unit> {
        return try {
            val data = getAllData().getOrThrow()
            val workbook = XSSFWorkbook()

            // Transactions sheet
            val transactionsSheet = workbook.createSheet("Transactions")
            val transactionsHeader = transactionsSheet.createRow(0)
            transactionsHeader.createCell(0).setCellValue("ID")
            transactionsHeader.createCell(1).setCellValue("Amount")
            transactionsHeader.createCell(2).setCellValue("Note")
            transactionsHeader.createCell(3).setCellValue("Category ID")
            transactionsHeader.createCell(4).setCellValue("Account ID")
            transactionsHeader.createCell(5).setCellValue("Date")
            transactionsHeader.createCell(6).setCellValue("Type")
            transactionsHeader.createCell(7).setCellValue("Tags")
            transactionsHeader.createCell(8).setCellValue("Location")
            transactionsHeader.createCell(9).setCellValue("Created At")
            transactionsHeader.createCell(10).setCellValue("Updated At")

            data.transactions.forEachIndexed { index, transaction ->
                val row = transactionsSheet.createRow(index + 1)
                row.createCell(0).setCellValue(transaction.id.toDouble())
                row.createCell(1).setCellValue(transaction.amount)
                row.createCell(2).setCellValue(transaction.note)
                row.createCell(3).setCellValue(transaction.categoryId.toDouble())
                row.createCell(4).setCellValue(transaction.accountId.toDouble())
                row.createCell(5).setCellValue(transaction.date.toString())
                row.createCell(6).setCellValue(transaction.type.name)
                row.createCell(7).setCellValue(transaction.tags.joinToString("|"))
                row.createCell(8).setCellValue(transaction.location ?: "")
                row.createCell(9).setCellValue(transaction.createdAt.toString())
                row.createCell(10).setCellValue(transaction.updatedAt.toString())
                exportProgress.value = (index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
            }

            // Accounts sheet
            val accountsSheet = workbook.createSheet("Accounts")
            val accountsHeader = accountsSheet.createRow(0)
            accountsHeader.createCell(0).setCellValue("ID")
            accountsHeader.createCell(1).setCellValue("Name")
            accountsHeader.createCell(2).setCellValue("Type")
            accountsHeader.createCell(3).setCellValue("Balance")
            accountsHeader.createCell(4).setCellValue("Currency")
            accountsHeader.createCell(5).setCellValue("Icon")
            accountsHeader.createCell(6).setCellValue("Color")
            accountsHeader.createCell(7).setCellValue("Note")
            accountsHeader.createCell(8).setCellValue("Is Archived")
            accountsHeader.createCell(9).setCellValue("Created At")
            accountsHeader.createCell(10).setCellValue("Updated At")

            data.accounts.forEachIndexed { index, account ->
                val row = accountsSheet.createRow(index + 1)
                row.createCell(0).setCellValue(account.id.toDouble())
                row.createCell(1).setCellValue(account.name)
                row.createCell(2).setCellValue(account.type.name)
                row.createCell(3).setCellValue(account.balance)
                row.createCell(4).setCellValue(account.currency)
                row.createCell(5).setCellValue(account.icon)
                row.createCell(6).setCellValue(account.color)
                row.createCell(7).setCellValue(account.note)
                row.createCell(8).setCellValue(account.isArchived)
                row.createCell(9).setCellValue(account.createdAt.toString())
                row.createCell(10).setCellValue(account.updatedAt.toString())
                exportProgress.value = (data.transactions.size + index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
            }

            // Categories sheet
            val categoriesSheet = workbook.createSheet("Categories")
            val categoriesHeader = categoriesSheet.createRow(0)
            categoriesHeader.createCell(0).setCellValue("ID")
            categoriesHeader.createCell(1).setCellValue("Name")
            categoriesHeader.createCell(2).setCellValue("Type")
            categoriesHeader.createCell(3).setCellValue("Icon")
            categoriesHeader.createCell(4).setCellValue("Color")
            categoriesHeader.createCell(5).setCellValue("Parent ID")
            categoriesHeader.createCell(6).setCellValue("Order")
            categoriesHeader.createCell(7).setCellValue("Is Archived")
            categoriesHeader.createCell(8).setCellValue("Created At")
            categoriesHeader.createCell(9).setCellValue("Updated At")

            data.categories.forEachIndexed { index, category ->
                val row = categoriesSheet.createRow(index + 1)
                row.createCell(0).setCellValue(category.id.toDouble())
                row.createCell(1).setCellValue(category.name)
                row.createCell(2).setCellValue(category.type.name)
                row.createCell(3).setCellValue(category.icon)
                row.createCell(4).setCellValue(category.color)
                row.createCell(5).setCellValue(category.parentId?.toDouble() ?: 0.0)
                row.createCell(6).setCellValue(category.order.toDouble())
                row.createCell(7).setCellValue(category.isArchived)
                row.createCell(8).setCellValue(category.createdAt.toString())
                row.createCell(9).setCellValue(category.updatedAt.toString())
                exportProgress.value = (data.transactions.size + data.accounts.size + index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
            }

            // Budgets sheet
            val budgetsSheet = workbook.createSheet("Budgets")
            val budgetsHeader = budgetsSheet.createRow(0)
            budgetsHeader.createCell(0).setCellValue("ID")
            budgetsHeader.createCell(1).setCellValue("Name")
            budgetsHeader.createCell(2).setCellValue("Amount")
            budgetsHeader.createCell(3).setCellValue("Category ID")
            budgetsHeader.createCell(4).setCellValue("Start Date")
            budgetsHeader.createCell(5).setCellValue("End Date")
            budgetsHeader.createCell(6).setCellValue("Notify Threshold")
            budgetsHeader.createCell(7).setCellValue("Is Enabled")
            budgetsHeader.createCell(8).setCellValue("Is Repeating")
            budgetsHeader.createCell(9).setCellValue("Repeat Interval")
            budgetsHeader.createCell(10).setCellValue("Created At")
            budgetsHeader.createCell(11).setCellValue("Updated At")

            data.budgets.forEachIndexed { index, budget ->
                val row = budgetsSheet.createRow(index + 1)
                row.createCell(0).setCellValue(budget.id.toDouble())
                row.createCell(1).setCellValue(budget.name)
                row.createCell(2).setCellValue(budget.amount)
                row.createCell(3).setCellValue(budget.categoryId?.toDouble() ?: 0.0)
                row.createCell(4).setCellValue(budget.startDate.toString())
                row.createCell(5).setCellValue(budget.endDate.toString())
                row.createCell(6).setCellValue(budget.notifyThreshold)
                row.createCell(7).setCellValue(budget.isEnabled)
                row.createCell(8).setCellValue(budget.isRepeating)
                row.createCell(9).setCellValue(budget.repeatInterval?.name ?: "")
                row.createCell(10).setCellValue(budget.createdAt.toString())
                row.createCell(11).setCellValue(budget.updatedAt.toString())
                exportProgress.value = (data.transactions.size + data.accounts.size + data.categories.size + index + 1) * 100 / (data.transactions.size + data.accounts.size + data.categories.size + data.budgets.size)
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
            } ?: throw ExportError.FileCreationError("Failed to create output stream")

            workbook.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ExportError.WriteError(e.message ?: "Unknown error"))
        } finally {
            exportProgress.value = 0
        }
    }

    override suspend fun importFromExcel(uri: Uri): Result<ImportResult> {
        return try {
            val transactions = mutableListOf<Transaction>()
            val accounts = mutableListOf<Account>()
            val categories = mutableListOf<Category>()
            val budgets = mutableListOf<Budget>()
            var totalRows = 0

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)

                // Count total rows
                totalRows += workbook.getSheet("Transactions")?.physicalNumberOfRows ?: 0
                totalRows += workbook.getSheet("Accounts")?.physicalNumberOfRows ?: 0
                totalRows += workbook.getSheet("Categories")?.physicalNumberOfRows ?: 0
                totalRows += workbook.getSheet("Budgets")?.physicalNumberOfRows ?: 0

                var currentRow = 0

                // Parse transactions
                workbook.getSheet("Transactions")?.let { sheet ->
                    for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                        currentRow++
                        importProgress.value = currentRow * 100 / totalRows
                        val row = sheet.getRow(rowIndex)
                        transactions.add(
                            Transaction(
                                id = row.getCell(0).numericCellValue.toLong(),
                                amount = row.getCell(1).numericCellValue,
                                note = row.getCell(2).stringCellValue,
                                categoryId = row.getCell(3).numericCellValue.toLong(),
                                accountId = row.getCell(4).numericCellValue.toLong(),
                                date = LocalDateTime.parse(row.getCell(5).stringCellValue),
                                type = TransactionType.valueOf(row.getCell(6).stringCellValue),
                                tags = row.getCell(7).stringCellValue.split("|").filter { it.isNotEmpty() },
                                location = row.getCell(8).stringCellValue.takeIf { it.isNotEmpty() },
                                createdAt = LocalDateTime.parse(row.getCell(9).stringCellValue),
                                updatedAt = LocalDateTime.parse(row.getCell(10).stringCellValue)
                            )
                        )
                    }
                }

                // Parse accounts
                workbook.getSheet("Accounts")?.let { sheet ->
                    for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                        currentRow++
                        importProgress.value = currentRow * 100 / totalRows
                        val row = sheet.getRow(rowIndex)
                        accounts.add(
                            Account(
                                id = row.getCell(0).numericCellValue.toLong(),
                                name = row.getCell(1).stringCellValue,
                                type = AccountType.valueOf(row.getCell(2).stringCellValue),
                                balance = row.getCell(3).numericCellValue,
                                currency = row.getCell(4).stringCellValue,
                                icon = row.getCell(5).stringCellValue,
                                color = row.getCell(6).stringCellValue,
                                note = row.getCell(7).stringCellValue,
                                isArchived = row.getCell(8).booleanCellValue,
                                createdAt = LocalDateTime.parse(row.getCell(9).stringCellValue),
                                updatedAt = LocalDateTime.parse(row.getCell(10).stringCellValue)
                            )
                        )
                    }
                }

                // Parse categories
                workbook.getSheet("Categories")?.let { sheet ->
                    for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                        currentRow++
                        importProgress.value = currentRow * 100 / totalRows
                        val row = sheet.getRow(rowIndex)
                        categories.add(
                            Category(
                                id = row.getCell(0).numericCellValue.toLong(),
                                name = row.getCell(1).stringCellValue,
                                type = CategoryType.valueOf(row.getCell(2).stringCellValue),
                                icon = row.getCell(3).stringCellValue,
                                color = row.getCell(4).stringCellValue,
                                parentId = row.getCell(5).numericCellValue.toLong().takeIf { it > 0 },
                                order = row.getCell(6).numericCellValue.toInt(),
                                isArchived = row.getCell(7).booleanCellValue,
                                createdAt = LocalDateTime.parse(row.getCell(8).stringCellValue),
                                updatedAt = LocalDateTime.parse(row.getCell(9).stringCellValue)
                            )
                        )
                    }
                }

                // Parse budgets
                workbook.getSheet("Budgets")?.let { sheet ->
                    for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                        currentRow++
                        importProgress.value = currentRow * 100 / totalRows
                        val row = sheet.getRow(rowIndex)
                        budgets.add(
                            Budget(
                                id = row.getCell(0).numericCellValue.toLong(),
                                name = row.getCell(1).stringCellValue,
                                amount = row.getCell(2).numericCellValue,
                                categoryId = row.getCell(3).numericCellValue.toLong().takeIf { it > 0 },
                                startDate = LocalDateTime.parse(row.getCell(4).stringCellValue),
                                endDate = LocalDateTime.parse(row.getCell(5).stringCellValue),
                                notifyThreshold = row.getCell(6).numericCellValue,
                                isEnabled = row.getCell(7).booleanCellValue,
                                isRepeating = row.getCell(8).booleanCellValue,
                                repeatInterval = row.getCell(9).stringCellValue.takeIf { it.isNotEmpty() }?.let { RepeatInterval.valueOf(it) },
                                createdAt = LocalDateTime.parse(row.getCell(10).stringCellValue),
                                updatedAt = LocalDateTime.parse(row.getCell(11).stringCellValue)
                            )
                        )
                    }
                }

                workbook.close()
            } ?: throw ImportError.InvalidFormat("Failed to read input stream")

            val data = ExportData(
                transactions = transactions,
                accounts = accounts,
                categories = categories,
                budgets = budgets
            )

            importData(data).getOrThrow()
        } catch (e: Exception) {
            Result.failure(ImportError.InvalidFormat(e.message ?: "Unknown error"))
        } finally {
            importProgress.value = 0
        }
    }

    override suspend fun getAllData(): Result<ExportData> {
        return try {
            val transactions = transactionDao.getAllTransactions().first()
            val accounts = accountDao.getAllAccounts().first()
            val categories = categoryDao.getAllCategories().first()
            val budgets = budgetDao.getAllActiveBudgets().first()

            Result.success(
                ExportData(
                    transactions = transactions.map { it.toDomain() },
                    accounts = accounts.map { it.toDomain() },
                    categories = categories.map { it.toDomain() },
                    budgets = budgets.map { it.toDomain() }
                )
            )
        } catch (e: Exception) {
            Result.failure(ExportError.DatabaseError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun importData(data: ExportData): Result<ImportResult> {
        return try {
            database.runInTransaction {
                // Import categories first (for foreign key constraints)
                data.categories.forEach { category ->
                    categoryDao.insert(category.toEntity())
                }

                // Import accounts
                data.accounts.forEach { account ->
                    accountDao.insert(account.toEntity())
                }

                // Import budgets
                data.budgets.forEach { budget ->
                    budgetDao.insert(budget.toEntity())
                }

                // Import transactions
                data.transactions.forEach { transaction ->
                    transactionDao.insert(transaction.toEntity())
                }
            }

            Result.success(
                ImportResult(
                    transactionsImported = data.transactions.size,
                    accountsImported = data.accounts.size,
                    categoriesImported = data.categories.size,
                    budgetsImported = data.budgets.size
                )
            )
        } catch (e: Exception) {
            Result.failure(ImportError.DatabaseError(e.message ?: "Unknown error"))
        }
    }

    override fun getExportProgress(): Flow<Int> = exportProgress

    override fun getImportProgress(): Flow<Int> = importProgress

    private fun parseTransaction(values: List<String>): Transaction {
        return Transaction(
            id = values[0].toLong(),
            amount = values[1].toDouble(),
            note = values[2],
            categoryId = values[3].toLong(),
            accountId = values[4].toLong(),
            date = LocalDateTime.parse(values[5]),
            type = TransactionType.valueOf(values[6]),
            tags = values[7].split("|").filter { it.isNotEmpty() },
            location = values[8].takeIf { it.isNotEmpty() },
            createdAt = LocalDateTime.parse(values[9]),
            updatedAt = LocalDateTime.parse(values[10])
        )
    }

    private fun parseAccount(values: List<String>): Account {
        return Account(
            id = values[0].toLong(),
            name = values[1],
            type = AccountType.valueOf(values[2]),
            balance = values[3].toDouble(),
            currency = values[4],
            icon = values[5],
            color = values[6],
            note = values[7],
            isArchived = values[8].toBoolean(),
            createdAt = LocalDateTime.parse(values[9]),
            updatedAt = LocalDateTime.parse(values[10])
        )
    }

    private fun parseCategory(values: List<String>): Category {
        return Category(
            id = values[0].toLong(),
            name = values[1],
            type = CategoryType.valueOf(values[2]),
            icon = values[3],
            color = values[4],
            parentId = values[5].toLongOrNull(),
            order = values[6].toInt(),
            isArchived = values[7].toBoolean(),
            createdAt = LocalDateTime.parse(values[8]),
            updatedAt = LocalDateTime.parse(values[9])
        )
    }

    private fun parseBudget(values: List<String>): Budget {
        return Budget(
            id = values[0].toLong(),
            name = values[1],
            amount = values[2].toDouble(),
            categoryId = values[3].toLongOrNull(),
            startDate = LocalDateTime.parse(values[4]),
            endDate = LocalDateTime.parse(values[5]),
            notifyThreshold = values[6].toDouble(),
            isEnabled = values[7].toBoolean(),
            isRepeating = values[8].toBoolean(),
            repeatInterval = values[9].takeIf { it.isNotEmpty() }?.let { RepeatInterval.valueOf(it) },
            createdAt = LocalDateTime.parse(values[10]),
            updatedAt = LocalDateTime.parse(values[11])
        )
    }
} 