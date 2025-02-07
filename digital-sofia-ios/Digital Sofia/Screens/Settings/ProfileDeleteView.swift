//
//  ProfileDeleteView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 6.06.23.
//

import SwiftUI

struct ProfileDeleteView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @Environment(\.dismiss) var dismiss
    @Binding var canDeleteUser: Bool
    @StateObject private var viewModel = DeleteProfileViewModel()
    
    var body: some View {
        LoadingStack(isPresented: $viewModel.isLoading) {
            VStack {
                LogoHeaderView(hideTitle: true)
                
                Spacer()
                
                Text(canDeleteUser ? AppConfig.UI.Text.deleteProfileDetails.localized : AppConfig.UI.Text.deleteProfileRequestDetails.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.large))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                Spacer()
                Spacer()
                
                HStack() {
                    BlueTextButton(title: AppConfig.UI.Titles.Button.back.localized) {
                        dismiss()
                    }
                    
                    if canDeleteUser {
                        BlueBackgroundButton(title: AppConfig.UI.Titles.Button.delete.localized,
                                             action: {
                            viewModel.deleteUser { error in
                                if error != nil {
                                    appState.alertItem = AlertProvider.errorAlert(message: error?.description ?? "")
                                }
                            }
                        })
                    }
                }
                .frame(width: .infinity)
                .padding(.bottom, AppConfig.Dimensions.Padding.XL)
            }
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXL)
        }
        .onChange(of: viewModel.networkError) { newValue in
            appState.alertItem = AlertProvider.errorAlert(message: newValue ?? "")
        }
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
}

struct ProfileDeleteView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileDeleteView(canDeleteUser: .constant(false))
    }
}
