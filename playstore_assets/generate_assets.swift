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

func loadCGImage(path: String) -> CGImage? {
    guard let provider = CGDataProvider(filename: path) else { return nil }
    return CGImage(pngDataProviderSource: provider, decode: nil, shouldInterpolate: true, intent: .defaultIntent)
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
    
    context.strokeEllipse(in: CGRect(x: frontWheel.x - wheelOuterR, y: frontWheel.y - wheelOuterR, width: wheelOuterR * 2, height: wheelOuterR * 2))
    context.fillEllipse(in: CGRect(x: frontWheel.x - wheelInnerR, y: frontWheel.y - wheelInnerR, width: wheelInnerR * 2, height: wheelInnerR * 2))
    
    context.strokeEllipse(in: CGRect(x: rearWheel.x - wheelOuterR, y: rearWheel.y - wheelOuterR, width: wheelOuterR * 2, height: wheelOuterR * 2))
    context.fillEllipse(in: CGRect(x: rearWheel.x - wheelInnerR, y: rearWheel.y - wheelInnerR, width: wheelInnerR * 2, height: wheelInnerR * 2))
    
    // Car body
    let carPath = CGMutablePath()
    carPath.move(to: pt(240, 335))
    carPath.addCurve(to: pt(245, 445), control1: pt(235, 365), control2: pt(238, 410))
    carPath.addCurve(to: pt(310, 500), control1: pt(255, 475), control2: pt(280, 495))
    carPath.addLine(to: pt(380, 510))
    carPath.addCurve(to: pt(460, 680), control1: pt(400, 570), control2: pt(425, 640))
    carPath.addLine(to: pt(620, 680))
    carPath.addCurve(to: pt(760, 510), control1: pt(675, 625), control2: pt(715, 560))
    carPath.addLine(to: pt(820, 485))
    carPath.addCurve(to: pt(860, 430), control1: pt(845, 475), control2: pt(855, 455))
    carPath.addLine(to: pt(860, 355))
    carPath.addCurve(to: pt(830, 335), control1: pt(860, 340), control2: pt(845, 335))
    carPath.addLine(to: pt(755, 335))
    
    // Front wheel arch
    carPath.addArc(center: frontWheel, radius: 96.0 * scale, startAngle: CGFloat.pi, endAngle: 0, clockwise: false)
    carPath.addLine(to: pt(445, 335))
    // Rear wheel arch
    carPath.addArc(center: rearWheel, radius: 96.0 * scale, startAngle: CGFloat.pi, endAngle: 0, clockwise: false)
    carPath.addLine(to: pt(240, 335))
    
    context.addPath(carPath)
    context.strokePath()
    
    // Windows
    let winPath = CGMutablePath()
    winPath.move(to: pt(470, 655))
    winPath.addLine(to: pt(530, 655))
    winPath.addLine(to: pt(530, 520))
    winPath.addLine(to: pt(415, 520))
    winPath.addCurve(to: pt(470, 655), control1: pt(435, 570), control2: pt(455, 620))
    winPath.closeSubpath()
    
    winPath.move(to: pt(550, 655))
    winPath.addLine(to: pt(610, 655))
    winPath.addCurve(to: pt(730, 520), control1: pt(655, 610), control2: pt(690, 560))
    winPath.addLine(to: pt(550, 520))
    winPath.closeSubpath()
    
    context.setLineWidth(detailStrokeW)
    context.addPath(winPath)
    context.strokePath()
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
    
    let colors = [
        CGColor(red: 22/255.0, green: 163/255.0, blue: 74/255.0, alpha: 1.0),
        CGColor(red: 15/255.0, green: 118/255.0, blue: 110/255.0, alpha: 1.0)
    ] as CFArray
    let gradient = CGGradient(colorsSpace: colorSpace, colors: colors, locations: [0.0, 1.0])!
    ctx.drawLinearGradient(gradient, start: CGPoint(x: 0, y: 500), end: CGPoint(x: 1024, y: 0), options: [])
    
    ctx.setFillColor(CGColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.08))
    ctx.fillEllipse(in: CGRect(x: 650, y: 50, width: 400, height: 400))
    ctx.setFillColor(CGColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.04))
    ctx.fillEllipse(in: CGRect(x: 580, y: -20, width: 540, height: 540))
    
    drawCarAndCharger(context: ctx, centerX: 820, centerY: 240, size: 420)
    
    let nsContext = NSGraphicsContext(cgContext: ctx, flipped: false)
    NSGraphicsContext.current = nsContext
    
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
    
    let tag = "⚡ Target SOC  •  Battery Health (SOH)  •  go-e Charger Automation"
    let tagAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 18, weight: .medium),
        .foregroundColor: NSColor(white: 1.0, alpha: 0.85)
    ]
    (tag as NSString).draw(at: NSPoint(x: 66, y: 180), withAttributes: tagAttrs)
    
    return ctx.makeImage()
}

// 3. Generate High-Res Play Store Screenshot Cards with Real Screenshots
func generateFramedScreenshot(title: String, subtitle: String, rawImageName: String) -> CGImage? {
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
    
    // Ambient glow
    ctx.setFillColor(CGColor(red: 22/255.0, green: 163/255.0, blue: 74/255.0, alpha: 0.15))
    ctx.fillEllipse(in: CGRect(x: 100, y: 1700, width: 880, height: 600))
    
    let nsContext = NSGraphicsContext(cgContext: ctx, flipped: false)
    NSGraphicsContext.current = nsContext
    
    // Header Title
    let titleAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 62, weight: .heavy),
        .foregroundColor: NSColor.white
    ]
    let titleRect = (title as NSString).boundingRect(with: NSSize(width: 960, height: 200), options: .usesLineFragmentOrigin, attributes: titleAttrs)
    (title as NSString).draw(at: NSPoint(x: (1080 - titleRect.width) / 2, y: 2150), withAttributes: titleAttrs)
    
    // Subtitle
    let subAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 32, weight: .medium),
        .foregroundColor: NSColor(red: 134/255.0, green: 239/255.0, blue: 172/255.0, alpha: 1.0)
    ]
    let subRect = (subtitle as NSString).boundingRect(with: NSSize(width: 960, height: 100), options: .usesLineFragmentOrigin, attributes: subAttrs)
    (subtitle as NSString).draw(at: NSPoint(x: (1080 - subRect.width) / 2, y: 2065), withAttributes: subAttrs)
    
    // Phone Device Frame
    let phoneX: CGFloat = 90
    let phoneY: CGFloat = 80
    let phoneW: CGFloat = 900
    let phoneH: CGFloat = 1920
    let phoneCorner: CGFloat = 56
    
    let phoneRect = CGRect(x: phoneX, y: phoneY, width: phoneW, height: phoneH)
    let phonePath = CGPath(roundedRect: phoneRect, cornerWidth: phoneCorner, cornerHeight: phoneCorner, transform: nil)
    
    // Device shadow
    ctx.saveGState()
    ctx.setShadow(offset: CGSize(width: 0, height: -20), blur: 50, color: CGColor(red: 0, green: 0, blue: 0, alpha: 0.6))
    ctx.setFillColor(CGColor(red: 30/255.0, green: 41/255.0, blue: 59/255.0, alpha: 1.0))
    ctx.addPath(phonePath)
    ctx.fillPath()
    ctx.restoreGState()
    
    // Phone bezel border
    ctx.saveGState()
    ctx.setFillColor(CGColor(red: 24/255.0, green: 30/255.0, blue: 44/255.0, alpha: 1.0))
    ctx.setStrokeColor(CGColor(red: 71/255.0, green: 85/255.0, blue: 105/255.0, alpha: 0.8))
    ctx.setLineWidth(5)
    ctx.addPath(phonePath)
    ctx.drawPath(using: .fillStroke)
    ctx.restoreGState()
    
    // Inside Screen
    let screenPadding: CGFloat = 12
    let screenRect = CGRect(x: phoneX + screenPadding, y: phoneY + screenPadding, width: phoneW - (screenPadding * 2), height: phoneH - (screenPadding * 2))
    let screenPath = CGPath(roundedRect: screenRect, cornerWidth: phoneCorner - 8, cornerHeight: phoneCorner - 8, transform: nil)
    
    ctx.saveGState()
    ctx.addPath(screenPath)
    ctx.clip()
    
    // Draw the real screenshot into screenRect
    if let rawImage = loadCGImage(path: "\(assetsDir)/\(rawImageName)") {
        ctx.draw(rawImage, in: screenRect)
    } else {
        // Fallback white background
        ctx.setFillColor(CGColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0))
        ctx.fill(screenRect)
    }
    
    ctx.restoreGState()
    return ctx.makeImage()
}

// Generate all 4 Screenshots
if let s1 = generateFramedScreenshot(
    title: "Precise EV Calculation",
    subtitle: "Exact kWh & required charging energy at a glance",
    rawImageName: "raw_s1.png"
) {
    saveCGImageAsPNG(cgImage: s1, path: "\(assetsDir)/screenshot_1_calculator.png")
}

if let s2 = generateFramedScreenshot(
    title: "1-Tap Quick Presets",
    subtitle: "Daily 80% • Top Up 90% • Road Trip 100%",
    rawImageName: "raw_s2.png"
) {
    saveCGImageAsPNG(cgImage: s2, path: "\(assetsDir)/screenshot_2_presets.png")
}

if let s3 = generateFramedScreenshot(
    title: "go-eCharger Integration",
    subtitle: "Send calculated limit directly to your wallbox",
    rawImageName: "raw_s3.png"
) {
    saveCGImageAsPNG(cgImage: s3, path: "\(assetsDir)/screenshot_3_wallbox.png")
}

if let s4 = generateFramedScreenshot(
    title: "Battery & 9 Languages",
    subtitle: "Configure SOH, losses & in-app language",
    rawImageName: "raw_s4.png"
) {
    saveCGImageAsPNG(cgImage: s4, path: "\(assetsDir)/screenshot_4_settings.png")
}

if let icon = generate512Icon() {
    saveCGImageAsPNG(cgImage: icon, path: "\(assetsDir)/icon_512x512.png")
}

if let feature = generateFeatureGraphic() {
    saveCGImageAsPNG(cgImage: feature, path: "\(assetsDir)/feature_graphic_1024x500.png")
}

print("All 4 new Play Store screenshots and assets generated successfully!")
