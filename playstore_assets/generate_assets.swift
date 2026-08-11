import Cocoa
import CoreGraphics
import ImageIO
import UniformTypeIdentifiers

let assetsDir = "/Volumes/Thunderbolt/Users/pkormann/evcharge-calc-android/playstore_assets"
try? FileManager.default.createDirectory(atPath: assetsDir, withIntermediateDirectories: true, attributes: nil)

func saveCGImageAsPNG(cgImage: CGImage, path: String) {
    let url = URL(fileURLWithPath: path) as CFURL
    guard let destination = CGImageDestinationCreateWithURL(url, UTType.png.identifier as CFString, 1, nil) else {
        print("Failed to create destination for \(path)")
        return
    }
    CGImageDestinationAddImage(destination, cgImage, nil)
    if CGImageDestinationFinalize(destination) {
        print("Saved: \(path)")
    } else {
        print("Failed to write \(path)")
    }
}

func drawCarAndCharger(context: CGContext, centerX: CGFloat, centerY: CGFloat, size: CGFloat) {
    let scale = size / 1024.0
    let ox = (centerX / scale) - 512.0
    let oy = (centerY / scale) - 512.0
    
    func pt(_ x: CGFloat, _ y: CGFloat) -> CGPoint {
        return CGPoint(x: (x + ox) * scale, y: (y + oy) * scale)
    }
    
    let white = CGColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0)
    context.setStrokeColor(white)
    context.setFillColor(white)
    context.setLineCap(.round)
    context.setLineJoin(.round)
    
    let mainStrokeW = 34.0 * scale
    let detailStrokeW = 24.0 * scale
    context.setLineWidth(mainStrokeW)
    
    // Wheels
    let rearWheel = pt(380, 335)
    let frontWheel = pt(690, 335)
    let wheelOuterR = 66.0 * scale
    let wheelInnerR = 24.0 * scale
    let wheelArchR = 96.0 * scale
    
    context.strokeEllipse(in: CGRect(x: frontWheel.x - wheelOuterR, y: frontWheel.y - wheelOuterR, width: wheelOuterR * 2, height: wheelOuterR * 2))
    context.fillEllipse(in: CGRect(x: frontWheel.x - wheelInnerR, y: frontWheel.y - wheelInnerR, width: wheelInnerR * 2, height: wheelInnerR * 2))
    
    context.strokeEllipse(in: CGRect(x: rearWheel.x - wheelOuterR, y: rearWheel.y - wheelOuterR, width: wheelOuterR * 2, height: wheelOuterR * 2))
    context.fillEllipse(in: CGRect(x: rearWheel.x - wheelInnerR, y: rearWheel.y - wheelInnerR, width: wheelInnerR * 2, height: wheelInnerR * 2))
    
    // Car body
    let carPath = CGMutablePath()
    carPath.move(to: pt(240, 335))
    carPath.addCurve(to: pt(245, 445), control1: pt(235, 365), control2: pt(238, 410))
    carPath.addCurve(to: pt(310, 500), control1: pt(255, 475), control2: pt(280, 495))
    
    carPath.addCurve(to: pt(460, 660), control1: pt(345, 565), control2: pt(395, 645))
    carPath.addCurve(to: pt(610, 660), control1: pt(510, 672), control2: pt(560, 672))
    
    carPath.addCurve(to: pt(735, 500), control1: pt(665, 645), control2: pt(710, 565))
    carPath.addCurve(to: pt(820, 455), control1: pt(765, 480), control2: pt(795, 470))
    carPath.addCurve(to: pt(830, 395), control1: pt(840, 440), control2: pt(845, 415))
    carPath.addCurve(to: pt(790, 335), control1: pt(825, 355), control2: pt(810, 335))
    
    carPath.addLine(to: pt(690 + 96, 335))
    carPath.addArc(center: frontWheel, radius: wheelArchR, startAngle: 0, endAngle: CGFloat.pi, clockwise: false)
    
    carPath.addLine(to: pt(380 + 96, 335))
    carPath.addArc(center: rearWheel, radius: wheelArchR, startAngle: 0, endAngle: CGFloat.pi, clockwise: false)
    carPath.addLine(to: pt(240, 335))
    
    context.addPath(carPath)
    context.strokePath()
    
    // Windows
    let windowPath = CGMutablePath()
    windowPath.move(to: pt(460, 605))
    windowPath.addLine(to: pt(520, 608))
    windowPath.addLine(to: pt(520, 505))
    windowPath.addLine(to: pt(345, 505))
    windowPath.addCurve(to: pt(460, 605), control1: pt(375, 550), control2: pt(415, 595))
    
    windowPath.move(to: pt(550, 608))
    windowPath.addCurve(to: pt(600, 605), control1: pt(565, 608), control2: pt(585, 607))
    windowPath.addCurve(to: pt(680, 505), control1: pt(635, 595), control2: pt(665, 550))
    windowPath.addLine(to: pt(550, 505))
    windowPath.closeSubpath()
    
    context.saveGState()
    context.setLineWidth(detailStrokeW)
    context.addPath(windowPath)
    context.strokePath()
    context.restoreGState()
    
    // Details
    let headlightCenter = pt(800, 435)
    let headlightR = 20.0 * scale
    context.strokeEllipse(in: CGRect(x: headlightCenter.x - headlightR, y: headlightCenter.y - headlightR, width: headlightR * 2, height: headlightR * 2))
    let headlightInnerR = 8.0 * scale
    context.fillEllipse(in: CGRect(x: headlightCenter.x - headlightInnerR, y: headlightCenter.y - headlightInnerR, width: headlightInnerR * 2, height: headlightInnerR * 2))
    
    let taillightCenter = pt(265, 455)
    let taillightR = 14.0 * scale
    context.strokeEllipse(in: CGRect(x: taillightCenter.x - taillightR, y: taillightCenter.y - taillightR, width: taillightR * 2, height: taillightR * 2))
    
    let handlePath = CGMutablePath()
    handlePath.move(to: pt(480, 480))
    handlePath.addLine(to: pt(515, 480))
    context.saveGState()
    context.setLineWidth(detailStrokeW)
    context.addPath(handlePath)
    context.strokePath()
    context.restoreGState()
    
    // Charger & Cable
    let boxW: CGFloat = 84 * scale
    let boxH: CGFloat = 120 * scale
    let boxRect = CGRect(x: pt(85, 610).x, y: pt(85, 610).y, width: boxW, height: boxH)
    let boxPath = CGPath(roundedRect: boxRect, cornerWidth: 22 * scale, cornerHeight: 22 * scale, transform: nil)
    
    context.saveGState()
    context.setLineWidth(mainStrokeW)
    context.addPath(boxPath)
    context.strokePath()
    
    let boltPath = CGMutablePath()
    boltPath.move(to: pt(132, 705))
    boltPath.addLine(to: pt(115, 665))
    boltPath.addLine(to: pt(129, 665))
    boltPath.addLine(to: pt(122, 628))
    boltPath.addLine(to: pt(141, 672))
    boltPath.addLine(to: pt(127, 672))
    boltPath.closeSubpath()
    
    context.setFillColor(white)
    context.addPath(boltPath)
    context.fillPath()
    context.restoreGState()
    
    let cablePath = CGMutablePath()
    cablePath.move(to: pt(285, 490))
    cablePath.addCurve(to: pt(150, 430), control1: pt(225, 485), control2: pt(180, 380))
    cablePath.addCurve(to: pt(127, 610), control1: pt(125, 475), control2: pt(127, 550))
    
    context.saveGState()
    context.setLineWidth(detailStrokeW)
    context.addPath(cablePath)
    context.strokePath()
    
    let plugRect = CGRect(x: pt(280, 480).x, y: pt(280, 480).y, width: 22 * scale, height: 20 * scale)
    let plugPath = CGPath(roundedRect: plugRect, cornerWidth: 6 * scale, cornerHeight: 6 * scale, transform: nil)
    context.setFillColor(white)
    context.addPath(plugPath)
    context.fillPath()
    context.restoreGState()
}

// 1. Generate 512x512 App Icon
func generate512Icon() -> CGImage? {
    let size = 512
    let colorSpace = CGColorSpaceCreateDeviceRGB()
    guard let ctx = CGContext(data: nil, width: size, height: size, bitsPerComponent: 8, bytesPerRow: size * 4, space: colorSpace, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue) else { return nil }
    
    ctx.setAllowsAntialiasing(true)
    ctx.setShouldAntialias(true)
    
    let bgGreen = CGColor(red: 25/255.0, green: 165/255.0, blue: 75/255.0, alpha: 1.0)
    ctx.setFillColor(bgGreen)
    ctx.fill(CGRect(x: 0, y: 0, width: size, height: size))
    
    drawCarAndCharger(context: ctx, centerX: 256 + 10, centerY: 256 + 5, size: 512)
    return ctx.makeImage()
}

// 2. Generate 1024x500 Feature Graphic
func generateFeatureGraphic() -> CGImage? {
    let width = 1024
    let height = 500
    let colorSpace = CGColorSpaceCreateDeviceRGB()
    guard let ctx = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8, bytesPerRow: width * 4, space: colorSpace, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue) else { return nil }
    
    ctx.setAllowsAntialiasing(true)
    ctx.setShouldAntialias(true)
    
    // Background gradient: Rich emerald green to deep teal
    let colors = [
        CGColor(red: 22/255.0, green: 163/255.0, blue: 74/255.0, alpha: 1.0),
        CGColor(red: 15/255.0, green: 118/255.0, blue: 110/255.0, alpha: 1.0)
    ] as CFArray
    let gradient = CGGradient(colorsSpace: colorSpace, colors: colors, locations: [0.0, 1.0])!
    ctx.drawLinearGradient(gradient, start: CGPoint(x: 0, y: 500), end: CGPoint(x: 1024, y: 0), options: [])
    
    // Draw decorative circle glow on the right
    ctx.setFillColor(CGColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.08))
    ctx.fillEllipse(in: CGRect(x: 650, y: 50, width: 400, height: 400))
    ctx.setFillColor(CGColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.04))
    ctx.fillEllipse(in: CGRect(x: 580, y: -20, width: 540, height: 540))
    
    // Draw car & charger illustration on right
    drawCarAndCharger(context: ctx, centerX: 820, centerY: 240, size: 420)
    
    // Feature graphic texts
    let title = "EV Charge Calc"
    let titleAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 58, weight: .bold),
        .foregroundColor: NSColor.white
    ]
    (title as NSString).draw(at: NSPoint(x: 64, y: 285), withAttributes: titleAttrs)
    
    let subtitle = "Smart EV Charging Energy & Limit Calculator"
    let subtitleAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 26, weight: .semibold),
        .foregroundColor: NSColor(red: 220/255.0, green: 252/255.0, blue: 231/255.0, alpha: 1.0)
    ]
    (subtitle as NSString).draw(at: NSPoint(x: 66, y: 235), withAttributes: subtitleAttrs)
    
    let tag = "⚡ Target SOC  •  Battery Health (SOH)  •  go-e Charger Integration"
    let tagAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 18, weight: .medium),
        .foregroundColor: NSColor(white: 1.0, alpha: 0.85)
    ]
    (tag as NSString).draw(at: NSPoint(x: 66, y: 180), withAttributes: tagAttrs)
    
    return ctx.makeImage()
}

// 3. Generate Screenshots (1080x2400)
func generateScreenshotCard(title: String, subtitle: String, screenType: Int) -> CGImage? {
    let width = 1080
    let height = 2400
    let colorSpace = CGColorSpaceCreateDeviceRGB()
    guard let ctx = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8, bytesPerRow: width * 4, space: colorSpace, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue) else { return nil }
    
    ctx.setAllowsAntialiasing(true)
    ctx.setShouldAntialias(true)
    
    // Background gradient: Modern dark aesthetic
    let bgColors = [
        CGColor(red: 18/255.0, green: 24/255.0, blue: 38/255.0, alpha: 1.0),
        CGColor(red: 10/255.0, green: 15/255.0, blue: 26/255.0, alpha: 1.0)
    ] as CFArray
    let bgGradient = CGGradient(colorsSpace: colorSpace, colors: bgColors, locations: [0.0, 1.0])!
    ctx.drawLinearGradient(bgGradient, start: CGPoint(x: 0, y: 2400), end: CGPoint(x: 0, y: 0), options: [])
    
    // Top glow
    ctx.setFillColor(CGColor(red: 22/255.0, green: 163/255.0, blue: 74/255.0, alpha: 0.12))
    ctx.fillEllipse(in: CGRect(x: 100, y: 1700, width: 880, height: 600))
    
    let nsContext = NSGraphicsContext(cgContext: ctx, flipped: false)
    NSGraphicsContext.current = nsContext
    
    // Header Title
    let titleAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 66, weight: .heavy),
        .foregroundColor: NSColor.white
    ]
    let titleRect = (title as NSString).boundingRect(with: NSSize(width: 960, height: 200), options: .usesLineFragmentOrigin, attributes: titleAttrs)
    (title as NSString).draw(at: NSPoint(x: (1080 - titleRect.width) / 2, y: 2150), withAttributes: titleAttrs)
    
    // Subtitle
    let subAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 34, weight: .medium),
        .foregroundColor: NSColor(red: 134/255.0, green: 239/255.0, blue: 172/255.0, alpha: 1.0)
    ]
    let subRect = (subtitle as NSString).boundingRect(with: NSSize(width: 960, height: 100), options: .usesLineFragmentOrigin, attributes: subAttrs)
    (subtitle as NSString).draw(at: NSPoint(x: (1080 - subRect.width) / 2, y: 2060), withAttributes: subAttrs)
    
    // Phone Device Mockup Frame
    let phoneX: CGFloat = 90
    let phoneY: CGFloat = 80
    let phoneW: CGFloat = 900
    let phoneH: CGFloat = 1900
    let phoneCorner: CGFloat = 64
    
    let phoneRect = CGRect(x: phoneX, y: phoneY, width: phoneW, height: phoneH)
    let phonePath = CGPath(roundedRect: phoneRect, cornerWidth: phoneCorner, cornerHeight: phoneCorner, transform: nil)
    
    // Outer phone border (Sleek slate dark glass)
    ctx.saveGState()
    ctx.setFillColor(CGColor(red: 24/255.0, green: 30/255.0, blue: 44/255.0, alpha: 1.0))
    ctx.setStrokeColor(CGColor(red: 55/255.0, green: 65/255.0, blue: 81/255.0, alpha: 1.0))
    ctx.setLineWidth(6)
    ctx.addPath(phonePath)
    ctx.drawPath(using: .fillStroke)
    ctx.restoreGState()
    
    // Inside Screen Area
    let screenPadding: CGFloat = 20
    let screenRect = CGRect(x: phoneX + screenPadding, y: phoneY + screenPadding, width: phoneW - (screenPadding * 2), height: phoneH - (screenPadding * 2))
    let screenPath = CGPath(roundedRect: screenRect, cornerWidth: phoneCorner - 10, cornerHeight: phoneCorner - 10, transform: nil)
    
    ctx.saveGState()
    ctx.addPath(screenPath)
    ctx.clip()
    
    // Screen background
    ctx.setFillColor(CGColor(red: 15/255.0, green: 23/255.0, blue: 42/255.0, alpha: 1.0)) // Slate-900
    ctx.fill(screenRect)
    
    // App Top Bar inside Screen
    let topBarH: CGFloat = 120
    let topBarY = screenRect.maxY - topBarH
    ctx.setFillColor(CGColor(red: 30/255.0, green: 41/255.0, blue: 59/255.0, alpha: 1.0))
    ctx.fill(CGRect(x: screenRect.minX, y: topBarY, width: screenRect.width, height: topBarH))
    
    let appTitle = screenType == 3 ? "Settings" : "EVCharge Calc"
    let appTitleAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 38, weight: .bold),
        .foregroundColor: NSColor.white
    ]
    (appTitle as NSString).draw(at: NSPoint(x: screenRect.minX + 40, y: topBarY + 36), withAttributes: appTitleAttrs)
    
    if screenType == 1 || screenType == 2 || screenType == 3 {
        // Main Screen UI Elements:
        // Result Card (Hero)
        let heroRect = CGRect(x: screenRect.minX + 36, y: topBarY - 340, width: screenRect.width - 72, height: 300)
        let heroPath = CGPath(roundedRect: heroRect, cornerWidth: 28, cornerHeight: 28, transform: nil)
        
        let heroGradColors = [
            CGColor(red: 22/255.0, green: 163/255.0, blue: 74/255.0, alpha: 1.0),
            CGColor(red: 16/255.0, green: 185/255.0, blue: 129/255.0, alpha: 1.0)
        ] as CFArray
        let heroGrad = CGGradient(colorsSpace: colorSpace, colors: heroGradColors, locations: [0.0, 1.0])!
        
        ctx.saveGState()
        ctx.addPath(heroPath)
        ctx.clip()
        ctx.drawLinearGradient(heroGrad, start: CGPoint(x: heroRect.minX, y: heroRect.maxY), end: CGPoint(x: heroRect.maxX, y: heroRect.minY), options: [])
        ctx.restoreGState()
        
        let resLabel = "Required Energy"
        let resLabelAttrs: [NSAttributedString.Key: Any] = [
            .font: NSFont.systemFont(ofSize: 26, weight: .semibold),
            .foregroundColor: NSColor(white: 1.0, alpha: 0.9)
        ]
        (resLabel as NSString).draw(at: NSPoint(x: heroRect.minX + 36, y: heroRect.maxY - 65), withAttributes: resLabelAttrs)
        
        let energyValue = screenType == 2 ? "53.1 kWh" : "39.8 kWh"
        let energyAttrs: [NSAttributedString.Key: Any] = [
            .font: NSFont.systemFont(ofSize: 76, weight: .heavy),
            .foregroundColor: NSColor.white
        ]
        (energyValue as NSString).draw(at: NSPoint(x: heroRect.minX + 36, y: heroRect.maxY - 170), withAttributes: energyAttrs)
        
        let energySub = screenType == 2 ? "+60% SOC (20% ➔ 80%)  •  Incl. 10% charging losses" : "+45% SOC (35% ➔ 80%)  •  Incl. 10% charging losses"
        let energySubAttrs: [NSAttributedString.Key: Any] = [
            .font: NSFont.systemFont(ofSize: 22, weight: .medium),
            .foregroundColor: NSColor(white: 1.0, alpha: 0.85)
        ]
        (energySub as NSString).draw(at: NSPoint(x: heroRect.minX + 36, y: heroRect.maxY - 235), withAttributes: energySubAttrs)
        
        // Sliders Card
        let slidersRect = CGRect(x: screenRect.minX + 36, y: heroRect.minY - 450, width: screenRect.width - 72, height: 410)
        let slidersPath = CGPath(roundedRect: slidersRect, cornerWidth: 28, cornerHeight: 28, transform: nil)
        ctx.setFillColor(CGColor(red: 30/255.0, green: 41/255.0, blue: 59/255.0, alpha: 1.0))
        ctx.addPath(slidersPath)
        ctx.fillPath()
        
        let curLabel = screenType == 2 ? "Current Battery (SOC):  20 %" : "Current Battery (SOC):  35 %"
        let curAttrs: [NSAttributedString.Key: Any] = [
            .font: NSFont.systemFont(ofSize: 26, weight: .bold),
            .foregroundColor: NSColor.white
        ]
        (curLabel as NSString).draw(at: NSPoint(x: slidersRect.minX + 30, y: slidersRect.maxY - 60), withAttributes: curAttrs)
        
        // Current SOC Slider Bar
        let curPct: CGFloat = screenType == 2 ? 0.20 : 0.35
        ctx.setFillColor(CGColor(red: 51/255.0, green: 65/255.0, blue: 85/255.0, alpha: 1.0))
        ctx.fill(CGRect(x: slidersRect.minX + 30, y: slidersRect.maxY - 110, width: slidersRect.width - 60, height: 16))
        ctx.setFillColor(CGColor(red: 34/255.0, green: 197/255.0, blue: 94/255.0, alpha: 1.0))
        ctx.fill(CGRect(x: slidersRect.minX + 30, y: slidersRect.maxY - 110, width: (slidersRect.width - 60) * curPct, height: 16))
        ctx.fillEllipse(in: CGRect(x: slidersRect.minX + 30 + (slidersRect.width - 60) * curPct - 20, y: slidersRect.maxY - 110 - 12, width: 40, height: 40))
        
        let targetLabel = "Target Battery (SOC):  80 %"
        (targetLabel as NSString).draw(at: NSPoint(x: slidersRect.minX + 30, y: slidersRect.maxY - 220), withAttributes: curAttrs)
        
        // Target SOC Slider Bar
        ctx.setFillColor(CGColor(red: 51/255.0, green: 65/255.0, blue: 85/255.0, alpha: 1.0))
        ctx.fill(CGRect(x: slidersRect.minX + 30, y: slidersRect.maxY - 270, width: slidersRect.width - 60, height: 16))
        ctx.setFillColor(CGColor(red: 59/255.0, green: 130/255.0, blue: 246/255.0, alpha: 1.0))
        ctx.fill(CGRect(x: slidersRect.minX + 30, y: slidersRect.maxY - 270, width: (slidersRect.width - 60) * 0.8, height: 16))
        ctx.fillEllipse(in: CGRect(x: slidersRect.minX + 30 + (slidersRect.width - 60) * 0.8 - 20, y: slidersRect.maxY - 270 - 12, width: 40, height: 40))
        
        // Quick Presets Buttons
        let presetsY = slidersRect.minY - 260
        let presetW = (screenRect.width - 72 - 32) / 3
        let presets = ["80% Daily", "90% Top Up", "100% Trip"]
        for (i, p) in presets.enumerated() {
            let pRect = CGRect(x: screenRect.minX + 36 + CGFloat(i) * (presetW + 16), y: presetsY, width: presetW, height: 95)
            let pPath = CGPath(roundedRect: pRect, cornerWidth: 20, cornerHeight: 20, transform: nil)
            ctx.setFillColor(i == 0 ? CGColor(red: 22/255.0, green: 163/255.0, blue: 74/255.0, alpha: 0.3) : CGColor(red: 30/255.0, green: 41/255.0, blue: 59/255.0, alpha: 1.0))
            ctx.setStrokeColor(i == 0 ? CGColor(red: 34/255.0, green: 197/255.0, blue: 94/255.0, alpha: 1.0) : CGColor(red: 71/255.0, green: 85/255.0, blue: 105/255.0, alpha: 1.0))
            ctx.setLineWidth(2)
            ctx.addPath(pPath)
            ctx.drawPath(using: .fillStroke)
            
            let pAttrs: [NSAttributedString.Key: Any] = [
                .font: NSFont.systemFont(ofSize: 22, weight: .bold),
                .foregroundColor: i == 0 ? NSColor(red: 134/255.0, green: 239/255.0, blue: 172/255.0, alpha: 1.0) : NSColor.white
            ]
            let pTextRect = (p as NSString).boundingRect(with: NSSize(width: 200, height: 50), options: .usesLineFragmentOrigin, attributes: pAttrs)
            (p as NSString).draw(at: NSPoint(x: pRect.minX + (pRect.width - pTextRect.width) / 2, y: pRect.minY + 30), withAttributes: pAttrs)
        }
        
        // Wallbox Action Button
        let wbRect = CGRect(x: screenRect.minX + 36, y: presetsY - 140, width: screenRect.width - 72, height: 110)
        let wbPath = CGPath(roundedRect: wbRect, cornerWidth: 24, cornerHeight: 24, transform: nil)
        ctx.setFillColor(CGColor(red: 37/255.0, green: 99/255.0, blue: 235/255.0, alpha: 1.0))
        ctx.addPath(wbPath)
        ctx.fillPath()
        
        let wbText = "⚡ Send Limit to go-e Charger"
        let wbAttrs: [NSAttributedString.Key: Any] = [
            .font: NSFont.systemFont(ofSize: 28, weight: .bold),
            .foregroundColor: NSColor.white
        ]
        let wbTextRect = (wbText as NSString).boundingRect(with: NSSize(width: 700, height: 50), options: .usesLineFragmentOrigin, attributes: wbAttrs)
        (wbText as NSString).draw(at: NSPoint(x: wbRect.minX + (wbRect.width - wbTextRect.width) / 2, y: wbRect.minY + 38), withAttributes: wbAttrs)
        
        // If Screen 3: draw Success Confirmation Toast
        if screenType == 3 {
            let toastRect = CGRect(x: screenRect.minX + 50, y: wbRect.minY - 130, width: screenRect.width - 100, height: 90)
            let toastPath = CGPath(roundedRect: toastRect, cornerWidth: 18, cornerHeight: 18, transform: nil)
            ctx.setFillColor(CGColor(red: 6/255.0, green: 78/255.0, blue: 59/255.0, alpha: 0.95))
            ctx.setStrokeColor(CGColor(red: 16/255.0, green: 185/255.0, blue: 129/255.0, alpha: 1.0))
            ctx.setLineWidth(2)
            ctx.addPath(toastPath)
            ctx.drawPath(using: .fillStroke)
            
            let toastText = "✓ Limit (39800 Wh) sent to Wallbox (192.168.1.150)"
            let toastAttrs: [NSAttributedString.Key: Any] = [
                .font: NSFont.systemFont(ofSize: 22, weight: .bold),
                .foregroundColor: NSColor(red: 167/255.0, green: 243/255.0, blue: 208/255.0, alpha: 1.0)
            ]
            let tRect = (toastText as NSString).boundingRect(with: NSSize(width: 700, height: 40), options: .usesLineFragmentOrigin, attributes: toastAttrs)
            (toastText as NSString).draw(at: NSPoint(x: toastRect.minX + (toastRect.width - tRect.width) / 2, y: toastRect.minY + 30), withAttributes: toastAttrs)
        }
        
    } else if screenType == 4 {
        // Settings Screen Mockup
        let items = [
            ("Usable Battery Capacity", "77.0 kWh", "Net battery capacity of your EV"),
            ("State of Health (SOH)", "96 %", "Accounts for battery degradation over time"),
            ("Charging Losses", "10 %", "Compensates for AC/DC conversion losses"),
            ("go-e Charger IP Address", "192.168.1.150", "Local IP of your wallbox on home Wi-Fi")
        ]
        
        var curY = topBarY - 180
        for item in items {
            let cardRect = CGRect(x: screenRect.minX + 36, y: curY - 120, width: screenRect.width - 72, height: 160)
            let cardPath = CGPath(roundedRect: cardRect, cornerWidth: 22, cornerHeight: 22, transform: nil)
            ctx.setFillColor(CGColor(red: 30/255.0, green: 41/255.0, blue: 59/255.0, alpha: 1.0))
            ctx.addPath(cardPath)
            ctx.fillPath()
            
            let labelAttrs: [NSAttributedString.Key: Any] = [
                .font: NSFont.systemFont(ofSize: 26, weight: .bold),
                .foregroundColor: NSColor.white
            ]
            (item.0 as NSString).draw(at: NSPoint(x: cardRect.minX + 30, y: cardRect.maxY - 48), withAttributes: labelAttrs)
            
            let valAttrs: [NSAttributedString.Key: Any] = [
                .font: NSFont.systemFont(ofSize: 28, weight: .heavy),
                .foregroundColor: NSColor(red: 52/255.0, green: 211/255.0, blue: 153/255.0, alpha: 1.0)
            ]
            let valRect = (item.1 as NSString).boundingRect(with: NSSize(width: 300, height: 50), options: .usesLineFragmentOrigin, attributes: valAttrs)
            (item.1 as NSString).draw(at: NSPoint(x: cardRect.maxX - valRect.width - 30, y: cardRect.maxY - 50), withAttributes: valAttrs)
            
            let descAttrs: [NSAttributedString.Key: Any] = [
                .font: NSFont.systemFont(ofSize: 20, weight: .regular),
                .foregroundColor: NSColor(red: 148/255.0, green: 163/255.0, blue: 184/255.0, alpha: 1.0)
            ]
            (item.2 as NSString).draw(at: NSPoint(x: cardRect.minX + 30, y: cardRect.minY + 25), withAttributes: descAttrs)
            
            curY -= 195
        }
    }
    
    ctx.restoreGState()
    return ctx.makeImage()
}

// Generate files
if let icon = generate512Icon() {
    saveCGImageAsPNG(cgImage: icon, path: "\(assetsDir)/icon_512x512.png")
}

if let feature = generateFeatureGraphic() {
    saveCGImageAsPNG(cgImage: feature, path: "\(assetsDir)/feature_graphic_1024x500.png")
}

if let s1 = generateScreenshotCard(title: "Precise Energy Calculation", subtitle: "Exact kWh required to reach your target SOC", screenType: 1) {
    saveCGImageAsPNG(cgImage: s1, path: "\(assetsDir)/screenshot_1_calculator.png")
}

if let s2 = generateScreenshotCard(title: "1-Tap Quick Presets", subtitle: "80% Daily, 90% Top Up, or 100% Road Trip", screenType: 2) {
    saveCGImageAsPNG(cgImage: s2, path: "\(assetsDir)/screenshot_2_presets.png")
}

if let s3 = generateScreenshotCard(title: "go-e Charger Integration", subtitle: "Send calculated energy limit directly to wallbox", screenType: 3) {
    saveCGImageAsPNG(cgImage: s3, path: "\(assetsDir)/screenshot_3_wallbox.png")
}

if let s4 = generateScreenshotCard(title: "Custom EV Battery Settings", subtitle: "Configure usable capacity, SOH & charging losses", screenType: 4) {
    saveCGImageAsPNG(cgImage: s4, path: "\(assetsDir)/screenshot_4_settings.png")
}

print("All 4 Play Store screenshots generated successfully!")

print("All Play Store assets generated successfully!")
