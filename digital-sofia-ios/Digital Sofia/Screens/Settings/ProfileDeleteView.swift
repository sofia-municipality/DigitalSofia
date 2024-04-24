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
    
    @StateObject private var viewModel = DeleteProfileViewModel()
    @State private var isLoading = false
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack {
                LogoHeaderView(hideTitle: true)
                
                Spacer()
                
                Text(viewModel.hasPendingRequests ? AppConfig.UI.Text.deleteProfileRequestDetails.localized : AppConfig.UI.Text.deleteProfileDetails.localized)
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
                    
                    if viewModel.hasPendingRequests == false {
                        BlueBackgroundButton(title: AppConfig.UI.Titles.Button.delete.localized, 
                                             action: {
                            isLoading = true
                            viewModel.deleteUser { error in
                                isLoading = false
                                
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
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
}

struct ProfileDeleteView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileDeleteView()
    }
}
