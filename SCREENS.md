# App Screens & Features

## Screen Locations
```
ui/screens/
├── dashboard/       # Main dashboard with overview
├── recurring/       # Recurring transactions (5 tabs)
├── savings/         # Savings tracking
├── reports/         # Reports and analytics
├── report/          # Monthly reports
├── emi/             # EMI calculator
├── loans/           # Loan management
├── scheduler/       # Schedule/alarm management
├── wisdom/          # Wisdom/quotes
├── onboarding/      # First-time user flow
└── settings/        # App settings
```

## Feature Summary

### Dashboard
- Overview of finances
- Quick actions
- Recent transactions

### Recurring Transactions (UPGRADED)
- **Rules Tab**: Search, filter, expandable cards
- **Transactions Tab**: All instances with status
- **Calendar Tab**: Monthly view
- **Insights Tab**: Yearly projection, category breakdown, pattern suggestions
- **History Tab**: Execution history

### Savings
- Add/withdraw savings
- History and category filtering

### Reports
- Monthly summaries
- Category-wise analysis

### EMI Calculator
- EMI computation
- Loan amortization

### Loans
- Borrow/lend tracking
- Repayment management

### Scheduler
- Alarm management
- Ringtone picker
- Repeating schedules

### Wisdom
- Daily quotes/wisdom

## Navigation
- Bottom navigation bar (if implemented)
- Or drawer navigation
- Back navigation via `onNavigateBack` callback

## Data Flow
```
Screen → ViewModel → Repository → DAO → Room Database
                ↓
            Engine (business logic)
```

## Key ViewModels
| ViewModel | Factory | Key Dependencies |
|-----------|---------|------------------|
| RecurringViewModel | RecurringViewModelFactory | RecurringRepository, RecurringEngine |
| DashboardViewModel | - | Multiple repositories |
| SavingsViewModel | SavingsViewModelFactory | SavingsRepository |
| EmiViewModel | EmiViewModelFactory | - |
| LoanViewModel | LoanViewModelFactory | LoanRepository |
| ReportsViewModel | ReportsViewModelFactory | - |
| SchedulerViewModel | - | ScheduleRepository |
