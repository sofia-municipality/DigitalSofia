//
//  AlertItem.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 26.10.23.
//

import SwiftUI

class BaseAlertItem: Identifiable {
    var id = UUID()
    var title: Text
    var message: Text?
    
    init(title: Text, message: Text? = nil) {
        self.title = title
        self.message = message
    }
}

class DismissAlertItem: BaseAlertItem {
    var dismissButton: Alert.Button?
    
    init(title: Text, message: Text? = nil, dismissButton: Alert.Button? = nil) {
        super.init(title: title, message: message)
        self.dismissButton = dismissButton
    }
    
    var alert: Alert {
        return Alert(title: title, 
                     message: message,
                     dismissButton: dismissButton)
    }
}

class PrimaryAndSecondaryAlertItem: BaseAlertItem {
    var primaryButton: Alert.Button?
    var secondaryButton: Alert.Button?
    
    init(title: Text, message: Text? = nil, primaryButton: Alert.Button? = nil, secondaryButton: Alert.Button? = nil) {
        super.init(title: title, message: message)
        self.primaryButton = primaryButton
        self.secondaryButton = secondaryButton
    }
    
    var alert: Alert {
        return Alert(title: title,
                     message: message,
                     primaryButton: primaryButton ?? .cancel(),
                     secondaryButton: secondaryButton ?? .cancel())
    }
}
