//
//  ProfileLanguage.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.06.23.
//

import SwiftUI

struct ProfileLanguageView: View {
    @State private var isEnglish = LanguageProvider.shared.appLanguage == .english
    @State private var isBulgarian = LanguageProvider.shared.appLanguage == .bulgarian
    @EnvironmentObject var appState: AppState
    
    var body: some View {
        VStack() {
            CustomNavigationBar()
            
            ProfileTiltleView(title: SettingsType.language.description)
            
            Spacer()
            
            VStack {
                CustomToggleView(title: Languages.bulgarian.description, toggle: $isBulgarian)
                    .onChange(of: isBulgarian, perform: { value in
                        isEnglish = !value
                        
                        if value {
                            changeLanguage(new: .bulgarian)
                        }
                    })
                    .padding(.bottom, AppConfig.Dimensions.Padding.XXXXL)
                
                CustomToggleView(title: Languages.english.description, toggle: $isEnglish)
                    .onChange(of: isEnglish, perform: { value in
                        isBulgarian = !value
                        
                        if value {
                            changeLanguage(new: .english)
                        }
                    })
                    .padding(.bottom, AppConfig.Dimensions.Padding.XXXXL)
            }
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXXL)
            
            Spacer()
            Spacer()
            Spacer()
        }
        .background(DSColors.background)
        .navigationBarHidden(true)
    }
    
    private func changeLanguage(new: Languages) {
        LanguageProvider.shared.appLanguage = new
        appState.language = new
    }
}

struct ProfileLanguage_Previews: PreviewProvider {
    static var previews: some View {
        ProfileLanguageView()
    }
}
