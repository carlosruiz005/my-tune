import SwiftUI

@main
struct MyTuneApp: App {
    // Initialize repositories
    private let tuningRepository = TuningRepository()
    private let settingsRepository = SettingsRepository()
    @StateObject private var themeProvider = ThemeProvider()
    
    var body: some Scene {
        WindowGroup {
            TunerView(
                tuningRepository: tuningRepository,
                settingsRepository: settingsRepository
            )
            .environmentObject(themeProvider)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        TunerView(
            tuningRepository: TuningRepository(),
            settingsRepository: SettingsRepository()
        )
    }
}
