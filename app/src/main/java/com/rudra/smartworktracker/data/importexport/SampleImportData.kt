package com.rudra.smartworktracker.data.importexport

object SampleImportData {

    fun getSampleRows(type: ImportEntityType): List<Map<String, String>> = when (type) {
        ImportEntityType.EXPENSE -> listOf(
            mapOf("date" to "2026-06-01", "amount" to "350.00", "category" to "FOOD_AND_DINING", "accountName" to "Main Wallet", "merchant" to "Kacchi Bhai", "notes" to "Office lunch"),
            mapOf("date" to "2026-06-02", "amount" to "120.00", "category" to "TRANSPORTATION", "accountName" to "Main Wallet", "merchant" to "Uber", "notes" to "Cab to client meeting"),
            mapOf("date" to "2026-06-03", "amount" to "2500.00", "category" to "SHOPPING", "accountName" to "Main Bank", "merchant" to "Daraz", "notes" to "New office bag"),
            mapOf("date" to "2026-06-04", "amount" to "800.00", "category" to "ENTERTAINMENT", "accountName" to "Main Wallet", "merchant" to "Cineplex", "notes" to "Movie with friends"),
            mapOf("date" to "2026-06-05", "amount" to "500.00", "category" to "BILLS_AND_UTILITIES", "accountName" to "Main Bank", "merchant" to "", "notes" to "Internet bill")
        )

        ImportEntityType.INCOME -> listOf(
            mapOf("date" to "2026-06-01", "amount" to "55000.00", "source" to "Salary", "accountName" to "Main Bank", "category" to "Salary", "description" to "June salary"),
            mapOf("date" to "2026-06-05", "amount" to "15000.00", "source" to "Freelance", "accountName" to "Main Wallet", "category" to "Freelance", "description" to "Web dev project"),
            mapOf("date" to "2026-06-10", "amount" to "3000.00", "source" to "Dividend", "accountName" to "Main Bank", "category" to "Investment", "description" to "Stock dividend"),
            mapOf("date" to "2026-06-15", "amount" to "5000.00", "source" to "Tutoring", "accountName" to "Main Wallet", "category" to "Side Hustle", "description" to "Math tutoring"),
            mapOf("date" to "2026-06-20", "amount" to "2000.00", "source" to "Cashback", "accountName" to "Main Wallet", "category" to "Other", "description" to "Credit card cashback")
        )

        ImportEntityType.WORK_LOG -> listOf(
            mapOf("date" to "2026-06-01", "workType" to "OFFICE", "startTime" to "09:00", "endTime" to "18:00", "isOvertime" to "false", "overtimeRate" to ""),
            mapOf("date" to "2026-06-02", "workType" to "OFFICE", "startTime" to "08:30", "endTime" to "19:00", "isOvertime" to "true", "overtimeRate" to "1.5"),
            mapOf("date" to "2026-06-03", "workType" to "HOME", "startTime" to "10:00", "endTime" to "17:00", "isOvertime" to "false", "overtimeRate" to ""),
            mapOf("date" to "2026-06-04", "workType" to "OFFICE", "startTime" to "09:00", "endTime" to "18:00", "isOvertime" to "false", "overtimeRate" to ""),
            mapOf("date" to "2026-06-05", "workType" to "OFFICE", "startTime" to "09:00", "endTime" to "20:00", "isOvertime" to "true", "overtimeRate" to "2.0"),
            mapOf("date" to "2026-06-06", "workType" to "WEEKEND", "startTime" to "", "endTime" to "", "isOvertime" to "false", "overtimeRate" to ""),
            mapOf("date" to "2026-06-07", "workType" to "WEEKEND", "startTime" to "", "endTime" to "", "isOvertime" to "false", "overtimeRate" to ""),
            mapOf("date" to "2026-06-08", "workType" to "OFFICE", "startTime" to "09:00", "endTime" to "18:00", "isOvertime" to "false", "overtimeRate" to ""),
            mapOf("date" to "2026-06-09", "workType" to "OFFICE", "startTime" to "09:30", "endTime" to "18:30", "isOvertime" to "false", "overtimeRate" to ""),
            mapOf("date" to "2026-06-10", "workType" to "HOME", "startTime" to "10:00", "endTime" to "16:00", "isOvertime" to "false", "overtimeRate" to "")
        )

        ImportEntityType.ACCOUNT -> listOf(
            mapOf("name" to "Main Wallet", "type" to "WALLET", "provider" to "CASH", "balance" to "15000.00", "accountNumber" to "", "notes" to "Daily expenses wallet"),
            mapOf("name" to "Main Bank", "type" to "BANK", "provider" to "DBBL", "balance" to "250000.00", "accountNumber" to "1234567890", "notes" to "Salary account"),
            mapOf("name" to "bKash", "type" to "MOBILE_BANKING", "provider" to "BKASH", "balance" to "35000.00", "accountNumber" to "01XXXXXXXXX", "notes" to "Mobile wallet"),
            mapOf("name" to "Nagad", "type" to "MOBILE_BANKING", "provider" to "NAGAD", "balance" to "10000.00", "accountNumber" to "01XXXXXXXXY", "notes" to "Secondary mobile wallet"),
            mapOf("name" to "Savings Account", "type" to "BANK", "provider" to "DBBL", "balance" to "500000.00", "accountNumber" to "0987654321", "notes" to "Long-term savings")
        )

        ImportEntityType.LOAN -> listOf(
            mapOf("date" to "2026-01-15", "personName" to "Rahim Bhai", "amount" to "50000.00", "loanType" to "BORROWED", "loanCategory" to "PERSONAL", "accountName" to "Main Wallet", "dueDate" to "2026-12-31", "notes" to "Emergency loan"),
            mapOf("date" to "2026-02-01", "personName" to "Karim", "amount" to "30000.00", "loanType" to "LENT", "loanCategory" to "FRIEND", "accountName" to "Main Bank", "dueDate" to "2026-08-01", "notes" to "Friend needed help"),
            mapOf("date" to "2026-03-10", "personName" to "Sister", "amount" to "100000.00", "loanType" to "LENT", "loanCategory" to "FAMILY", "accountName" to "Main Bank", "dueDate" to "2027-03-10", "notes" to "Sister's wedding"),
            mapOf("date" to "2026-04-05", "personName" to "Office Colleague", "amount" to "15000.00", "loanType" to "BORROWED", "loanCategory" to "PERSONAL", "accountName" to "Main Wallet", "dueDate" to "2026-07-05", "notes" to "Lunch money advance"),
            mapOf("date" to "2026-05-20", "personName" to "Uncle", "amount" to "200000.00", "loanType" to "BORROWED", "loanCategory" to "FAMILY", "accountName" to "Main Bank", "dueDate" to "2027-05-20", "notes" to "Home renovation")
        )

        ImportEntityType.SAVINGS -> listOf(
            mapOf("date" to "2026-06-01", "amount" to "10000.00", "accountName" to "Main Bank", "category" to "DEPOSIT", "note" to "Monthly savings June"),
            mapOf("date" to "2026-06-05", "amount" to "5000.00", "accountName" to "Main Bank", "category" to "DEPOSIT", "note" to "Extra savings from freelance"),
            mapOf("date" to "2026-06-10", "amount" to "2000.00", "accountName" to "Savings Account", "category" to "DEPOSIT", "note" to "Emergency fund top-up"),
            mapOf("date" to "2026-06-15", "amount" to "-3000.00", "accountName" to "Main Bank", "category" to "WITHDRAWAL", "note" to "Emergency medical expense"),
            mapOf("date" to "2026-06-20", "amount" to "8000.00", "accountName" to "Savings Account", "category" to "DEPOSIT", "note" to "Quarterly bonus savings")
        )

        ImportEntityType.HABIT -> listOf(
            mapOf("name" to "Morning Exercise", "description" to "30 min exercise every morning", "difficulty" to "MEDIUM"),
            mapOf("name" to "Read Daily", "description" to "Read 20 pages of a book daily", "difficulty" to "EASY"),
            mapOf("name" to "Meditation", "description" to "10 min mindfulness meditation", "difficulty" to "EASY"),
            mapOf("name" to "Learn Coding", "description" to "Practice DSA for 1 hour", "difficulty" to "HARD"),
            mapOf("name" to "Drink Water", "description" to "Drink 8 glasses of water daily", "difficulty" to "EASY"),
            mapOf("name" to "Journal Writing", "description" to "Write daily journal before bed", "difficulty" to "MEDIUM"),
            mapOf("name" to "No Sugar", "description" to "Avoid sugary foods and drinks", "difficulty" to "HARD")
        )

        ImportEntityType.HEALTH_METRIC -> listOf(
            mapOf("date" to "2026-06-01", "metricType" to "WEIGHT", "value" to "72.5", "unit" to "kg", "notes" to "Morning weight"),
            mapOf("date" to "2026-06-01", "metricType" to "SLEEP", "value" to "7.5", "unit" to "hrs", "notes" to "Good sleep"),
            mapOf("date" to "2026-06-02", "metricType" to "STEPS", "value" to "8500", "unit" to "steps", "notes" to "Walked to office"),
            mapOf("date" to "2026-06-02", "metricType" to "WATER", "value" to "2.0", "unit" to "L", "notes" to "Drank enough water"),
            mapOf("date" to "2026-06-03", "metricType" to "WEIGHT", "value" to "72.3", "unit" to "kg", "notes" to ""),
            mapOf("date" to "2026-06-03", "metricType" to "HEART_RATE", "value" to "72", "unit" to "bpm", "notes" to "Resting heart rate"),
            mapOf("date" to "2026-06-04", "metricType" to "SLEEP", "value" to "6.0", "unit" to "hrs", "notes" to "Late night work")
        )

        ImportEntityType.DAILY_JOURNAL -> listOf(
            mapOf("date" to "2026-06-01", "morningIntention" to "Complete the quarterly report", "gratitude" to "Grateful for my team's support", "eveningReflection" to "Productive day. Finished the report on time."),
            mapOf("date" to "2026-06-02", "morningIntention" to "Focus on deep work", "gratitude" to "Grateful for good health", "eveningReflection" to "Got distracted but still made progress."),
            mapOf("date" to "2026-06-03", "morningIntention" to "Learn something new", "gratitude" to "Thankful for online learning resources", "eveningReflection" to "Completed a Kotlin course module - feeling good!"),
            mapOf("date" to "2026-06-04", "morningIntention" to "Be patient and kind", "gratitude" to "Grateful for family", "eveningReflection" to "Had a great dinner with family."),
            mapOf("date" to "2026-06-05", "morningIntention" to "Finish pending tasks", "gratitude" to "Grateful for the weekend ahead", "eveningReflection" to "Tied up all loose ends. Ready for the weekend!")
        )

        ImportEntityType.CREDIT_CARD -> listOf(
            mapOf("cardName" to "DBBL Mastercard", "cardNumber" to "****4567", "cardLimit" to "150000.00", "accountName" to "Main Bank", "statementDate" to "5", "dueDate" to "25"),
            mapOf("cardName" to "bKash Card", "cardNumber" to "****8901", "cardLimit" to "50000.00", "accountName" to "bKash", "statementDate" to "10", "dueDate" to "30"),
            mapOf("cardName" to "City Bank Amex", "cardNumber" to "****2345", "cardLimit" to "300000.00", "accountName" to "Main Bank", "statementDate" to "3", "dueDate" to "20"),
            mapOf("cardName" to "EBL Visa", "cardNumber" to "****6789", "cardLimit" to "100000.00", "accountName" to "Main Bank", "statementDate" to "7", "dueDate" to "22")
        )

        ImportEntityType.RECURRING_RULE -> listOf(
            mapOf("name" to "Monthly Savings", "amount" to "10000.00", "transactionType" to "SAVINGS_ADD", "frequency" to "MONTHLY", "startDate" to "2026-01-01", "sourceAccountName" to "Main Bank", "destinationAccountName" to "", "notes" to "Auto savings on 1st"),
            mapOf("name" to "Internet Bill", "amount" to "1200.00", "transactionType" to "EXPENSE", "frequency" to "MONTHLY", "startDate" to "2026-01-05", "sourceAccountName" to "Main Bank", "destinationAccountName" to "", "notes" to "Internet bill payment"),
            mapOf("name" to "Gym Membership", "amount" to "2000.00", "transactionType" to "EXPENSE", "frequency" to "MONTHLY", "startDate" to "2026-01-10", "sourceAccountName" to "Main Wallet", "destinationAccountName" to "", "notes" to "Gym fee"),
            mapOf("name" to "Weekly Snacks Budget", "amount" to "500.00", "transactionType" to "EXPENSE", "frequency" to "WEEKLY", "startDate" to "2026-06-01", "sourceAccountName" to "Main Wallet", "destinationAccountName" to "", "notes" to "Weekly office snacks"),
            mapOf("name" to "Quarterly Bonus Transfer", "amount" to "50000.00", "transactionType" to "TRANSFER", "frequency" to "QUARTERLY", "startDate" to "2026-03-01", "sourceAccountName" to "Main Bank", "destinationAccountName" to "Savings Account", "notes" to "Bonus to savings")
        )

        ImportEntityType.COLLEAGUE -> listOf(
            mapOf("name" to "Tanvir Ahmed", "designation" to "Software Engineer", "department" to "Engineering", "phone" to "+8801712345678", "email" to "tanvir@company.com"),
            mapOf("name" to "Nusrat Jahan", "designation" to "Product Manager", "department" to "Product", "phone" to "+8801812345678", "email" to "nusrat@company.com"),
            mapOf("name" to "Hasan Mahmud", "designation" to "Team Lead", "department" to "Engineering", "phone" to "+8801912345678", "email" to "hasan@company.com"),
            mapOf("name" to "Farzana Rahman", "designation" to "Designer", "department" to "Design", "phone" to "+8801512345678", "email" to "farzana@company.com"),
            mapOf("name" to "Kabir Hossain", "designation" to "HR Manager", "department" to "Human Resources", "phone" to "+8801612345678", "email" to "kabir@company.com"),
            mapOf("name" to "Sadia Islam", "designation" to "Business Analyst", "department" to "Strategy", "phone" to "+8801711111111", "email" to "sadia@company.com")
        )

        ImportEntityType.FINANCIAL_TRANSACTION -> listOf(
            mapOf("date" to "2026-06-01", "amount" to "55000.00", "type" to "INCOME", "sourceAccountName" to "", "destinationAccountName" to "Main Bank", "note" to "Salary June"),
            mapOf("date" to "2026-06-02", "amount" to "350.00", "type" to "EXPENSE", "sourceAccountName" to "Main Wallet", "destinationAccountName" to "", "note" to "Lunch"),
            mapOf("date" to "2026-06-03", "amount" to "10000.00", "type" to "SAVINGS_ADD", "sourceAccountName" to "Main Bank", "destinationAccountName" to "Savings Account", "note" to "Monthly savings transfer"),
            mapOf("date" to "2026-06-04", "amount" to "5000.00", "type" to "TRANSFER", "sourceAccountName" to "Main Bank", "destinationAccountName" to "bKash", "note" to "Sent to bKash for expenses"),
            mapOf("date" to "2026-06-05", "amount" to "2000.00", "type" to "LOAN_LEND", "sourceAccountName" to "Main Wallet", "destinationAccountName" to "", "note" to "Lent to Karim")
        )
    }
}
