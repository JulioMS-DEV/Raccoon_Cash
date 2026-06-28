package ni.edu.uam.raccooncash.ui.transactions

import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import java.util.Locale

enum class CategorySortOption(val label: String) {
    ALPHABETICAL("Orden alfabético"),
    TRANSACTION_COUNT("Más transacciones")
}

fun buildCategoryMovementCounts(
    categories: List<CategoryResponse>,
    transactions: List<TransactionResponse>
): Map<Long, Int> {
    return categories.associate { category ->
        category.id to countCategoryMovements(category, transactions, categories)
    }
}

fun sortCategoriesForDisplay(
    categories: List<CategoryResponse>,
    sortOption: CategorySortOption,
    movementCounts: Map<Long, Int>
): List<CategoryResponse> {
    return when (sortOption) {
        CategorySortOption.ALPHABETICAL -> categories.sortedWith(categoryNameComparator())
        CategorySortOption.TRANSACTION_COUNT -> categories.sortedWith(
            compareByDescending<CategoryResponse> { movementCounts[it.id] ?: 0 }
                .then(categoryNameComparator())
        )
    }
}

fun countCategoryMovements(
    category: CategoryResponse,
    transactions: List<TransactionResponse>,
    categories: List<CategoryResponse>
): Int {
    val relatedCategoryIds = categories
        .filter { it.parentCategoryId == category.id }
        .map { it.id }
        .toMutableSet()
        .apply { add(category.id) }

    return transactions.count { transaction ->
        if (transaction.type != category.type) return@count false

        val transactionCategory = transaction.category
        val transactionCategoryId = transaction.categoryId ?: transactionCategory?.id
        when {
            transactionCategoryId != null && transactionCategoryId in relatedCategoryIds -> true
            transactionCategory?.parentCategoryId == category.id -> true
            transactionCategoryId == null && transaction.categoryName?.equals(category.name, ignoreCase = true) == true -> true
            else -> false
        }
    }
}

private fun categoryNameComparator(): Comparator<CategoryResponse> {
    return compareBy<CategoryResponse> { it.name.lowercase(Locale.getDefault()) }
        .thenBy { it.type }
        .thenBy { it.id }
}
