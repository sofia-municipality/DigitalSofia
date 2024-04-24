//
//  KeyPad.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.04.23.
//

import SwiftUI

struct KeyPad: View {
    @Binding var string: String
    @Binding var isRecording: Bool
    
    var limit: Int?
    var shouldShowBiometrics: Bool = false
    var didClickOnBiometrics: (() -> ())?
    
    var body: some View {
        VStack {
            KeyPadRow(keys: ["1", "2", "3"], isRecording: $isRecording)
            KeyPadRow(keys: ["4", "5", "6"], isRecording: $isRecording)
            KeyPadRow(keys: ["7", "8", "9"], isRecording: $isRecording)
            KeyPadRow(keys: [" ", "0", "⌫"], shouldShowBiometrics: shouldShowBiometrics, isRecording: $isRecording)
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
    @Binding var isRecording: Bool
    
    var body: some View {
        HStack(spacing: 0) {
            ForEach(keys, id: \.self) { key in
                KeyPadButton(key: key, shouldShowBiometrics: shouldShowBiometrics, isRecording: $isRecording)
            }
        }
    }
}

struct KeyPadButton: View {
    var key: String
    var shouldShowBiometrics: Bool = false
    @Binding var isRecording: Bool
    
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
        .buttonStyle(StaticButtonStyle(isRecording: isRecording))
        .padding([.top, .bottom], AppConfig.Dimensions.Padding.medium)
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.large / 2)
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

struct StaticButtonStyle: ButtonStyle {
    var isRecording: Bool
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .opacity((configuration.isPressed && !isRecording) ? 0.5 : 1.0)
    }
}
