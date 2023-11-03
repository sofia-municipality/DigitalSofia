//
//  LoginView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.07.23.
//

import SwiftUI

struct InitialView: View {
    @ObservedObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var showRegister = false
    @State private var showHome = false
    
    var body: some View {
        LaunchScreenBackground(content: VStack {
            Text(AppConfig.UI.Text.welcomeToDigitalSofiaText.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.top, .bottom], AppConfig.Dimensions.Padding.XXXL)
            
            Text(AppConfig.UI.Titles.Screens.login.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XL))
                .foregroundColor(DSColors.Text.indigoDark)
                .lineSpacing(6)
                .frame(maxWidth: .infinity, alignment: .center)
            
            Spacer()
            
            navigation()
            
            HStack {
                BlueBackgroundButton(title: AppConfig.UI.Titles.Button.forward.localized) {
                    showRegister = true
                }
            }
        })
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: RegisterView()
                .environmentObject(networkMonitor)
                .environmentObject(appState),
                           isActive: $showRegister) { EmptyView() }
            
            NavigationLink(destination: TabbarView(appState: appState).environmentObject(networkMonitor),
                           isActive: $showHome) { EmptyView() }
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        InitialView(appState: AppState())
    }
}
