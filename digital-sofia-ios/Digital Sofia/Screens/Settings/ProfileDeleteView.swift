//
//  ProfileDeleteView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 6.06.23.
//

import SwiftUI

struct ProfileDeleteView: View {
    @Environment(\.presentationMode) var presentationMode
    var hasDeleteRequest = false
    
    var body: some View {
        VStack {
            LogoHeaderView(hideTitle: true)
            
            Spacer()
            
            Text(hasDeleteRequest ? AppConfig.UI.Text.deleteProfileRequestDetails.localized : AppConfig.UI.Text.deleteProfileDetails.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.large))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Spacer()
            Spacer()
            
            HStack() {
                BlueTextButton(title: AppConfig.UI.Titles.Button.back.localized) {
                    presentationMode.wrappedValue.dismiss()
                }
                
                if hasDeleteRequest == false {
                    BlueBackgroundButton(title: AppConfig.UI.Titles.Button.delete.localized, action: {
                        UserProvider.shared.logout()
                    })
                }
            }
            .frame(width: .infinity)
            .padding(.bottom, AppConfig.Dimensions.Padding.XL)
        }
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXL)
        .background(DSColors.background)
        .navigationBarHidden(true)
    }
}

struct ProfileDeleteView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileDeleteView()
    }
}
