//
//  RegisterBiometricsAuthView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.08.23.
//

import SwiftUI

struct RegisterBiometricsAuthView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @State private var showConfirmAuth = false
    @State private var loginViewModel: LoginViewModel?
    
    private let verticalPadding = AppConfig.Dimensions.Padding.XXL
    private let horizontalPadding = AppConfig.Dimensions.Padding.XXL
    
    var body: some View {
        VStack(spacing: verticalPadding) {
            navigation()
            
            Text(AppConfig.UI.Text.biometricDataTitle.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.top, .bottom], verticalPadding)
            
            Text(AppConfig.UI.Text.biometricDataDetails.localized)
                .multilineTextAlignment(.center)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.bottom, verticalPadding)
            
            Image(systemName: BiometricProvider.biometricType == .face ? ImageProvider.SystemImages.faceID : ImageProvider.SystemImages.touchID)
                .resizable()
                .scaledToFit()
                .foregroundColor(DSColors.Blue.blue)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .frame(width: AppConfig.Dimensions.Standart.iconHeight * 1.5, height: AppConfig.Dimensions.Standart.iconHeight * 1.5)
            
            Spacer()
            Spacer()
            
            HStack() {
                BlueTextButton(title: AppConfig.UI.Titles.Button.no.localized) {
                    showConfirmAuth = true
                }
                
                BlueBackgroundButton(title: AppConfig.UI.Titles.Button.yes.localized, action: {
                    loginViewModel?.authenticateWithBiometrics()
                })
            }
            .frame(width: .infinity)
            .padding(.bottom, verticalPadding)
        }
        .onAppear {
            self.loginViewModel = LoginViewModel(authenticationResult: { error in
                if let error = error {
                    appState.alertItem = AlertProvider.errorAlert(message: error.description)
                } else {
                    var user = UserProvider.shared.getUser()
                    user?.useBiometrics = true
                    UserProvider.shared.save(user: user)
                    
                    showConfirmAuth = true
                }
            })
        }
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
        .padding([.leading, .trailing], horizontalPadding)
        .background(DSColors.background)
        .navigationBarHidden(true)
    }
    
    private func navigation() -> some View {
        HStack {
            NavigationLink(destination: AuthenticationConfirmationView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showConfirmAuth) { EmptyView() }
        }
    }
}

struct RegisterBiometricsAuthView_Previews: PreviewProvider {
    static var previews: some View {
        RegisterBiometricsAuthView()
    }
}
