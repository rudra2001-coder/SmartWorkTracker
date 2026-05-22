# Smart Work Tracker — User Manual

A comprehensive **personal productivity, finance, and life management** Android app. Track your work, expenses, habits, health, loans, savings, and more — all in one place.

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Navigation Overview](#2-navigation-overview)
3. [Productivity & Work Tracking](#3-productivity--work-tracking)
4. [Financial Management](#4-financial-management)
5. [Personal Growth](#5-personal-growth)
6. [People & Scheduling](#6-people--scheduling)
7. [Reports & Analytics](#7-reports--analytics)
8. [App Management](#8-app-management)
9. [Background Services](#9-background-services)
10. [Tips & Best Practices](#10-tips--best-practices)

---

## 1. Getting Started

### First Launch
When you open the app for the first time, you'll see the **Onboarding** screen — a one-time walkthrough. After completing it, you'll land on the **Dashboard**.

### Profile Setup
Navigate to **User Profile** (from All Features or Settings) to set up:
- Name, email, phone number
- Skills, experience
- Monthly salary and salary period
- Bio

### Default Data
The app automatically seeds some sample data on first launch:
- 7 sample work log entries
- 4 default meal types: Breakfast (৳30), Lunch (৳80), Dinner (৳80), Snacks (৳20)

---

## 2. Navigation Overview

### Bottom Navigation Bar (5 Tabs)
| Tab | Purpose |
|-----|---------|
| **Dashboard** | Daily summary with key metrics at a glance |
| **Calendar** | View your work logs on a calendar |
| **Analytics** | Charts and trends of your progress |
| **All Features** | Grid launcher to access all 35+ screens |
| **Settings** | App customization and preferences |

### Navigation Drawer
Swipe from the left edge or tap the hamburger icon to open the drawer with links to all screens.

### Back Navigation
Swipe from the left edge or use the back arrow in the top bar to return to the previous screen.

---

## 3. Productivity & Work Tracking

### 3.1 Dashboard
The central hub showing today's summary:
- Quick stats: work hours, expenses, income
- Shortcut cards to common actions (Add Entry, Income, Expense, Loans)
- Recent activity overview

**How to use:** Tap any card to jump to that feature.

### 3.2 Add Entry
Log your daily work entries. This is how you track what you did each day.

**Fields:**
- **Date** — auto-set to today, changeable
- **Work Type** — select from: Office, Home Office, Off Day, Extra Work, Overtime
- **Start Time / End Time** — when you started and finished work
- **Overtime** — mark if you worked overtime (appears when relevant)

**How to use:**
1. Tap the **+** FAB on Dashboard or navigate to Add Entry
2. Fill in the fields
3. Tap **Save**

**Editing:** Navigate to the entry from Calendar view, make changes, and save.

### 3.3 Calendar
Visual calendar view of your work logs.

**How to use:**
- Days with entries are highlighted
- Tap a date to see that day's log
- Tap an entry to edit it
- Swipe between months

### 3.4 Work Timer
Track work sessions in real-time with start/stop functionality.

**How to use:**
1. Tap **Start** to begin tracking
2. The timer runs in the background
3. Tap **Stop** when done
4. Break periods are tracked automatically

### 3.5 Focus Sessions (Pomodoro / Deep Work)
A Pomodoro-style timer for focused work sessions.

**Session types:**
- **Deep Work** — extended focus period
- **Pomodoro** — 25 min work + 5 min break
- **Short Break** — quick rest
- **Long Break** — extended rest
- **Custom** — set your own duration

**How to use:**
1. Select your session type
2. Tap **Start Focus**
3. Work until the timer ends
4. Track interruptions (tap to log each interruption)
5. View your focus score at the end

### 3.6 Mindful Break
A guided relaxation screen for taking short mindful breaks between work sessions.

**How to use:** Tap to start a short guided break session.

### 3.7 Overtime
Track and calculate overtime hours separately from regular work hours.

**How to use:** Log overtime hours and rates; the system calculates overtime pay based on your configured overtime rate.

---

## 4. Financial Management

### 4.1 Accounts
Your financial accounts — the foundation of all financial tracking.

**Account types:**
- **Wallet** — cash on hand
- **Bank** — bank accounts
- **Mobile Banking** — bKash, Nagad, Rocket, etc.

**Account providers:**
Cash, Bank, Savings, DBBL, City Bank, BRAC Bank, BKB, Sonali Bank, bKash, Nagad, Rocket, UCash, Credit Card, Other

**How to use:**
1. Tap **+** to create a new account
2. Enter name, type, provider, and initial balance
3. Swipe **right** on an account to edit
4. Swipe **left** on an account to delete (with balance transfer option)

**Account Detail:** Tap any account to view:
- Transaction history
- Inflow/outflow metrics
- 7-day balance chart

### 4.2 Expense
Log your daily expenses, linked to your accounts.

**Categories:**
Food & Dining, Transport, Shopping, Entertainment, Bills, Healthcare, Education, Personal Care, Gifts, Travel, Subscriptions, Other

**How to use:**
1. Tap **+** to add an expense
2. Enter amount, select category, choose the account to deduct from
3. Add optional merchant name, notes, or an image
4. Tap **Save**

**Rules:**
- Balance is checked before deduction — insufficient funds will show an error
- Expenses are linked to accounts for accurate balance tracking

### 4.3 Income
Track all sources of income.

**How to use:**
1. Tap **+** to add income
2. Enter amount, source, and select the account to credit
3. Tap **Save**

### 4.4 Savings
Deposit or withdraw from your savings, linked to your accounts.

**Categories:** Deposit, Withdrawal, Interest, Transfer, Other

**How to use:**
1. Tap **+** to add a savings transaction
2. Enter amount, select a **mandatory** account (no "account-less" option exists)
3. Select Deposit or Withdrawal toggle
4. Deposits deduct from selected account; withdrawals add to selected account
5. Each transaction creates a FinancialTransaction record for audit
6. Account selection is **required** — confirm button stays disabled until an account is chosen

### 4.5 Loans
Full loan management for both borrowed and lent money.

**Loan types:**
- **Borrowed** — money you borrowed from someone
- **Lent** — money you lent to someone

**Categories:**
Personal, Home, Car, Education, Business, Medical, Other

**How to use:**
1. Tap **+** FAB to add a loan
2. Fill in: person name, amount, type, category, optional interest/EMI/due date
3. Select an account (for LENT, money is deducted; for BORROWED, money is added)
4. Tap **Add Loan**

**Repayment:**
1. Tap **Make Payment** (Borrowed) or **Receive Payment** (Lent) on a loan card
2. Select the account to use
3. Confirm — full repayment is required (partial payments not allowed)

**Loan details:** Tap a loan card to view transaction history, due dates, interest rate, and more.

### 4.6 EMI
EMI (Equated Monthly Installment) schedule management linked to loans.

**Statuses:** Upcoming, Due, Overdue, Paid, Skipped

**How to use:**
1. Create a loan with EMI details (amount, total EMIs)
2. The EMI schedule generates automatically
3. Pay EMIs on time from the EMI screen
4. Each payment deducts from the selected payment account

### 4.7 Credit Card
Manage credit cards with transaction tracking and bill payment.

**How to use:**
1. Tap **Add Credit Card** — enter card name, number, limit, statement/due dates
2. Tap a card to open the action sheet:
   - **Add Charge** — log a credit card purchase (increases card balance)
   - **Pay Bill** — pay down the card balance (select an account to pay from)
   - **Transfer** — transfer money from the card to an account
   - **View History** — see all transactions
   - **Edit** — update card details
   - **Delete** — remove card (settles balance first)

**Rules:**
- Card limit is enforced — charges cannot exceed available credit
- Bill payments validate account balance before deducting
- Deleting a card with outstanding balance requires sufficient funds in the linked account

### 4.8 Transfer
Transfer money between your accounts.

**How to use:**
1. Select **From Account** and **To Account**
2. Enter the amount
3. Confirm — the system creates a FinancialTransaction record and updates both balances

**Rules:**
- Daily transfer limits are enforced per account
- Insufficient balance will show an error

### 4.9 Financial Statement
A unified ledger showing all financial transactions across all accounts.

**How to use:** Browse through all transactions; filter by type, account, or date range.

### 4.10 Recurring Transactions
Set up automated recurring money movements.

**Frequency options:**
Daily, Weekly, Biweekly, Monthly, Quarterly, Yearly, Custom, Weekly Specific Days

**Types:** Income, Expense, Savings, Transfer

**Priority levels:** Critical, High, Medium, Low, Optional

**How to use:**
1. Tap **+** to create a recurring rule
2. Set: name, type, amount, frequency, accounts
3. Optionally set priority and balance protection
4. The system automatically executes the rule on schedule

**Balance protection:** Higher priority transactions execute first; low-priority ones may be skipped if funds are insufficient.

### 4.11 Spend Advisor
Analyzes your spending patterns and provides insights.

**Analysis:** Detects if spending is Increasing, Decreasing, or Stable

**Advice severity:** Good, Warning, Danger

**How to use:** Open the screen to see automated analysis of your expense trends with savings tips.

### 4.12 Calculation (Multi-Meal Rate System)
A comprehensive meal cost calculator with multi-meal support.

**Features:**
- **Multiple meal types** — Breakfast, Lunch, Dinner, Snacks (configurable)
- **Default rates** — set a base rate for each meal type
- **Weekly overrides** — set different rates per week (Week 1–5)
- **Daily overrides** — select specific dates and set custom rates
- **Rate resolution** — Daily rate → Weekly rate → Default rate (most specific wins)
- **Quarterly projection** — 3-month cost estimate

**How to use:**
1. Navigate to the month you want to calculate
2. **Meal Types & Rates** — add/edit/delete meal types with default rates
3. **Weekly Rate Override** — select a week tab, set per-meal rates for that week
4. **Date-Specific Rates** — tap dates on the calendar to select them; set per-meal rates for selected dates
5. View the **Meal Cost Breakdown** for per-meal weekly/monthly/yearly costs
6. View the **Total Cost Summary** for combined meal + travel + other costs with quarterly projection
7. The **Monthly Breakdown Chart** shows all 12 months at a glance

---

## 5. Personal Growth

### 5.1 Habit Tracker
Build and maintain habits with streak tracking.

**Difficulty levels:** Easy, Medium, Hard

**Features:**
- Habit chaining (a habit can trigger another habit)
- Streak counting with visual progress

**How to use:**
1. Tap **+** to add a habit
2. Set name, description, difficulty
3. Mark the habit as done each day to build your streak
4. View your streaks on the main screen

### 5.2 Health Metrics
Comprehensive health tracking with 30+ metric types.

**Trackable metrics:**
- Weight, Sleep hours, Water intake, Exercise duration
- Heart rate, Blood pressure, Steps
- Mood, Stress level, Meditation time
- Skin care, Calories, Macros (protein/carbs/fat)
- Wake time, Bed time, and more

**How to use:**
1. Tap **+** to log a metric
2. Select the metric type, enter the value
3. Add optional secondary values (e.g., systolic/diastolic for blood pressure)
4. Tap **Save**
5. View trends over time on the main screen

### 5.3 Daily Journal
A reflective journal with one entry per day.

**Sections:**
- **Morning Intention** — what you want to achieve today
- **Gratitude** — what you're grateful for
- **Evening Reflection** — how the day went

**How to use:**
1. Open the journal for today
2. Fill in any or all sections
3. Tap **Save** — only one entry per day is allowed

### 5.4 Achievements
Gamification system with unlockable achievements.

**Types:**
- **Streak-based** — maintain habit streaks
- **Focus-based** — complete focus sessions

**How to use:** Use the app consistently — achievements unlock automatically as you hit milestones.

### 5.5 Reality Tracker
Track goals, promises, and plans vs. what actually happens.

**Types:** Goal, Promise, Plan

**Categories:** Work, Health, Learning, Personal, Finance, Social

**How to use:**
1. Tap **+** to add a reality entry
2. Set the type, title, target date
3. Mark as completed when you achieve it
4. View your completion rate over time

### 5.6 Future Self (Decision Impact)
A decision impact tracker based on the "Future Self" framework.

**Future Identity types:**
- Fit Self — health and fitness
- Rich Self — financial goals
- Disciplined Self — self-control
- Happy Self — emotional wellbeing
- Growing Self — learning and growth

**How to use:**
1. Log decisions you make throughout the day
2. Tag each decision with which future self it serves
3. View your alignment score — how well your daily decisions match your goals

### 5.7 Wisdom Library
Curated wisdom quotes organized by category.

**Categories:** Productivity, Mindfulness, Habits

**How to use:** Browse quotes for inspiration and motivation.

---

## 6. People & Scheduling

### 6.1 Team (Colleague Manager)
Manage your colleagues with detailed profiles.

**Profile fields:** Name, designation, department, skills, contact info, collaboration rating, trust score

**Features:**
- Contact picker integration
- Duty swap notifications

**How to use:**
1. Tap **+** to add a colleague
2. Fill in their details (use the contact picker to import from your phone)
3. View and manage your network

### 6.2 Scheduler / Alarms
Set alarms and schedules with advanced features.

**Features:**
- One-time and repeating alarms
- Custom ringtones
- Vibration patterns
- Snooze (5 min or 15 min)
- Schedule history

**How to use:**
1. Tap **+** to create a schedule
2. Set the time, choose repeat days (optional)
3. Select ringtone and vibration pattern
4. The alarm fires with a full-screen notification showing the time, Dismiss and Snooze buttons

---

## 7. Reports & Analytics

### 7.1 Analytics
Visual charts and trends of your work and financial data.

**How to use:** Navigate through the analytics dashboard to see graphs of your productivity and spending patterns.

### 7.2 Monthly Report
A comprehensive monthly insights dashboard combining work and financial data with flexible filtering and period comparison.

**How to use:** Select a month and year using the dropdown and arrow buttons, or toggle to Custom Date Range for arbitrary start/end dates. Toggle "Compare with previous period" to see side-by-side metrics with percentage changes.

**Shows:**
- **Overview Cards** — Work Days, Income, Expense, Net (4 stat cards with icons and gradients)
- **Work Distribution** — Pie chart showing Office, Home, Off, Extra days
- **Expense by Category** — Pie chart with color-coded expense categories
- **Income by Category** — Pie chart with income source breakdown
- **Savings Activity** — Deposited, Withdrawn, and Net savings for the period
- **Detailed Summary** — Full work breakdown (office, home, off, extra, overtime) + financial summary (income, expense, meal cost, net)
- **Period Comparison** — When enabled, shows current vs previous period metrics side-by-side with percentage change

### 7.3 Reports
Generate comprehensive work reports.

**How to use:** Select date ranges and report type to generate formatted reports of your work activity.

---

## 8. App Management

### 8.1 Settings
Customize the app to your preferences.

**Options:**
- Theme (light/dark)
- Language (English, Bengali)
- Default meal rate, overtime rate
- Daily work hours, working days per week

### 8.2 Appearance
Fine-tune the look and feel:
- Light or dark mode
- Color customization

### 8.3 Backup & Restore
Export your entire database to a JSON file or import a previous backup.

**How to use:**
1. Go to **Backup** from All Features or Settings
2. **Export** — saves all data to a JSON file (manual or automatic)
3. **Import** — select a JSON backup file to restore all data
4. The system clears existing data and replaces it with the backup

**Auto Backup:** The app automatically creates a backup every night at 12:05 AM.

### 8.4 User Profile / Profile Setup
View and edit your personal profile.

**Fields:** Name, email, phone, bio, skills, experience, monthly salary, savings

**How to use:** Go to **User Profile** to edit. On first use (if skipped), go to **Profile Setup** from All Features.

---

## 9. Background Services

These run automatically — no user action needed:

### Daily Auto Backup (12:05 AM)
Every day at 12:05 AM, the app exports your entire database to a JSON file in the Downloads folder. On Android 10+, it uses MediaStore for compatibility.

### Recurring Transaction Processing (Every Hour)
Every hour, the system checks for due recurring rules and executes them automatically. You'll receive notifications for:
- Successful transactions
- Failed transactions (insufficient funds, etc.)
- Upcoming transactions

### Alarm Execution
When a scheduled alarm fires:
1. A full-screen activity appears with the alarm time
2. Options: **Dismiss** or **Snooze** (5 or 15 minutes)
3. Sound plays and device vibrates until dismissed

---

## 10. Tips & Best Practices

### Start with Accounts
Before logging any expenses, income, or loans, create at least one account. The financial system relies on accounts as the source of truth for balances.

### Log Work Days First
The Calendar and Calculation screens depend on work log data. Log your work days regularly to get accurate cost projections.

### Use the Dashboard as Your Home
The Dashboard gives you a daily snapshot. Start your day here to see what needs attention.

### Enable Auto Backup
The auto backup runs daily. For extra safety, do a manual backup before making major changes.

### Financial Integrity Rules
The app enforces strict financial rules:
- You cannot overdraw an account — transactions will fail with a clear error
- Loan repayments must be full (partial repayments not allowed)
- Card limits are enforced for credit cards
- All money movements create audit trail records

### Habit Stacking
Create habit chains where completing one habit triggers another. This builds momentum and helps maintain streaks.

### Use EMI for Large Loans
If you have a loan with installments, set up EMI details when creating the loan. The system will track due dates and remaining installments automatically.
