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
    @State private var showTagAlert = false
    //    @State private var showNotificationPermission = false
    
    var body: some View {
        BetaTagScreenCover(isPresented: $showTagAlert) {
            LaunchScreenBackground(
                navigationItem: AnyView(BetaTagView {
                    showTagAlert = true
                }),
                content: VStack {
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
                        GradientBorderButton(title: AppConfig.UI.Titles.Button.enter.localized) {
                            showRegister = true
                            //                            PermissionProvider.getNotificationsStatus { status in
                            //                                switch status {
                            //                                case .notDetermined:
                            //                                    showNotificationPermission = true
                            //                                default:
                            //                                    showRegister = true
                            //                                }
                            //                            }
                        }
                    }
                })
        }
        .onAppear {
            DocumentsNotificationHelper.resetTab()
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: RegisterView()
                .environmentObject(networkMonitor)
                .environmentObject(appState)
                .environmentObject(KeyboardInactivityHandlerConfig()),
                           isActive: $showRegister) { EmptyView() }
            
            NavigationLink(destination: TabbarView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showHome) { EmptyView() }
            
            //            NavigationLink(destination: NotificationPermissionAgreementView()
            //                .environmentObject(appState)
            //                .environmentObject(networkMonitor),
            //                           isActive: $showNotificationPermission) { EmptyView() }
            
            NavigationLink(destination: RegisterView()
                .environmentObject(networkMonitor)
                .environmentObject(appState)
                .environmentObject(KeyboardInactivityHandlerConfig()),
                           isActive: $showRegister) { EmptyView() }
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        InitialView(appState: AppState())
    }
}
