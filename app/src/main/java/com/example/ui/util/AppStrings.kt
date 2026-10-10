package com.example.ui.util

import androidx.compose.runtime.compositionLocalOf
import com.example.model.AppLanguage

data class AppStrings(
    // Bottom Navigation
    val navHome: String = "Home",
    val navTransactions: String = "Transactions",
    val navReports: String = "Reports",
    val navSettings: String = "Settings",

    // Settings Screen
    val settingsTitle: String = "Settings & Profile",
    val settingsAccountGroup: String = "Account Settings",
    val settingsCountry: String = "Country & Currency",
    val settingsLanguage: String = "Language",
    val settingsLanguageSubtitle: String = "Change app language",
    val settingsSelectLanguage: String = "Select Language",
    val settingsSelectLanguagePrompt: String = "Choose your preferred language for PromptPay.",
    val settingsCategoryManagement: String = "Category Management",
    val settingsCategorySubtitle: String = "Manage tags & keyword rules",
    val settingsPushNotifications: String = "Pending Review Reminders",
    val settingsPushNotificationsSubtitle: String = "Local daily notifications",
    val settingsSecurityGroup: String = "Security & Privacy",
    val settingsMomoScam: String = "MoMo Scam Detection",
    val settingsMomoScamSubtitle: String = "Flag suspicious SMS & duplicate reversals",
    val settingsLockMode: String = "App Lock Mode",
    val settingsLockModeSubtitle: String = "Choose between None, PIN, or Biometrics",
    val settingsChangePin: String = "Change 5-Digit PIN",
    val settingsChangePinSubtitle: String = "Update your local security passcode",
    val settingsSignOut: String = "Sign Out",
    val settingsConfirm: String = "Confirm Change",
    val settingsCancel: String = "Cancel",
    val settingsLanguageSwitched: String = "Language updated",

    // Home Screen
    val homeTotalMonthlySpend: String = "Total Monthly Spending",
    val homePendingReviewNotice: String = "Transactions Need Categorization",
    val homePendingAction: String = "Review Now",
    val homeRecentTransactions: String = "Recent Transactions",
    val homeSeeAll: String = "See All",
    val homeNoRecentTransactions: String = "No recent transactions",
    val homeNoTransactionsHint: String = "Transactions detected via SMS will appear here automatically",
    val homeSimulateSms: String = "Simulate SMS Alert",

    // Transactions Screen
    val txTitle: String = "Transactions",
    val txSearchPlaceholder: String = "Search merchant or notes...",
    val txFilterAll: String = "All",
    val txResetFilters: String = "Reset Filters",
    val txNoMatches: String = "No transactions match your search.",
    val txFetchOlder: String = "Fetching older records...",
    val txLoadOlder: String = "Load Older Transactions",

    // Transaction Details
    val detailTitle: String = "Transaction Detail",
    val detailPaymentMethod: String = "Payment Method",
    val detailSource: String = "Source",
    val detailSourceSms: String = "SMS Alert (Fixed)",
    val detailSourceManual: String = "Manual Entry (Fixed)",
    val detailFee: String = "Transaction Fee",
    val detailFeeCategory: String = "Fee Category",
    val detailStatus: String = "Status",
    val detailDescriptionLabel: String = "Transaction Description",
    val detailDescriptionPlaceholder: String = "Enter transaction description...",
    val detailCategoryLabel: String = "Category",
    val detailNotesLabel: String = "Notes",
    val detailNotesPlaceholder: String = "Add transaction note...",
    val detailRecurringTitle: String = "Flag as Recurring",
    val detailRecurringSubtitle: String = "Monthly rent, subscriptions, or fixed bills",
    val detailSaveChanges: String = "Save Changes",
    val detailDeletePrompt: String = "Delete Transaction",
    val detailDeleteConfirm: String = "Are you sure you want to delete this transaction?",

    // Add Transaction Screen
    val addTitle: String = "Add Manual Transaction",
    val addAmountLabel: String = "Enter Amount",
    val addFeeLabel: String = "Transaction Fee - Optional",
    val addDescLabel: String = "Description / Merchant Name",
    val addDescPlaceholder: String = "e.g. Starbucks, Simba Supermarket",
    val addCategoryLabel: String = "Select Category",
    val addSaveButton: String = "Save Transaction",

    // Reports Screen
    val reportsTitle: String = "Reports & Insights",
    val reportsWeekly: String = "Weekly",
    val reportsMonthly: String = "Monthly",
    val reportsYearly: String = "Yearly",
    val reportsTotalSpending: String = "Total Spending",
    val reportsTotalFees: String = "Total Transaction Fees",
    val reportsFeesCategory: String = "Category: Transaction Fees",
    val reportsSmartTipTitle: String = "Smart Tip",
    val reportsSmartTipBody: String = "Transactions are automatically sorted by category. Review your categories to keep reports accurate.",
    val reportsTopCategories: String = "Top Spending Categories",
    val reportsNoExpenses: String = "No categorized expenses yet",
    val reportsExportTitle: String = "Export Financial Data",
    val reportsExportSubtitle: String = "Download CSV / JSON summary for your records",

    // Pending Review Screen
    val pendingTitle: String = "Pending Review",
    val pendingAllSetTitle: String = "All set for today!",
    val pendingAllSetBody: String = "No pending transactions waiting for review. Automated SMS detection will prompt you when new alerts arrive.",
    val pendingBackButton: String = "Back to Dashboard",
    val pendingCategoryPrompt: String = "Select Category:",
    val pendingConfirmButton: String = "Confirm & Add"
) {
    companion object {
        val English = AppStrings()

        val French = AppStrings(
            // Bottom Navigation
            navHome = "Accueil",
            navTransactions = "Transactions",
            navReports = "Rapports",
            navSettings = "Paramètres",

            // Settings Screen
            settingsTitle = "Paramètres & Profil",
            settingsAccountGroup = "Paramètres du compte",
            settingsCountry = "Pays & Devise",
            settingsLanguage = "Langue",
            settingsLanguageSubtitle = "Changer la langue de l'application",
            settingsSelectLanguage = "Choisir la langue",
            settingsSelectLanguagePrompt = "Choisissez votre langue préférée pour PromptPay.",
            settingsCategoryManagement = "Gestion des catégories",
            settingsCategorySubtitle = "Gérer les balises et règles de mots-clés",
            settingsPushNotifications = "Rappels de vérification en attente",
            settingsPushNotificationsSubtitle = "Notifications quotidiennes locales",
            settingsSecurityGroup = "Sécurité & Confidentialité",
            settingsMomoScam = "Détection d'arnaques MoMo",
            settingsMomoScamSubtitle = "Signaler les SMS suspects et fausses annulations",
            settingsLockMode = "Mode de verrouillage",
            settingsLockModeSubtitle = "Choisir entre Aucun, Code PIN ou Biométrie",
            settingsChangePin = "Modifier le code PIN à 5 chiffres",
            settingsChangePinSubtitle = "Mettre à jour votre code de sécurité local",
            settingsSignOut = "Se déconnecter",
            settingsConfirm = "Confirmer",
            settingsCancel = "Annuler",
            settingsLanguageSwitched = "Langue modifiée avec succès",

            // Home Screen
            homeTotalMonthlySpend = "Dépenses mensuelles totales",
            homePendingReviewNotice = "Transactions à catégoriser",
            homePendingAction = "Examiner",
            homeRecentTransactions = "Transactions récentes",
            homeSeeAll = "Voir tout",
            homeNoRecentTransactions = "Aucune transaction récente",
            homeNoTransactionsHint = "Les transactions détectées par SMS apparaîtront ici automatiquement",
            homeSimulateSms = "Simuler une alerte SMS",

            // Transactions Screen
            txTitle = "Transactions",
            txSearchPlaceholder = "Rechercher commerçant ou notes...",
            txFilterAll = "Toutes",
            txResetFilters = "Réinitialiser les filtres",
            txNoMatches = "Aucune transaction ne correspond à votre recherche.",
            txFetchOlder = "Récupération des anciens enregistrements...",
            txLoadOlder = "Charger les anciennes transactions",

            // Transaction Details
            detailTitle = "Détail de la transaction",
            detailPaymentMethod = "Mode de paiement",
            detailSource = "Source",
            detailSourceSms = "Alerte SMS (Fixe)",
            detailSourceManual = "Saisie manuelle (Fixe)",
            detailFee = "Frais de transaction",
            detailFeeCategory = "Catégorie de frais",
            detailStatus = "Statut",
            detailDescriptionLabel = "Description de la transaction",
            detailDescriptionPlaceholder = "Entrer la description...",
            detailCategoryLabel = "Catégorie",
            detailNotesLabel = "Notes",
            detailNotesPlaceholder = "Ajouter une note de transaction...",
            detailRecurringTitle = "Marquer comme récurrent",
            detailRecurringSubtitle = "Loyer mensuel, abonnements ou factures fixes",
            detailSaveChanges = "Enregistrer les modifications",
            detailDeletePrompt = "Supprimer la transaction",
            detailDeleteConfirm = "Êtes-vous sûr de vouloir supprimer cette transaction ?",

            // Add Transaction Screen
            addTitle = "Ajouter une transaction manuelle",
            addAmountLabel = "Entrer le montant",
            addFeeLabel = "Frais de transaction - Optionnel",
            addDescLabel = "Description / Nom du commerçant",
            addDescPlaceholder = "ex. Starbucks, Supermarché Simba",
            addCategoryLabel = "Sélectionner la catégorie",
            addSaveButton = "Enregistrer la transaction",

            // Reports Screen
            reportsTitle = "Rapports & Statistiques",
            reportsWeekly = "Hebdomadaire",
            reportsMonthly = "Mensuel",
            reportsYearly = "Annuel",
            reportsTotalSpending = "Dépenses totales",
            reportsTotalFees = "Frais de transaction totaux",
            reportsFeesCategory = "Catégorie : Frais de transaction",
            reportsSmartTipTitle = "Conseil intelligent",
            reportsSmartTipBody = "Les transactions sont automatiquement triées par catégorie. Vérifiez vos catégories pour garder des rapports précis.",
            reportsTopCategories = "Principales catégories de dépenses",
            reportsNoExpenses = "Aucune dépense catégorisée pour le moment",
            reportsExportTitle = "Exporter les données financières",
            reportsExportSubtitle = "Télécharger le résumé CSV / JSON pour vos archives",

            // Pending Review Screen
            pendingTitle = "En attente de révision",
            pendingAllSetTitle = "Tout est en ordre pour aujourd'hui !",
            pendingAllSetBody = "Aucune transaction en attente. La détection automatique par SMS vous avertira dès réception de nouvelles alertes.",
            pendingBackButton = "Retour au tableau de bord",
            pendingCategoryPrompt = "Sélectionner la catégorie :",
            pendingConfirmButton = "Confirmer et ajouter"
        )

        fun get(language: AppLanguage): AppStrings {
            return when (language) {
                AppLanguage.FRENCH -> French
                AppLanguage.ENGLISH -> English
            }
        }
    }
}

val LocalAppStrings = compositionLocalOf { AppStrings.English }
