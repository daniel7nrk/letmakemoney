import SwiftUI

public struct CountrySelectionView: View {
    @ObservedObject var viewModel: MainViewModel
    public let onContinue: () -> Void

    public var body: some View {
        ZStack {
            Color.promptSurface.ignoresSafeArea()

            VStack(alignment: .leading, spacing: 20) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Select Your Country")
                        .font(.system(size: 26, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    Text("This customizes your default mobile money provider patterns and currency display.")
                        .font(.system(size: 15))
                        .foregroundColor(.promptTextSecondary)
                }
                .padding(.horizontal, 24)
                .padding(.top, 24)

                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(Country.allCases) { country in
                            let isSelected = (viewModel.selectedCountry == country)
                            Button(action: {
                                viewModel.selectCountry(country)
                            }) {
                                HStack(spacing: 16) {
                                    Text(country.flagEmoji)
                                        .font(.system(size: 32))

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(country.countryName)
                                            .font(.system(size: 16, weight: .semibold))
                                            .foregroundColor(.promptTextPrimary)

                                        Text("\(country.currencyCode) - \(country.currencyName)")
                                            .font(.system(size: 13))
                                            .foregroundColor(.promptTextSecondary)
                                    }

                                    Spacer()

                                    if isSelected {
                                        Image(systemName: "checkmark.circle.fill")
                                            .font(.system(size: 22))
                                            .foregroundColor(.royalBlue)
                                    } else {
                                        Image(systemName: "circle")
                                            .font(.system(size: 22))
                                            .foregroundColor(.promptBorder)
                                    }
                                }
                                .padding(16)
                                .background(isSelected ? Color.royalBlue.opacity(0.06) : Color.promptCard)
                                .cornerRadius(14)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 14)
                                        .stroke(isSelected ? Color.royalBlue : Color.promptBorder, lineWidth: 1.5)
                                )
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding(.horizontal, 24)
                }

                Spacer()

                Button(action: onContinue) {
                    Text("Continue")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.royalBlue)
                        .cornerRadius(14)
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 16)
            }
        }
    }
}
