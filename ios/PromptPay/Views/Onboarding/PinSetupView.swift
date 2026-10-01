import SwiftUI

public struct PinSetupView: View {
    @ObservedObject var viewModel: MainViewModel
    public let onComplete: () -> Void

    @State private var pin: String = ""
    @State private var confirmPin: String = ""
    @State private var isConfirming: Bool = false
    @State private var errorMessage: String? = nil

    public var body: some View {
        ZStack {
            Color.promptSurface.ignoresSafeArea()

            VStack(spacing: 32) {
                VStack(spacing: 8) {
                    Image(systemName: "lock.shield.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.royalBlue)
                        .padding(.top, 40)

                    Text(isConfirming ? "Confirm Your PIN" : "Set Up 4-Digit PIN")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    Text(isConfirming ? "Re-enter the 4-digit PIN you just chose" : "Secure PromptPay on your iPhone")
                        .font(.system(size: 15))
                        .foregroundColor(.promptTextSecondary)
                }

                // PIN indicator dots
                HStack(spacing: 20) {
                    let currentLength = isConfirming ? confirmPin.count : pin.count
                    ForEach(0..<4) { index in
                        Circle()
                            .fill(index < currentLength ? Color.royalBlue : Color.promptBorder)
                            .frame(width: 18, height: 18)
                    }
                }
                .padding(.vertical, 16)

                if let error = errorMessage {
                    Text(error)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.promptDanger)
                }

                Spacer()

                // Custom Keypad
                VStack(spacing: 16) {
                    ForEach([[1, 2, 3], [4, 5, 6], [7, 8, 9]], id: \.self) { row in
                        HStack(spacing: 32) {
                            ForEach(row, id: \.self) { number in
                                keypadButton("\(number)") {
                                    appendDigit("\(number)")
                                }
                            }
                        }
                    }

                    HStack(spacing: 32) {
                        Color.clear.frame(width: 72, height: 72)

                        keypadButton("0") {
                            appendDigit("0")
                        }

                        Button(action: deleteDigit) {
                            Image(systemName: "delete.left.fill")
                                .font(.system(size: 22))
                                .foregroundColor(.promptTextPrimary)
                                .frame(width: 72, height: 72)
                        }
                    }
                }
                .padding(.bottom, 32)
            }
        }
    }

    private func keypadButton(_ text: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(text)
                .font(.system(size: 28, weight: .medium))
                .foregroundColor(.promptTextPrimary)
                .frame(width: 72, height: 72)
                .background(Color.promptCard)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color.promptBorder, lineWidth: 1))
        }
    }

    private func appendDigit(_ digit: String) {
        errorMessage = nil
        if !isConfirming {
            guard pin.count < 4 else { return }
            pin.append(digit)
            if pin.count == 4 {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                    isConfirming = true
                }
            }
        } else {
            guard confirmPin.count < 4 else { return }
            confirmPin.append(digit)
            if confirmPin.count == 4 {
                if pin == confirmPin {
                    viewModel.savePin(pin)
                    onComplete()
                } else {
                    errorMessage = "PINs do not match. Try again."
                    confirmPin = ""
                    pin = ""
                    isConfirming = false
                }
            }
        }
    }

    private func deleteDigit() {
        if isConfirming {
            if !confirmPin.isEmpty { confirmPin.removeLast() }
        } else {
            if !pin.isEmpty { pin.removeLast() }
        }
    }
}
