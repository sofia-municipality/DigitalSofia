//
//  KeyPad.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.04.23.
//

import SwiftUI

struct KeyPad: View {
    @Binding var string: String
    var limit: Int?
    var shouldShowBiometrics: Bool = false
    var didClickOnBiometrics: (() -> ())?
    
    var body: some View {
        VStack {
            KeyPadRow(keys: ["1", "2", "3"])
            KeyPadRow(keys: ["4", "5", "6"])
            KeyPadRow(keys: ["7", "8", "9"])
            KeyPadRow(keys: [" ", "0", "⌫"], shouldShowBiometrics: shouldShowBiometrics)
        }.environment(\.keyPadButtonAction, self.keyWasPressed(_:))
    }
    
    private func keyWasPressed(_ key: String) {
        switch key {
        case "⌫":
            if string.isEmpty == false {
                string.removeLast()
            }
        case " ":
            didClickOnBiometrics?()
        default:
            if let limit = limit {
                if string.count <= limit - 1 {
                    string += key
                }
            } else {
                string += key
            }
        }
    }
}

extension EnvironmentValues {
    var keyPadButtonAction: (String) -> Void {
        get { self[KeyPadButton.ActionKey.self] }
        set { self[KeyPadButton.ActionKey.self] = newValue }
    }
}

struct KeyPadRow: View {
    var keys: [String]
    var shouldShowBiometrics: Bool = false
    
    var body: some View {
        HStack {
            ForEach(keys, id: \.self) { key in
                KeyPadButton(key: key, shouldShowBiometrics: shouldShowBiometrics)
            }
        }
    }
}

struct KeyPadButton: View {
    var key: String
    var shouldShowBiometrics: Bool = false
    
    var body: some View {
        Button(action: { self.action(self.key) }) {
            if key == "⌫" {
                Image(systemName: ImageProvider.SystemImages.deleteLeft)
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .center)
            } else if key == " " {
                if shouldShowBiometrics {
                    Image(systemName: BiometricProvider.biometricType == .face ? ImageProvider.SystemImages.faceID : ImageProvider.SystemImages.touchID)
                        .foregroundColor(DSColors.Text.indigoDark)
                        .frame(maxWidth: .infinity, alignment: .center)
                } else {
                    getKey(key: key)
                }
            } else {
                getKey(key: key)
            }
        }
        .padding(.all, AppConfig.Dimensions.Padding.XL)
    }
    
    private func getKey(key: String) -> some View {
        Text(key)
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXLarge))
            .foregroundColor(DSColors.Text.indigoDark)
            .frame(maxWidth: .infinity, alignment: .center)
    }
    
    enum ActionKey: EnvironmentKey {
        static var defaultValue: (String) -> Void { { _ in } }
    }
    
    @Environment(\.keyPadButtonAction) var action: (String) -> Void
}
