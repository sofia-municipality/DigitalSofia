//
//  ViewController+ETCustomization.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.12.23.
//

import UIKit
import EvrotrustSDK

extension ViewController {
    func customizeET() {
        let customization: EvrotrustCustomization = EvrotrustCustomization()
        customization.mainColor1 = DSColors.ETCustomColors.evrotrustMainColor1.uiColor
        customization.mainColor2 = DSColors.ETCustomColors.evrotrustMainColor2.uiColor
        customization.mainColor3 = DSColors.ETCustomColors.evrotrustMainColor3.uiColor
        customization.backgroundColor1 = DSColors.ETCustomColors.evrotrustBackgroundColor1.uiColor
        customization.backgroundColor2 = DSColors.ETCustomColors.evrotrustBackgroundColor2.uiColor
        customization.backgroundColor3 = DSColors.ETCustomColors.evrotrustBackgroundColor3.uiColor
        customization.textColor1 = DSColors.ETCustomColors.evrotrustTextColor1.uiColor
        customization.textColor2 = DSColors.ETCustomColors.evrotrustTextColor2.uiColor
        customization.hintTextColor = DSColors.ETCustomColors.evrotrustHintTextColor.uiColor
        
        if let logo = UIImage(named: ImageProvider.ETImages.logoVC),
           let logoInstructions = UIImage(named: ImageProvider.ETImages.logoScanInstructionsVC) {
            customization.imageCustomizations.questionsTitleImage = logo
            customization.imageCustomizations.contactsTitleImage = logo
            customization.imageCustomizations.documentsTitleImage = logo
            customization.imageCustomizations.scanInstructionsImage = logoInstructions
        }
        
        customization.setCustomFont("SofiaSans-Regular", withFileName: "SofiaSans-Regular", andFileExtension: "ttf")
        Evrotrust.sdk()?.setCustomization(customization)
    }
}
