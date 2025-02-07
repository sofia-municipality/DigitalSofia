//
//  NotificationPermissionAgreementView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 13.11.24.
//

import SwiftUI

struct NotificationPermissionAgreementView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var showRegister = false
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            Image(ImageProvider.launchScreenSplashBg)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .opacity(0.5)
                .ignoresSafeArea()
            
            navigation()
            
            VStack(spacing: 0) {
                LogoHeaderView()
                    .padding(.bottom, AppConfig.Dimensions.Padding.XL)
                
                Text(AppConfig.UI.Text.notificationPermissionAgreementTitleText.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.medium))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .lineSpacing(7)
                    .frame(maxWidth: .infinity, alignment: .center)
                
                Spacer()
                
                Text(AppConfig.UI.Text.notificationPermissionAgreementSubtitleText.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.medium))
                    .foregroundColor(DSColors.Text.notificationRed)
                    .lineSpacing(7)
                    .frame(maxWidth: .infinity, alignment: .center)
                
                Spacer()
                Spacer()
                
                HStack {
                    BlueBackgroundButton(title: AppConfig.UI.Titles.Button.understood.localized) {
                        PermissionProvider.getNotificationsPermission { _ in
                            showRegister = true
                        }
                    }
                }
            }
            .padding([.leading, .trailing], RegisterFlowConstants.padding * 1.1)
            .padding([.top, .bottom], AppConfig.Dimensions.Standart.rowHeight)
        }
        .log(view: self)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: RegisterView()
                .environmentObject(networkMonitor)
                .environmentObject(appState)
                .environmentObject(KeyboardInactivityHandlerConfig()),
                           isActive: $showRegister) { EmptyView() }
        }
    }
}

#Preview {
    NotificationPermissionAgreementView()
}
