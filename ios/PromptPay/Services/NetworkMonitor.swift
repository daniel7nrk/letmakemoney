import Foundation
import Network
import Combine

/// Monitors network reachability using NWPathMonitor.
/// Publishes `isConnected` on the main thread — wire a `.onChange` to trigger Supabase sync.
public final class NetworkMonitor: ObservableObject {
    public static let shared = NetworkMonitor()

    @Published public private(set) var isConnected: Bool = false

    private let monitor = NWPathMonitor()
    private let monitorQueue = DispatchQueue(label: "com.promptpay.networkmonitor", qos: .utility)

    private init() {
        monitor.pathUpdateHandler = { [weak self] path in
            let connected = path.status == .satisfied
            DispatchQueue.main.async {
                self?.isConnected = connected
            }
        }
        monitor.start(queue: monitorQueue)
    }

    deinit {
        monitor.cancel()
    }
}
