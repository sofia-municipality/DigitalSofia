//
//  AdaptableSizeButton.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 21.07.23.
//

import UIKit

class AdaptableSizeButton: UIButton {
    override var intrinsicContentSize: CGSize {
        let labelSize = titleLabel?.sizeThatFits(CGSize(width: frame.size.width, height: CGFloat.greatestFiniteMagnitude)) ?? .zero
        if let insets = configuration?.contentInsets {
            let size = CGSize(width: labelSize.width + insets.leading + insets.trailing, height: labelSize.height + insets.top + insets.bottom)
            return size
        }
        
        return .zero
    }
}
