//
//  LoginFromPortalNotificationView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 30.11.23.
//

import SwiftUI

enum LoginRequestStatus: String {
    case confirmed, cancelled
}

struct LoginFromPortalNotificationView: View {
    @Environment(\.dismiss) var dismiss
    var loginRequestResponse: (LoginRequestStatus) -> ()
    
    private let padding = AppConfig.Dimensions.Padding.XXL
    
    var body: some View {
        VStack {
            Spacer()
            
            Text(AppConfig.UI.Text.loginFromPortalTitleText.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.bold, size: DSFonts.FontSize.XLarge))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.bottom], padding)
            
            Spacer()
            
            Image(ImageProvider.loginFromPortalLogo)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: UIScreen.main.bounds.width * 0.35, height: UIScreen.main.bounds.height * 0.2)
            
            Spacer()
            
            Text(AppConfig.UI.Text.loginFromPortalDetailsTitleText.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.bold, size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.bottom], padding)
            
            Text(AppConfig.UI.Text.loginFromPortalDetailsText.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .multilineTextAlignment(.center)
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
            
            Spacer()
            
            HStack() {
                BlueTextButton(title: AppConfig.UI.Titles.Button.deny.localized) {
                    loginRequestResponse(.cancelled)
                    dismiss()
                }
                .padding([.trailing], padding / 2)
                
                BlueBackgroundButton(title: AppConfig.UI.Titles.Button.confirm.localized, action: {
                    loginRequestResponse(.confirmed)
                    dismiss()
                })
                .padding([.leading], padding / 2)
            }
            
            Spacer()
            Spacer()
        }
        .log(view: self)
        .alert()
        .padding([.leading, .trailing], padding)
    }
}

#Preview {
    LoginFromPortalNotificationView(loginRequestResponse: { _ in})
}
