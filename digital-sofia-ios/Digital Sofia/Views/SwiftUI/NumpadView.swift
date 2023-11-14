//
//  NumpadView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.04.23.
//

import SwiftUI

struct NumpadView: View {
    @Binding var reset: Bool
    
    var shouldShowBiometrics: Bool = false
    var pinCodeLength = 6
    var didReachedCodeLength: (([Int]) -> ())?
    var didClickOnBiometrics: (() -> ())?
    
    @State private var pin: [Int] = []
    @State private var string = ""
    
    var body: some View {
        VStack {
            HStack() {
                ForEach((0..<(pin.isEmpty ? 0 : pin.count)), id: \.self) { _ in
                    createDot(active: true)
                }
                
                ForEach((0..<(pinCodeLength - (pin.isEmpty ? 0 : pin.count))), id: \.self) { _ in
                    createDot(active: false)
                }
            }
            
            Spacer()
            
            KeyPad(string: $string, limit: pinCodeLength, shouldShowBiometrics: shouldShowBiometrics, didClickOnBiometrics: didClickOnBiometrics)
            
            Spacer()
            Spacer()
        }
        .onChange(of: string, perform: { newValue in
            let array = Array(newValue)
            pin = array.compactMap { Int(String($0)) }
            
            if pin.count == pinCodeLength {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    didReachedCodeLength?(pin)
                }
            }
        })
        .onChange(of: reset) { newValue in
            if reset {
                pin = []
                string = ""
                reset = false
            }
        }
    }
    
    private func createDot(active: Bool) -> some View {
        Circle()
            .strokeBorder(active ? DSColors.Blue.blue : DSColors.Blue.blue1, lineWidth: 2)
            .background(Circle().foregroundColor(active ? DSColors.Blue.blue : Color.white))
            .frame(width: 10, height: 10)
    }
}


