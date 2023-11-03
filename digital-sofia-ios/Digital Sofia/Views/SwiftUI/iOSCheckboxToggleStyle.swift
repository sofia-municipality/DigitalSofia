//
//  iOSCheckboxToggleStyle.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.04.23.
//

import SwiftUI

struct iOSCheckboxToggleStyle: ToggleStyle {
    func makeBody(configuration: Configuration) -> some View {
        Button(action: {
            configuration.isOn.toggle()
        }, label: {
            HStack {
                ZStack {
                    RoundedRectangle(cornerRadius: AppConfig.Dimensions.CornerRadius.mini)
                        .stroke(configuration.isOn ? DSColors.Blue.blue : DSColors.toggleDeselected,
                                lineWidth: AppConfig.Dimensions.Standart.lineHeight / 2)
                        .background {
                            configuration.isOn ? DSColors.Blue.blue : Color.white
                        }
                    
                    if configuration.isOn {
                        Image(ImageProvider.check)
                            .foregroundColor(Color.white)
                            .padding(.all, AppConfig.Dimensions.Padding.standart)
                    }
                }
                .frame(width: AppConfig.Dimensions.Standart.iconHeight, height: AppConfig.Dimensions.Standart.iconHeight)
                
                configuration.label
            }
        })
    }
}
