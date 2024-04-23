//
//  ConfirmDataShareView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.10.23.
//

import SwiftUI

struct ConfirmDataShareView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var identityConfig: IdentityRequestConfig
    @Environment(\.dismiss) var dismiss
    
    private let verticalPadding = AppConfig.Dimensions.Padding.XXXL
    private let horizontalPadding = AppConfig.Dimensions.Padding.XXXL
    
    var body: some View {
        IdentityRequestView(content: {
            VStack(spacing: verticalPadding) {
                Text(AppConfig.UI.Text.shareDataTitleText.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, 
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.XXXL))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding([.top, .bottom], verticalPadding)
                
                Text(AppConfig.UI.Text.shareDataDetailsText.localized)
                    .multilineTextAlignment(.center)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, 
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.large))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .center)
                
                Spacer()
                
                HStack() {
                    BlueTextButton(title: AppConfig.UI.Titles.Button.no.localized) {
                        dismiss()
                    }
                    
                    BlueBackgroundButton(title: AppConfig.UI.Titles.Button.yes.localized, action: {
                        identityConfig.fetchRequest = true
                    })
                }
                .padding(.bottom, verticalPadding)
            }
            .padding([.leading, .trailing], horizontalPadding)
        }, type: .authenticate)
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .log(view: self)
    }
}

#Preview {
    ConfirmDataShareView()
}
