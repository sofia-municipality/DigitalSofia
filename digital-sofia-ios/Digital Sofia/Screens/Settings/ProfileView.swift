//
//  ProfileView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.06.23.
//

import SwiftUI

struct ProfileView: View {
    
    var body: some View {
        VStack() {
            CustomNavigationBar()
            
            ProfileTiltleView(title: SettingsType.profile.description)
            
            VStack(alignment: .leading) {
                Image(ImageProvider.person)
                    .padding(.bottom, AppConfig.Dimensions.Padding.medium)
                
                profileInfoView()
                
                Spacer()
            }
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXXL)
        }
        .background(DSColors.background)
        .navigationBarHidden(true)
    }
    
    private func profileInfoView() -> some View {
        let user = UserProvider.shared.getUser()
        let name = LanguageProvider.shared.appLanguage == .bulgarian ? user?.fullName : user?.fullLatinName
        
        return Text(name ?? "")
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XL))
            .foregroundColor(DSColors.Text.indigoDark)
            .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileView()
    }
}
