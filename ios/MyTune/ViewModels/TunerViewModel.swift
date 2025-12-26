//
//  TunerViewModel.swift
//  MyTune
//
//  ViewModel for the tuner screen, managing audio processing and state.
//

import Foundation
import Combine

/// ViewModel managing tuner state and audio processing
@MainActor
class TunerViewModel: ObservableObject {
    @Published var pitchResult: PitchResult?
    @Published var isRunning: Bool = false
    @Published var errorMessage: String?
    @Published var currentTuning: Tuning
    @Published var showInTuneConfirmation: Bool = false
    
    private let audioProcessor: IOSAudioProcessor
    private let tuningRepository: TuningRepositoryProtocol
    private let settingsRepository: SettingsRepositoryProtocol
    private var cancellables = Set<AnyCancellable>()
    private var inTuneConfirmationTask: Task<Void, Never>?
    
    init(tuningRepository: TuningRepositoryProtocol,
         settingsRepository: SettingsRepositoryProtocol) {
        self.tuningRepository = tuningRepository
        self.settingsRepository = settingsRepository
        
        let settings = settingsRepository.getSettings()
        self.currentTuning = tuningRepository.getTuning(byId: settings.selectedTuningId)
            ?? tuningRepository.getStandardTuning()
        
        self.audioProcessor = IOSAudioProcessor(
            tuningRepository: tuningRepository,
            settingsRepository: settingsRepository
        )
        
        setupSubscriptions()
    }
    
    /// Starts the tuner
    func start() async {
        errorMessage = nil
        
        let result = await audioProcessor.start()
        
        switch result {
        case .success:
            isRunning = true
        case .failure(let error):
            errorMessage = error.localizedDescription
            isRunning = false
        }
    }
    
    /// Stops the tuner
    func stop() {
        audioProcessor.stop()
        isRunning = false
        pitchResult = nil
    }
    
    /// Toggles tuner on/off
    func toggle() async {
        if isRunning {
            stop()
        } else {
            await start()
        }
    }
    
    /// Triggers in-tune confirmation animation
    func triggerInTuneConfirmation() {
        // Cancel any existing confirmation animation
        inTuneConfirmationTask?.cancel()
        
        showInTuneConfirmation = true
        
        // Auto-hide after 1 second
        inTuneConfirmationTask = Task {
            try? await Task.sleep(nanoseconds: 1_000_000_000)
            if !Task.isCancelled {
                showInTuneConfirmation = false
            }
        }
    }
    
    // MARK: - Private Methods
    
    private func setupSubscriptions() {
        // Observe pitch results
        audioProcessor.observePitchResults()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] result in
                self?.pitchResult = result
            }
            .store(in: &cancellables)
        
        // Observe settings changes for tuning updates
        settingsRepository.settingsPublisher
            .receive(on: DispatchQueue.main)
            .sink { [weak self] settings in
                if let tuning = self?.tuningRepository.getTuning(byId: settings.selectedTuningId) {
                    self?.currentTuning = tuning
                }
            }
            .store(in: &cancellables)
    }
}
