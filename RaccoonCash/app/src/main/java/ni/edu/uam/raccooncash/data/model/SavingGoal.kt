package ni.edu.uam.raccooncash.data.model

data class SavingGoal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: String, // ISO date string
    val color: String,
    val icon: String? = null,
    val currency: String = "C$"
)
