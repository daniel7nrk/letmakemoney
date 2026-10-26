import SwiftUI

public struct NotificationsView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject var viewModel: MainViewModel
    @State private var selectedFilter: String = "All"
    @State private var showSimulateSms: Bool = false

    private let filterOptions = ["All", "Pending Review", "Scam Alerts", "Reports", "Security"]

    public init(viewModel: MainViewModel) {
        self.viewModel = viewModel
    }

    public var filteredNotifications: [AppNotification] {
        if selectedFilter == "All" {
            return viewModel.notifications
        }
        return viewModel.notifications.filter { $0.type == selectedFilter }
    }

    public var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Filter Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(filterOptions, id: \.self) { filter in
                            let isSelected = selectedFilter == filter
                            Button(action: { selectedFilter = filter }) {
                                Text(filter)
                                    .font(.urbanist(13, weight: isSelected ? .bold : .medium))
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(isSelected ? Color.royalBlue : Color.promptCard)
                                    .foregroundColor(isSelected ? .white : .promptTextPrimary)
                                    .clipShape(Capsule())
                                    .overlay(
                                        Capsule()
                                            .stroke(isSelected ? Color.clear : Color.promptBorder, lineWidth: 1)
                                    )
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                }
                .background(Color.promptSurface)

                Divider()

                // Notifications List
                if filteredNotifications.isEmpty {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: "bell.slash")
                            .font(.system(size: 42))
                            .foregroundColor(.promptTextSecondary.opacity(0.6))
                        Text("No notifications in \(selectedFilter)")
                            .font(.urbanist(16, weight: .semibold))
                            .foregroundColor(.promptTextSecondary)
                    }
                    Spacer()
                } else {
                    List {
                        ForEach(filteredNotifications) { notif in
                            NotificationRowView(notification: notif) {
                                viewModel.deleteNotification(notif)
                            }
                            .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                            .listRowBackground(Color.clear)
                            .listRowSeparator(.hidden)
                        }
                    }
                    .listStyle(PlainListStyle())
                }

                // Quick Simulation & Test Notification Section
                VStack(spacing: 10) {
                    Text("Developer Test Triggers")
                        .font(.urbanist(12, weight: .bold))
                        .foregroundColor(.promptTextSecondary)
                        .textCase(.uppercase)

                    HStack(spacing: 10) {
                        Button(action: { viewModel.triggerPendingReviewReminder() }) {
                            Text("Pending Review")
                                .font(.urbanist(12, weight: .semibold))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .background(Color.promptCard)
                                .foregroundColor(.promptTextPrimary)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(Color.promptBorder, lineWidth: 1))
                        }

                        Button(action: { viewModel.triggerMonthlyReportNotification() }) {
                            Text("Monthly Report")
                                .font(.urbanist(12, weight: .semibold))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .background(Color.promptCard)
                                .foregroundColor(.promptTextPrimary)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(Color.promptBorder, lineWidth: 1))
                        }

                        Button(action: { showSimulateSms = true }) {
                            Text("+ Simulate SMS")
                                .font(.urbanist(12, weight: .bold))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .background(Color.royalBlue)
                                .foregroundColor(.white)
                                .clipShape(Capsule())
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(Color.promptCard)
            }
            .background(Color.promptSurface.ignoresSafeArea())
            .navigationBarTitle("Notifications", displayMode: .inline)
            .navigationBarItems(
                leading: Button("Close") {
                    presentationMode.wrappedValue.dismiss()
                },
                trailing: Button("Clear All") {
                    viewModel.clearAllNotifications()
                }
                .foregroundColor(.promptDanger)
            )
            .sheet(isPresented: $showSimulateSms) {
                SimulateSmsSheet(viewModel: viewModel)
            }
        }
    }
}

public struct NotificationRowView: View {
    public let notification: AppNotification
    public let onDelete: () -> Void

    public var body: some View {
        HStack(alignment: .top, spacing: 14) {
            // Icon based on type
            ZStack {
                Circle()
                    .fill(iconBgColor.opacity(0.12))
                    .frame(width: 40, height: 40)

                Image(systemName: iconName)
                    .font(.system(size: 18))
                    .foregroundColor(iconBgColor)
            }

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(notification.title)
                        .font(.urbanist(15, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    Spacer()

                    Text(notification.timestamp)
                        .font(.urbanist(12, weight: .regular))
                        .foregroundColor(.promptTextSecondary)
                }

                Text(notification.message)
                    .font(.urbanist(13, weight: .regular))
                    .foregroundColor(.promptTextSecondary)
                    .lineLimit(2)

                HStack {
                    Text(notification.type)
                        .font(.urbanist(10, weight: .bold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(iconBgColor.opacity(0.1))
                        .foregroundColor(iconBgColor)
                        .clipShape(Capsule())

                    Spacer()

                    Button(action: onDelete) {
                        Image(systemName: "trash")
                            .font(.system(size: 13))
                            .foregroundColor(.promptTextSecondary.opacity(0.6))
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                .padding(.top, 4)
            }
        }
        .padding(14)
        .background(Color.promptCard)
        .cornerRadius(18)
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(Color.promptBorder, lineWidth: 1)
        )
    }

    private var iconName: String {
        switch notification.type {
        case "Scam Alerts": return "shield.slash.fill"
        case "Pending Review": return "exclamationmark.circle.fill"
        case "Reports": return "chart.pie.fill"
        case "Security": return "lock.shield.fill"
        default: return "bell.fill"
        }
    }

    private var iconBgColor: Color {
        switch notification.type {
        case "Scam Alerts": return .rapportTomato
        case "Pending Review": return .rapportButter
        case "Reports": return .rapportPine
        case "Security": return .royalBlue
        default: return .royalBlue
        }
    }
}
